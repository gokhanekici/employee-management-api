package com.example.restservice;

import java.util.List;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class EmployeeManager {

    private final Employees employees = new Employees();

    public EmployeeManager() {
        employees.setEmployeeList(List.of(
                new Employee("E1001", "Ada", "Lovelace",
                        "ada.lovelace@example.com", "Software Engineer"),
                new Employee("E1002", "Alan", "Turing",
                        "alan.turing@example.com", "Research Scientist"),
                new Employee("E1003", "Grace", "Hopper",
                        "grace.hopper@example.com", "Engineering Manager")
        ));
    }

    public Employees getEmployees() {
        return employees;
    }

    // Rejects duplicates: employee_id must be unique (returns false if it already exists).
    public boolean addEmployee(Employee employee) {
        if (employees.containsId(employee.getEmployeeId())) {
            return false;
        }
        employees.addEmployee(employee);
        return true;
    }

    public boolean updateEmployee(Employee employee) {
        return employees.replace(employee);
    }

    public Optional<Employee> findEmployee(String employeeId) {
        return employees.findById(employeeId);
    }
}