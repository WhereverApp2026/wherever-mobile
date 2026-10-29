# CHECKLIST_PR.md — Obligatorio

> Constitución §8. 1 revisor. `main` protegida.

- [ ] Spec en `specs/nv-0X` aprobada y enlazada (sin cambio de contrato sin spec)
- [ ] `./gradlew assembleDebug` y `ktlintCheck detekt lintDebug` sin errores
- [ ] Unit verdes
- [ ] Data (Room en memoria + MockWebServer) verdes
- [ ] Security + UI + Arquitectura verdes
- [ ] Rutas: `docs/routes.md` actualizado
- [ ] Permisos: `docs/permissions-matrix.md` verificado
- [ ] MASVS: `docs/masvs-checklist.md` verificado
- [ ] Semgrep sin altos; Dependency-Check sin hallazgos altos nuevos
- [ ] Cobertura ≥45% (`scripts/check_coverage.sh --min 45`)
- [ ] Sin secretos, sin URLs fijas, sin tokens en logs; solo HTTPS
- [ ] Estados cubiertos: cargando, vacío, error, sin red
- [ ] Accesibilidad: contraste, descripciones y áreas de 48 dp
- [ ] `Memoria.md` actualizada, sin secretos
- [ ] YAML válido (`yaml.safe_load`), `assembleDebug` después de la mutación si se usó
- [ ] Revisor: ______  Fecha: ______
