# Lab 1 Arqui Soft — Backend

[![Build](https://github.com/edu-delahoz/banco2025-backend/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/edu-delahoz/banco2025-backend/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=edu-delahoz_banco2025-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=edu-delahoz_banco2025-backend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=edu-delahoz_banco2025-backend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=edu-delahoz_banco2025-backend)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=edu-delahoz_banco2025-backend&metric=bugs)](https://sonarcloud.io/summary/new_code?id=edu-delahoz_banco2025-backend)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=edu-delahoz_banco2025-backend&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=edu-delahoz_banco2025-backend)
[![Coverage Status](https://coveralls.io/repos/github/edu-delahoz/banco2025-backend/badge.svg?branch=main)](https://coveralls.io/github/edu-delahoz/banco2025-backend?branch=main)
[![Known Vulnerabilities](https://snyk.io/test/github/edu-delahoz/banco2025-backend/badge.svg)](https://snyk.io/test/github/edu-delahoz/banco2025-backend)
[![Docker Image](https://img.shields.io/docker/v/edudelahoz/banco2025-backend?label=docker%20hub)](https://hub.docker.com/r/edudelahoz/banco2025-backend)

API REST bancaria (clientes + transferencias) construida con **Spring Boot** y **MySQL 8**.
Parte del Lab 1 del curso de Arquitectura de Software (UdeA). El frontend vive en un repo aparte:
[`lab1-arqsoft-frontend`](https://github.com/edu-delahoz/lab1-arqsoft-frontend).

## Levantar todo con Docker

```bash
docker compose up --build
```

Esto arranca:
- **MySQL 8** en el puerto `3306` (base `banco2025`, usuario/clave `root`)
- **Backend** en el puerto `8080`, esperando a que MySQL esté sano

## Desarrollo local (sin Docker)

Requiere una instancia de MySQL con la base `banco2025`. Luego:

```bash
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080`.

## CI/CD y cómo desplegar

API desplegada: <https://banco2025-backendspring-datasource-url.onrender.com/api/customers> (plan free: la primera petición tarda ~1 min si el servicio estaba dormido).

El pipeline está en [`.github/workflows/build.yml`](.github/workflows/build.yml):

| Job | Cuándo corre | Qué hace |
|---|---|---|
| Unit tests | todo push y PR | `mvn verify` con JaCoCo (mínimo 75 % de líneas) |
| SonarCloud | todo push y PR | análisis de calidad y cobertura en SonarCloud |
| Build jar | todo push y PR | empaqueta `target/banco2025.jar` como artifact |
| Docker Hub | solo push a `main` | publica `edudelahoz/banco2025-backend:latest` y `:<sha>` (`Dockerfile.ci`) |
| Deploy to Render | solo push a `main` | llama al deploy hook de Render, espera a que responda y corre `scripts/smoke.sh` |

Flujo de ramas: `feature/*` → PR a `develop` → PR de `develop` a `main`. Al mergear a `main` se despliega solo.

Configuración necesaria en el repo (Settings → Secrets and variables → Actions):

- Secrets: `SONAR_TOKEN`, `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `RENDER_DEPLOY_HOOK_URL`
- Variable: `RENDER_URL` (URL pública del servicio en Render)

En Render: una base **PostgreSQL** (plan free, caduca a los 30 días) y un **Web Service** desde la imagen
`docker.io/edudelahoz/banco2025-backend:latest` con las variables `SPRING_DATASOURCE_URL`
(`jdbc:postgresql://<hostname>/banco2025`), `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.

Prueba de humo manual contra cualquier despliegue:

```bash
scripts/smoke.sh https://<servicio>.onrender.com
```

## Probar la API en la nube

Ejemplos con `curl` contra el despliegue de Render (también sirven en Postman con los mismos cuerpos JSON):

```bash
URL=https://banco2025-backendspring-datasource-url.onrender.com

# Listar clientes
curl -s "$URL/api/customers"

# Crear dos clientes
curl -s -X POST -H 'Content-Type: application/json' "$URL/api/customers" \
  -d '{"firstName":"Ana","lastName":"Pérez","accountNumber":"ACC-100","balance":500.0}'
curl -s -X POST -H 'Content-Type: application/json' "$URL/api/customers" \
  -d '{"firstName":"Luis","lastName":"Gómez","accountNumber":"ACC-200","balance":0.0}'

# Transferir 120 de ACC-100 a ACC-200 y ver el historial
curl -s -X POST -H 'Content-Type: application/json' "$URL/api/transactions" \
  -d '{"senderAccountNumber":"ACC-100","receiverAccountNumber":"ACC-200","amount":120.0}'
curl -s "$URL/api/transactions/ACC-100"
```

Si el número de cuenta ya existe, usa otro: la base en la nube es compartida.
