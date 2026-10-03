# Base44 Dev Environment

## Project Overview
A **Spring Boot (Java 21) web application** for vehicle rental management, backed by **PostgreSQL**.
The browser UI (served by Spring Boot as static assets) talks to a REST API under `/api/**`.

Features: customer management, vehicle fleet management, rent/return tracking, and billing/reporting
(revenue per vehicle, overdue rentals, summary stats).

## Architecture
- `pom.xml` — Maven build, Spring Boot 3.3.x parent, Java 21, Lombok.
- `src/main/java/com/rental/`
  - `model/` — JPA entities: `Customer`, `Vehicle`, `Rental`.
  - `repository/` — Spring Data JPA repositories.
  - `service/RentalService.java` — rent/return logic and billing (late days billed at 1.5x the daily rate).
  - `controller/` — REST controllers (`/api/customers`, `/api/vehicles`, `/api/rentals`, `/api/reports`) plus a `GlobalExceptionHandler`.
  - `config/DataSeeder.java` — seeds a small demo dataset on first start (only when the DB is empty).
- `src/main/resources/application.properties` — port 3000, datasource from `SPRING_DATASOURCE_*` env vars.
- `src/main/resources/static/` — the frontend (`index.html`, `app.js`, `styles.css`), served directly from the source tree during dev.

## How It Runs Here
- `docker-compose.base44.yml` starts `db` (Postgres 16) and `app`.
- `app` builds from `Dockerfile.base44` (Maven + Temurin 21), bind-mounts the repo at `/app`,
  caches Maven deps in the `m2-cache` volume, and runs `mvn spring-boot:run` on port 3000.
- Schema is created/updated by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

## Verification
- `docker compose -f docker-compose.base44.yml up -d --build` starts both services; `app` becomes `healthy`.
- `curl http://localhost:3000/api/vehicles` returns JSON; `curl http://localhost:3000/` returns the UI.
- First start is slow (Maven downloads dependencies); the healthcheck allows a 240s start period.

## No Secrets Required
The Postgres credentials are local development values wired through compose `environment:`.
No external service credentials are needed.
