# 🛒 E-Commerce API

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/LuisMiguelPerinotte/ecommerce-api/actions)
[![Java](https://img.shields.io/badge/Java-21-blue)](https://adoptopenjdk.net/)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## ✨ Overview
E-Commerce API is a backend RESTful service for online stores built with Java and Spring Boot.
It exposes endpoints for user management, product catalog, shopping cart, order processing and payment integration.
The codebase is structured to be modular and easy to extend — suitable as a learning project or a starting point for production systems.

> **Project Status:** In development 🚧 — contributions, issues and pull requests are welcome.

---

## 💡 Motivation
This project aims to:
- Demonstrate best practices in Java/Spring Boot API development
- Serve as a portfolio piece for backend engineering
- Provide a modular, extensible base for e-commerce applications

---

## 🚀 Features
| Feature                        | Status      | Description                                 |
|------------------------------- |-------------|---------------------------------------------|
| 👥 User registration/login     | Implemented | JWT-based authentication and authorization  |
| 📦 Product management          | Implemented | CRUD for products and categories            |
| 🧺 Shopping cart               | Implemented | Add/remove/update items, view cart          |
| 🧾 Order processing            | Implemented | Place orders, order history                 |
| 💳 Payment integration         | Implemented | Stripe gateway integration                  |
| 🏠 Address management          | Implemented | Manage shipping addresses                   |
| 🛠️ Admin panel                | Implemented | Admin endpoints for managing catalog/users  |
| 📚 API documentation (Swagger) | Implemented | Interactive API docs with Springdoc/OpenAPI |
| ✅ Automated tests             | In Progress | Unit and integration tests                  |

---

## Architecture 🏗️
- 🌱 **Spring Boot**: Main framework for REST API
- 🗄️ **Spring Data JPA**: ORM and database access
- 🧭 **Flyway**: Database migrations
- 🔐 **JWT**: Secure authentication
- 🐳 **Docker**: Containerization for easy deployment
- 🐘 **PostgreSQL**: Default database (can be swapped)

### 🗂️ Directory Structure
```
<repo-root>/
├─ pom.xml                      # Maven build file: dependencies, plugins, build lifecycle
├─ mvnw, mvnw.cmd               # Maven wrapper: run Maven without installing it globally
├─ Dockerfile                   # Image build instructions for the application
├─ docker-compose.yml          # Compose stack (Postgres, Redis) used for local development
├─ README.md                    # Project documentation (this file)
├─ LICENSE                      # Project license (MIT)
├─ .env.example                 # Example env vars used by application.yml via config.import
├─ .gitignore                   # Files/dirs ignored by Git
└─ src/
   ├─ main/
   │  ├─ java/
   │  │  └─ com/java/luismiguel/ecommerce_api/
   │  │     ├─ EcommerceApiApplication.java   # Spring Boot entrypoint (main method)
   │  │     ├─ api/                           # HTTP layer (controllers, request/response DTOs)
   │  │     │  ├─ controller/                 # REST controllers, mapping endpoints
   │  │     │  └─ dto/                        # DTOs for requests and responses
   │  │     ├─ application/                   # service implementations
   │  │     ├─ domain/                        # JPA entities, repositories and enums
   │  │     └─ infrastructure/                # Integrations & technical implementations
   │  │        ├─ client/                     # External clients
   │  │        ├─ config/                     # Spring configuration classes
   │  │        ├─ exception/                  # Exception types and handlers (GlobalExceptionHandler)
   │  │        └─ security/                   # Security Config and JWT
   │  └─ resources/
   │     ├─ application.yml                  # Main configuration (reads env vars)
   │     ├─ db/
   │     │  └─ migration/                    # Flyway SQL migrations (V1__, V2__, ...)
   │     ├─ static/                          # Static assets served by Spring (if any)
   │     └─ templates/                       # Template views (thymeleaf/free marker) if used
   └─ test/
      ├─ java/
      │  └─ com/java/luismiguel/             # Unit and integration tests mirroring main packages
      └─ resources/
         └─ application-test.yml             # Test properties
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
- `STRIPE_API_KEY`, `STRIPE_WEBHOOK_SECRET` — Stripe credential and webhook secret.

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

stripe:
  api:
    key: ${STRIPE_API_KEY}

  webhook:
    secret: ${STRIPE_WEBHOOK_SECRET}

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION}
```

Notes:
- `application.yml` includes `config.import: optional:file:.env[.properties]`, so using a `.env` file in the repository root is a convenient way to load variables during local development. In production prefer environment variables or a secrets manager.
- `POSTGRES_DB_URL` must be a valid JDBC URL. When using managed hosts (like Supabase) confirm whether `sslmode=require` or certificate options are required.

---


## 🧰 Stack

### ⚙️ Languages & Runtimes

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

### 🧩 Frameworks & Libraries

![Spring](https://img.shields.io/badge/Spring%20Boot-6DB33F.svg?style=for-the-badge&logo=Spring-Boot&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0200.svg?style=for-the-badge&logo=Flyway&logoColor=white)
![Swagger](https://img.shields.io/badge/-Swagger-%23Clojure?style=for-the-badge&logo=swagger&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-f5f5f5?style=for-the-badge&logo=junit5&logoColor=dc524a)

### 🔒 Security

![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F.svg?style=for-the-badge&logo=Spring-Security&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

### 🗃️ Database & Cache

![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![H2](https://img.shields.io/badge/H2%20Database-09476B.svg?style=for-the-badge&logo=H2-Database&logoColor=white)

### 🚀 DevOps & Containerization

![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Apache%20Maven-C71A36.svg?style=for-the-badge&logo=Apache-Maven&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF.svg?style=for-the-badge&logo=GitHub-Actions&logoColor=white)

### 💳 Payments & Integrations

![Stripe](https://img.shields.io/badge/Stripe-5469d4?style=for-the-badge&logo=stripe&logoColor=ffffff)

---

## 🗃️ Database — local or hosted (e.g. Supabase)
This project supports running Postgres either locally (via Docker Compose) or using a hosted Postgres service such as Supabase, AWS RDS, ElephantSQL, DigitalOcean, etc.

Options:

-- 🖥️ Local (recommended for development): the repository includes a `docker-compose.yml` service for Postgres. To start a local Postgres instance:

  1. Copy or create an `.env` with local credentials (see `.env.example`).
  2. Start Postgres with Docker Compose:

     ```bash
     docker-compose up -d postgres
     ```

  3. The container exposes port 5432 by default. Use a JDBC URL like:

     ```text
     POSTGRES_DB_URL=jdbc:postgresql://localhost:5432/ecommerce_db
     POSTGRES_DB_USER=ecom_user
     POSTGRES_DB_PASSWORD=secret
     ```

-- ☁️ Hosted / VPS (Supabase): if you prefer a managed DB, set the environment variables from your provider. For Supabase you typically need the host, port, database, user and password. When using Supabase you may need to include `?sslmode=require` in the JDBC URL, for example:

  ```text
  POSTGRES_DB_URL=jdbc:postgresql://db.abcd.supabase.co:5432/postgres?sslmode=require
  POSTGRES_DB_USER=postgres
  POSTGRES_DB_PASSWORD=your_supabase_db_password
  ```

Notes:

- The application reads `POSTGRES_DB_URL`, `POSTGRES_DB_USER` and `POSTGRES_DB_PASSWORD` from the environment or `.env` file (via `application.yml`).
- Flyway migrations are included in `src/main/resources/db/migration` — when you run the app (or `./mvnw flyway:migrate`) Flyway will apply the migrations to the configured database.
- Postgres and Redis services are defined together in the repository root `docker-compose.yml`. You can start both services at once with `docker-compose up -d` (or specify service names to start them individually).

---

## ⚡ Redis — local or cloud (e.g. Redis Cloud)
The application uses Redis for caching and session-related features. You can run Redis locally via Docker Compose or use a hosted provider such as Redis Cloud, AWS ElastiCache, Upstash, etc.

-- 🖥️ Local (Docker Compose): the included `docker-compose.yml` has a Redis service. Start it with:

  ```bash
  docker-compose up -d redis
  ```

  Or start both Postgres and Redis together:

  ```bash
  docker-compose up -d postgres redis
  ```

-- ☁️ Hosted / Redis Cloud: set `REDIS_HOST` and `REDIS_PORT` (and any required auth) in your environment or `.env`. When using Redis Cloud or other managed providers, use the provided host, port and password. Example `.env` entries:

  ```text
  REDIS_HOST=your-redis-host.example.com
  REDIS_PORT=6379
  REDIS_PASSWORD=xxxxxxxx
  ```

Notes:

- If you use a managed Redis service with TLS or special connection parameters, make sure to provide the proper client configuration in `application.yml` or environment variables.
- Postgres and Redis are available in the same `docker-compose.yml` file at the repository root — running `docker-compose up -d` will bring both up. Use `docker-compose up -d postgres redis` to be explicit about which services to start.

---

## 📖 Swagger / OpenAPI (API docs)
The project uses `springdoc-openapi` (see `pom.xml`) to automatically generate OpenAPI docs. By default the UI is available at one of these URLs (depending on Springdoc version and configuration):

- `/swagger-ui.html`
- `/swagger-ui/index.html`
- OpenAPI JSON at `/v3/api-docs`

If the UI doesn't appear, check the `springdoc` configuration and application logs.

---

## 🏁 Getting Started

### ✅ Pre-requisites
- Java 21
- Maven
- Docker (optional)

### ▶️ Quick start (local dev)
```bash
# Clone
git clone https://github.com/LuisMiguelPerinotte/ecommerce-api.git
cd ecommerce-api

# Copy the example env file and edit values
# Linux / macOS / Git Bash:
cp .env.example .env
# Windows (PowerShell):
# Copy-Item .env.example .env
# Windows (cmd.exe):
# copy .env.example .env

# Start services defined in docker-compose (Postgres and/or Redis)
docker-compose up -d

# Run flyway migrations (optional, if not using docker)
# On Linux / macOS / Git Bash:
./mvnw flyway:migrate
# On Windows (PowerShell / cmd):
./mvnw.cmd flyway:migrate

# Start the app
# On Linux / macOS / Git Bash:
./mvnw spring-boot:run
# On Windows (PowerShell / cmd):
./mvnw.cmd spring-boot:run
```

When running, check `http://localhost:8080/actuator/health` (if actuator enabled) and `http://localhost:8080/swagger-ui.html` (or `/swagger-ui/index.html`) for API docs.

### 🐳 Running with Docker
```bash
docker-compose up --build
```

---

## 🧪 Testing
To run tests locally:

```bash
./mvnw test
```

Add or expand test suites in `src/test/java/...` as the project grows.

---

## Contributing 🤝
1. Fork the repository
2. Create a new branch (`git checkout -b feature/your-feature`)
3. Commit your changes
4. Push to your fork and open a Pull Request

Please include clear descriptions, tests for new logic, and keep commits atomic.

---


## FAQ ❓

**Q: Can I use another database?**

A: Yes — the app uses Spring Data JPA with Postgres by default. Configure `POSTGRES_DB_URL`, `POSTGRES_DB_USER` and `POSTGRES_DB_PASSWORD` to point to your desired Postgres host.

**Q: How do I get a Stripe Api Key?**

A: Register at [Stripe](https://stripe.com/br) and create an app to obtain credentials. Put them in `STRIPE_API_KEY` and related environment variables.

**Q: Where can I change JWT settings?**

A: JWT configuration is read from environment variables (`JWT_SECRET`, `JWT_EXPIRATION`, `JWT_REFRESH_EXPIRATION`) and mapped in `application.yml` — override them per profile if needed.

---

> ⚠️ **Note:** This documentation is under construction and will be updated as the project evolves.
