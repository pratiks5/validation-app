package com.prats.validation;

import com.prats.validation.service.FileValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ValidationAppApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ValidationAppApplication.class, args);
	}

	@Autowired
	private FileValidationService fileValidationService;

	@Override
	public void run(String... args) throws Exception {
		String csvPath = "src/main/resources/sample.csv";
		String jsonPath = "src/main/resources/output.json";
		fileValidationService.convertCsvToJson(csvPath, jsonPath);
		System.out.println("CSV to JSON conversion done!");
	}

}
