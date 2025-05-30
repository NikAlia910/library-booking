# Library Booking System - Cucumber Tests

This document describes the Gherkin feature files and Cucumber step definitions created for the library booking system based on the user story requirements.

## 📁 File Structure

```
src/test/resources/com/mycompany/myapp/cucumber/
├── resource-booking.feature              # Main user scenarios
├── reservation-business-rules.feature    # Business rule validation scenarios

src/test/java/com/mycompany/myapp/cucumber/stepdefs/
├── ResourceBookingStepDefs.java          # Step definitions for resource booking
├── ReservationBusinessRulesStepDefs.java # Step definitions for business rules
└── UserStepDefs.java                     # Existing user step definitions
```

## 🎯 Feature Files Overview

### 1. `resource-booking.feature`

Contains scenarios covering the main user acceptance criteria:

- **Search functionality**: By title, author, keyword, and resource type
- **Reservation creation**: Basic reservation workflow
- **Availability checking**: Calendar view and time slot validation
- **Business constraints**: Maximum reservations, advance booking window, overlap prevention

### 2. `reservation-business-rules.feature`

Focuses specifically on business rule enforcement:

- Maximum 5 active reservations per patron
- 30-day advance booking window
- Overlap prevention with validation messages
- Time constraint validation (minimum/maximum duration)
- Unique reservation ID generation

## 🔧 Step Definitions Architecture

### ✅ **CORRECTLY IMPLEMENTED**: Following Best Practices

The step definitions follow the **CRITICAL RULE** by:

1. **Calling Actual Services**: All step definitions use `@Autowired` to inject real application services:

   ```java
   @Autowired
   private ResourceService resourceService;

   @Autowired
   private ReservationService reservationService;

   ```

2. **No Business Logic**: Step definitions contain NO business logic implementation:

   ```java
   @When("I attempt to reserve {string} for {string} from {string} to {string}")
   public void i_attempt_to_reserve_for_date_and_time(...) {
       // ✅ CORRECT: Calls actual service
       reservationResult = reservationService.save(reservationDTO);

       // ❌ WRONG: Would be implementing business logic here
       // if (userReservationCount >= 5) { throw new Exception(...); }
   }
   ```

3. **Creating Failing Tests**: Tests will fail until proper business logic is implemented in the service layer.

## 🚨 Required Business Logic Implementation

To make these tests pass, the following business logic must be implemented in the **SERVICE LAYER**:

### 1. **ResourceService Enhancements**

```java
// Add search methods to ResourceService or ResourceRepository
public List<ResourceDTO> findByTitleContaining(String title);

public List<ResourceDTO> findByAuthorContaining(String author);

public List<ResourceDTO> findByKeywordsContaining(String keywords);

public List<ResourceDTO> findByResourceType(ResourceType type);

```

### 2. **ReservationService Business Rules**

```java
// Add validation in ReservationService.save() method
public ReservationDTO save(ReservationDTO reservationDTO) {
  // Validate maximum reservations per user (5)
  validateMaxReservationsPerUser(reservationDTO.getUser());

  // Validate 30-day advance booking window
  validateAdvanceBookingWindow(reservationDTO.getReservationDate());

  // Validate no overlapping reservations
  validateNoOverlappingReservations(reservationDTO);

  // Validate time constraints
  validateTimeConstraints(reservationDTO);
  // Continue with save...
}

```

### 3. **Custom Repository Methods**

```java
// Add to ReservationRepository
@Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = :userId")
long countActiveReservationsByUserId(@Param("userId") Long userId);

@Query("SELECT r FROM Reservation r WHERE r.resource.id = :resourceId AND " + "((r.startTime <= :endTime) AND (r.endTime >= :startTime))")
List<Reservation> findOverlappingReservations(
  @Param("resourceId") Long resourceId,
  @Param("startTime") Instant startTime,
  @Param("endTime") Instant endTime
);

```

## 🧪 Running the Tests

```bash
# Run all Cucumber tests
mvn test -Dtest=CucumberIT

# Run with specific feature
mvn test -Dtest=CucumberIT -Dcucumber.options="--tags @booking"
```

## 📋 Test Scenarios Coverage

### User Story Acceptance Criteria ✅

- [x] Search for resources by title, author, keyword, resource type
- [x] View availability calendar for specific resources
- [x] Select date and time slot for reservation
- [x] Receive confirmation email with reservation details
- [x] Prevent booking when maximum reservation limit reached
- [x] Prevent overlapping reservations

### Business Logic Validation ✅

- [x] Maximum 5 active reservations per patron
- [x] 30-day advance booking window enforcement
- [x] Overlap prevention with proper error messages
- [x] Time constraint validation
- [x] Unique reservation ID generation

## 🎯 Expected Test Results

**CURRENT STATE**: Tests will **FAIL** with missing business logic errors

**EXPECTED BEHAVIOR**:

- Tests should fail until business logic is implemented in services
- No business logic should be added to step definitions
- All validations should happen in the service layer
- Step definitions should only call services and assert results

## 🔄 Development Workflow

1. **Run tests** - They should fail initially
2. **Implement business logic** in service classes (not step definitions)
3. **Re-run tests** - They should gradually pass as logic is implemented
4. **Refactor services** based on test feedback
5. **All tests green** = Business requirements satisfied

## 📝 Notes

- **User Creation**: Test users are created with unique emails and passwords using `RandomStringUtils.insecure().nextAlphanumeric(60)`
- **Data Cleanup**: Each scenario starts with a clean database state
- **Real Services**: All interactions use actual Spring services, not mocks
- **Validation Messages**: Expected error messages are defined in test scenarios

This test suite provides comprehensive coverage of the library booking system requirements and will guide the implementation of robust business logic in the service layer.
