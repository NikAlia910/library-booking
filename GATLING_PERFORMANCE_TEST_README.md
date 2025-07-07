# 🚀 Comprehensive API Performance Testing with Gatling

## 📋 Overview

This repository contains a robust Gatling simulation (`ComprehensiveApiGatlingTest.java`) designed to perform comprehensive performance testing of the Library Booking System API. The simulation covers full CRUD operations with JWT authentication, realistic data generation, and advanced load patterns.

## ✨ Features

### 🔐 Authentication

- **JWT Token-based Authentication**: Automatically authenticates using `POST /api/authenticate`
- **Token Extraction**: Extracts `id_token` from response and reuses across all requests
- **Bearer Token Headers**: Includes `Authorization: Bearer <token>` in all authenticated requests
- **Debug Logging**: Prints authentication status and token snippets for debugging

### 📦 Comprehensive API Coverage

#### Resource Entity (`/api/resources`)

- ✅ **POST** - Create resource with realistic data generation
- ✅ **GET** by ID - Retrieve specific resource
- ✅ **PUT** - Update resource with modified fields
- ✅ **DELETE** - Remove resource
- ✅ **GET** all - Paginated resource listing
- ✅ **Search** operations:
  - Search by title (`/search/title`)
  - Search by author (`/search/author`)
  - Search by keywords (`/search/keywords`)
  - Search by type (`/search/type`)
  - Advanced search with multiple parameters

#### Reservation Entity (`/api/reservations`)

- ✅ **POST** - Create reservation with availability checking
- ✅ **GET** by ID - Retrieve specific reservation
- ✅ **PUT** - Update reservation (extend time slots)
- ✅ **DELETE** - Remove reservation
- ✅ **GET** all - Paginated reservation listing
- ✅ **Special endpoints**:
  - Check availability (`/availability/{resourceId}`)
  - Get reservations by resource (`/resource/{resourceId}`)
  - Get active reservations by user (`/user/{userId}/active`)

### 🧠 Intelligent Data Generation

#### Dynamic Timestamps

```java
ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
```

#### Unique Resource Data

- **Title**: `PERF_RESOURCE_<UUID>` with timestamp uniqueness
- **Author**: `Author_<UUID>` for realistic diversity
- **Keywords**: `"performance,test,gatling,<UUID>"` for searchability
- **Resource Type**: Rotates between `BOOK`, `MEETING_ROOM`, `EQUIPMENT`

#### Smart Reservation Logic

- **Future Dates**: Reservations created for tomorrow at 9 AM
- **Realistic Duration**: 2-hour initial slots, extended to 3 hours in updates
- **Unique IDs**: `PERF_TEST_<timestamp>_<UUID>` format
- **Availability Check**: Always verifies resource availability before booking

### 📊 Performance Assertions

#### Global Performance Requirements

```java
assertions(
    global().successfulRequests().percent().gte(95),    // 95% success rate
    global().responseTime().mean().lte(1000),           // 1s mean response time
    global().responseTime().percentile3().lte(2000),    // 2s 99th percentile
    forAll().failedRequests().count().lte(10)           // Max 10 failures
)
```

#### Per-Request Assertions

- **Status Codes**: `200`, `201`, `204` for success scenarios
- **Response Times**:
  - Simple GETs: ≤ 1000ms
  - Complex operations (POST/PUT): ≤ 1500ms
  - Authentication: ≤ 2000ms
- **Data Validation**: JSON path assertions for returned IDs and values

### 🎯 Load Patterns

#### Scenario 1: Full CRUD Operations (10 users)

```java
fullCrudScenario.injectOpen(
    rampUsers(10).during(Duration.ofSeconds(10))
)
```

- Complete lifecycle testing per user
- Resource creation → CRUD operations → Reservation lifecycle → Cleanup
- Realistic pauses between operations (1-3 seconds)

#### Scenario 2: Read-Only Operations (5 users)

```java
readOnlyScenario.injectOpen(
    rampUsers(5).during(Duration.ofSeconds(15))
)
```

- Continuous browsing simulation
- Repeated read operations with variable pauses
- No data modification (safe for production)

## 🚀 How to Run

### Prerequisites

- Java 17+
- Maven 3.6+
- Running application on `http://localhost:8080` (or configure `baseUrl`)

### Basic Execution

```bash
# Run with default settings
mvn gatling:test -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiGatlingTest

# Run with custom base URL
mvn gatling:test -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiGatlingTest -DbaseUrl=http://localhost:9090

# Run with custom user count (system properties)
mvn gatling:test -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiGatlingTest -Dusers=20 -Dramp=30
```

### Environment Configuration

```bash
# Production testing
mvn gatling:test -DbaseUrl=https://api.production.com

# Staging environment
mvn gatling:test -DbaseUrl=https://staging.api.com
```

### CI/CD Integration

```yaml
# GitHub Actions / CI Pipeline
- name: Run Performance Tests
  run: |
    mvn gatling:test -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiGatlingTest
    # Test reports available in target/gatling/
```

## 📈 Test Reports

Gatling generates comprehensive HTML reports in:

```
target/gatling/comprehensiveapigatllingtest-<timestamp>/
├── index.html          # Main report dashboard
├── js/                 # Interactive charts
├── css/               # Styling
└── images/            # Graphs and metrics
```

### Key Metrics to Monitor

- **Response Time Distribution**: P50, P95, P99 percentiles
- **Request Rate**: Requests per second over time
- **Success/Failure Ratio**: Overall and per-endpoint
- **Error Analysis**: Detailed breakdown of any failures

## 🔧 Customization

### Modifying User Credentials

```java
private static final String USERNAME = "your-username";

private static final String PASSWORD = "your-password";

```

### Adjusting Load Patterns

```java
// Increase load
rampUsers(50).during(Duration.ofSeconds(30))

// Constant load
constantUsersPerSec(10).during(Duration.ofMinutes(5))

// Step load
incrementUsersPerSec(5)
    .times(5)
    .eachLevelLasting(Duration.ofMinutes(2))
    .separatedByRampsLasting(Duration.ofSeconds(30))
```

### Adding Custom Endpoints

```java
ChainBuilder customEndpoint() {
  return exec(
    http("Custom Operation")
      .get("/api/custom-endpoint")
      .headers(authHeaders)
      .check(status().is(200))
      .check(responseTimeInMillis().lte(1000))
  );
}

```

## 🛡️ Stability Features

### Built-in Error Handling

- **Fail-Fast**: `.exitHereIfFailed()` after critical operations
- **Authentication Guard**: Session validation before API calls
- **Data Cleanup**: Automatic resource deletion to prevent data pollution

### Debug Support

```java
System.out.println("Token: " + session.getString("jwt_token"));
System.out.println("Created Resource ID: " + session.getString("resource_id"));
```

### Session Management

- **Token Reuse**: JWT tokens cached per user session
- **Variable Storage**: Resource/Reservation IDs tracked across requests
- **State Management**: User-specific data isolation

## 🔍 Troubleshooting

### Common Issues

#### Authentication Failures

```bash
# Check if admin user exists and password is correct
curl -X POST http://localhost:8080/api/authenticate \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

#### Connection Refused

```bash
# Verify application is running
curl http://localhost:8080/api/account
```

#### High Response Times

- Check database performance
- Monitor JVM memory usage
- Verify network latency

### Debug Mode

Add debug logging:

```java
.exec(session -> {
    System.out.println("Session state: " + session.attributes());
    return session;
})
```

## 🎯 Best Practices

1. **Environment Isolation**: Use dedicated test data
2. **Gradual Ramp-up**: Start with low load, increase progressively
3. **Resource Cleanup**: Always delete test data
4. **Monitoring**: Watch system resources during tests
5. **Baseline Establishment**: Run tests consistently to track performance trends

## 📋 Test Coverage Summary

| Operation             | Endpoint                              | Method | Coverage | Data Validation            |
| --------------------- | ------------------------------------- | ------ | -------- | -------------------------- |
| Authentication        | `/api/authenticate`                   | POST   | ✅       | JWT token extraction       |
| Create Resource       | `/api/resources`                      | POST   | ✅       | ID generation & validation |
| Read Resource         | `/api/resources/{id}`                 | GET    | ✅       | Data integrity check       |
| Update Resource       | `/api/resources/{id}`                 | PUT    | ✅       | Field modification         |
| Delete Resource       | `/api/resources/{id}`                 | DELETE | ✅       | Cleanup verification       |
| Search Resources      | `/api/resources/search/*`             | GET    | ✅       | Query parameter handling   |
| List Resources        | `/api/resources`                      | GET    | ✅       | Pagination support         |
| Check Availability    | `/api/reservations/availability/{id}` | GET    | ✅       | Boolean response           |
| Create Reservation    | `/api/reservations`                   | POST   | ✅       | Relationship validation    |
| Read Reservation      | `/api/reservations/{id}`              | GET    | ✅       | Complete data retrieval    |
| Update Reservation    | `/api/reservations/{id}`              | PUT    | ✅       | Time extension logic       |
| Delete Reservation    | `/api/reservations/{id}`              | DELETE | ✅       | Cascade cleanup            |
| Resource Reservations | `/api/reservations/resource/{id}`     | GET    | ✅       | Filtered listing           |

**Total Coverage**: 13 unique endpoints, 15+ operations, 100% CRUD completion

---

🎯 **Ready for Production**: This simulation is designed for both development testing and production monitoring, ensuring your Library Booking System performs optimally under real-world load conditions.
