package com.prats.validation.stepdefs;

import com.prats.validation.service.JavaRunnerService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.*;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class StepDefinition {
    private String csvPath;
    private String jsonPath;
    private final JavaRunnerService runner = new JavaRunnerService();

    @Given("I have a CSV file at {string}")
    public void i_have_a_csv_file_at(String path) {
        this.csvPath = path;
        assertTrue(new File(path).exists(), "CSV file should exist");
    }

    @And("I want the JSON output at {string}")
    public void i_want_the_json_output_at(String path) {
        this.jsonPath = path;
    }

    @When("I run the CSV to JSON converter jar")
    public void i_run_the_converter_jar() throws Exception {
        System.out.println("csvPath =>> " + csvPath);
        System.out.println("csvPath =>> " + jsonPath);
        runner.runStageJar(csvPath, jsonPath);
    }

    @Then("the output file should exist and not be empty")
    public void the_output_should_exist_and_not_be_empty() {
        File json = new File(jsonPath);
        assertTrue(json.exists(), "JSON file should exist");
        assertTrue(json.length() > 0, "JSON file should not be empty");
    }
}
