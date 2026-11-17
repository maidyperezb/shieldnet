#!/bin/bash
# Genera las cuentas de baresip a partir de las variables de entorno y
# arranca el softphone. Las credenciales no se guardan en la imagen.
set -euo pipefail

: "${SOFTPHONE_SIP_DOMAIN:=asterisk}"
: "${SOFTPHONE_EXT_1001:=1001}"
: "${SOFTPHONE_EXT_1002:=1002}"
: "${SOFTPHONE_EXT_1001_SECRET:?Falta SOFTPHONE_EXT_1001_SECRET}"
: "${SOFTPHONE_EXT_1002_SECRET:?Falta SOFTPHONE_EXT_1002_SECRET}"

ami_port="${SOFTPHONE_AMI_PORT:-5038}"

# Al reiniciar el demonio de Docker los contenedores arrancan a la vez y
# baresip podría registrar antes de que Asterisk esté listo. Espera al AMI.
for _ in $(seq 1 60); do
    if (echo > "/dev/tcp/${SOFTPHONE_SIP_DOMAIN}/${ami_port}") 2>/dev/null; then
        break
    fi
    sleep 1
done

mkdir -p /root/.baresip
cp /etc/baresip/baresip.conf /root/.baresip/config

umask 077
cat > /root/.baresip/accounts <<EOF
<sip:${SOFTPHONE_EXT_1001}@${SOFTPHONE_SIP_DOMAIN}>;auth_user=${SOFTPHONE_EXT_1001};auth_pass=${SOFTPHONE_EXT_1001_SECRET};regint=60
<sip:${SOFTPHONE_EXT_1002}@${SOFTPHONE_SIP_DOMAIN}>;auth_user=${SOFTPHONE_EXT_1002};auth_pass=${SOFTPHONE_EXT_1002_SECRET};regint=60
EOF
chmod 600 /root/.baresip/accounts

echo "Registrando ${SOFTPHONE_EXT_1001} y ${SOFTPHONE_EXT_1002} en ${SOFTPHONE_SIP_DOMAIN}"
exec baresip -f /root/.baresip
