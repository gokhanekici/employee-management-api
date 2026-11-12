package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Employees {

    private List<Employee> employeeList = new ArrayList<>();

    @JsonProperty("Employees")
    public List<Employee> getEmployeeList() {
        return List.copyOf(employeeList);
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = new ArrayList<>(employeeList);
    }
}