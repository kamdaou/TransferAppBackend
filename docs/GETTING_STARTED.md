# Getting Started — TransferApp Backend

## Prerequisites

- **Java 17+** (JDK)
- **Docker & Docker Compose** (for PostgreSQL)
- **IntelliJ IDEA** (recommended) or any IDE with Kotlin support

## 1. Clone & Setup Environment

```bash
cd ~/Desktop/transfer\ app
```

Create the `.env` file in the project root (this file is gitignored):

```bash
cp .env.example .env
```

Or create it manually:

```
DB_NAME=transferapp
DB_URL=jdbc:postgresql://localhost:5432/transferapp
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=super-secret-key-change-me-in-production-min-256-bits
```

For production, use strong values for `DB_PASSWORD` and `JWT_SECRET` (min 256-bit key).

## 2. Start PostgreSQL

```bash
docker compose up -d
```

Verify it's running:

```bash
docker compose ps
```

You should see the `db` service as `healthy`.

To check logs if something goes wrong:

```bash
docker compose logs db
```

## 3. Run the App

### Option A: IntelliJ IDEA

1. Open the project in IntelliJ
2. Let Gradle sync finish
3. Run `TransferAppApplication.kt` (green play button)

The app uses dev defaults in `application.properties`, so it works without setting environment variables in IntelliJ.

### Option B: Command line

```bash
./gradlew bootRun
```

### Option C: Build and run JAR

```bash
./gradlew bootJar
java -jar build/libs/transfer_app-0.0.1-SNAPSHOT.jar
```

## 4. Verify

The app starts on port **8080** by default. Check it's running:

```bash
curl http://localhost:8080/swagger-ui/index.html
```

Or open in browser: http://localhost:8080/swagger-ui/index.html

## 5. Default Super Admin

A super admin is auto-created on first startup:

| Field | Default Value |
|-------|--------------|
| Phone | `+23500000000` |
| PIN | `000000` |
| Company Code | `__SYSTEM__` |

Login:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"companyCode": "__SYSTEM__", "phone": "+23500000000", "pin": "000000"}'
```

To customize these, set environment variables before starting:

```
SUPER_ADMIN_PHONE=+235XXXXXXXX
SUPER_ADMIN_PIN=your-pin
```

## 6. Initial Setup Flow

After logging in as super admin:

1. **Create a company**: `POST /api/v1/companies`
2. **Create cities** for the company: `POST /api/v1/companies/{id}/cities`
3. **Create a company admin**: `POST /api/v1/companies/{id}/admin`
4. **Login as company admin** and set up commissions, pairings, etc.
5. **Agents self-register** via `POST /api/v1/auth/register` with the company code
6. **Company admin approves agents** via `POST /api/v1/agents/{id}/approve`

## Useful Commands

| Command | Description |
|---------|-------------|
| `docker compose up -d` | Start PostgreSQL |
| `docker compose down` | Stop PostgreSQL (data persists) |
| `docker compose down -v` | Stop PostgreSQL and delete all data |
| `./gradlew bootRun` | Run the app |
| `./gradlew compileKotlin` | Compile only (check for errors) |
| `./gradlew bootJar` | Build production JAR |

## Ports

| Service | Port |
|---------|------|
| TransferApp API | 8080 |
| PostgreSQL | 5432 |

## Troubleshooting

### Port 8080 already in use

```bash
lsof -ti:8080 | xargs kill -9
```

### PostgreSQL connection refused

Make sure Docker is running and the container is healthy:

```bash
docker compose ps
docker compose up -d
```

### Gradle build fails

```bash
./gradlew clean compileKotlin
```
