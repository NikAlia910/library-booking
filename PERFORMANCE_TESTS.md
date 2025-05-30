# Performance Tests

This project includes Gatling performance tests to measure and monitor the application's performance under load.

## Available Scripts

### Local Development

```bash
npm run backend:performance:test
```

This command:

1. Runs all Gatling performance tests
2. Automatically opens the HTML report in your default browser after completion

### CI/CD Pipeline

```bash
npm run ci:performance:test
```

This command runs the performance tests without opening the browser (suitable for automated pipelines).

## Test Configuration

The performance tests are located in `src/test/java/gatling/simulations/` and include:

- **ReservationGatlingTest**: Tests the reservation API endpoints under load
- **ResourceGatlingTest**: Tests the resource management API endpoints under load

### Default Test Parameters

- **Users**: 100 concurrent users (configurable via `-Dusers=N`)
- **Ramp-up Time**: 1 minute (configurable via `-Dramp=N`)
- **Base URL**: http://localhost:8080 (configurable via `-DbaseURL=URL`)

### Customizing Test Parameters

```bash
# Run with 50 users over 2 minutes
./mvnw gatling:test -Dusers=50 -Dramp=2

# Run against different environment
./mvnw gatling:test -DbaseURL=https://staging.example.com
```

## Reports

Performance test reports are generated in `target/gatling/` directory. Each test run creates a timestamped folder containing:

- `index.html` - Main report with charts and statistics
- `simulation.log` - Raw test data
- `js/` and `style/` - Report assets

## GitHub Actions Integration

Performance tests are automatically run in the CI/CD pipeline after backend tests complete. Reports are uploaded as GitHub Actions artifacts and can be downloaded for analysis.

## Prerequisites

- Java 17+
- Maven 3.6+
- Running application instance (for local testing)

## Notes

- Ensure the application is running on the target URL before executing performance tests
- For production load testing, consider running tests from a separate environment to avoid impacting live users
- Review and adjust test scenarios in the simulation files to match your application's usage patterns
