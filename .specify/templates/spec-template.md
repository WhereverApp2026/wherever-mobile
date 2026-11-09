# Spec Template — nv-0X (7 secciones + Aprobación)

> Constitución: `.specify/memory/constitution.md` §11. Sin cambio de contrato con la API sin spec.

## 1. Objetivo
<!-- Qué funcionalidad o slice cubre. Filosofía 00+01+02+03+04 liberable. -->

## 2. Alcance / No-alcance
<!-- In: spec→datos→dominio→ViewModel→UI→tests→gates→tag. Out: lo que esta spec no toca. -->

## 3. Contratos (API y navegación)
<!-- Endpoints que consume (enlazar el backend), rutas de navegación en docs/routes.md, errores {codigo,mensaje,campos[]} → AppError. -->

## 4. Modelo de datos
<!-- Entidades Room, DAOs, esquema exportado, migración, DTO de red, mapeos. -->

## 5. Seguridad y privacidad
<!-- Tokens, permisos, red, datos locales, MASVS. -->

## 6. Tests / Gates
<!-- Obligatorias: Unit, Data (integración: Room en memoria + MockWebServer) y Security (const. §7). Opcionales: UI, Arquitectura. Cobertura ≥45%. Verificado por scripts/check_specs.sh. -->

## 7. Plan de entrega
<!-- assembleDebug/Release, verificación manual en emulador, tag vX.Y.Z-featN. -->

## Aprobación
- [ ] Revisor 1 — fecha:
- [ ] CHECKLIST_PR.md OK
- [ ] Sin conflicto con constitución
