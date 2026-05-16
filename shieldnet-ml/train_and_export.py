"""
train_and_export.py — ShieldNet ML
Entrena un modelo que predice DEGRADACIÓN FUTURA del QoS,
no solo el estado actual (que ya lo calcula el sistema).

Lógica: dado el historial de un host, ¿su próxima medición
va a ser peor, igual o mejor que la actual?
"""

import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report
import pickle
import json
import warnings
warnings.filterwarnings('ignore')

# ── Etiquetas de predicción ───────────────────────────────────
# El modelo predice el ESTADO FUTURO del host (próxima medición)
LABELS = ['ESTABLE', 'EN RIESGO', 'DEGRADANDO', 'CRITICO']

# ── Datos reales históricos ───────────────────────────────────
# Formato: (jitter_ms, latency_ms, mos_score, packet_loss, estado_actual)
data_real = [
    (15.47, 32.35, 4.19, 0.0, 'BUENO'),
    (1.55, 91.29, 4.5, 0.0, 'EXCELENTE'),
    (1.55, 16.48, 4.5, 0.0, 'EXCELENTE'),
    (6.25, 18.48, 4.5, 0.0, 'EXCELENTE'),
    (8.35, 32.0, 4.5, 0.0, 'EXCELENTE'),
    (6.0, 95.76, 4.5, 0.0, 'EXCELENTE'),
    (689.2, 579.73, 1.0, 10.0, 'MUY MALO'),
    (208.8, 301.36, 1.0, 10.0, 'MUY MALO'),
    (62.7, 133.91, 4.15, 0.0, 'BUENO'),
    (387.2, 395.09, 1.0, 10.0, 'MUY MALO'),
    (446.2, 360.55, 1.0, 10.0, 'MUY MALO'),
    (25.1, 105.33, 4.35, 0.0, 'EXCELENTE'),
    (42.7, 107.73, 4.43, 0.0, 'EXCELENTE'),
    (25.89, 89.85, 3.95, 0.0, 'ACEPTABLE'),
    (5.0, 50.10, 4.5, 0.0, 'EXCELENTE'),
    (3.1, 20.82, 4.5, 0.0, 'EXCELENTE'),
    (0.9, 23.36, 4.5, 0.0, 'EXCELENTE'),
    (29.4, 46.36, 4.5, 0.0, 'EXCELENTE'),
    (59.8, 164.52, 3.66, 0.0, 'ACEPTABLE'),
    (0.0, 0.0, 1.0, 100.0, 'MUY MALO'),
    (2.5, 29.33, 4.5, 0.0, 'EXCELENTE'),
    (78.94, 186.53, 1.81, 5.0, 'MUY MALO'),
    (50.58, 52.35, 3.43, 0.0, 'MALO'),
    (69.89, 81.15, 2.9, 0.0, 'MUY MALO'),
    (95.67, 299.9, 1.5, 0.0, 'MUY MALO'),
    (5.0, 67.5, 4.31, 0.0, 'EXCELENTE'),
    (28.26, 109.45, 3.88, 0.0, 'ACEPTABLE'),
    (3.44, 19.4, 4.36, 0.0, 'EXCELENTE'),
    (40.58, 67.25, 3.65, 0.0, 'ACEPTABLE'),
    (39.32, 114.65, 3.63, 0.0, 'ACEPTABLE'),
    (3.11, 71.35, 4.34, 0.0, 'EXCELENTE'),
    (42.63, 114.8, 3.55, 0.0, 'MALO'),
    (4.47, 51.05, 4.33, 0.0, 'EXCELENTE'),
    (4.32, 50.3, 4.33, 0.0, 'EXCELENTE'),
    (4.53, 22.5, 4.35, 0.0, 'EXCELENTE'),
    (0.0, 0.0, 1.0, 100.0, 'MUY MALO'),
    (14.11, 34.4, 4.21, 0.0, 'BUENO'),
    (0.0, 0.0, 1.0, 100.0, 'MUY MALO'),
    (0.0, 0.0, 1.0, 100.0, 'MUY MALO'),
    (441.0, 333.2, 1.0, 0.0, 'MUY MALO'),
    (0.0, 0.0, 1.0, 100.0, 'MUY MALO'),
    (8.56, 23.2, 4.3, 0.0, 'EXCELENTE'),
    (0.0, 0.0, 1.0, 100.0, 'MUY MALO'),
    (1.79, 3.5, 4.39, 0.0, 'EXCELENTE'),
    (0.0, 0.0, 1.0, 100.0, 'MUY MALO'),
    (15.26, 23.9, 4.2, 0.0, 'BUENO'),
    (2.68, 18.0, 4.37, 0.0, 'EXCELENTE'),
    (28.84, 36.4, 3.94, 0.0, 'ACEPTABLE'),
    (2.68, 17.55, 4.37, 0.0, 'EXCELENTE'),
    (2.0, 17.0, 4.38, 0.0, 'EXCELENTE'),
    (3.63, 18.15, 4.36, 0.0, 'EXCELENTE'),
    (24.42, 39.95, 4.02, 0.0, 'BUENO'),
    (3.32, 18.15, 4.36, 0.0, 'EXCELENTE'),
]

df_base = pd.DataFrame(data_real, columns=[
    'jitter_ms', 'latency_ms', 'mos_score', 'packet_loss_percent', 'quality_status'
])

# ── Función para generar features de TENDENCIA ────────────────
def build_trend_features(df):
    """
    Simula secuencias de mediciones y extrae features de tendencia.
    En producción estos valores vienen de la BD (medición anterior del mismo host).
    """
    records = []

    # Ordenar y simular pares consecutivos (medición anterior → actual)
    for i in range(1, len(df)):
        prev = df.iloc[i - 1]
        curr = df.iloc[i]

        mos_actual     = curr['mos_score']
        mos_anterior   = prev['mos_score']
        delta_mos      = mos_actual - mos_anterior          # negativo = empeoró

        jitter_actual  = curr['jitter_ms']
        jitter_ant     = prev['jitter_ms']
        delta_jitter   = jitter_actual - jitter_ant         # positivo = empeoró

        lat_actual     = curr['latency_ms']
        lat_ant        = prev['latency_ms']
        delta_lat      = lat_actual - lat_ant

        loss_actual    = curr['packet_loss_percent']

        # ── Etiqueta de predicción (estado futuro) ────────────
        # Usamos el estado actual de la fila siguiente para simular "¿qué va a pasar?"
        next_idx = i + 1 if i + 1 < len(df) else i
        next_mos = df.iloc[next_idx]['mos_score']

        if next_mos >= 4.0:
            future_label = 'ESTABLE'
        elif next_mos >= 3.6:
            future_label = 'EN RIESGO'
        elif next_mos >= 3.0:
            future_label = 'DEGRADANDO'
        else:
            future_label = 'CRITICO'

        records.append({
            'mos_actual':       mos_actual,
            'mos_anterior':     mos_anterior,
            'delta_mos':        delta_mos,
            'jitter_actual':    jitter_actual,
            'delta_jitter':     delta_jitter,
            'latency_actual':   lat_actual,
            'delta_latency':    delta_lat,
            'packet_loss':      loss_actual,
            'future_label':     future_label,
        })

    return pd.DataFrame(records)

# ── Generar datos sintéticos de tendencia ─────────────────────
def generate_synthetic_trends(n=800):
    """
    Genera escenarios de tendencia realistas para entrenar el modelo.
    Cubre los 4 casos: estable, en riesgo, degradando, crítico.
    """
    np.random.seed(42)
    records = []

    for _ in range(n):
        scenario = np.random.choice(['estable', 'riesgo', 'degradando', 'critico'],
                                     p=[0.40, 0.25, 0.20, 0.15])

        if scenario == 'estable':
            mos_ant  = np.random.uniform(4.0, 4.5)
            mos_act  = mos_ant + np.random.uniform(-0.1, 0.1)
            jit_ant  = np.random.uniform(1, 20)
            jit_act  = jit_ant + np.random.uniform(-5, 5)
            lat_ant  = np.random.uniform(10, 80)
            lat_act  = lat_ant + np.random.uniform(-10, 10)
            loss     = 0.0
            label    = 'ESTABLE'

        elif scenario == 'riesgo':
            mos_ant  = np.random.uniform(3.8, 4.2)
            mos_act  = mos_ant - np.random.uniform(0.1, 0.4)   # bajando
            jit_ant  = np.random.uniform(10, 40)
            jit_act  = jit_ant + np.random.uniform(5, 20)       # subiendo
            lat_ant  = np.random.uniform(50, 150)
            lat_act  = lat_ant + np.random.uniform(10, 50)
            loss     = np.random.uniform(0, 1)
            label    = 'EN RIESGO'

        elif scenario == 'degradando':
            mos_ant  = np.random.uniform(3.2, 3.8)
            mos_act  = mos_ant - np.random.uniform(0.2, 0.6)
            jit_ant  = np.random.uniform(30, 100)
            jit_act  = jit_ant + np.random.uniform(20, 80)
            lat_ant  = np.random.uniform(100, 300)
            lat_act  = lat_ant + np.random.uniform(30, 100)
            loss     = np.random.uniform(1, 5)
            label    = 'DEGRADANDO'

        else:  # critico
            mos_ant  = np.random.uniform(1.0, 3.0)
            mos_act  = max(1.0, mos_ant - np.random.uniform(0.3, 1.0))
            jit_ant  = np.random.uniform(100, 500)
            jit_act  = jit_ant + np.random.uniform(50, 200)
            lat_ant  = np.random.uniform(200, 600)
            lat_act  = lat_ant + np.random.uniform(50, 200)
            loss     = np.random.uniform(5, 100)
            label    = 'CRITICO'

        records.append({
            'mos_actual':     np.clip(mos_act, 1.0, 5.0),
            'mos_anterior':   np.clip(mos_ant, 1.0, 5.0),
            'delta_mos':      np.clip(mos_act - mos_ant, -4.0, 4.0),
            'jitter_actual':  max(0, jit_act),
            'delta_jitter':   jit_act - jit_ant,
            'latency_actual': max(0, lat_act),
            'delta_latency':  lat_act - lat_ant,
            'packet_loss':    np.clip(loss, 0, 100),
            'future_label':   label,
        })

    return pd.DataFrame(records)

# ── Construir dataset ─────────────────────────────────────────
print("Construyendo dataset de tendencias...")
df_trend_real  = build_trend_features(df_base)
df_trend_synth = generate_synthetic_trends(800)

df_all = pd.concat([df_trend_real, df_trend_synth], ignore_index=True)
print(f"Total registros: {len(df_all)}")
print(f"Distribución:\n{df_all['future_label'].value_counts()}\n")

# ── Entrenar modelo ───────────────────────────────────────────
FEATURES = [
    'mos_actual', 'mos_anterior', 'delta_mos',
    'jitter_actual', 'delta_jitter',
    'latency_actual', 'delta_latency',
    'packet_loss'
]

X = df_all[FEATURES]
y = df_all['future_label']

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)

# Random Forest: mejor que Decision Tree para tendencias
clf = RandomForestClassifier(
    n_estimators=100,
    max_depth=8,
    min_samples_split=5,
    min_samples_leaf=2,
    random_state=42,
    class_weight='balanced'
)
clf.fit(X_train, y_train)

y_pred = clf.predict(X_test)
acc = accuracy_score(y_test, y_pred)

print(f"Exactitud: {acc * 100:.1f}%\n")
print("Reporte por clase:")
print(classification_report(y_test, y_pred))

# Importancia de features
print("\nImportancia de features:")
for feat, imp in sorted(zip(FEATURES, clf.feature_importances_), key=lambda x: -x[1]):
    print(f"  {feat:20s}: {imp:.3f}")

# ── Exportar modelo ───────────────────────────────────────────
with open('qos_model.pkl', 'wb') as f:
    pickle.dump(clf, f)
print("\n✓ Modelo exportado: qos_model.pkl")

# ── Exportar metadata ─────────────────────────────────────────
metadata = {
    "model_type": "RandomForestClassifier",
    "version": "2.0",
    "prediction_target": "estado_futuro_del_host",
    "labels": LABELS,
    "label_descriptions": {
        "ESTABLE":    "La conexión seguirá siendo buena en la próxima medición",
        "EN RIESGO":  "La calidad está bajando, podría empeorar pronto",
        "DEGRADANDO": "Tendencia clara de deterioro, revisar la red",
        "CRITICO":    "Degradación severa inminente, intervención recomendada"
    },
    "features": FEATURES,
    "accuracy": round(acc, 4),
    "trained_on": len(df_all),
    "thresholds": {
        "ESTABLE":    {"mos_futuro_min": 4.0},
        "EN RIESGO":  {"mos_futuro_min": 3.6, "mos_futuro_max": 4.0},
        "DEGRADANDO": {"mos_futuro_min": 3.0, "mos_futuro_max": 3.6},
        "CRITICO":    {"mos_futuro_max": 3.0}
    }
}

with open('qos_model_thresholds.json', 'w') as f:
    json.dump(metadata, f, indent=2, ensure_ascii=False)
print("✓ Metadata exportada: qos_model_thresholds.json")
print(json.dumps(metadata, indent=2, ensure_ascii=False))