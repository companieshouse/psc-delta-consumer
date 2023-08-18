Feature: Psc delta
  Scenario Outline: Can transform and send a company profile of kind "<pscKind>"
    Given the application is running
    When the consumer receives a message of kind "<pscKind>" for company "<companyNumber>" with id "<pscId>"
    Then a PUT request is sent to the psc api with the transformed data for psc of kind "<pscKind>" for company "<companyNumber>" with id "<pscId>"

    Examples:
      | pscKind               | companyNumber       | pscId                       |
      | corporate_entity_BO   | 00623672            | hZ_JFH9mW2suVRdVM7jm9w2MD10 |
      | corporate_entity      | 00623672            | AoRE4bhxdSdXur_NLdfh4JF81Y4 |
      | individual_BO         | 00623672            | ERyiOut_ZiY1GMAZbkeN6PLCDNc |
      | individual            | 00623672            | lXgouUAR16hSIwxdJSpbr_dhyT8 |