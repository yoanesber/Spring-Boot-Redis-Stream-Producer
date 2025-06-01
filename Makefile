# Variables for the application container
APP_CONTAINER_IMAGE=my-redis-stream-producer-app
APP_CONTAINER_NAME=redis-stream-producer-app
APP_DOCKER_CONTEXT=.
APP_DOCKERFILE=./src/main/docker/app/Dockerfile
APP_PORT=8081

# Variables for the Redis container
REDIS_CONTAINER_IMAGE=my-redis-stream-server
REDIS_CONTAINER_NAME=redis-stream-server
REDIS_DOCKERFILE=./src/main/docker/redis/Dockerfile
REDIS_DOCKER_CONTEXT=./src/main/docker/redis
REDIS_PORT=6379

# Network for the application and RabbitMQ containers
NETWORK=app-network

# Running in development mode
dev:
	@echo "Running in development mode..."
	./mvnw spring-boot:run

# Building the application as a JAR file
# This will run Maven Lifecycle phase "package": clean → validate → compile → test → package, 
# which cleans the target directory, compiles the code, runs tests, and packages the application into a JAR file.
package:
	@echo "Building the application as a JAR file..."
	./mvnw clean package -DskipTests



# Docker related targets
# Create a Docker network if it does not exist
docker-create-network:
	docker network inspect $(NETWORK) >NUL 2>&1 || docker network create $(NETWORK)

# Remove the Docker network if it exists
docker-remove-network:
	docker network rm $(NETWORK)



# --- Redis Stream Producer Targets ---
docker-build-redis:
	docker build -f $(REDIS_DOCKERFILE) -t $(REDIS_CONTAINER_IMAGE) $(REDIS_DOCKER_CONTEXT)

docker-run-redis:
	docker run --name $(REDIS_CONTAINER_NAME) --network $(NETWORK) -p $(REDIS_PORT):$(REDIS_PORT) \
	-d $(REDIS_CONTAINER_IMAGE)

docker-build-run-redis: docker-build-redis docker-run-redis

docker-remove-redis:
	docker stop $(REDIS_CONTAINER_NAME)
	docker rm $(REDIS_CONTAINER_NAME)



# --- Application Docker Targets ---
# Build the application in Docker
docker-build-app:
	docker build -f $(APP_DOCKERFILE) -t $(APP_CONTAINER_IMAGE) $(APP_DOCKER_CONTEXT)

# Run the application in Docker
docker-run-app: 
	docker run --name $(APP_CONTAINER_NAME) --network $(NETWORK) -p $(APP_PORT):$(APP_PORT) \
	-e SERVER_PORT=$(APP_PORT) \
	-d $(APP_CONTAINER_IMAGE)

# Build and run the application container
docker-build-run-app: docker-build-app docker-run-app

# Remove the application container
docker-remove-app:
	docker stop $(APP_CONTAINER_NAME)
	docker rm $(APP_CONTAINER_NAME)


# Optional: Run everything
docker-up: docker-create-network docker-build-run-redis docker-build-run-app
docker-down: docker-remove-app docker-remove-redis docker-remove-network

.PHONY: dev package \
	docker-create-network docker-remove-network \
	docker-build-redis docker-run-redis docker-build-run-redis docker-remove-redis \
	docker-build-app docker-run-app docker-build-run-app docker-remove-app \
	docker-up docker-down