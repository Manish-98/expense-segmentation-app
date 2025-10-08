.PHONY: help build up down logs backend-shell db-shell frontend-shell \
        start stop restart ps clean health rebuild-backend rebuild-frontend

# Default target
help:
	@echo "Available commands:"
	@echo ""
	@echo "Docker Commands:"
	@echo "  make build             - Build all services"
	@echo "  make up                - Start all services in detached mode"
	@echo "  make down              - Stop and remove containers"
	@echo "  make start             - Start existing containers"
	@echo "  make stop              - Stop running containers"
	@echo "  make restart           - Restart all services"
	@echo "  make logs              - Follow logs of all services"
	@echo "  make logs-backend      - Follow backend logs"
	@echo "  make logs-frontend     - Follow frontend logs"
	@echo "  make logs-db           - Follow database logs"
	@echo "  make ps                - List running containers"
	@echo "  make clean             - Remove containers, volumes, and images"
	@echo ""
	@echo "Shell Access Commands:"
	@echo "  make backend-shell     - Access backend container shell"
	@echo "  make db-shell          - Access PostgreSQL shell"
	@echo "  make frontend-shell    - Access frontend container shell"
	@echo ""
	@echo "Utility Commands:"
	@echo "  make health            - Check health of all services"
	@echo "  make rebuild-backend   - Rebuild and restart backend only"
	@echo "  make rebuild-frontend  - Rebuild and restart frontend only"

# Build all services
build:
	@echo "Building all services..."
	docker-compose build
	@echo "✅ All services built successfully"

# Start all services in detached mode
up:
	@echo "Starting all services..."
	docker-compose up -d
	@echo "Waiting for services to be healthy..."
	@sleep 15
	@make health

# Stop and remove containers
down:
	@echo "Stopping and removing containers..."
	docker-compose down
	@echo "✅ All containers stopped and removed"

# Start existing containers
start:
	docker-compose start
	@echo "✅ Containers started"

# Stop running containers
stop:
	docker-compose stop
	@echo "✅ Containers stopped"

# Restart all services
restart:
	@echo "Restarting all services..."
	docker-compose restart
	@echo "✅ All services restarted"

# Follow logs of all services
logs:
	docker-compose logs -f

# Follow backend logs
logs-backend:
	docker-compose logs -f backend

# Follow frontend logs
logs-frontend:
	docker-compose logs -f frontend

# Follow database logs
logs-db:
	docker-compose logs -f database

# List running containers
ps:
	docker-compose ps

# Clean up everything (containers, volumes, images)
clean:
	@echo "Cleaning up all containers, volumes, and images..."
	docker-compose down -v --rmi all
	@echo "✅ Cleaned up all containers, volumes, and images"

# Access backend container shell
backend-shell:
	docker-compose exec backend sh

# Access PostgreSQL shell
db-shell:
	docker-compose exec database psql -U postgres -d expense_segmentation

# Access frontend container shell
frontend-shell:
	docker-compose exec frontend sh

# Check health of all services
health:
	@echo "Checking service health..."
	@echo ""
	@echo "Database:"
	@docker-compose exec -T database pg_isready -U postgres && echo "✅ Database is healthy" || echo "❌ Database is not responding"
	@echo ""
	@echo "Backend:"
	@curl -f http://localhost:8080/health && echo "✅ Backend is healthy" || echo "❌ Backend is not responding"
	@echo ""
	@echo "Frontend:"
	@curl -f http://localhost:3000 && echo "✅ Frontend is healthy" || echo "❌ Frontend is not responding"

# Rebuild and restart backend only
rebuild-backend:
	@echo "Rebuilding and restarting backend..."
	docker-compose up -d --build backend
	@sleep 10
	@curl -f http://localhost:8080/health && echo "✅ Backend is healthy" || echo "❌ Backend is not responding"

# Rebuild and restart frontend only
rebuild-frontend:
	@echo "Rebuilding and restarting frontend..."
	docker-compose up -d --build frontend
	@sleep 5
	@curl -f http://localhost:3000 && echo "✅ Frontend is healthy" || echo "❌ Frontend is not responding"
