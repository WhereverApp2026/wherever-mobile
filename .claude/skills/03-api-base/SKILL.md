---
name: 03-api-base
description: Cliente Retrofit/OkHttp con cadena fija
---
# 03-api-base

> Rector: `.specify/memory/constitution.md`. Ante conflicto, manda la constitución.

## What I do
- `RequestId → AuthHeader → RedactedLogging`.
- Base URL por `BuildConfig`.

## Cuando usarme
Tocar `core/network`.

## No hacer
- Salirse de la constitución o añadir pantallas del producto en `nv-00…nv-03`.

## Verify
Test de orden de interceptores y de redacción de logs.
