# 🛒 E-Commerce API

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/your-username/your-ecommerce-api-repo/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptopenjdk.net/)

---

## Overview
E-Commerce API is a backend RESTful service for online stores,
built with Java and Spring Boot. It provides endpoints for user management,
product catalog, shopping cart, order processing, and payment integration.
The project is designed for learning, experimentation, and as a foundation for real-world e-commerce solutions.

> **Project Status:** In development 🚧 — Contributions and suggestions are welcome!

---

## Motivation
This project aims to:
- Demonstrate best practices in Java/Spring Boot API development
- Serve as a portfolio piece for backend engineering
- Provide a modular, extensible base for e-commerce applications

---

## Features
| Feature                        | Status      | Description                                      |
|------------------------------- |-------------|-------------------------------------------------|
| User registration/login        | Implemented | JWT-based authentication and authorization       |
| Product management             | Implemented | CRUD for products and categories                 |
| Shopping cart                  | Implemented | Add/remove/update items, view cart               |
| Order processing               | Implemented | Place orders, order history                      |
| Payment integration            | Implemented | Mercado Pago gateway integration                 |
| Address management             | Implemented | Manage shipping addresses                        |
| Admin panel                    | Planned     | Admin endpoints for managing catalog/users       |
| API documentation (Swagger)    | In Progress | Interactive API docs with Springdoc/OpenAPI      |
| Automated tests                | In Progress | Unit and integration tests                       |

---

## Architecture
- **Spring Boot**: Main framework for REST API
- **Spring Data JPA**: ORM and database access
- **Flyway**: Database migrations
- **JWT**: Secure authentication
- **Docker**: Containerization for easy deployment
- **PostgreSQL**: Default database (can be swapped)

### Directory Structure
```
src/
  main/
    java/com/java/luismiguel/ecommerce_api/...
    resources/
      application.yml
      db/migration/
  test/
    java/com/java/luismiguel/ecommerce_api/...
```

---

## Configuration: `.env.example` and `application.yml`
This project reads basic configuration from environment variables and `application.yml`. A `.env.example` is included at the repository root to show the variables the application expects. Copy it to `.env` (or set variables in your environment) and update the values.

Key variables in `.env.example` (already present in the repo):
- `POSTGRES_DB_URL` — JDBC URL used by Spring Boot. When using Supabase, set it like:

  jdbc:postgresql://<SUPABASE_HOST>:5432/<DB_NAME>?sslmode=require

  Example:
  ```text
  POSTGRES_DB_URL=jdbc:postgresql://db.abcd.supabase.co:5432/postgres?sslmode=require
  POSTGRES_DB_USER=postgres
  POSTGRES_DB_PASSWORD=your_supabase_db_password
  ```

- `POSTGRES_DB_USER`, `POSTGRES_DB_PASSWORD` — database credentials.
- `JWT_SECRET`, `JWT_EXPIRATION`, `JWT_REFRESH_EXPIRATION` — used by the JWT implementation (keep `JWT_SECRET` secret in production).
- `REDIS_HOST`, `REDIS_PORT` — connection for Redis (used for caches/sessions). Default in the `.env.example` points to a `redis` host used by Docker Compose.
- `MP_ACCESS_TOKEN`, `MP_WEBHOOK_SECRET`, `MP_NOTIFICATION_URL` — Mercado Pago credentials and webhook config.

How `application.yml` maps these variables (see `src/main/resources/application.yml`):
```yaml
spring:
  datasource:
    url: ${POSTGRES_DB_URL}
    username: ${POSTGRES_DB_USER}
    password: ${POSTGRES_DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
  flyway:
    placeholders:
      admin_email: ${ADMIN_EMAIL}
      admin_password: ${ADMIN_PASSWORD}
  mercadopago:
    access-token: ${MP_ACCESS_TOKEN}
    webhook-secret: ${MP_WEBHOOK_SECRET}
    notification-url: ${MP_NOTIFICATION_URL}

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION}
```

Notes:
- `application.yml` includes `config.import: optional:file:.env[.properties]`, so using a `.env` file in the repository root is a convenient way to load variables during local development. In production prefer environment variables or a secrets manager.
- `POSTGRES_DB_URL` must be a valid JDBC URL. When using managed hosts (like Supabase) confirm whether `sslmode=require` or certificate options are required.

---

## Database — Supabase (current setup) and alternatives
In this project the database is running on Supabase (Postgres). Supabase provides a hosted Postgres instance; to use it:
1. Create a Supabase project
2. From the project dashboard, copy the DB connection details (host, port, database, user, password)
3. Fill the `.env` or environment variables accordingly (see examples above)

If you prefer another host (local Postgres, AWS RDS, ElephantSQL, DigitalOcean, etc.), simply set `POSTGRES_DB_URL`, `POSTGRES_DB_USER` and `POSTGRES_DB_PASSWORD` accordingly. The code uses standard Spring Data JPA, so switching the Postgres host requires no code changes.

Example local Postgres JDBC URL:
```text
POSTGRES_DB_URL=jdbc:postgresql://localhost:5432/ecommerce_db
POSTGRES_DB_USER=ecom_user
POSTGRES_DB_PASSWORD=secret
```

---

## Redis — local container (default) or cloud
This project uses Redis for caching/sessions (Reactive Redis client). In development I run Redis locally inside a container (referenced in `docker-compose.yml`), but you can use a managed Redis (AWS Elasticache, Upstash, Redis Labs, etc.) in production.

Local Docker Compose snippet (example - you may already have similar service in `docker-compose.yml`):

```yaml
services:
  redis:
    image: redis:7-alpine
    ports:
      - 6379:6379
    volumes:
      - redis-data:/data

volumes:
  redis-data:
```

To use a hosted Redis, update `REDIS_HOST` and `REDIS_PORT` in your `.env` or environment.

---

## Swagger / OpenAPI (API docs)
The project uses `springdoc-openapi` (see `pom.xml`) to automatically generate OpenAPI docs. By default the UI is available at one of these URLs (depending on Springdoc version and configuration):

- `/swagger-ui.html`
- `/swagger-ui/index.html`
- OpenAPI JSON at `/v3/api-docs`

If the UI doesn't appear, check the `springdoc` configuration and application logs.

---

## Getting Started

### Pre-requisites
- Java 17 or higher
- Maven
- Docker (optional)

### Quick start (local dev)
```bash
# Clone
git clone https://github.com/your-username/your-ecommerce-api-repo.git
cd ecommerce-api

# Copy env example and edit
cp .env.example .env
# Edit .env with your credentials (e.g. supabase or local DB)

# Start Redis locally (optional) and other containers
docker-compose up -d redis

# Run flyway migrations (optional, if not using docker)
./mvnw flyway:migrate

# Start the app
./mvnw spring-boot:run
```

When running, check `http://localhost:8080/actuator/health` (if actuator enabled) and `http://localhost:8080/swagger-ui.html` (or `/swagger-ui/index.html`) for API docs.

### Running with Docker
```bash
docker-compose up --build
```

---

## Testing
To run tests locally:

```bash
./mvnw test
```

Add or expand test suites in `src/test/java/...` as the project grows.

---

## Contributing
1. Fork the repository
2. Create a new branch (`git checkout -b feature/your-feature`)
3. Commit your changes
4. Push to your fork and open a Pull Request

Please include clear descriptions, tests for new logic, and keep commits atomic.

---

## FAQ
**Q: Can I use another database?**
A: Yes — the app uses Spring Data JPA with Postgres. Configure `POSTGRES_DB_URL` and credentials to point to your host.

**Q: How do I get a Mercado Pago access token?**
A: Register at [Mercado Pago Developers](https://www.mercadopago.com.br/developers/) and create an app to get your credentials. Put them in `MP_ACCESS_TOKEN` and related env vars.

**Q: Where can I change JWT settings?**
A: In your environment variables (`JWT_SECRET`, `JWT_EXPIRATION`, `JWT_REFRESH_EXPIRATION`) or override them in `application.yml` for each profile.

---

## Contact
Created by [Luís Miguel](https://github.com/LuísMiguelPerinotte) — feel free to reach out!

---

> **Note:** This documentation is under construction and will be updated as the project evolves.
