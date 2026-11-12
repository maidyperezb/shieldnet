"""
=============================================================
  SHIELDNET - MODELO DE OPTIMIZACIÓN DE RECURSOS (Simplex)
=============================================================
  Investigación de Operaciones
  Herramienta independiente de optimización conectada a MySQL

  Modelo:
    Variables:
      x1 = eventos críticos procesados
      x2 = eventos medios procesados
      x3 = eventos bajos procesados

    Función objetivo:
      Max Z = 5x1 + 3x2 + x3

    Restricciones:
      x1 + x2 + x3 <= 100
      2x1 + x2 + 0.5x3 <= 120
      x1, x2, x3 >= 0
=============================================================
"""

import os
import sys
import json
import pymysql
from scipy.optimize import linprog

# La conexión se lee del entorno (mismas variables que usa Spring en .env)
# para no tener credenciales hardcodeadas ni duplicadas en el repositorio.
DB_CONFIG = {
    "host": os.environ.get("MYSQL_HOST", "127.0.0.1"),
    "port": int(os.environ.get("MYSQL_PORT", "3306")),
    "user": os.environ.get("MYSQL_USERNAME", "root"),
    "password": os.environ.get("MYSQL_PASSWORD", ""),
    "database": os.environ.get("MYSQL_DATABASE", "shieldnet"),
    "charset": "utf8mb4"
}


# ─────────────────────────────────────────────
# CONEXIÓN MYSQL
# ─────────────────────────────────────────────
def conectar():
    conn = pymysql.connect(**DB_CONFIG)
    return conn


# ─────────────────────────────────────────────
# OBTENER DATOS REALES
# ─────────────────────────────────────────────
def obtener_datos(conn):

    cursor = conn.cursor()

    # X1 = EVENTOS CRÍTICOS
    cursor.execute("""
        SELECT COUNT(*)
        FROM dispositivos
        WHERE estado_autorizacion = 'DESCONOCIDO'
    """)
    dispositivos_desconocidos = cursor.fetchone()[0]

    cursor.execute("""
        SELECT COUNT(*)
        FROM qos_metrics
        WHERE quality_status IN (
            'MUY_MALO', 'MALO',
            'muy_malo', 'malo',
            'bad', 'BAD'
        )
    """)
    qos_malos = cursor.fetchone()[0]

    x1 = dispositivos_desconocidos + qos_malos

    # X2 = EVENTOS MEDIOS
    cursor.execute("""
        SELECT COUNT(*)
        FROM qos_metrics
        WHERE quality_status NOT IN (
            'MUY_MALO', 'MALO',
            'muy_malo', 'malo',
            'bad', 'BAD'
        )
    """)
    qos_medios = cursor.fetchone()[0]

    cursor.execute("""
        SELECT COUNT(*)
        FROM eventos
        WHERE LOWER(descripcion) LIKE '%escan%'
           OR LOWER(descripcion) LIKE '%puerto%'
           OR LOWER(descripcion) LIKE '%mac%'
    """)
    eventos_medios = cursor.fetchone()[0]

    x2 = qos_medios + eventos_medios

    # X3 = EVENTOS BAJOS
    cursor.execute("""
        SELECT COUNT(*)
        FROM eventos
        WHERE LOWER(descripcion) LIKE '%ping%'
           OR LOWER(descripcion) LIKE '%usuario%'
    """)
    x3 = cursor.fetchone()[0]

    detalles = {
        "x1": {
            "total": x1,
            "dispositivos_desconocidos": dispositivos_desconocidos,
            "qos_malos": qos_malos
        },
        "x2": {
            "total": x2,
            "qos_medios": qos_medios,
            "eventos_medios": eventos_medios
        },
        "x3": {
            "total": x3
        }
    }

    cursor.close()
    return x1, x2, x3, detalles


# ─────────────────────────────────────────────
# SIMPLEX
# ─────────────────────────────────────────────
def resolver_simplex(x1_real, x2_real, x3_real):

    # Max Z = 5x1 + 3x2 + x3
    # linprog minimiza → negamos coeficientes
    c = [-5, -3, -1]

    A_ub = [
        [1, 1, 1],
        [2, 1, 0.5]
    ]

    b_ub = [100, 120]

    bounds = [
        (0, x1_real),
        (0, x2_real),
        (0, x3_real)
    ]

    return linprog(c, A_ub=A_ub, b_ub=b_ub, bounds=bounds, method='highs')


# ─────────────────────────────────────────────
# CONSTRUIR Y IMPRIMIR RESULTADO
# SOLO imprime JSON — nada más
# ─────────────────────────────────────────────
def mostrar_resultados(x1_real, x2_real, x3_real, detalles, resultado):

    if not resultado.success:
        print(json.dumps({
            "success": False,
            "error": resultado.message
        }))
        return

    ox1 = round(resultado.x[0])
    ox2 = round(resultado.x[1])
    ox3 = round(resultado.x[2])
    Z   = round(-resultado.fun)

    r1 = ox1 + ox2 + ox3
    r2 = round(2 * ox1 + ox2 + 0.5 * ox3, 2)

    total_real = x1_real + x2_real + x3_real

    if total_real <= 50:
        estado = "Capacidad Normal"
    elif total_real <= 80:
        estado = "Capacidad Media"
    else:
        estado = "Capacidad Alta"

    resultado_json = {
        "success": True,

        "x1": ox1,
        "x2": ox2,
        "x3": ox3,
        "z":  Z,

        "r1": r1,
        "r2": r2,

        "estado": estado,

        "x1_real": x1_real,
        "x2_real": x2_real,
        "x3_real": x3_real,

        "detalle_x1": detalles["x1"],
        "detalle_x2": detalles["x2"],
        "detalle_x3": detalles["x3"],

        "interpretacion": (
            f"Se priorizaron {ox1} eventos críticos, "
            f"{ox2} eventos medios y "
            f"{ox3} eventos bajos. "
            f"Valor óptimo Z = {Z}. "
            f"Estado del sistema: {estado}."
        )
    }

    # IMPORTANTE: solo este print — Java lo captura completo
    print(json.dumps(resultado_json, ensure_ascii=False))


# ─────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────
if __name__ == "__main__":

    try:
        conn = conectar()
        x1, x2, x3, detalles = obtener_datos(conn)
        conn.close()

        resultado = resolver_simplex(x1, x2, x3)
        mostrar_resultados(x1, x2, x3, detalles, resultado)

    except Exception as e:
        print(json.dumps({
            "success": False,
            "error": str(e)
        }))
        sys.exit(1)