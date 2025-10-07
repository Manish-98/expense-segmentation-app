.PHONY: help build up down start stop restart logs clean test ps db-shell app-shell health

# Default target
help:
	@echo "Available commands:"
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
	@echo "  make test         - Run tests locally"
	@echo "  make db-shell     - Access PostgreSQL shell"
	@echo "  make app-shell    - Access application container shell"
	@echo "  make health       - Check application health"
	@echo "  make dev          - Run application locally (without Docker)"

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
