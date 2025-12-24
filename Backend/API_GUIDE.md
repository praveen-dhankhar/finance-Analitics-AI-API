# 🔌 FinFlow AI - API Integration Guide

This guide explains how to integrate the **FinFlow AI Backend** into other projects (Frontend, Mobile, or other Microservices).

---

## 🧭 API Base URL
- **Local Development**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## 🔐 Authentication Flow

FinFlow AI uses **JWT (JSON Web Tokens)** for stateless authentication.

### 1. Registration
Register a new user to get started.

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "Password123!"
}
```

### 2. Login
Obtain an `accessToken` and a `refreshToken`.

```http
POST /api/auth/login
Content-Type: application/json

{
  "emailOrUsername": "johndoe",
  "password": "Password123!"
}
```

**Response**:
```json
{
  "token": {
    "accessToken": "eyJhbG...",
    "refreshToken": "def456...",
    "tokenType": "Bearer",
    "expiresIn": 3600000
  },
  "user": { ... }
}
```

### 3. Authenticated Requests
Include the `accessToken` in the `Authorization` header of every request.

```http
GET /api/forecasts/insights
Authorization: Bearer <accessToken>
```

### 4. Refreshing Tokens
When the `accessToken` expires (401 Unauthorized), use the `refreshToken` to get a new one without asking the user for credentials.

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "<your_refresh_token>"
}
```

---

## 📊 Core Endpoints

### 🧠 AI Insights
Generate contextual financial analysis using Google Gemini.
- **Endpoint**: `GET /api/forecasts/insights`
- **Security**: Requires JWT
- **AI Model**: `gemini-2.5-flash`

### 📈 Forecasting
Generate statistical projections.
- **Endpoint**: `POST /api/forecasts/generate`
- **Payload**:
  ```json
  {
    "userId": 1,
    "category": "Dining",
    "algorithm": "LINEAR_REGRESSION",
    "period": 3
  }
  ```

---

## 🛠️ Integration Tips

### CORS (Cross-Origin Resource Sharing)
If you are calling this API from a browser, ensure your origin is allowed.
- **Dev**: Configured to allow all `localhost` ports.
- **Prod**: Update `SecurityConfig.java` -> `getAllowedOrigins()` with your production domain.

### Client Generation
You can automatically generate a Client SDK for any language (Typescript, Python, Go) using the OpenAPI JSON.
1. Download the JSON from `/v3/api-docs`.
2. Use [OpenAPI Generator](https://openapi-generator.tech/):
   ```bash
   npx @openapitools/openapi-generator-cli generate -i openapi.json -g typescript-axios -o ./client
   ```

---

## 📡 Webhooks (Coming Soon)
Future updates will include webhook support for real-time anomaly detection notifications.
