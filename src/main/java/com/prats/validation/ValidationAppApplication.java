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
		String csvResource = "sample.csv";   // resource name
		String jsonFilePath = "output.json"; // can be outside JAR

		fileValidationService.convertCsvToJson(csvResource, jsonFilePath);
		System.out.println("CSV to JSON conversion done!");
	}

}
