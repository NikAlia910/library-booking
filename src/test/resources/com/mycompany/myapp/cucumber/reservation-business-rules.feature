Feature: Reservation Business Rules Validation
  As a Library System
  I want to enforce business rules for reservations
  So that the system operates within defined constraints and policies

  Background:
    Given the library system is operational
    And the following resources are available:
      | title           | resourceType | author    |
      | Java Programming| BOOK         | John Doe  |
      | Meeting Room A  | MEETING_ROOM | N/A       |
      | Conference Room | MEETING_ROOM | N/A       |

  Scenario: Enforce maximum 5 active reservations per patron
    Given a patron "alice@example.com" exists
    And the patron has exactly 5 active reservations
    When the patron attempts to make a 6th reservation for "Java Programming"
    Then the reservation should be rejected
    And the system should return error "Maximum reservation limit of 5 active reservations reached"

  Scenario: Allow reservation when under the limit
    Given a patron "bob@example.com" exists  
    And the patron has exactly 4 active reservations
    When the patron attempts to make a reservation for "Java Programming"
    Then the reservation should be accepted
    And the patron should now have 5 active reservations

  Scenario: Prevent reservations beyond 30-day advance window
    Given today is "2024-01-15"
    And a patron "charlie@example.com" exists
    When the patron attempts to reserve "Meeting Room A" for "2024-02-20"
    Then the reservation should be rejected
    And the system should return error "Reservations cannot be made more than 30 days in advance"

  Scenario: Allow reservations within 30-day advance window
    Given today is "2024-01-15"
    And a patron "diana@example.com" exists
    When the patron attempts to reserve "Meeting Room A" for "2024-02-10"
    Then the reservation should be accepted

  Scenario: Prevent overlapping reservations for same resource
    Given a patron "eve@example.com" exists
    And "Conference Room" is reserved from "2024-02-15 10:00" to "2024-02-15 12:00"
    When the patron attempts to reserve "Conference Room" from "2024-02-15 11:30" to "2024-02-15 13:30"
    Then the reservation should be rejected
    And the system should return error "Selected time slot overlaps with an existing reservation"

  Scenario: Allow adjacent reservations (no overlap)
    Given a patron "frank@example.com" exists
    And "Conference Room" is reserved from "2024-02-15 10:00" to "2024-02-15 12:00"
    When the patron attempts to reserve "Conference Room" from "2024-02-15 12:00" to "2024-02-15 14:00"
    Then the reservation should be accepted

  Scenario: Validate reservation time constraints
    Given a patron "grace@example.com" exists
    When the patron attempts to reserve "Meeting Room A" with end time before start time
    Then the reservation should be rejected
    And the system should return error "End time must be after start time"

  Scenario: Enforce minimum reservation duration (1 hour)
    Given a patron "henry@example.com" exists
    When the patron attempts to reserve "Meeting Room A" from "2024-02-15 10:00" to "2024-02-15 10:30"
    Then the reservation should be rejected
    And the system should return error "Minimum reservation duration is 1 hour"

  Scenario: Enforce maximum reservation duration (8 hours)
    Given a patron "iris@example.com" exists
    When the patron attempts to reserve "Meeting Room A" from "2024-02-15 09:00" to "2024-02-15 18:00"
    Then the reservation should be rejected
    And the system should return error "Maximum reservation duration is 8 hours"

  Scenario: Generate unique reservation IDs
    Given a patron "jack@example.com" exists
    When the patron makes multiple reservations
    Then each reservation should have a unique reservation ID
    And no two reservations should share the same ID 