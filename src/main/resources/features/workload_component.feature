@component @workload
Feature: Workload Service Component Tests

  @positive @component
  Scenario: ADD event creates new trainer summary
    Given no trainer summary exists for "john.doe"
    When an ADD workload event is received for trainer "john.doe" with duration 60 in year 2026 month 3
    Then trainer "john.doe" should have total workload of 60 in year 2026 month 3

  @positive @component
  Scenario: ADD event accumulates duration for existing trainer
    Given trainer "john.doe" has existing workload of 60 in year 2026 month 3
    When an ADD workload event is received for trainer "john.doe" with duration 30 in year 2026 month 3
    Then trainer "john.doe" should have total workload of 90 in year 2026 month 3

  @positive @component
  Scenario: DELETE event decreases trainer workload
    Given trainer "john.doe" has existing workload of 60 in year 2026 month 3
    When a DELETE workload event is received for trainer "john.doe" with duration 60 in year 2026 month 3
    Then trainer "john.doe" should have total workload of 0 in year 2026 month 3

  @negative @component
  Scenario: DELETE event does not go below zero
    Given trainer "john.doe" has existing workload of 30 in year 2026 month 3
    When a DELETE workload event is received for trainer "john.doe" with duration 60 in year 2026 month 3
    Then trainer "john.doe" should have total workload of 0 in year 2026 month 3

  @positive @component
  Scenario: ADD event for new month adds separate month entry
    Given trainer "john.doe" has existing workload of 60 in year 2026 month 3
    When an ADD workload event is received for trainer "john.doe" with duration 45 in year 2026 month 4
    Then trainer "john.doe" should have total workload of 60 in year 2026 month 3
    And trainer "john.doe" should have total workload of 45 in year 2026 month 4

  @positive @component
  Scenario: ADD event for new year adds separate year entry
    Given trainer "john.doe" has existing workload of 60 in year 2026 month 3
    When an ADD workload event is received for trainer "john.doe" with duration 90 in year 2027 month 1
    Then trainer "john.doe" should have total workload of 60 in year 2026 month 3
    And trainer "john.doe" should have total workload of 90 in year 2027 month 1

  @positive @component
  Scenario: GET workload returns summary for existing trainer
    Given trainer "john.doe" has existing workload of 120 in year 2026 month 3
    When client requests workload summary for trainer "john.doe"
    Then response status should be 200
    And response should contain workload of 120 for year 2026 month 3

  @negative @component
  Scenario: GET workload returns 404 for unknown trainer
    Given no trainer summary exists for "ghost.user"
    When client requests workload summary for trainer "ghost.user"
    Then response status should be 404