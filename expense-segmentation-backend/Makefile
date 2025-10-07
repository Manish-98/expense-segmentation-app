.PHONY: help build up down start stop restart logs clean test ps db-shell app-shell health \
        format format-check coverage coverage-verify sonar gradle-build

# Default target
help:
	@echo "Available commands:"
	@echo ""
	@echo "Docker Commands:"
	@echo "  make build        - Build Docker images"
	@echo "  make up           - Start all services (build + run)"
	@echo "  make down         - Stop and remove all containers"
	@echo "  make start        - Start existing containers"
	@echo "  make stop         - Stop running containers"
	@echo "  make restart      - Restart all services"
	@echo "  make logs         - View logs (all services)"
	@echo "  make logs-app     - View application logs"
	@echo "  make logs-db      - View database logs"
	@echo "  make ps           - List running containers"
	@echo "  make clean        - Remove containers, volumes, and images"
	@echo ""
	@echo "Development Commands:"
	@echo "  make test         - Run tests"
	@echo "  make dev          - Run application locally (without Docker)"
	@echo "  make gradle-build - Full Gradle build (tests + coverage + format check)"
	@echo ""
	@echo "Code Quality Commands:"
	@echo "  make format       - Auto-format code with Spotless"
	@echo "  make format-check - Check code formatting"
	@echo "  make coverage     - Generate test coverage report"
	@echo "  make coverage-verify - Verify coverage meets 90% threshold"
	@echo "  make sonar        - Run SonarQube analysis"
	@echo ""
	@echo "Utility Commands:"
	@echo "  make db-shell     - Access PostgreSQL shell"
	@echo "  make app-shell    - Access application container shell"
	@echo "  make health       - Check application health"

# Build Docker images
build:
	docker-compose build

# Build and start all services
up:
	docker-compose up -d
	@echo "Services are starting..."
	@echo "Waiting for application to be healthy..."
	@sleep 10
	@make health

# Stop and remove containers
down:
	docker-compose down

# Start existing containers
start:
	docker-compose start

# Stop running containers
stop:
	docker-compose stop

# Restart all services
restart:
	docker-compose restart

# View logs for all services
logs:
	docker-compose logs -f

# View application logs only
logs-app:
	docker-compose logs -f app

# View database logs only
logs-db:
	docker-compose logs -f postgres

# List running containers
ps:
	docker-compose ps

# Clean up everything (containers, volumes, images)
clean:
	docker-compose down -v --rmi all
	@echo "Cleaned up all containers, volumes, and images"

# Run tests locally
test:
	./gradlew test

# Access PostgreSQL shell
db-shell:
	docker-compose exec postgres psql -U postgres -d expense_segmentation

# Access application container shell
app-shell:
	docker-compose exec app sh

# Check application health
health:
	@echo "Checking application health..."
	@curl -f http://localhost:8080/health && echo "\n✅ Application is healthy" || echo "\n❌ Application is not responding"

# Run application locally (without Docker)
dev:
	./gradlew bootRun

# Rebuild and restart only the application (useful during development)
rebuild-app:
	docker-compose up -d --build app
	@echo "Application rebuilt and restarted"
	@sleep 5
	@make health

# Full Gradle build with all checks
gradle-build:
	./gradlew build
	@echo "\n✅ Build completed with tests, coverage, and format checks"

# Auto-format code with Spotless
format:
	./gradlew spotlessApply
	@echo "✅ Code formatted successfully"

# Check code formatting
format-check:
	./gradlew spotlessCheck

# Generate test coverage report
coverage:
	./gradlew jacocoTestReport
	@echo "\n📊 Coverage report generated at: build/reports/jacoco/index.html"
	@echo "Open it with: open build/reports/jacoco/index.html"

# Verify coverage meets threshold (90%)
coverage-verify:
	./gradlew jacocoTestCoverageVerification
	@echo "✅ Coverage verification passed (90% threshold)"

# Run SonarQube analysis (requires SonarQube server running)
sonar:
	@echo "Starting SonarQube analysis..."
	@echo "Ensure SonarQube is running at http://localhost:9000"
	./gradlew sonar
	@echo "✅ SonarQube analysis completed"
	@echo "View results at: http://localhost:9000"
