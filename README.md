# 🎅 Santa Backend (`santa-backend`)

A lightweight, production-ready backend service for the festive family application **"Santa's Post Office"**.

The application was created for family members to share Christmas wishlists and secretly coordinate gift reservations. The primary objective was to build a clean, containerized service with automated migrations and deploy it to a remote cloud virtual machine (Oracle Cloud VM / VPS).

---

## 💡 Project Context & Architecture

The application is structured into two independent modules:

- **Backend (this repository)**: Designed and implemented **entirely by hand from scratch** using Java 21 and Spring Boot 3, backed by PostgreSQL, Flyway database migrations, and BCrypt password security.
- **Frontend ([`santa-frontend`](https://github.com/RafSzef/santa-frontend))**: Built as an experimental **Proof of Concept (PoC)** to evaluate **Google Gemini (via Antigravity)** in an AI-assisted frontend workflow (AI Pair Programming), testing its ability to build an interactive, storybook-style Angular interface integrated with a custom backend.

Both applications are fully containerized using multi-stage Docker builds and deployed on an Oracle Cloud VM behind Nginx Proxy Manager.

---

## 🛠️ Tech Stack

- **Language & Runtime**: Java 21 LTS
- **Framework**: Spring Boot 3.4.2
  - **Spring Data JPA**: Data access layer built on Hibernate
  - **Spring Web**: RESTful JSON API endpoints
  - **Spring Security Crypto**: BCrypt password hashing for room authentication
- **Database**:
  - **PostgreSQL 16**: Production relational database in Docker
  - **H2 Database**: Fast in-memory database for unit and integration testing
- **Database Migrations**: Flyway (following `VYYMMDD_HHmm__{name}.sql` versioning)
- **Containerization**: Docker & Docker Compose (multi-stage build: Eclipse Temurin JDK 21 -> non-root JRE 21 Alpine)
- **Build Tool**: Apache Maven (via `mvnw` wrapper)

---

## ✨ Key Features

1. **Family Rooms (Pages)**:
   - Dynamic URL routing per family/group (e.g., `/kowalscy`, `/alohomora`).
   - Access verification protected by secure BCrypt password hashes.
   - Built-in startup seed runner ensuring a default room is initialized if absent.

2. **Letters to Santa (Letters)**:
   - Categorized into adult wishlists (`ADULT`) and children's wishlists (`CHILD` / Elf Workshop).
   - Full CRUD operations for letters and their associated gifts.

3. **Secret Gift Claiming (Gifts)**:
   - In children's wishlists, family members can discreetly claim gifts (*Elf Wax Stamp*) to avoid accidental duplicate purchases.
   - Persisted via the `is_claimed` database flag.

4. **Live Synchronization Support (`touchPage` mechanism)**:
   - Any modification to letters or gifts automatically touches the parent `Page.updated_at` timestamp.
   - Enables lightweight polling on the client side, allowing all family devices to stay updated in real time without manual page refreshes.

---

## 🗄️ Database Schema & Flyway Migrations

Migration scripts are located under `src/main/resources/db/migration/`:
- `V261029_1952__init_schema.sql`: Initial schema defining `pages`, `letters`, and `gifts` tables with foreign keys and cascade rules.
- `V261029_2245__ensure_indexes.sql`: Foreign key performance indexes on parent-child relations.

---

## 🚀 Local Development

### Prerequisites:
- Java 21+
- Maven (or use the included `./mvnw`)
- PostgreSQL instance (or use the in-memory H2 profile)

### 1. Run with PostgreSQL (default profile):
Ensure PostgreSQL is running (default port `5432`) and credentials match `application.properties`, then execute:
```bash
./mvnw clean spring-boot:run
```

### 2. Run with Development Profile (In-Memory H2):
```bash
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev
```
The H2 web console will be accessible at: `http://localhost:8080/h2-console`.

### 3. Run Automated Tests:
```bash
./mvnw clean test
```

---

## 🐳 Docker & Production Deployment

The project includes an optimized multi-stage `Dockerfile`:
```bash
# Build Docker image
docker build -t santa-backend .

# Run container
docker run -d --name santa-backend -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db_host:5432/santadb \
  -e SPRING_DATASOURCE_USERNAME=santa_user \
  -e SPRING_DATASOURCE_PASSWORD=your_secure_password \
  santa-backend
```

In the production Oracle Cloud environment, the container runs in an isolated Docker network (`db-network` + `npm-network`) with reverse proxy routing managed by Nginx Proxy Manager.
