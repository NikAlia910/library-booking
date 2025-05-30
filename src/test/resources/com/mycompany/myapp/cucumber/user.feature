@user-management
Feature: User management
  As a system administrator,
  I want to manage user accounts and their roles,
  So that I can control system access and maintain user information.

  Background:
    Given the application is running
    And I am logged in as an administrator

  Rule: Administrators can search and view user details
    @smoke @search
    Scenario: Successfully retrieve administrator user
      When I search for user 'admin'
      Then the user should be found
      And their last name should be 'Administrator'
      And their role should be 'ADMIN'

    @search
    Scenario: Search for non-existent user
      When I search for user 'nonexistent'
      Then no user should be found
      And an appropriate error message should be displayed

  Rule: User account management operations
    @create
    Scenario: Create a new user account
      Given I am on the user creation page
      When I create a user with the following details:
        | username | email           | firstName | lastName |
        | testuser | test@email.com | Test      | User     |
      Then the user should be created successfully
      And the user should receive an activation email

    @update
    Scenario: Update existing user information
      Given a user exists with username 'testuser'
      When I update the user with the following details:
        | firstName | lastName |
        | Updated   | Name     |
      Then the user information should be updated successfully

  Rule: User role management
    @roles
    Scenario Outline: Assign different roles to users
      Given a user exists with username '<username>'
      When I assign the role '<role>' to the user
      Then the user's role should be updated to '<role>'
      And the user should have appropriate permissions

      Examples:
        | username  | role      |
        | user1     | USER      |
        | admin2    | ADMIN     |
        | manager1  | MANAGER   |
