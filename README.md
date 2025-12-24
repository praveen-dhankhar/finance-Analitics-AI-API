# Finance Forecast Backend

A robust, production-ready Spring Boot backend for financial forecasting and management.

## Tech Stack
- **Java**: 21
- **Framework**: Spring Boot 3.3.4
- **Database**: PostgreSQL 15
- **Security**: Spring Security + JWT
- **Build**: Maven

## Getting Started

### Prerequisites
- JDK 21+ installed
- Docker & Docker Compose
- Maven

### Running with Docker (Recommended)

To start the full stack (PostgreSQL + Redis + Backend):
```bash
docker-compose -f docker-compose.prod.yml up --build
```

### Running Locally

1. **Start Infrastructure**:
   ```bash
   docker-compose -f docker-compose.prod.yml up -d postgres redis
   ```

2. **Run Application**:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Documentation
Once running, Swagger UI is available at:
`http://localhost:8080/swagger-ui.html`

## Testing
Run unit and integration tests:
```bash
./mvnw verify
```
