#!/usr/bin/env python3
"""Dependencias vulnerables (deps-scan, constitución §8) contra la base pública OSV.

Lee la salida de `./gradlew :app:dependencies --configuration <conf>`, resuelve
las coordenadas Maven (`grupo:artefacto:versión`, aplicando `->`) y las consulta
en https://api.osv.dev (sin clave ni cuenta). Informa cada vulnerabilidad con su
severidad y sale:

- `0` si no hay hallazgos, o si los hay pero no alcanzan `--fail-on`;
- `1` si hay alguno con severidad `>= --fail-on` (por defecto `none`: solo informa);
- `2` si no se pudo leer la entrada.

Con `--advisory` siempre sale `0` (constitución §6: Dependency-Check informativo).
Uso: scripts/check_deps_osv.py build/deps.txt [--fail-on high] [--advisory]
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import urllib.error
import urllib.request
from pathlib import Path

OSV_BATCH = "https://api.osv.dev/v1/querybatch"
OSV_VULN = "https://api.osv.dev/v1/vulns/"
LEVELS = {"none": 0, "low": 1, "moderate": 2, "medium": 2, "high": 3, "critical": 4}

# "+--- grupo:artefacto:1.0 -> 1.1 (*)" o "\--- grupo:artefacto -> 1.1"
LINE = re.compile(r"[+\\]--- ([\w.\-]+):([\w.\-]+)(?::(\{[^}]*\}|[\w.\-+]+))?(?: -> ([\w.\-+]+))?")


def parse_coordinates(text: str) -> list[tuple[str, str]]:
    """Coordenadas únicas `(grupo:artefacto, versión)` resueltas por Gradle."""
    found: set[tuple[str, str]] = set()
    for match in LINE.finditer(text):
        group, artifact, declared, resolved = match.groups()
        version = resolved or declared
        if not version or version.startswith("{") or group == "project":
            continue
        found.add((f"{group}:{artifact}", version))
    return sorted(found)


def _checked(url: str) -> str:
    """Solo la API pública de OSV por HTTPS: nunca file:// ni otro host."""
    if not url.startswith("https://api.osv.dev/"):
        raise ValueError(f"URL no permitida: {url}")
    return url


def _post(url: str, payload: dict) -> dict:
    request = urllib.request.Request(
        _checked(url),
        data=json.dumps(payload).encode(),
        headers={"Content-Type": "application/json", "User-Agent": "wherever-ci"},
    )
    # nosemgrep: python.lang.security.audit.dynamic-urllib-use-detected.dynamic-urllib-use-detected
    with urllib.request.urlopen(request, timeout=60) as response:  # noqa: S310
        return json.load(response)


def _get(url: str) -> dict:
    request = urllib.request.Request(_checked(url), headers={"User-Agent": "wherever-ci"})
    # nosemgrep: python.lang.security.audit.dynamic-urllib-use-detected.dynamic-urllib-use-detected
    with urllib.request.urlopen(request, timeout=60) as response:  # noqa: S310
        return json.load(response)


def severity_of(vuln: dict) -> str:
    """Severidad textual de OSV/GHSA; sin dato se trata como `moderate`."""
    label = str(vuln.get("database_specific", {}).get("severity", "")).lower()
    if label in LEVELS:
        return label
    for affected in vuln.get("affected", []):
        label = str(affected.get("database_specific", {}).get("severity", "")).lower()
        if label in LEVELS:
            return label
    return "moderate"


def query(coords: list[tuple[str, str]]) -> list[dict]:
    """Hallazgos `{paquete, version, id, severidad, resumen}`."""
    findings: list[dict] = []
    for start in range(0, len(coords), 500):
        chunk = coords[start:start + 500]
        body = {
            "queries": [
                {"package": {"ecosystem": "Maven", "name": name}, "version": version}
                for name, version in chunk
            ]
        }
        results = _post(OSV_BATCH, body).get("results", [])
        for (name, version), result in zip(chunk, results):
            for item in result.get("vulns", []) or []:
                detail = _get(OSV_VULN + item["id"])
                findings.append({
                    "paquete": name,
                    "version": version,
                    "id": item["id"],
                    "severidad": severity_of(detail),
                    "resumen": detail.get("summary", ""),
                })
    return findings


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("deps_file", help="salida de ./gradlew :app:dependencies")
    parser.add_argument("--fail-on", default="none", choices=sorted(LEVELS))
    parser.add_argument("--advisory", action="store_true")
    args = parser.parse_args(argv)

    path = Path(args.deps_file)
    if not path.is_file():
        print(f"DEPS-ERROR: no existe {path}", file=sys.stderr)
        return 0 if args.advisory else 2
    coords = parse_coordinates(path.read_text(encoding="utf-8", errors="replace"))
    if not coords:
        print("DEPS-ERROR: no se encontraron dependencias en la entrada", file=sys.stderr)
        return 0 if args.advisory else 2

    try:
        findings = query(coords)
    except (urllib.error.URLError, TimeoutError, OSError) as error:
        print(f"DEPS-ERROR: OSV no respondió ({error})", file=sys.stderr)
        return 0 if args.advisory else 2

    print(f"DEPS: {len(coords)} dependencias revisadas, {len(findings)} hallazgos")
    for item in sorted(findings, key=lambda f: -LEVELS[f["severidad"]]):
        print(f" - [{item['severidad']}] {item['paquete']}:{item['version']} {item['id']} {item['resumen']}")

    threshold = LEVELS[args.fail_on]
    blocking = [f for f in findings if threshold and LEVELS[f["severidad"]] >= threshold]
    if blocking and not args.advisory:
        print(f"DEPS-FAIL: {len(blocking)} hallazgos con severidad >= {args.fail_on}", file=sys.stderr)
        return 1
    print("DEPS-OK" if not findings else "DEPS-ADVISORY")
    return 0


if __name__ == "__main__":
    sys.exit(main())
