---
name: 03-errors-middleware
description: Errores de la API a AppError y Authenticator
---
# 03-errors-middleware

> Rector: `.specify/memory/constitution.md`. Ante conflicto, manda la constitución.

## What I do
- `{codigo, mensaje, campos[]}` → `AppError`.
- `Authenticator` renueva el token una vez ante 401.

## Cuando usarme
Manejo de errores y sesión.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-03`.

## Verify
401 provoca un solo refresh; si falla, sesión limpia.
