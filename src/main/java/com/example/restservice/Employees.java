package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

public class Employees {

    private List<Employee> employeeList = new ArrayList<>();

    @JsonProperty("Employees")
    public List<Employee> getEmployeeList() {
        return List.copyOf(employeeList);
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
}

