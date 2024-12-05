Feature: Psc delete
  Scenario Outline: send DELETE request to the data api
  Given the application is running
  When the consumer receives a delete payload with <kind>
  Then a DELETE request is sent to the psc data api with the <expectedKind>
    Examples:
    | kind                                | expectedKind                                       |
    | "individual"                        | "individual-person-with-significant-control"       |
    | "corporate-entity"                  | "corporate-entity-person-with-significant-control" |
    | "legal-person"                      | "legal-person-person-with-significant-control"     |
    | "super-secure"                      | "super-secure-person-with-significant-control"     |
    | "individual-beneficial-owner"       | "individual-beneficial-owner"                      |
    | "corporate-entity-beneficial-owner" | "corporate-entity-beneficial-owner"                |
    | "legal-person-beneficial-owner"     | "legal-person-beneficial-owner"                    |
    | "super-secure-beneficial-owner"     | "super-secure-beneficial-owner"                    |

  Scenario: send DELETE with invalid JSON
  Given the application is running
  When the consumer receives an invalid delete payload
  Then the message should retry 3 times and then error

  Scenario: send DELETE with 400 from data api
  Given the application is running
  When the consumer receives a delete message but the data api returns a 400
  Then the message should be moved to topic psc-delta-invalid

  Scenario Outline: send DELETE with retryable response from data api
  Given the application is running
  When the consumer receives a delete message but the data api returns a <code>
  Then the message should retry 3 times and then error
  Examples:
  | code |
  | 404  |
  | 503  |