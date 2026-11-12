package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
import java.util.Optional;

public class Employees {

    private List<Employee> employeeList = new ArrayList<>();

    @JsonProperty("Employees")
    public List<Employee> getEmployeeList() {
        return employeeList.stream()
                .filter(Employee::isActive)
                .toList();
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = new ArrayList<>(employeeList);
    }

    public void addEmployee(Employee employee) {
        employeeList.add(employee);
    }

    public boolean containsId(String employeeId) {
        return employeeList.stream()
                .anyMatch(e -> Objects.equals(e.getEmployeeId(), employeeId));
    }

    public Optional<Employee> findById(String employeeId) {
        return employeeList.stream()
                .filter(Employee::isActive)
                .filter(e -> Objects.equals(e.getEmployeeId(), employeeId))
                .findFirst();
    }

    public boolean replace(Employee updated) {
        for (int i = 0; i < employeeList.size(); i++) {
            Employee existing = employeeList.get(i);
            if (existing.isActive()
                    && Objects.equals(existing.getEmployeeId(), updated.getEmployeeId())) {
                employeeList.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public boolean deactivate(String employeeId) {
        Optional<Employee> employee = findById(employeeId);
        employee.ifPresent(e -> e.setActive(false));
        return employee.isPresent();
    }
}

