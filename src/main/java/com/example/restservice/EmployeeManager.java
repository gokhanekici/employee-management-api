package com.example.restservice;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class EmployeeManager {

    private final EmployeeRepository repository;

    public EmployeeManager(EmployeeRepository repository) {
        this.repository = repository;
    }

    public Employees getEmployees() {
        Employees employees = new Employees();
        employees.setEmployeeList(repository.findByActiveTrueOrderByEmployeeId());
        return employees;
    }

    public Optional<Employee> findEmployee(String employeeId) {
        return repository.findByEmployeeIdAndActiveTrue(employeeId);
    }

    // Rejects duplicates: employee_id must be unique, including soft-deleted records.
    public boolean addEmployee(Employee employee) {
        if (repository.existsById(employee.getEmployeeId())) {
            return false;
        }
        repository.save(employee);
        return true;
    }

    public boolean updateEmployee(Employee employee) {
        if (findEmployee(employee.getEmployeeId()).isEmpty()) {
            return false;
        }
        repository.save(employee);
        return true;
    }

    // Soft delete: the record is kept but marked inactive, and its ID stays reserved.
    public boolean deleteEmployee(String employeeId) {
        Optional<Employee> employee = findEmployee(employeeId);
        employee.ifPresent(e -> {
            e.setActive(false);
            repository.save(e);
        });
        return employee.isPresent();
    }
}