# Expense Segmentation Backend

Spring Boot REST API backend for expense segmentation application.

## Tech Stack

- **Java 17+**
- **Spring Boot 3.2.5**
- **Gradle (Kotlin DSL)**
- **PostgreSQL**
- **Spring Security**
- **Spring Data JPA**
- **Lombok**
- **Docker & Docker Compose**

## Quick Start (Docker - Recommended)

The easiest way to run the application is using Docker and Make:

```bash
# Start the application and database
make up

# Check application health
make health

# View logs
make logs

# Stop the application
make down
```

The application will be available at `http://localhost:8080`

## Prerequisites

### For Docker (Recommended)
- Docker & Docker Compose
- Make (optional, but recommended)

### For Local Development
- Java 17 or higher
- PostgreSQL database
- Gradle (or use included Gradle wrapper)

## Running the Application

### Option 1: Using Docker & Make (Recommended)

```bash
# View all available commands
make help

# Build and start all services (app + database)
make up

# View application logs
make logs-app

# View database logs
make logs-db

# Stop all services
make down

# Restart services
make restart

# Clean up everything (containers, volumes, images)
make clean

# Check application health
make health

# Access PostgreSQL shell
make db-shell

# Rebuild and restart only the app
make rebuild-app
```

### Option 2: Using Docker Compose directly

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Option 3: Local Development (without Docker)

1. Create a PostgreSQL database:
```sql
CREATE DATABASE expense_segmentation;
```

2. Update credentials in `src/main/resources/application.yml` if needed

3. Run the application:
```bash
# Using Make
make dev

# Or using Gradle wrapper
./gradlew bootRun

# Or using installed Gradle
gradle bootRun
```

The application will start on `http://localhost:8080`

## Health Check

Test the application is running:

```bash
curl http://localhost:8080/health
```

Expected response: `OK`

## Project Structure

```
src/main/java/com/expense/segmentation/
├── controller/     # REST API controllers
├── service/        # Business logic
├── repository/     # Data access layer
├── model/          # Entity classes
├── dto/            # Data Transfer Objects
└── config/         # Configuration classes
```

## Build

```bash
./gradlew build
```

## Run Tests

```bash
# Using Make
make test

# Or using Gradle wrapper
./gradlew test
```

## Docker Configuration

### Services

The `docker-compose.yml` defines two services:

1. **postgres** - PostgreSQL 16 database
   - Port: 5432
   - Database: expense_segmentation
   - Username: postgres
   - Password: postgres

2. **app** - Spring Boot application
   - Port: 8080
   - Depends on PostgreSQL (waits for health check)

### Environment Variables

The application supports environment variables for configuration:

- `SPRING_DATASOURCE_URL` - Database connection URL (default: `jdbc:postgresql://localhost:5432/expense_segmentation`)
- `SPRING_DATASOURCE_USERNAME` - Database username (default: `postgres`)
- `SPRING_DATASOURCE_PASSWORD` - Database password (default: `postgres`)
- `SPRING_JPA_HIBERNATE_DDL_AUTO` - Hibernate DDL mode (default: `update`)

### Volumes

- `postgres-data` - Persistent storage for PostgreSQL data

## Make Commands Reference

```bash
make help          # Show all available commands
make build         # Build Docker images
make up            # Start all services (build + run)
make down          # Stop and remove all containers
make start         # Start existing containers
make stop          # Stop running containers
make restart       # Restart all services
make logs          # View logs (all services)
make logs-app      # View application logs
make logs-db       # View database logs
make ps            # List running containers
make clean         # Remove containers, volumes, and images
make test          # Run tests locally
make db-shell      # Access PostgreSQL shell
make app-shell     # Access application container shell
make health        # Check application health
make dev           # Run application locally (without Docker)
make rebuild-app   # Rebuild and restart only the application
```
