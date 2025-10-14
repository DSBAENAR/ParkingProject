# Parking Core Service

This is the Parking Core microservice — a Spring Boot application that manages vehicles, registers and basic authentication using JWT and Redis for token blacklisting. It uses H2 (file mode) as the main datastore for development and testing.

## Table of Contents

- Project summary
- Requirements
- Configuration
- Running locally
- Endpoints (examples)
- Authentication (JWT)
- Development notes

## Project summary

The service provides endpoints to:

- Register vehicle entrances and departures
- Manage vehicles (create, update, list)
- Generate simple reports
- Handle user sign up, login and logout with JWT

The codebase is a standard Spring Boot application. Important packages:

- `com.parking.core.handlers` — HTTP handlers / controllers
- `com.parking.core.service` — business logic
- `com.parking.core.model` — JPA entities (User, Vehicle, Register)
- `com.parking.core.auth` — authentication related logic (AuthHandler, JWT, blacklist)

## Requirements

- Java 17 (project uses JDK 17 in build/run logs)
- Maven
- Redis (for token blacklist in logout flow)

## Configuration

Configuration lives in `src/main/resources/application.properties`. Key properties:

- `spring.datasource.url=jdbc:h2:file:./data/parkingdb` — H2 file-based DB located in the project `data/` folder.
- `spring.h2.console.enabled=true` and `spring.h2.console.path=/h2-console` — H2 web console enabled for development.
- `spring.data.redis.host` and `spring.data.redis.port` — Redis connection details.
- `secret-key` — symmetric key used to sign JWT tokens.
- `jwt.expiration` — token lifetime in milliseconds (default 3600000 ms = 1 hour).

Example relevant section from `application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./data/parkingdb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.data.redis.host=localhost
spring.data.redis.port=6379

secret-key = BvqnIMmUVXd7je2b8zs4EJoY5ZP0mV1i8aZLEffu9bE=
jwt.expiration = 3600000
```

## Running locally

1. Start Redis (the application expects a running Redis instance by default):

  ```bash
  # using docker
  docker run -d --name redis-jwt -p 6379:6379 redis:7

  # or if you have redis-cli / system redis installed, start the service normally
  redis-server &
  ```

1. Build and run the application with Maven:

  ```bash
  mvn -B -DskipTests=true package
  java -jar target/core-0.0.1-SNAPSHOT.jar
  ```

1. Access the H2 console (if needed):

  Open the H2 console at: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

  JDBC URL: `jdbc:h2:file:./data/parkingdb`

## Endpoints (examples)

All endpoints are prefixed with `/api/v1/parking`.

### Authentication

- POST /api/v1/parking/auth/signUp
  - Request JSON body (AuthRequest):

```json
{
  "name": "John Doe",
  "username": "jdoe",
  "email": "jdoe@example.com",
  "password": "secret"
}
```

- Successful response: 200 OK with a JSON object containing `user`, `token` and `message`.

- POST /api/v1/parking/auth/login
  - Request same as signUp (username/email + password). Response includes `token`.

- POST /api/v1/parking/auth/logout
  - Requires `Authorization: Bearer <token>` header. This endpoint will add the token to a Redis blacklist until its expiration.

### Vehicles

- GET /api/v1/parking/vehicles/
  - Returns: `{ "vehicles": [ ... ] }`

- POST /api/v1/parking/vehicles/
  - Request JSON (Vehicle):

```json
{
  "id": "ABC-123",
  "type": "CAR"
}
```

- Response: `{ "message": "Vehicle created successfully", "vehicle": { ... } }`

- POST /api/v1/parking/vehicles/pay
  - Request: same Vehicle payload (calculates payment for a parked vehicle)
  - Response: `{ "price": 12.5, "Vehicle": { ... } }`

- PUT /api/v1/parking/vehicles/{id}
  - Update vehicle by id. Request body contains `Vehicle` JSON.

### Registers

- GET /api/v1/parking/registers
  - Returns: `{ "registers": [ ... ] }`

- POST /api/v1/parking/register
  - Request body: Vehicle JSON (register entrance)
  - Response: `{ "message": "Register created successfully", "register": { ... } }`

- POST /api/v1/parking/leave
  - Request body: Vehicle JSON (process exit)
  - Response: `{ "message": "Register updated successfully", "register": { ... } }`

### Reports

- GET /api/v1/parking/reports/monthly
  - Triggers a monthly report generation — response contains `message` and `report_file` path.

## Authentication & Token flow

- The service issues JWT tokens on login (see `AuthService` and `JWTService`).
- Tokens are signed using `secret-key` property and expire after `jwt.expiration` milliseconds.
- On logout the token is stored in Redis blacklist with its remaining lifetime; the `JWTAuthFilter` checks tokens against the blacklist to reject logged-out tokens.

## Example cURL flows

Login and use token to create a vehicle:

```bash
# login
curl -s -X POST http://localhost:8080/api/v1/parking/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"jdoe","password":"secret"}'

# Suppose the response contains { "token": "ey..." }

# create vehicle
curl -s -X POST http://localhost:8080/api/v1/parking/vehicles/ \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ey..." \
  -d '{"id":"ABC-123","type":"CAR"}'
```

## Development notes & tips

- H2 is used for convenience in development. For production, replace the datasource with a proper RDBMS and review `spring.jpa.hibernate.ddl-auto`.
- The sample `secret-key` in `application.properties` must be kept secret in production; use environment variables or an external secrets vault.
- If Redis is not available the logout / blacklist feature will fail; consider running it locally with Docker while developing.

## License

This project is provided as-is for educational purposes.
