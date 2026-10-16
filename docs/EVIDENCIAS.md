# Evidencias del Lab 2 — CI/CD

Grupo: Eduardo De La Hoz, Liceth Sarmiento, Iván Vélez.

Dónde ver cada parte del entorno colaborativo:

| Herramienta | Qué se evidencia | Enlace |
|---|---|---|
| GitHub (ramas y PRs) | flujo `feature/*` → `develop` → `main`, PRs revisados y mergeados | <https://github.com/edu-delahoz/banco2025-backend/pulls?q=is%3Apr> · gráfico: <https://github.com/edu-delahoz/banco2025-backend/network> |
| GitHub Actions | pruebas, Sonar, build, Docker Hub y despliegue en cada push a `main` | <https://github.com/edu-delahoz/banco2025-backend/actions> |
| JaCoCo | cobertura ≥ 75 % (el build falla si baja); reporte como artifact `jacoco-report` | artifact de cada ejecución de *Unit tests* |
| SonarCloud | quality gate, cobertura, bugs y code smells | <https://sonarcloud.io/summary/new_code?id=edu-delahoz_banco2025-backend> |
| Coveralls | histórico de cobertura por commit | <https://coveralls.io/github/edu-delahoz/banco2025-backend> |
| Snyk | vulnerabilidades conocidas en las dependencias Maven | <https://snyk.io/test/github/edu-delahoz/banco2025-backend> |
| Docker Hub | imagen `latest` y una etiqueta por commit (`:<sha>`) | <https://hub.docker.com/r/edudelahoz/banco2025-backend> |
| Render | API pública y base PostgreSQL | <https://banco2025-backendspring-datasource-url.onrender.com/api/customers> |

Prueba en la nube: `scripts/smoke.sh <URL de Render>` crea dos clientes, hace una transferencia y verifica
el historial. Corre sola al final del job *Deploy to Render* y también a mano (ver README).
