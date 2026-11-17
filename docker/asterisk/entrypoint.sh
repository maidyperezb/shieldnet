#!/bin/sh
set -eu

: "${ASTERISK_AMI_USER:=admin}"
: "${ASTERISK_AMI_PASSWORD:=Admin123}"
: "${ASTERISK_AMI_PORT:=5038}"
: "${ASTERISK_SIP_PORT:=5060}"
: "${ASTERISK_EXTERNAL_ADDRESS:=127.0.0.1}"
: "${ASTERISK_EXT_1001_SECRET:=Ventas2026}"
: "${ASTERISK_EXT_1002_SECRET:=Almacen2026}"

export ASTERISK_AMI_USER ASTERISK_AMI_PASSWORD ASTERISK_AMI_PORT
export ASTERISK_SIP_PORT ASTERISK_EXTERNAL_ADDRESS
export ASTERISK_EXT_1001_SECRET ASTERISK_EXT_1002_SECRET

log() {
    printf '%s %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

log "Renderizando configuración de Asterisk..."
mkdir -p /var/log/asterisk/cdr-csv /var/log/asterisk/cdr-custom /var/run/asterisk
chown -R asterisk:asterisk /var/log/asterisk /var/run/asterisk 2>/dev/null || true

# Los templates se renderizan en cada arranque para tomar los valores del .env.
for template in /etc/asterisk-templates/*.conf.template; do
    [ -e "$template" ] || continue
    target="/etc/asterisk/$(basename "$template" .template)"
    envsubst < "$template" > "$target"
done

# Los archivos .conf sin sufijo .template se copian tal cual.
for file in /etc/asterisk-templates/*.conf; do
    [ -e "$file" ] || continue
    cp "$file" "/etc/asterisk/$(basename "$file")"
done

if [ -z "$ASTERISK_EXTERNAL_ADDRESS" ]; then
    sed -i '/^externaddr=/d' /etc/asterisk/sip.conf
fi

# Evita que un checkout con CRLF deje valores con \r dentro de Asterisk.
find /etc/asterisk -maxdepth 1 -type f -name '*.conf' -exec sed -i 's/\r$//' {} +

chown -R asterisk:asterisk /etc/asterisk 2>/dev/null || true

log "Iniciando Asterisk (SIP ${ASTERISK_SIP_PORT}, AMI ${ASTERISK_AMI_PORT}, externaddr ${ASTERISK_EXTERNAL_ADDRESS:-<none>})..."
exec /usr/sbin/asterisk -f -U asterisk -G asterisk -vvv
