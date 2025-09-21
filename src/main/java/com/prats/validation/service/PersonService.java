package com.prats.validation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.prats.validation.model.Person;

@Service
public class PersonService {

    public List<Person> getAllPersons() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            InputStream inputStream = new ClassPathResource("output.json").getInputStream();
            return mapper.readValue(inputStream, new TypeReference<List<Person>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to read persons.json", e);
        }
    }
}
