FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive \
    TZ=America/Bogota

# Asterisk 20 (LTS) de Ubuntu 24.04 incluye chan_sip y cdr_csv, que son los
# módulos que espera el backend actual (acciones AMI SIPpeers y CDR CSV).
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        asterisk \
        asterisk-core-sounds-es-gsm \
        ca-certificates \
        gettext-base \
        tzdata \
    && rm -rf /var/lib/apt/lists/*

COPY config/ /etc/asterisk-templates/
COPY entrypoint.sh /usr/local/bin/asterisk-entrypoint.sh

RUN sed -i 's/\r$//' /usr/local/bin/asterisk-entrypoint.sh \
    && chmod +x /usr/local/bin/asterisk-entrypoint.sh

EXPOSE 5060/udp 5060/tcp 5038/tcp 10000-10100/udp

ENTRYPOINT ["/usr/local/bin/asterisk-entrypoint.sh"]
