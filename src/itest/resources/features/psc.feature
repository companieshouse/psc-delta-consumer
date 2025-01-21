Feature: Psc delta
  Scenario Outline: Can transform and send a company profile of kind "<pscKind>"
    Given the application is running
    When the consumer receives a message of kind "<pscKind>" for company "<companyNumber>" with psc id "<pscId>"
    Then a PUT request is sent to the psc api with the transformed data for psc of kind "<pscKind>" for company "<companyNumber>" with id "<pscId>"

    Examples:
      | pscKind                 | companyNumber       | pscId                       |
      | super_secure_entity_BO  | OE623672            | 0ewxT9lV_MPjngrHhR-vsOUHOpo |
      | super_secure_entity     | 00623672            | Gh7E2SSkj-YBM3i396MI-ycubGY |

  Scenario: Process invalid avro message
    Given the application is running
    When an invalid avro message is sent
    Then the message should be moved to topic psc-delta-invalid

  Scenario: Process message with invalid data
    Given the application is running
    When a message with invalid data is sent
    Then the message should retry 3 times and then error

  Scenario Outline: Process message when the api returns 400
    Given the application is running
    When the consumer receives a message for company "00623672" with notification id "lXgouUAR16hSIwxdJSpbr_dhyT8" but the api returns a <status_code>
    Then the message should be moved to topic psc-delta-invalid
    Examples:
      | status_code |
      | 400         |
      | 409         |

  Scenario Outline: Process message when the api returns 503
    Given the application is running
    When the consumer receives a message for company "00623672" with notification id "lXgouUAR16hSIwxdJSpbr_dhyT8" but the api returns a <status_code>
    Then the message should retry 3 times and then error
    Examples:
      | status_code |
      | 401         |
      | 403         |
      | 404         |
      | 405         |
      | 500         |
      | 503         |