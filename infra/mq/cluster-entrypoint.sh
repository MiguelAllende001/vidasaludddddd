#!/bin/sh
# Arranca este nodo normalmente y, apenas puede, se une al cluster del nodo
# semilla (rabbit@rabbitmq1). Basado en el patron estandar documentado por
# la comunidad para clusterizar RabbitMQ con docker-compose (arrancar en
# background, esperar a que levante, y recien ahi correr rabbitmqctl).
set -e

/usr/local/bin/docker-entrypoint.sh rabbitmq-server &

echo "[cluster-entrypoint] Esperando a que este nodo termine de arrancar..."
until rabbitmqctl await_startup >/dev/null 2>&1; do
  sleep 2
done

if rabbitmqctl cluster_status 2>/dev/null | grep -q "rabbit@rabbitmq1"; then
  echo "[cluster-entrypoint] Ya soy parte del cluster de rabbit@rabbitmq1, no hago nada mas."
else
  echo "[cluster-entrypoint] Esperando a que el nodo semilla (rabbit@rabbitmq1) responda..."
  until rabbitmq-diagnostics -q ping -n rabbit@rabbitmq1 >/dev/null 2>&1; do
    sleep 2
  done

  echo "[cluster-entrypoint] Uniendome al cluster..."
  rabbitmqctl stop_app
  rabbitmqctl reset
  rabbitmqctl join_cluster rabbit@rabbitmq1
  rabbitmqctl start_app
  echo "[cluster-entrypoint] Nodo unido al cluster."
fi

wait
