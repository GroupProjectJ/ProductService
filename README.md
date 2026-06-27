# ProductService

A RESTful microservice for managing products, built with Spring Boot 4.1.0 and Java 21. Part of an Enterprise E-Commerce application.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.1.0 |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| ORM | Hibernate / Spring Data JPA |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, MockMvc, H2 |
| Build | Maven (via Maven Wrapper) |
| Container | Docker + Docker Compose |

---

## Project Structure

```
src/
├── main/java/enterpriseapplication/productservice/
│   ├── api/                  # REST Controllers
│   ├── service/              # Business logic (interface + impl)
│   ├── repository/           # Spring Data JPA repositories
│   ├── model/                # JPA entities
│   ├── dto/                  # Data Transfer Objects
│   └── exception/            # Custom exceptions + global handler
└── test/java/enterpriseapplication/productservice/
    ├── api/                  # Controller tests (MockMvc)
    ├── service/              # Service unit tests (Mockito)
    ├── repository/           # Repository tests (H2 + @DataJpaTest)
    └── exception/            # Exception handler tests
```

---

## API Endpoints

Base URL: `http://localhost:8080/api/products`

| Method | Endpoint | Description | Status |
|---|---|---|---|
| POST | `/api/products` | Create a new product | 201 Created |
| GET | `/api/products` | Get all products | 200 OK |
| GET | `/api/products/{id}` | Get product by ID | 200 OK |
| DELETE | `/api/products/{id}` | Delete product by ID | 204 No Content |

### Request Body (POST)

```json
{
  "name": "Headphone",
  "unitPrice": 600.00,
  "description": "Electronic Items",
  "category": "Electronics",
  "stock": 10
}
```

### Validation Rules

| Field | Rule |
|---|---|
| `name` | Required (not blank) |
| `unitPrice` | Required, must be positive |
| `description` | Optional |
| `category` | Optional |
| `stock` | Optional |

---

## Running the Project

### Option 1 — Docker (Recommended)

Requires [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

```bash
docker compose up --build
```

Starts both PostgreSQL and the application automatically. No manual database setup needed.

- App: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

To stop:
```bash
docker compose down
```

### Option 2 — Local (Maven Wrapper)

Requires PostgreSQL running locally with a database named `Ecommerce`.

```bash
# Windows
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

---

## Running Tests

Tests use H2 in-memory database — no PostgreSQL or Docker needed.

```bash
# Windows
.\mvnw.cmd test

# macOS / Linux
./mvnw test
```

### Test Coverage

| Test Class | Coverage |
|---|---|
| `ProductServiceImplTest` | All 4 service methods (8 tests) |
| `ProductControllerTest` | All 4 endpoints + validation (10 tests) |
| `ProductRepositoryTest` | CRUD operations via H2 (9 tests) |
| `GlobalExceptionHandlerTest` | 404 + 400 response structure (6 tests) |
| **Total** | **33 tests** |

---

## Docker Hub

### Build and Push

```bash
docker build -t <your-dockerhub-username>/product-service:latest .
docker login
docker push <your-dockerhub-username>/product-service:latest
```

### Pull and Run from Docker Hub

```bash
docker pull <your-dockerhub-username>/product-service:latest

docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/Ecommerce \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=2001 \
  <your-dockerhub-username>/product-service:latest
```

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/Ecommerce` | JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `2001` | Database password |

---

## Swagger UI

Interactive API documentation:
```
http://localhost:8080/swagger-ui/index.html
```

API schema (JSON):
```
http://localhost:8080/api-docs
```
