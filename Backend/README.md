# 💰 FinFlow AI - Advanced Financial Analytics & Forecasting

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?style=flat&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Google Gemini](https://img.shields.io/badge/AI-Google%20Gemini-4285F4?style=flat&logo=google-gemini&logoColor=white)](https://deepmind.google/technologies/gemini/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=flat&logo=apache-maven&logoColor=white)](https://maven.apache.org/)

**FinFlow AI** is a high-performance, intelligence-driven financial backend designed to transform raw financial data into actionable insights. It combines traditional statistical forecasting models with cutting-edge Large Language Models (LLMs) to provide users with deep understanding and future projections of their financial health.

---

## 🚀 Key Features

### 🧠 AI-Powered Insights
- **Gemini AI Integration**: Uses Google's latest `gemini-2.5-flash` model to analyze financial summaries.
- **Narrative Analysis**: Generates human-readable insights about spending patterns, savings momentum, and potential risks.
- **Contextual Intelligence**: Analyzes spikes in expenses (e.g., "15% increase in Dining Out") and provides specific tactical advice.

### 📈 Predictive Forecasting
- **Multiple Algorithms**: Supports a variety of forecasting models:
  - **Linear Regression**: For steady growth/decline trends.
  - **SMA (Simple Moving Average)**: For smoothed trend analysis.
  - **EWMA (Exponential Weighted Moving Average)**: Prioritizes recent data points.
  - **Seasonal Decomposition**: Handles repeating monthly/yearly cycles.
- **Accuracy Metrics**: Automatic calculation of RMSE, MAE, and MAPE to evaluate model performance.
- **Batch Processing**: Generate forecasts for multiple categories or accounts in a single request.

### 🛡️ Enterprise-Grade Security
- **JWT Authentication**: Secure stateless authentication with short-lived access tokens and refresh tokens.
- **Role-Based Access Control (RBAC)**: Fine-grained permissions for User and Admin roles.
- **Data Integrity**: Audit logging and standardized error handling for all financial transactions.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Framework** | Spring Boot 3.3.4 (Java 21) |
| **AI SDK** | Google Gen AI Java SDK (`com.google.genai`) |
| **Database** | PostgreSQL (Production) / H2 (Development) |
| **Migrations** | Flyway DB |
| **Security** | Spring Security, JJWT |
| **Documentation** | SpringDoc OpenAPI (Swagger UI) |
| **Monitoring** | Spring Boot Actuator, Prometheus |

---

## 🚦 Getting Started

### Prerequisites
- **JDK 21** or higher
- **Maven 3.x**
- **Google AI Studio API Key** (for Gemini features)

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/praveen-dhankhar/finance-Analitics-AI-API.git
   cd FinFlow-Ai
   ```

2. **Configure Environment**:
   Create a `src/main/resources/application.properties` file (this is ignored by Git for security):
   ```properties
   gemini.api.key=YOUR_GOOGLE_AI_STUDIO_KEY
   gemini.model=gemini-2.5-flash
   
   # Database Configuration (Defaults to H2 in-memory)
   spring.datasource.url=jdbc:h2:mem:finflowdb
   ```

3. **Build and Run**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access Swagger UI**:
   Open [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) to explore the API.

---

## 🗺️ API Overview

### 🔐 Authentication
- `POST /api/auth/register` - Create a new account.
- `POST /api/auth/login` - Obtain JWT tokens.
- `POST /api/auth/refresh` - Refresh an expired access token.

### 📊 Forecasts & AI
- `GET /api/forecasts/insights` - Get AI-generated analysis of user financial data.
- `POST /api/forecasts/generate` - Run a specific forecasting algorithm.
- `GET /api/forecasts/accuracy` - Get performance metrics for a forecast model.

### 💼 Financial Data
- `GET /api/financial-data` - Manage income and expense records.
- `GET /api/categories` - Organize transactions by category.

---

## 🧪 Development Workflow

### Testing
We use JUnit 5 and Spring Security Test for comprehensive coverage:
```bash
mvn test
```

### Migrations
Database schema changes are managed via Flyway in `src/main/resources/db/migration`. Any new `.sql` file added there will be automatically applied on startup.

---

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.

---
*Developed with ❤️ by Praveen Dhankhar*
