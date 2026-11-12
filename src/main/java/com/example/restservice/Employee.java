package com.example.restservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonPropertyOrder({"employee_id", "first_name", "last_name", "email", "title"})
public class Employee {

    @NotBlank
    private String employeeId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String title;

    private boolean active = true;

    public Employee() {
    }

    public Employee(String employeeId, String firstName, String lastName,
                    String email, String title) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.title = title;
    }

    @JsonProperty("employee_id")
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    @JsonProperty("first_name")
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    @JsonProperty("last_name")
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @JsonIgnore
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}