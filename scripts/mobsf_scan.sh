#!/usr/bin/env bash
# MAST estático del APK con MobSF (constitución §6). Uso: scripts/mobsf_scan.sh <apk> <salida.json>
# Levanta MobSF en Docker (imagen fijada por digest), sube el APK, lo analiza y guarda el reporte JSON.
# Imprime la calificación y los hallazgos altos para el resumen de la CI.
set -euo pipefail
APK="$1"
OUT="$2"
IMAGE="opensecurity/mobile-security-framework-mobsf@sha256:cf1465a3cc0c95488b9a6cdb1c7799d4b576d391ee28232c221f6a196bf2f74c" # v4.6.4
KEY="$(head -c 24 /dev/urandom | base64 | tr -dc 'A-Za-z0-9')"
URL="http://127.0.0.1:8000"

docker run -d --rm --name mobsf -p 127.0.0.1:8000:8000 -e MOBSF_API_KEY="$KEY" "$IMAGE" >/dev/null
trap 'docker stop mobsf >/dev/null 2>&1 || true' EXIT
for _ in $(seq 1 60); do
  curl -sf "$URL/login/" >/dev/null 2>&1 && break
  sleep 5
done

HASH="$(curl -sf -H "Authorization: $KEY" -F "file=@$APK" "$URL/api/v1/upload" | python3 -c 'import sys,json; print(json.load(sys.stdin)["hash"])')"
curl -sf -H "Authorization: $KEY" --data "hash=$HASH" "$URL/api/v1/scan" >/dev/null
curl -sf -H "Authorization: $KEY" --data "hash=$HASH" "$URL/api/v1/report_json" -o "$OUT"
curl -sf -H "Authorization: $KEY" --data "hash=$HASH" "$URL/api/v1/scorecard" -o "${OUT%.json}.scorecard.json" || true

python3 - "$OUT" "${OUT%.json}.scorecard.json" <<'PY'
import json, sys
from pathlib import Path
card = Path(sys.argv[2])
if card.exists():
    data = json.loads(card.read_text())
    print(f"MobSF: calificación {data.get('security_score')} / 100")
    for level in ("high", "warning"):
        items = data.get(level, [])
        print(f"- {level}: {len(items)}")
        for item in items[:15]:
            print(f"  - {item.get('title')}")
else:
    print("MobSF: sin scorecard; ver el reporte JSON")
PY
