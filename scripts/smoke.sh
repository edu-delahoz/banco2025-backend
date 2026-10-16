#!/usr/bin/env bash
# Prueba de humo contra la API desplegada.
# Uso: scripts/smoke.sh https://<servicio>.onrender.com
set -euo pipefail

BASE_URL="${1:-${SMOKE_URL:?Falta la URL base (argumento o SMOKE_URL)}}"
BASE_URL="${BASE_URL%/}"
SUFFIX="$(date +%s)$RANDOM"
A="SMK-A-$SUFFIX"
B="SMK-B-$SUFFIX"
JSON='Content-Type: application/json'

echo "== GET /api/customers"
curl -fsS "$BASE_URL/api/customers" > /dev/null

echo "== POST /api/customers ($A, $B)"
curl -fsS -X POST -H "$JSON" "$BASE_URL/api/customers" \
  -d "{\"firstName\":\"Smoke\",\"lastName\":\"A\",\"accountNumber\":\"$A\",\"balance\":100.0}" > /dev/null
curl -fsS -X POST -H "$JSON" "$BASE_URL/api/customers" \
  -d "{\"firstName\":\"Smoke\",\"lastName\":\"B\",\"accountNumber\":\"$B\",\"balance\":0.0}" > /dev/null

echo "== POST /api/transactions ($A -> $B, 25)"
curl -fsS -X POST -H "$JSON" "$BASE_URL/api/transactions" \
  -d "{\"senderAccountNumber\":\"$A\",\"receiverAccountNumber\":\"$B\",\"amount\":25.0}" > /dev/null

echo "== GET /api/transactions/$A"
curl -fsS "$BASE_URL/api/transactions/$A" | grep -q "$B"

echo "OK: prueba de humo superada contra $BASE_URL"
