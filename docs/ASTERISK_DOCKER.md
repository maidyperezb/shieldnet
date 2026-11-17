# Asterisk en Docker (ShieldNet)

Esta configuración levanta **Asterisk 20 LTS** en un contenedor con:

- **Extensión 1001** — Ventas
- **Extensión 1002** — Almacén
- **Softphone de demostración** que mantiene ambas extensiones registradas para el panel de VoIP.
- **AMI** en `localhost:5038` para que el backend Spring Boot consulte el estado, las extensiones y las llamadas activas.
- **CDR en CSV** compartido con el proyecto para que el panel de VoIP muestre el historial.
- **Prueba de audio** marcando `600` (eco).

> El backend actual usa la acción AMI `SIPpeers`, que pertenece al canal SIP legado (`chan_sip`). Por eso esta configuración usa `chan_sip` y no PJSIP. Es la opción compatible sin modificar el backend.

## Requisitos

- Docker Desktop funcionando (WSL 2 en Windows).
- Puerto **5060/udp** libre. Si antes levantaste Asterisk en WSL, detén ese servicio antes de arrancar el contenedor.

## Arranque rápido

Desde la raíz del proyecto:

```powershell
docker compose up -d --build
```

El comando anterior levanta Asterisk y el softphone de demostración (ver más abajo). Si solo quieres Asterisk:

```powershell
docker compose up -d --build asterisk
```

O usando el script que detecta automáticamente la IP LAN del equipo:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\asterisk-up.ps1
```

Ver logs:

```powershell
docker compose logs -f asterisk
```

Ver las extensiones registradas:

```powershell
docker exec shieldnet-asterisk asterisk -rx "sip show peers"
```

Ver la versión:

```powershell
docker exec shieldnet-asterisk asterisk -rx "core show version"
```

## Configurar los softphones

Puedes usar Zoiper, MicroSIP, Linphone o cualquier softphone SIP.

| Campo | Extensión 1001 | Extensión 1002 |
|---|---|---|
| Usuario / extensión | `1001` | `1002` |
| Contraseña | `Ventas2026` | `Almacen2026` |
| Servidor / dominio | `127.0.0.1` o la IP LAN del equipo | igual |
| Puerto SIP | `5060` | `5060` |
| Transporte | UDP | UDP |

### ¿Qué IP usar en el softphone?

- Si los dos softphones corren en **el mismo PC** donde está Docker: `127.0.0.1`.
- Si un softphone está en **otro equipo de la LAN**: usa la IP LAN del PC que ejecuta Docker. En Windows: `ipconfig` y busca la IPv4 de Wi-Fi o Ethernet.

### Importante para el audio (RTP)

Asterisk debe anunciar una dirección alcanzable por los softphones. Esa dirección se configura en `.env`:

```env
# Mismo PC:
ASTERISK_EXTERNAL_ADDRESS=127.0.0.1

# Softphones en otros equipos de la LAN:
ASTERISK_EXTERNAL_ADDRESS=192.168.1.3
```

Si cambias este valor, reinicia el contenedor:

```powershell
docker compose restart asterisk
```

El script `asterisk-up.ps1` detecta la IP LAN y la pasa automáticamente, así que es la forma recomendada si no quieres editar el `.env` cada vez que cambies de red.

### Prueba de llamada

1. Registra el softphone `1001`.
2. Registra el softphone `1002`.
3. Desde `1001`, marca `1002`. Debe sonar y poder conversar.
4. Para probar el audio de tu propio softphone, marca `600` (aplicación Echo).

## Softphone de demostración (contenedor)

El servicio `softphone` del `docker-compose.yml` levanta un cliente **baresip**
que registra automáticamente las extensiones **1001** y **1002** contra
Asterisk. Sirve para que el panel de VoIP muestre las extensiones en línea
durante una demostración, sin depender de teléfonos o softphones externos.

```powershell
docker compose logs -f softphone     # ver el registro SIP
docker compose stop softphone        # desactivarlo (las extensiones pasan a Fuera de Línea)
docker compose start softphone       # volver a activarlo
```

Características y límites:

- Usa las mismas contraseñas que Asterisk (`ASTERISK_EXT_1001_SECRET` y
  `ASTERISK_EXT_1002_SECRET` del `.env`); las credenciales no se guardan en
  la imagen.
- `depends_on` espera a que Asterisk esté saludable y el entrypoint espera al
  puerto AMI antes de registrarse, para tolerar reinicios. Además se registra
  cada 60 segundos, así que se recupera solo si Asterisk se reinicia.
- **No tiene audio real**: no puede originar ni atender llamadas; solo mantiene
  el registro SIP. Para llamadas reales usa Zoiper, MicroSIP o Linphone.
- Si vas a registrar un softphone real con la misma extensión, detén primero
  la demo (`docker compose stop softphone`): ambos compiten por el mismo
  registro y Asterisk usa el último que se registró.
- El estado "En Línea" del panel depende del registro SIP, no de que la
  extensión exista en `sip.conf`. Si detienes la demo, aparecerán fuera de
  línea hasta que registres un softphone real.

## Puertos

| Puerto | Protocolo | Uso |
|---|---|---|
| `5060` | UDP/TCP | Señalización SIP |
| `5038` | TCP | AMI (publicado solo en `127.0.0.1`) |
| `10000-10100` | UDP | Audio RTP |

## Integración con el backend

El archivo `.env` ya apunta al contenedor:

```env
ASTERISK_HOST=localhost
ASTERISK_PORT=5038
ASTERISK_USERNAME=admin
ASTERISK_PASSWORD=Admin123
ASTERISK_CDR_PATH=docker/asterisk/data/log/cdr-csv/Master.csv
```

El CDR se escribe en:

```text
docker/asterisk/data/log/cdr-csv/Master.csv
```

El backend lee ese archivo para el historial de llamadas. Ya no es necesario usar `wsl tail`. Si ejecutas el backend dentro de otro contenedor, cambia `ASTERISK_HOST` al nombre del servicio (`asterisk`) y `ASTERISK_CDR_PATH` a `/var/log/asterisk/cdr-csv/Master.csv`.

## Estructura

```text
docker/
├── asterisk/
│   ├── Dockerfile
│   ├── entrypoint.sh
│   ├── .dockerignore
│   ├── config/
│   │   ├── sip.conf.template
│   │   ├── manager.conf.template
│   │   ├── extensions.conf
│   │   ├── cdr.conf
│   │   ├── rtp.conf
│   │   ├── logger.conf
│   │   └── modules.conf
│   └── data/                  # creado al arrancar (ignorado por git)
│       └── log/
│           └── cdr-csv/
│               └── Master.csv
└── softphone/
    ├── Dockerfile
    ├── entrypoint.sh
    └── config/
        └── baresip.conf
docker-compose.yml
scripts/
└── asterisk-up.ps1
```

## Cambiar nombres, números o contraseñas

- **Contraseñas:** edita `ASTERISK_EXT_1001_SECRET` y `ASTERISK_EXT_1002_SECRET` en `.env` y reinicia con `docker compose restart asterisk`.
- **Nombres visibles:** edita `callerid` y `description` en `docker/asterisk/config/sip.conf.template` y reinicia. El CDR usa `callerid`; el panel de extensiones usa `description`.
- **Números de extensión:** cambia las secciones `[1001]` / `[1002]` en `sip.conf.template` y las reglas correspondientes en `extensions.conf`. Reinicia el contenedor.
- **Contraseña del AMI:** cambia `ASTERISK_PASSWORD` en `.env` y reinicia. El backend toma el mismo valor.

Los cambios en `docker/asterisk/config/` se aplican al reiniciar (no hace falta reconstruir la imagen). Los cambios en `Dockerfile` o `entrypoint.sh` sí requieren `docker compose up -d --build`.

## Solución de problemas

### `sip show peers` no muestra las extensiones

```powershell
docker compose logs --tail=100 asterisk
docker exec shieldnet-asterisk asterisk -rx "module show like chan_sip"
```

Debe aparecer `chan_sip.so` cargado. Si el puerto 5060 está ocupado, detén el Asterisk de WSL o cambia el mapeo en `docker-compose.yml`.

### El softphone registra pero no hay audio

1. Revisa que `ASTERISK_EXTERNAL_ADDRESS` sea la IP LAN correcta (no la IP de WSL ni la del contenedor).
2. Permite Asterisk/Docker en el Firewall de Windows para redes privadas.
3. Verifica que los puertos `10000-10100/udp` estén publicados.
4. Reinicia el contenedor después de cambiar `.env`.

### El historial de llamadas está vacío

1. Haz una llamada que se conteste (por ejemplo, marca `600`).
2. Verifica que exista el archivo:

```powershell
Get-Content .\docker\asterisk\data\log\cdr-csv\Master.csv -Tail 5
```

3. Confirma que `ASTERISK_CDR_PATH` en `.env` apunta a esa ruta.

### El backend muestra Asterisk fuera de línea

```powershell
docker compose ps
docker exec shieldnet-asterisk asterisk -rx "manager show user admin"
```

El backend debe usar `ASTERISK_HOST=localhost` y `ASTERISK_PORT=5038` cuando corre en el mismo Windows.

### El panel muestra las extensiones fuera de línea

El estado "En Línea" depende de que haya un cliente SIP registrado, no de que
la extensión esté definida en `sip.conf`. Comprueba:

```powershell
docker compose ps
docker exec shieldnet-asterisk asterisk -rx "sip show peers"
docker compose logs --tail=50 softphone
```

- `UNKNOWN` o `UNREACHABLE` significan que no hay softphone registrado.
- Si el contenedor `shieldnet-softphone` está detenido, las extensiones
  aparecerán fuera de línea hasta que registres uno real.
- Recarga la página del panel: la tabla de extensiones no se refresca sola;
  solo las estadísticas y las llamadas activas se actualizan cada 5 segundos.

## Seguridad

Esta configuración es para **desarrollo/demostración**:

- El AMI se publica únicamente en `127.0.0.1`; no lo expongas a Internet.
- Cambia `Admin123`, `Ventas2026` y `Almacen2026` antes de usar el sistema fuera de un entorno local.
- No publiques el puerto `5038` en `0.0.0.0`.
- El contenedor `shieldnet-softphone` es solo para demostraciones: deténlo con `docker compose stop softphone` en producción.
- Para producción, migra el backend de `SIPpeers` a PJSIP y usa TLS/SRTP.

## Detener y eliminar

```powershell
# Detener sin borrar datos
docker compose stop

# Detener y eliminar el contenedor
docker compose down

# Detener, eliminar el contenedor y borrar los CDR locales
docker compose down
Remove-Item -Recurse -Force .\docker\asterisk\data
```
