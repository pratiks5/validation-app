package com.prats.validation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prats.validation.model.Person;

import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class FileValidationService {

    public void convertCsvToJson(String csvFilePath, String jsonFilePath) throws IOException {
        try (
                Reader reader = new FileReader(csvFilePath);
                Writer writer = new FileWriter(jsonFilePath)
        ) {
            CsvToBean<Person> csvToBean = new CsvToBeanBuilder<Person>(reader)
                    .withType(Person.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<Person> people = csvToBean.parse();

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, people);
        }
    }

}
