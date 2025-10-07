# Docker Setup Guide

## Installed Components

✅ **Docker CLI** - v28.5.0
✅ **Docker Compose** - v2.40.0
✅ **Colima** - Lightweight Docker runtime for macOS

## Colima Management

Colima is a lightweight Docker runtime that runs Docker without Docker Desktop.

### Start Colima

```bash
colima start
```

### Stop Colima

```bash
colima stop
```

### Restart Colima

```bash
colima restart
```

### Check Colima Status

```bash
colima status
```

### Auto-start on Login (Optional)

```bash
brew services start colima
```

### Stop Auto-start

```bash
brew services stop colima
```

## Running the Application

Once Colima is running, you can use all Docker commands normally:

```bash
# Build and start the application
make up

# Or using docker-compose directly
docker-compose up -d

# Check running containers
docker ps

# View logs
make logs

# Stop the application
make down
```

## Troubleshooting

### Docker commands not working?

Check if Colima is running:
```bash
colima status
```

If not running, start it:
```bash
colima start
```

### Port conflicts

If port 5432 or 8080 is already in use:
```bash
# Check what's using the port
lsof -i :5432
lsof -i :8080

# Kill the process if needed
kill -9 <PID>
```

### Reset everything

```bash
# Stop colima
colima stop

# Delete colima VM (warning: deletes all containers and images)
colima delete

# Start fresh
colima start
```

## Resource Configuration

Colima default resources:
- CPUs: 2
- Memory: 2GB
- Disk: 60GB

To adjust resources:
```bash
colima start --cpu 4 --memory 4
```

## Next Steps

Now you're ready to run the application:

```bash
cd expense-segmentation-backend
make up
```

Visit `http://localhost:8080/health` to verify the application is running.
