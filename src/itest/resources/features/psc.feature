Feature: Psc delta
  Scenario Outline: Can transform and send a company profile of kind "<pscKind>"
    Given the application is running
    When the consumer receives a message of kind "<pscKind>" for company "<companyNumber>" with psc id "<pscId>"
    Then a PUT request is sent to the psc api with the transformed data for psc of kind "<pscKind>" for company "<companyNumber>" with id "<pscId>"

    Examples:
      | pscKind                 | companyNumber       | pscId                       |
      | corporate_entity_BO     | OE623672            | hZ_JFH9mW2suVRdVM7jm9w2MD10 |
      | corporate_entity        | 00623672            | AoRE4bhxdSdXur_NLdfh4JF81Y4 |
      | individual_BO           | OE623672            | ERyiOut_ZiY1GMAZbkeN6PLCDNc |
      | individual              | 00623672            | lXgouUAR16hSIwxdJSpbr_dhyT8 |
      | legal_person_BO         | OE623672            | nVygLIRIpytItqx33sVCo69WNt0 |
      | legal_person            | 00623672            | WWG3toZrwNqzvwUZlSa4JgQAvzY |
      | super_secure_entity_BO  | OE623672            | 0ewxT9lV_MPjngrHhR-vsOUHOpo |
      | super_secure_entity     | 00623672            | Gh7E2SSkj-YBM3i396MI-ycubGY |

  Scenario: Process invalid avro message
    Given the application is running
    When an invalid avro message is sent
    Then the message should be moved to topic psc-delta-invalid

  Scenario: Process message with invalid data
    Given the application is running
    When a message with invalid data is sent
    Then the message should be moved to topic psc-delta-invalid

  Scenario: Process message when the api returns 400
    Given the application is running
    When the consumer receives a message for company "00623672" with notification id "lXgouUAR16hSIwxdJSpbr_dhyT8" but the api returns a 400
    Then the message should be moved to topic psc-delta-invalid

  Scenario: Process message when the api returns 503
    Given the application is running
    When the consumer receives a message for company "00623672" with notification id "lXgouUAR16hSIwxdJSpbr_dhyT8" but the api returns a 503
    Then the message should retry 3 times and then error