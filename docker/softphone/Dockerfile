# Softphone de demostración: mantiene registradas las extensiones SIP
# para que el panel de VoIP las muestre en línea.
FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends baresip \
    && rm -rf /var/lib/apt/lists/*

COPY config/baresip.conf /etc/baresip/baresip.conf
COPY entrypoint.sh /usr/local/bin/entrypoint.sh

RUN chmod +x /usr/local/bin/entrypoint.sh

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
