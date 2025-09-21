Feature: Convert CSV to JSON using Spring Boot JAR

  Scenario: Valid CSV file is converted to JSON
    Given I have a CSV file at "src/test/resources/sample.csv"
    And I want the JSON output at "src/main/resources/output.json"
    When I run the CSV to JSON converter jar
    Then the output file should exist and not be empty
