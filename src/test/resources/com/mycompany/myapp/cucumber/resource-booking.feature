Feature: Book a Resource (Book, Meeting Room, Equipment)
  As a Library Patron
  I want to be able to reserve a resource online
  So that I can ensure the resource is available when I need it and avoid unnecessary trips to the library

  Background:
    Given I am a registered library patron

  Scenario: Search for available resources by title
    Given the following resources exist:
      | title         | author        | resourceType | keywords      |
      | Java Guide    | John Doe      | BOOK         | programming   |
      | Meeting Room A| N/A           | MEETING_ROOM | conference    |
      | Projector     | N/A           | EQUIPMENT    | presentation  |
    When I search for resources by title "Java"
    Then I should see 1 resource in the search results
    And the search results should contain "Java Guide"

  Scenario: Search for available resources by author
    Given the following resources exist:
      | title         | author        | resourceType | keywords      |
      | Java Guide    | John Doe      | BOOK         | programming   |
      | Python Basics | John Doe      | BOOK         | programming   |
      | Meeting Room A| N/A           | MEETING_ROOM | conference    |
    When I search for resources by author "John Doe"
    Then I should see 2 resources in the search results
    And the search results should contain "Java Guide"
    And the search results should contain "Python Basics"

  Scenario: Search for available resources by keyword
    Given the following resources exist:
      | title         | author        | resourceType | keywords      |
      | Java Guide    | John Doe      | BOOK         | programming   |
      | Meeting Room A| N/A           | MEETING_ROOM | conference    |
      | Projector     | N/A           | EQUIPMENT    | presentation  |
    When I search for resources by keyword "programming"
    Then I should see 1 resource in the search results
    And the search results should contain "Java Guide"

  Scenario: Search for available resources by resource type
    Given the following resources exist:
      | title         | author        | resourceType | keywords      |
      | Java Guide    | John Doe      | BOOK         | programming   |
      | Meeting Room A| N/A           | MEETING_ROOM | conference    |
      | Projector     | N/A           | EQUIPMENT    | presentation  |
    When I search for resources by type "MEETING_ROOM"
    Then I should see 1 resource in the search results
    And the search results should contain "Meeting Room A"

  Scenario: Successfully make a reservation within booking window
    Given a resource "Java Guide" exists
    And the resource "Java Guide" is available
    And I have 0 active reservations
    When I attempt to reserve "Java Guide" for "2024-02-15" from "10:00" to "12:00"
    Then the reservation should be successful
    And I should receive a confirmation with reservation details
    And the confirmation should include:
      | resourceName | date       | startTime | endTime |
      | Java Guide   | 2024-02-15 | 10:00     | 12:00   |

  Scenario: Cannot make reservation beyond 30-day advance booking window
    Given a resource "Java Guide" exists
    And today's date is "2024-01-01"
    When I attempt to reserve "Java Guide" for "2024-02-15" from "10:00" to "12:00"
    Then the reservation should fail
    And I should see an error message "Reservations cannot be made more than 30 days in advance"

  Scenario: Cannot make reservation when maximum limit reached
    Given a resource "Java Guide" exists
    And I have 5 active reservations
    When I attempt to reserve "Java Guide" for "2024-02-15" from "10:00" to "12:00"
    Then the reservation should fail
    And I should see an error message "Maximum reservation limit of 5 active reservations reached"

  Scenario: Cannot make overlapping reservation for same resource
    Given a resource "Meeting Room A" exists
    And the resource "Meeting Room A" has an existing reservation from "2024-02-15 10:00" to "2024-02-15 12:00"
    When I attempt to reserve "Meeting Room A" for "2024-02-15" from "11:00" to "13:00"
    Then the reservation should fail
    And I should see an error message "Selected time slot overlaps with an existing reservation"

  Scenario: View availability calendar for a specific resource
    Given a resource "Meeting Room A" exists
    And the resource "Meeting Room A" has the following reservations:
      | date       | startTime | endTime |
      | 2024-02-15 | 10:00     | 12:00   |
      | 2024-02-16 | 14:00     | 16:00   |
    When I view the availability calendar for "Meeting Room A"
    Then I should see the resource is booked on "2024-02-15" from "10:00" to "12:00"
    And I should see the resource is booked on "2024-02-16" from "14:00" to "16:00"
    And I should see the resource is available on "2024-02-17" from "10:00" to "12:00"

  Scenario: Successfully make adjacent reservation (no overlap)
    Given a resource "Meeting Room A" exists
    And the resource "Meeting Room A" has an existing reservation from "2024-02-15 10:00" to "2024-02-15 12:00"
    When I attempt to reserve "Meeting Room A" for "2024-02-15" from "12:00" to "14:00"
    Then the reservation should be successful
    And I should receive a confirmation with reservation details 