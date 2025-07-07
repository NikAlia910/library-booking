# Comprehensive API Gatling Performance Test

## Overview

The `ComprehensiveApiGatlingTest.java` simulation provides complete coverage of all API endpoints in the Library Booking System. This test is designed to validate performance, functionality, and reliability of the entire API surface.

## Features

### 🔐 Authentication

- **JWT Token Authentication**: Authenticates with admin credentials and extracts JWT token
- **Token Usage**: Uses the JWT token in Authorization header for all subsequent requests
- **Debug Output**: Prints token information for verification

### 📚 Resource Management (CRUD)

- **CREATE**: Creates new resources with unique identifiers and timestamps
- **READ**: Retrieves resources by ID and lists all resources with pagination
- **UPDATE**: Updates existing resources with new data
- **DELETE**: Removes resources (in cleanup phase)
- **Search Operations**:
  - Search by keywords
  - Search by resource type (BOOK, MEETING_ROOM, EQUIPMENT)
  - Advanced multi-criteria search

### 📅 Reservation Management (CRUD)

- **CREATE**: Creates reservations with proper date/time formatting
- **READ**: Retrieves reservations by ID and lists all reservations
- **READ by Resource**: Gets reservations for specific resources
- **READ by User**: Gets active reservations for users
- **Availability Check**: Validates resource availability for time slots
- **DELETE**: Removes reservations (in cleanup phase)

### 👥 User & Authority Management

- **Account Information**: Retrieves current user account details
- **Public Users**: Lists public user information
- **Admin Users**: Lists users via admin endpoints
- **Authorities**: Lists system authorities/roles

### ⚡ Performance & Validation

- **Response Time Assertions**: All requests must complete within 1000ms
- **Status Code Validation**: Verifies appropriate HTTP status codes (200, 201, 204)
- **JSON Path Validation**: Validates response structure and content
- **Unique Data Generation**: Uses UUIDs and timestamps to ensure data uniqueness
- **Global Performance Assertions**: Overall performance criteria for the entire test

## Usage

### Running the Simulation

#### With Maven

```bash
# Run with default settings (10 users, 10 second ramp-up, localhost:8080)
mvn gatling:test -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiGatlingTest

# Run with custom parameters
mvn gatling:test \
  -Dgatling.simulationClass=gatling.simulations.ComprehensiveApiGatlingTest \
  -DbaseURL=http://your-server:8080 \
  -Dusers=50 \
  -Dramp=30
```

#### With Gradle

```bash
# Run with default settings
./gradlew gatlingRun

# Run with custom parameters
./gradlew gatlingRun \
  -DbaseURL=http://your-server:8080 \
  -Dusers=50 \
  -Dramp=30
```

### Configuration Parameters

| Parameter | Default                 | Description                 |
| --------- | ----------------------- | --------------------------- |
| `baseURL` | `http://localhost:8080` | Base URL of the application |
| `users`   | `10`                    | Number of virtual users     |
| `ramp`    | `10`                    | Ramp-up time in seconds     |

### Environment Variables

You can also set these as environment variables:

```bash
export baseURL=http://your-server:8080
export users=25
export ramp=20
```

## Test Scenario Flow

1. **Authentication** (2 seconds)

   - POST `/api/authenticate` with admin credentials
   - Extract JWT token
   - Verify authentication

2. **Resource CRUD Operations** (2 seconds)

   - Create a new resource with unique data
   - Read the created resource
   - Update the resource
   - List all resources with pagination

3. **Reservation CRUD Operations** (2 seconds)

   - Create a reservation using the created resource
   - Read the created reservation
   - List all reservations
   - Get reservations by resource
   - Check resource availability

4. **Resource Search Operations** (2 seconds)

   - Search by keywords
   - Search by resource type
   - Advanced multi-criteria search

5. **User & Authority Operations** (2 seconds)

   - Get current account information
   - List public users
   - List authorities
   - List admin users

6. **Cleanup** (final phase)
   - Delete created reservation
   - Delete created resource

## Performance Assertions

The simulation includes comprehensive performance assertions:

- **Maximum Response Time**: 2000ms for any single request
- **Mean Response Time**: 1000ms average across all requests
- **Success Rate**: 95% or higher for all requests
- **Individual Request Timeout**: 1000ms for most operations

## Debug Output

The simulation provides extensive debug output including:

- Test phase announcements
- JWT token information (truncated for security)
- Created resource and reservation IDs
- Cleanup confirmations
- Test completion status

## Data Generation

The simulation uses smart data generation:

- **Unique IDs**: UUID-based unique identifiers for all created entities
- **Timestamps**: ISO-8601 formatted timestamps using UTC timezone
- **Realistic Data**: Meaningful test data with proper relationships
- **Time Calculations**: Proper date/time arithmetic for reservations

## Error Handling

- **Exit on Auth Failure**: Test stops if authentication fails
- **Status Code Validation**: Validates expected HTTP status codes
- **Response Time Limits**: Fails requests that exceed time limits
- **JSON Validation**: Validates response structure and required fields

## Extending the Simulation

To add new endpoints or modify the test:

1. **Add new ChainBuilder**: Create a new chain for your endpoint group
2. **Include in mainScenario**: Add your chain to the main execution flow
3. **Update assertions**: Add any specific assertions for your endpoints
4. **Add debug output**: Include logging for debugging and monitoring

## Requirements

- **JHipster Application**: Running on specified base URL
- **Admin User**: Default admin/admin credentials must be available
- **Database**: Must be properly initialized with required data
- **Network**: Stable connection to the target environment

## Troubleshooting

### Common Issues

1. **Authentication Failures**

   - Verify admin/admin credentials are correct
   - Check if the application is running
   - Ensure `/api/authenticate` endpoint is accessible

2. **Timeout Errors**

   - Increase timeout values if running on slower environment
   - Check network connectivity
   - Monitor server resources

3. **Data Conflicts**
   - The simulation uses unique data generation to avoid conflicts
   - Ensure proper cleanup is running
   - Check database constraints

### Monitoring

Watch for these key metrics during test execution:

- Response times for each endpoint type
- Success/failure rates
- JWT token extraction success
- CRUD operation completion rates
- Cleanup operation success

This simulation provides a complete performance and functional validation of your library booking system's API surface.
