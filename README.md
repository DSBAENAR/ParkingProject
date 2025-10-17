# Parking Core Service

A Spring Boot RESTful API for parking lot management with JWT authentication, Redis-based token blacklisting, Stripe payment integration, and H2 database. This service handles vehicle registration, parking session tracking, payment calculation, customer management, invoice generation, and monthly reporting.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication)
  - [Vehicles](#vehicles)
  - [Parking Registers](#parking-registers)
  - [Users](#users)
  - [Reports](#reports)
  - [Payments (Stripe)](#payments-stripe)
- [Authentication & Security](#authentication--security)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Features

- 🔐 JWT-based authentication with role-based access control (ADMIN/USER)
- 🚗 Vehicle management (CRUD operations)
- 📝 Parking session tracking (entry/exit timestamps)
- 💰 Automatic payment calculation based on parking duration and vehicle type
- � **Stripe integration** for payment processing (customers, cards, invoices)
- �📊 Monthly resident vehicle reports
- 🔒 Token blacklisting on logout using Redis
- 💾 H2 in-memory/file database for development
- 🎯 RESTful API design with comprehensive error handling

## Tech Stack

- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Security** (JWT authentication)
- **Spring Data JPA** (ORM)
- **H2 Database** (development)
- **Redis** (token blacklist)
- **Stripe API** (payment processing)
- **Maven** (build tool)
- **JJWT** (JWT library)

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 17** or higher
  - Verify: `java -version`
  - Download: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)

- **Apache Maven 3.6+**
  - Verify: `mvn -version`
  - Download: [Maven](https://maven.apache.org/download.cgi)

- **Redis Server**
  - Verify: `redis-cli ping` (should return `PONG`)
  - Install on macOS: `brew install redis`
  - Install with Docker: `docker pull redis:7`

- **Stripe Account** (optional, for payment features)
  - Sign up at [stripe.com](https://stripe.com)
  - Get your test API keys from the [Dashboard](https://dashboard.stripe.com/test/apikeys)

- **Git** (optional, for cloning)
  - Verify: `git --version`

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/DSBAENAR/ParkingProject.git
cd ParkingProject/core
```

Or download and extract the ZIP file.

### 2. Install Dependencies

Maven will automatically download all required dependencies when you build the project:

```bash
mvn clean install
```

This will:

- Download Spring Boot, H2, Redis, JWT, and other dependencies
- Compile the source code
- Run tests (use `-DskipTests` to skip)
- Package the application as a JAR file

## Configuration

### Application Properties

Configuration is stored in `src/main/resources/application.properties`. Key settings:

| Property | Description | Default Value |
|----------|-------------|---------------|
| `spring.datasource.url` | H2 database file location | `jdbc:h2:file:./data/parkingdb` |
| `spring.datasource.username` | Database username | `sa` |
| `spring.datasource.password` | Database password | _(empty)_ |
| `spring.h2.console.enabled` | Enable H2 web console | `true` |
| `spring.h2.console.path` | H2 console URL path | `/h2-console` |
| `spring.data.redis.host` | Redis server hostname | `localhost` |
| `spring.data.redis.port` | Redis server port | `6379` |
| `secret-key` | JWT signing secret key | _(see file)_ |
| `jwt.expiration` | Token expiration time (ms) | `3600000` (1 hour) |
| `sk_stripe` | Stripe API secret key | _(from environment variable)_ |

### Environment Variables (Recommended for Production)

For security, use environment variables instead of hardcoded values:

1. Create a `.env` file in the project root (add to `.gitignore`):

```bash
# .env
SECRET_KEY=your-secure-secret-key-here
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key_here
SPRING_DATASOURCE_URL=jdbc:h2:file:./data/parkingdb
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
JWT_EXPIRATION=3600000
```

2. Update `application.properties` to use environment variables:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:h2:file:./data/parkingdb}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:sa}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}

spring.data.redis.host=${SPRING_DATA_REDIS_HOST:localhost}
spring.data.redis.port=${SPRING_DATA_REDIS_PORT:6379}

secret-key=${SECRET_KEY:BvqnIMmUVXd7je2b8zs4EJoY5ZP0mV1i8aZLEffu9bE=}
jwt.expiration=${JWT_EXPIRATION:3600000}

# Stripe configuration
sk_stripe=${STRIPE_SECRET_KEY}
```

3. Load environment variables before running:

```bash
# On macOS/Linux
export $(cat .env | xargs)
# Or use set -a; source .env; set +a

java -jar target/core-0.0.1-SNAPSHOT.jar
```

### Database Setup

The application uses **H2** (file-based) by default. The database file is created automatically at `./data/parkingdb.mv.db` on first run.

**Access H2 Console:**

1. Start the application
2. Navigate to: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
3. Use these settings:
   - **JDBC URL:** `jdbc:h2:file:./data/parkingdb`
   - **User Name:** `sa`
   - **Password:** _(leave empty)_

### Redis Setup

Redis is required for JWT token blacklisting (logout functionality).

**Option 1: Docker (Recommended)**

```bash
docker run -d --name redis-jwt -p 6379:6379 redis:7
```

**Option 2: Local Installation**

```bash
# macOS
brew install redis
brew services start redis

# Ubuntu/Debian
sudo apt-get install redis-server
sudo systemctl start redis
```

Verify Redis is running:

```bash
redis-cli ping
# Should return: PONG
```

### Stripe Setup

The application integrates with **Stripe** for payment processing (customers, cards, and invoices).

**Get your Stripe API Keys:**

1. Sign up for a free Stripe account at [stripe.com](https://stripe.com)
2. Navigate to [Dashboard → Developers → API Keys](https://dashboard.stripe.com/test/apikeys)
3. Copy your **Secret key** (starts with `sk_test_` for test mode)
4. Add it to your `.env` file:

```bash
STRIPE_SECRET_KEY=sk_test_51abc...your_secret_key
```

**Test Mode vs Live Mode:**

- **Test mode** (`sk_test_`): Use test card numbers, no real charges
- **Live mode** (`sk_live_`): Real transactions, production use only

**Test Cards for Development:**

```text
Card Number: 4242 4242 4242 4242
Exp: Any future date (e.g., 12/34)
CVC: Any 3 digits (e.g., 123)
ZIP: Any 5 digits (e.g., 12345)
```

For more test cards, see [Stripe Test Cards](https://stripe.com/docs/testing).

## Running the Application

### Quick Start

1. **Start Redis** (required for logout functionality):

   ```bash
   docker run -d --name redis-jwt -p 6379:6379 redis:7
   ```

2. **Build the application**:

   ```bash
   mvn clean package -DskipTests
   ```

3. **Run the application**:

   ```bash
   java -jar target/core-0.0.1-SNAPSHOT.jar
   ```

   Or use Maven:

   ```bash
   mvn spring-boot:run
   ```

4. **Verify the application is running**:

   Open [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

The application will start on **port 8080** by default.

### First-Time Setup

1. The application will create the H2 database file automatically
2. Tables are created via JPA (Hibernate) on startup
3. You can create your first user via the `/auth/signUp` endpoint (see below)

### Stopping the Application

- Press `Ctrl+C` in the terminal running the application
- Stop Redis: `docker stop redis-jwt`

## Project Structure

```text
core/
├── src/
│   ├── main/
│   │   ├── java/com/parking/core/
│   │   │   ├── CoreApplication.java          # Main application entry point
│   │   │   ├── auth/                          # Authentication module
│   │   │   │   ├── AuthHandler.java           # Auth endpoints (signup, login, logout)
│   │   │   │   ├── Config/SecurityConfig.java # Spring Security configuration
│   │   │   │   ├── model/                     # Auth DTOs (AuthRequest, BlackListToken)
│   │   │   │   └── services/                  # JWT & auth services
│   │   │   ├── enums/                         # Enumerations (Roles, VehicleType)
│   │   │   ├── handlers/                      # REST controllers
│   │   │   │   ├── ParkingHandler.java        # Vehicle endpoints
│   │   │   │   ├── RegisterHandler.java       # Entry/exit endpoints
│   │   │   │   ├── UserHandler.java           # User management
│   │   │   │   └── ReportHandler.java         # Reporting endpoints
│   │   │   ├── model/                         # JPA entities (User, Vehicle, Register)
│   │   │   ├── repository/                    # Spring Data JPA repositories
│   │   │   └── service/                       # Business logic layer
│   │   └── resources/
│   │       └── application.properties         # Configuration file
│   └── test/                                  # Unit and integration tests
├── data/                                      # H2 database files (auto-generated)
├── reports/                                   # Generated reports (auto-generated)
├── pom.xml                                    # Maven dependencies
└── README.md                                  # This file
```

## API Endpoints

Base URL: `http://localhost:8080/api/v1/parking`

All endpoints (except authentication) require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

### Authentication

#### 1. Sign Up (Register New User)

**Endpoint:** `POST /api/v1/parking/auth/signUp`

**Description:** Creates a new user account and returns a JWT token.

**Request Body:**

```json
{
  "name": "John Doe",
  "username": "jdoe",
  "email": "jdoe@example.com",
  "password": "password123"
}
```

**Response (200 OK):**

```json
{
  "message": "user created successfully",
  "user": {
    "name": "John Doe",
    "username": "jdoe",
    "email": "jdoe@example.com",
    "role": "USER"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/api/v1/parking/auth/signUp \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "username": "jdoe",
    "email": "jdoe@example.com",
    "password": "password123"
  }'
```

#### 2. Bulk Sign Up

**Endpoint:** `POST /api/v1/parking/auth/signUp/bulk`

**Description:** Creates multiple users at once.

**Request Body:**

```json
[
  {
    "name": "Alice Smith",
    "username": "asmith",
    "email": "alice@example.com",
    "password": "pass123"
  },
  {
    "name": "Bob Johnson",
    "username": "bjohnson",
    "email": "bob@example.com",
    "password": "pass456"
  }
]
```

**Response (200 OK):**

```json
{
  "processed": 2,
  "results": [
    {
      "message": "user created successfully",
      "user": { "username": "asmith", ... },
      "token": "eyJ..."
    },
    {
      "message": "user created successfully",
      "user": { "username": "bjohnson", ... },
      "token": "eyJ..."
    }
  ]
}
```

#### 3. Login

**Endpoint:** `POST /api/v1/parking/auth/login`

**Description:** Authenticates a user and returns a JWT token.

**Request Body:**

```json
{
  "username": "jdoe",
  "password": "password123"
}
```

Or using email:

```json
{
  "email": "jdoe@example.com",
  "password": "password123"
}
```

**Response (200 OK):**

```json
{
  "message": "User logged-in correctly",
  "user": {
    "name": "John Doe",
    "username": "jdoe",
    "email": "jdoe@example.com",
    "role": "USER"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/api/v1/parking/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jdoe",
    "password": "password123"
  }'
```

#### 4. Logout

**Endpoint:** `POST /api/v1/parking/auth/logout`

**Description:** Invalidates the JWT token by adding it to the Redis blacklist.

**Headers:**

```
Authorization: Bearer <your-jwt-token>
```

**Response (200 OK):**

```json
{
  "message": "Logout successfully"
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/api/v1/parking/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Vehicles

#### 1. Get All Vehicles

**Endpoint:** `GET /api/v1/parking/vehicles/`

**Description:** Retrieves a list of all registered vehicles.

**Response (200 OK):**

```json
{
  "vehicles": [
    {
      "id": "ABC-123",
      "type": "CAR"
    },
    {
      "id": "XYZ-789",
      "type": "MOTORCYCLE"
    }
  ]
}
```

**cURL Example:**

```bash
curl -X GET http://localhost:8080/api/v1/parking/vehicles/ \
  -H "Authorization: Bearer <token>"
```

#### 2. Create Vehicle

**Endpoint:** `POST /api/v1/parking/vehicles/`

**Description:** Registers a new vehicle in the system.

**Request Body:**

```json
{
  "id": "ABC-123",
  "type": "CAR"
}
```

**Vehicle Types:**

- `CAR`
- `MOTORCYCLE`
- `OFFICIAL` (special vehicles)

**Response (200 OK):**

```json
{
  "message": "Vehicle created successfully",
  "vehicle": {
    "id": "ABC-123",
    "type": "CAR"
  }
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/api/v1/parking/vehicles/ \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "id": "ABC-123",
    "type": "CAR"
  }'
```

#### 3. Update Vehicle

**Endpoint:** `PUT /api/v1/parking/vehicles/{id}`

**Description:** Updates vehicle information.

**Path Parameter:**

- `id` - Vehicle license plate

**Request Body:**

```json
{
  "type": "MOTORCYCLE"
}
```

**Response (200 OK):**

```json
{
  "message": "Vehicle updated successfully",
  "vehicle": {
    "id": "ABC-123",
    "type": "MOTORCYCLE"
  }
}
```

#### 4. Calculate Parking Payment

**Endpoint:** `POST /api/v1/parking/vehicles/pay`

**Description:** Calculates the parking fee based on vehicle type and time spent.

**Request Body:**

```json
{
  "id": "ABC-123"
}
```

**Response (200 OK):**

```json
{
  "price": 12.5,
  "Vehicle": {
    "id": "ABC-123",
    "type": "CAR"
  }
}
```

**Pricing Rules:**

- Cars: Base rate calculation
- Motorcycles: Reduced rate
- Official vehicles: Free or special rate

#### 5. Get Month Start Dates

**Endpoint:** `GET /api/v1/parking/vehicles/startsMonth`

**Description:** Returns the starting dates of the current month for parking records.

### Parking Registers

#### 1. Get All Registers

**Endpoint:** `GET /api/v1/parking/registers`

**Description:** Retrieves all parking session records.

**Response (200 OK):**

```json
{
  "registers": [
    {
      "id": 1,
      "vehicle": {
        "id": "ABC-123",
        "type": "CAR"
      },
      "entrydate": "15-10-2025 08:30:00",
      "exitdate": "15-10-2025 17:45:00",
      "minutes": 555
    }
  ]
}
```

**cURL Example:**

```bash
curl -X GET http://localhost:8080/api/v1/parking/registers \
  -H "Authorization: Bearer <token>"
```

#### 2. Register Vehicle Entry

**Endpoint:** `POST /api/v1/parking/register`

**Description:** Records a vehicle entering the parking lot.

**Request Body:**

```json
{
  "id": "ABC-123",
  "type": "CAR"
}
```

**Response (200 OK):**

```json
{
  "message": "Register created successfully",
  "register": {
    "id": 1,
    "vehicle": {
      "id": "ABC-123",
      "type": "CAR"
    },
    "entrydate": "16-10-2025 09:00:00",
    "exitdate": null,
    "minutes": 0
  }
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/api/v1/parking/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "id": "ABC-123",
    "type": "CAR"
  }'
```

#### 3. Register Vehicle Exit

**Endpoint:** `POST /api/v1/parking/leave`

**Description:** Records a vehicle leaving the parking lot and calculates duration.

**Request Body:**

```json
{
  "id": "ABC-123"
}
```

**Response (200 OK):**

```json
{
  "message": "Register updated successfully",
  "register": {
    "id": 1,
    "vehicle": {
      "id": "ABC-123",
      "type": "CAR"
    },
    "entrydate": "16-10-2025 09:00:00",
    "exitdate": "16-10-2025 17:30:00",
    "minutes": 510
  }
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/api/v1/parking/leave \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "id": "ABC-123"
  }'
```

### Users

#### 1. Find User

**Endpoint:** `GET /api/v1/parking/users/user`

**Description:** Searches for a user by name, username, or email.

**Query Parameters:**

- `name` (optional)
- `username` (optional)
- `email` (optional)

**Example:**

```
GET /api/v1/parking/users/user?username=jdoe
```

**Response (200 OK):**

```json
{
  "user": {
    "name": "John Doe",
    "username": "jdoe",
    "email": "jdoe@example.com",
    "role": "USER"
  }
}
```

**cURL Example:**

```bash
curl -X GET "http://localhost:8080/api/v1/parking/users/user?username=jdoe" \
  -H "Authorization: Bearer <token>"
```

#### 2. Get All Users

**Endpoint:** `GET /api/v1/parking/users/`

**Description:** Retrieves all registered users.

**Response (200 OK):**

```json
{
  "users": [
    {
      "name": "John Doe",
      "username": "jdoe",
      "email": "jdoe@example.com",
      "role": "USER"
    },
    {
      "name": "Admin User",
      "username": "admin",
      "email": "admin@example.com",
      "role": "ADMIN"
    }
  ]
}
```

#### 3. Get Users (Paginated)

**Endpoint:** `GET /api/v1/parking/users/pages`

**Description:** Retrieves users with pagination.

**Query Parameters:**

- `pageNumber` - Page number (0-indexed)

**Example:**

```
GET /api/v1/parking/users/pages?pageNumber=0
```

**Response (200 OK):**

```json
{
  "content": [ /* users */ ],
  "totalPages": 5,
  "totalElements": 50,
  "currentPage": 0,
  "size": 10
}
```

### Reports

#### 1. Generate Monthly Report

**Endpoint:** `GET /api/v1/parking/reports/monthly`

**Description:** Generates a monthly report for resident vehicles and saves it to a file.

**Response (200 OK):**

```json
{
  "message": "report created successfully",
  "report_file": "/Users/.../reports/informe_estacionamiento.txt"
}
```

**Report Contents:**

The generated text file includes:

- Total resident vehicles
- List of vehicles with their parking sessions
- Entry and exit timestamps
- Total time spent

**cURL Example:**

```bash
curl -X GET http://localhost:8080/api/v1/parking/reports/monthly \
  -H "Authorization: Bearer <token>"
```

### Payments (Stripe)

The application integrates with Stripe to handle payment processing, including customer creation, card management, and invoice generation.

⚠️ **Important:** Make sure you have configured your `STRIPE_SECRET_KEY` environment variable before using these endpoints.

#### 1. Create Customer

**Endpoint:** `POST /api/customers/customer`

**Description:** Creates a new customer in Stripe with their billing information.

**Request Body:**

```json
{
  "id": "user123",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "address": {
    "country": "US",
    "city": "New York",
    "line1": "123 Main Street"
  }
}
```

**Address Fields:**

- `country`: Two-letter country code (e.g., "US", "MX", "CA")
- `city`: City name
- `line1`: Street address or PO box

**Response (200 OK):**

```json
{
  "message": "Customer created correctly",
  "customer": {
    "id": "cus_ABC123XYZ",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "address": {
      "city": "New York",
      "country": "US",
      "line1": "123 Main Street",
      "line2": null,
      "postal_code": null,
      "state": null
    },
    "balance": 0,
    "created": 1697500800,
    "currency": "usd",
    "default_source": null,
    "delinquent": false,
    "description": null,
    "discount": null,
    "invoice_prefix": "ABC1234",
    "livemode": false
  }
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/api/customers/customer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "id": "user123",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "address": {
      "country": "US",
      "city": "New York",
      "line1": "123 Main Street"
    }
  }'
```

#### 2. Add Card to Customer

**Endpoint:** `POST /api/cards/card`

**Description:** Attaches a payment method (card) to an existing customer.

**Request Body:**

```json
{
  "customer": {
    "id": "user123",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "address": {
      "country": "US",
      "city": "New York",
      "line1": "123 Main Street"
    }
  },
  "cvc": "123",
  "expMonth": 12,
  "expYear": 2025,
  "number": "4242424242424242",
  "currency": "USD"
}
```

**Card Fields:**

- `number`: Card number (use test cards in development)
- `expMonth`: Expiration month (1-12)
- `expYear`: Expiration year (e.g., 2025)
- `cvc`: Card verification code (3-4 digits)
- `currency`: Currency code (USD, EUR, MXN, etc.)

**Supported Currencies:**

- `USD` - US Dollar
- `EUR` - Euro
- `MXN` - Mexican Peso
- `GBP` - British Pound
- `CAD` - Canadian Dollar

**Response (200 OK):**

```json
{
  "id": "pm_1ABC123XYZ",
  "object": "payment_method",
  "billing_details": {
    "address": {
      "city": "New York",
      "country": "US",
      "line1": "123 Main Street",
      "line2": null,
      "postal_code": null,
      "state": null
    },
    "email": "john.doe@example.com",
    "name": "John Doe",
    "phone": null
  },
  "card": {
    "brand": "visa",
    "checks": {
      "address_line1_check": null,
      "address_postal_code_check": null,
      "cvc_check": "pass"
    },
    "country": "US",
    "exp_month": 12,
    "exp_year": 2025,
    "fingerprint": "ABC123XYZ",
    "funding": "credit",
    "last4": "4242",
    "networks": {
      "available": ["visa"],
      "preferred": null
    }
  },
  "created": 1697500800,
  "customer": "cus_ABC123XYZ",
  "livemode": false,
  "type": "card"
}
```

**Test Cards:**

```text
Visa (Success): 4242 4242 4242 4242
Visa (Declined): 4000 0000 0000 0002
Mastercard: 5555 5555 5555 4444
American Express: 3782 822463 10005
```

**cURL Example:**

```bash
curl -X POST http://localhost:8080/api/cards/card \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "customer": {
      "id": "user123",
      "name": "John Doe",
      "email": "john.doe@example.com",
      "address": {
        "country": "US",
        "city": "New York",
        "line1": "123 Main Street"
      }
    },
    "cvc": "123",
    "expMonth": 12,
    "expYear": 2025,
    "number": "4242424242424242",
    "currency": "USD"
  }'
```

#### 3. Create Invoice

**Endpoint:** `POST /api/invoices/invoice`

**Description:** Creates an invoice for a customer based on parking hours and price.

**Request Body:**

```json
{
  "customer": "cus_ABC123XYZ",
  "currency": "USD",
  "product": {
    "hours": 5,
    "price": 2500
  }
}
```

**Product Fields:**

- `hours`: Number of parking hours
- `price`: Total price in **cents** (e.g., 2500 = $25.00)

**Currency Options:**

- `USD`, `EUR`, `MXN`, `GBP`, `CAD`

**Response (200 OK):**

```json
{
  "message": "Invoice created correctly",
  "invoice": {
    "id": "in_1ABC123XYZ",
    "object": "invoice",
    "amount_due": 2500,
    "amount_paid": 0,
    "amount_remaining": 2500,
    "attempt_count": 0,
    "attempted": false,
    "auto_advance": false,
    "billing_reason": "manual",
    "collection_method": "charge_automatically",
    "created": 1697500800,
    "currency": "usd",
    "customer": "cus_ABC123XYZ",
    "customer_email": "john.doe@example.com",
    "customer_name": "John Doe",
    "description": "Parking fee for 5 hours",
    "hosted_invoice_url": "https://invoice.stripe.com/i/acct_xxx/test_xxx",
    "invoice_pdf": "https://pay.stripe.com/invoice/acct_xxx/test_xxx/pdf",
    "lines": {
      "data": [
        {
          "id": "il_1ABC123XYZ",
          "amount": 2500,
          "currency": "usd",
          "description": "Parking - 5 hours",
          "quantity": 1
        }
      ]
    },
    "livemode": false,
    "paid": false,
    "status": "draft",
    "subtotal": 2500,
    "total": 2500
  }
}
```

**Invoice URLs:**

- `hosted_invoice_url`: Customer-facing payment page
- `invoice_pdf`: Downloadable PDF invoice

**cURL Example:**

```bash
curl -X POST http://localhost:8080/api/invoices/invoice \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "customer": "cus_ABC123XYZ",
    "currency": "USD",
    "product": {
      "hours": 5,
      "price": 2500
    }
  }'
```

**Complete Payment Flow Example:**

```bash
# 1. Create a customer
CUSTOMER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/customers/customer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "id": "user123",
    "name": "John Doe",
    "email": "john@example.com",
    "address": {
      "country": "US",
      "city": "New York",
      "line1": "123 Main St"
    }
  }')

CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | jq -r '.customer.id')
echo "Customer ID: $CUSTOMER_ID"

# 2. Add a payment method (card)
curl -X POST http://localhost:8080/api/cards/card \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "{
    \"customer\": {
      \"id\": \"user123\",
      \"name\": \"John Doe\",
      \"email\": \"john@example.com\",
      \"address\": {
        \"country\": \"US\",
        \"city\": \"New York\",
        \"line1\": \"123 Main St\"
      }
    },
    \"cvc\": \"123\",
    \"expMonth\": 12,
    \"expYear\": 2025,
    \"number\": \"4242424242424242\",
    \"currency\": \"USD\"
  }"

# 3. Create an invoice for 5 hours of parking ($25.00)
curl -X POST http://localhost:8080/api/invoices/invoice \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "{
    \"customer\": \"$CUSTOMER_ID\",
    \"currency\": \"USD\",
    \"product\": {
      \"hours\": 5,
      \"price\": 2500
    }
  }"
```

**Notes:**

- All prices must be in **cents** (smallest currency unit)
- Example: $25.00 = 2500 cents, €10.50 = 1050 cents
- Invoices are created in `draft` status and must be finalized/paid separately via Stripe
- Use test mode keys (`sk_test_`) during development
- See [Stripe API Documentation](https://stripe.com/docs/api) for advanced features

## Authentication & Security

### JWT Token Flow

1. **User signs up or logs in** → Receives a JWT token in the response
2. **Token structure**: The JWT contains user information (username, email, role) and is signed with the secret key
3. **Token expiration**: Tokens are valid for 1 hour by default (configurable via `jwt.expiration`)
4. **Using the token**: Include in the `Authorization` header for all protected endpoints:

   ```http
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

5. **Logout**: Tokens are added to a Redis blacklist with their remaining TTL
6. **Token validation**: `JWTAuthFilter` checks every request:
   - Extracts token from header
   - Validates signature and expiration
   - Checks if token is blacklisted
   - Loads user details and sets authentication context

### Security Features

- **Password Hashing**: Passwords are encrypted using BCrypt before storage
- **Role-Based Access Control**: Users have roles (USER, ADMIN) for authorization
- **Token Blacklisting**: Logout invalidates tokens immediately via Redis
- **CORS Configuration**: Configurable for production deployments
- **Input Validation**: Request bodies are validated using Jakarta Validation

### Security Best Practices

⚠️ **For Production:**

1. **Never commit** `.env` files or secrets to version control
2. **Use strong secret keys**: Generate with `openssl rand -base64 32`
3. **Use HTTPS**: Always encrypt traffic in production
4. **Use external secret management**: AWS Secrets Manager, Azure Key Vault, etc.
5. **Set appropriate CORS policies**: Don't use `*` in production
6. **Monitor Redis**: Set up alerts for connection failures
7. **Database**: Replace H2 with PostgreSQL/MySQL for production

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=CoreApplicationTests
```

### Generate Test Coverage Report

```bash
mvn clean test jacoco:report
```

### Manual API Testing

Use the provided cURL examples or tools like:

- **Postman**: Import endpoints and create collections
- **Insomnia**: REST client alternative
- **HTTPie**: Command-line tool (`brew install httpie`)

Example test workflow:

```bash
# 1. Sign up a new user
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/parking/auth/signUp \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "username": "testuser",
    "email": "test@example.com",
    "password": "test123"
  }' | jq -r '.token')

echo "Token: $TOKEN"

# 2. Create a vehicle
curl -X POST http://localhost:8080/api/v1/parking/vehicles/ \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "id": "TEST-001",
    "type": "CAR"
  }'

# 3. Register entry
curl -X POST http://localhost:8080/api/v1/parking/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "id": "TEST-001",
    "type": "CAR"
  }'

# 4. List all registers
curl -X GET http://localhost:8080/api/v1/parking/registers \
  -H "Authorization: Bearer $TOKEN"

# 5. Register exit
curl -X POST http://localhost:8080/api/v1/parking/leave \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "id": "TEST-001"
  }'

# 6. Logout
curl -X POST http://localhost:8080/api/v1/parking/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

## Troubleshooting

### Common Issues

#### 1. Redis Connection Error

**Problem:** `Unable to connect to Redis at localhost:6379`

**Solution:**

```bash
# Check if Redis is running
redis-cli ping

# If not running, start Redis
docker start redis-jwt
# or
redis-server
```

#### 2. Port 8080 Already in Use

**Problem:** `Web server failed to start. Port 8080 was already in use.`

**Solution:**

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change the port in application.properties
echo "server.port=8081" >> src/main/resources/application.properties
```

#### 3. H2 Database Lock

**Problem:** `Database may be already in use: "Locked by another process"`

**Solution:**

```bash
# Stop all running instances of the application
# Delete the lock file
rm data/parkingdb.lock.db
```

#### 4. JWT Token Expired

**Problem:** `401 Unauthorized` - Token has expired

**Solution:**

- Login again to get a new token
- Increase `jwt.expiration` in `application.properties` for development

#### 5. Invalid Token Format

**Problem:** `Invalid or expired token`

**Solution:**

- Ensure the token is prefixed with `Bearer` followed by a space
- Check that the token wasn't modified or truncated
- Verify you're using the latest token (logout invalidates tokens)

#### 6. Maven Build Fails

**Problem:** `Failed to execute goal...`

**Solution:**

```bash
# Clean Maven cache
mvn clean

# Update dependencies
mvn dependency:resolve

# Skip tests if they're failing
mvn clean install -DskipTests
```

### Enable Debug Logging

Add to `application.properties`:

```properties
logging.level.com.parking.core=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Health Check Endpoint

Check if the application is running:

```bash
# Check if server responds
curl http://localhost:8080/h2-console

# Check Redis connection
redis-cli ping
```

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is provided as-is for educational purposes.

## Contact & Support

- **Repository**: [github.com/DSBAENAR/ParkingProject](https://github.com/DSBAENAR/ParkingProject)
- **Issues**: Report bugs or request features via GitHub Issues

---
