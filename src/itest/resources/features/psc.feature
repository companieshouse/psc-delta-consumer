Feature: Psc delta
  Scenario: Can transform and send a company profile
    Given the application is running
    When the consumer receives a message
    Then a PUT request is sent to the psc api with the transformed data
