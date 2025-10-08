# Expense Segmentation Application

A full-stack expense segmentation application with React frontend, Spring Boot backend, and PostgreSQL database.

## Architecture

- **Frontend**: React + Vite + TypeScript (Port 3000)
- **Backend**: Spring Boot + Java (Port 8080)
- **Database**: PostgreSQL 16 (Port 5432)

## Prerequisites

- Docker (version 20.10 or higher)
- Docker Compose (version 2.0 or higher)

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd expense-segmentation-app
```

### 2. Configure Environment Variables

Copy the example environment file and customize it:

```bash
cp .env.example .env
```

**Important**: Edit the `.env` file and set secure values, especially for:
- `POSTGRES_PASSWORD` - Change from default
- `JWT_SECRET` - Generate a new secret (see Security section below)

### 3. Start All Services

```bash
# Build and start all services
make up

# Or using docker-compose directly
docker-compose up -d
```

### 4. Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Database**: localhost:5432

## Makefile Commands

The project includes a Makefile with convenient commands:

```bash
make help          # Show all available commands
make up            # Start all services
make down          # Stop all services
make restart       # Restart all services
make logs          # View logs from all services
make logs-backend  # View backend logs only
make logs-frontend # View frontend logs only
make logs-db       # View database logs only
make build         # Rebuild all images
make clean         # Remove containers, volumes, and images
make ps            # Show status of all services
make shell-backend # Open shell in backend container
make shell-frontend # Open shell in frontend container
make shell-db      # Open PostgreSQL shell
```

## Environment Variables

### Database Configuration

```env
POSTGRES_DB=expense_segmentation        # Database name
POSTGRES_USER=postgres                   # Database user
POSTGRES_PASSWORD=<secure-password>      # Database password (CHANGE THIS!)
POSTGRES_PORT=5432                       # Database port
```

### Backend Configuration

```env
BACKEND_PORT=8080                        # Backend API port
HIBERNATE_DDL_AUTO=update                # Hibernate DDL mode (update/create/create-drop)
JWT_SECRET=<base64-encoded-secret>       # JWT signing secret (CHANGE THIS!)
```

### Frontend Configuration

```env
FRONTEND_PORT=3000                       # Frontend port
VITE_API_BASE_URL=http://localhost:8080  # Backend API URL
```

## Security Considerations

### Generate Secure JWT Secret

The JWT secret should be a strong, randomly generated value. Generate a new one using:

```bash
openssl rand -base64 64
```

Then set it in your `.env` file:

```env
JWT_SECRET=<your-generated-secret-here>
```

### Change Default Database Password

Never use the default `postgres` password in production. Set a strong password in your `.env` file.

## Development Workflow

### Starting Development

```bash
# Start all services in detached mode
make up

# View logs to ensure everything started correctly
make logs

# Check service status
make ps
```

### Making Changes

#### Frontend Changes

The frontend uses a multi-stage Docker build. After making changes:

```bash
# Rebuild and restart frontend
docker-compose up -d --build frontend
```

#### Backend Changes

After making changes to the backend:

```bash
# Rebuild and restart backend
docker-compose up -d --build backend
```

### Viewing Logs

```bash
# All services
make logs

# Specific service
make logs-backend
make logs-frontend
make logs-db

# Follow logs in real-time
docker-compose logs -f backend
```

### Accessing Containers

```bash
# Backend shell
make shell-backend

# Frontend shell
make shell-frontend

# Database shell (PostgreSQL CLI)
make shell-db
```

## Health Checks

All services include health checks:

- **Database**: Checks PostgreSQL availability using `pg_isready`
- **Backend**: Checks `/health` endpoint availability
- **Frontend**: Checks nginx serving on port 3000

Services will automatically restart if health checks fail (configured with `restart: unless-stopped`).

## Data Persistence

PostgreSQL data is persisted using a Docker named volume:

```yaml
volumes:
  postgres-data:
    driver: local
```

This ensures your data survives container restarts. To completely reset the database:

```bash
make clean  # WARNING: This deletes all data!
```

## Networking

All services communicate via the `expense-network` Docker bridge network:

- Frontend can reach backend at `http://backend:8080`
- Backend can reach database at `jdbc:postgresql://database:5432/expense_segmentation`

## Troubleshooting

### Services Won't Start

Check logs for specific errors:

```bash
make logs
```

Ensure ports are not already in use:

```bash
# Check if ports 3000, 8080, or 5432 are in use
lsof -i :3000
lsof -i :8080
lsof -i :5432
```

### Database Connection Issues

Verify database is healthy:

```bash
docker-compose ps database
```

Check database logs:

```bash
make logs-db
```

### Frontend Can't Connect to Backend

1. Verify backend is healthy: `docker-compose ps backend`
2. Check `VITE_API_BASE_URL` in `.env` matches your backend URL
3. Remember: Environment variables are embedded at build time for Vite, so rebuild after changes:

```bash
docker-compose up -d --build frontend
```

### Port Conflicts

If default ports conflict with existing services, change them in `.env`:

```env
FRONTEND_PORT=3001
BACKEND_PORT=8081
POSTGRES_PORT=5433
```

Then restart:

```bash
make restart
```

## Production Deployment

For production deployment:

1. **Never use default passwords** - Generate secure passwords for all services
2. **Generate unique JWT secret** - Use `openssl rand -base64 64`
3. **Set `HIBERNATE_DDL_AUTO=validate`** - Prevents automatic schema changes
4. **Use external database** - Consider managed PostgreSQL service
5. **Enable HTTPS** - Configure reverse proxy (nginx/traefik) with SSL certificates
6. **Set resource limits** - Add CPU/memory limits in docker-compose.yml
7. **Configure logging** - Set up centralized logging
8. **Regular backups** - Implement database backup strategy

## Architecture Details

### Multi-Stage Builds

Both frontend and backend use multi-stage Docker builds to:
- Minimize final image size
- Separate build dependencies from runtime dependencies
- Improve build caching

### Frontend (React + Vite)

- **Build Stage**: Compiles TypeScript and bundles assets with Vite
- **Runtime Stage**: Serves static files with nginx
- Environment variables are embedded at build time via build args

### Backend (Spring Boot)

- **Build Stage**: Compiles Java code with Maven
- **Runtime Stage**: Runs JAR with OpenJDK, using non-root user for security

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]
