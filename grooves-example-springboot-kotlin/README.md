# Grooves Example - Spring Boot Kotlin

This example demonstrates event sourcing with Grooves using Spring Boot and Kotlin with MongoDB as the data store.

## Prerequisites

### MongoDB Setup

This application requires MongoDB to run.
The build configuration automatically handles MongoDB differently for different scenarios:

#### For `bootRun` (Development)

- **Systems supporting embedded MongoDB**: MongoDB will be automatically started and stopped
- **Mac ARM64 and other unsupported systems**: You need to start MongoDB manually using Docker:

```bash
docker run --name mongo-dev -p 27021:27017 -d mongo:4.4.0
```

To stop the MongoDB container:
```bash
docker stop mongo-dev
docker rm mongo-dev
```

#### For Tests

Tests use Testcontainers to automatically start and stop MongoDB containers.
Docker must be running on your system for tests to work.

## Running the Application

```bash
./gradlew bootRun
```

## Running Tests

```bash
./gradlew test
```

The tests will automatically start a MongoDB container using Testcontainers.