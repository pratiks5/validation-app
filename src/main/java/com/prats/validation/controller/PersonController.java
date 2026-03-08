package com.prats.validation.controller;

import com.prats.validation.model.Person;
import com.prats.validation.service.PersonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/api/persons")
    public List<Person> getPersons() {
        return personService.getAllPersons();
    }
}