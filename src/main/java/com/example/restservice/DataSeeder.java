package com.example.restservice;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepository repository;

    public DataSeeder(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                new Employee("E1001", "Ada", "Lovelace",
                        "ada.lovelace@example.com", "Software Engineer"),
                new Employee("E1002", "Alan", "Turing",
                        "alan.turing@example.com", "Research Scientist"),
                new Employee("E1003", "Grace", "Hopper",
                        "grace.hopper@example.com", "Engineering Manager")));
    }
}