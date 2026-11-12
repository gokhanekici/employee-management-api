package com.example.restservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.web.bind.annotation.PutMapping;

import jakarta.validation.Valid;

@RestController
public class EmployeeController {

    private final EmployeeManager employeeManager;

    public EmployeeController(EmployeeManager employeeManager) {
        this.employeeManager = employeeManager;
    }

    @GetMapping("/employees")
    public Employees getEmployees() {
        return employeeManager.getEmployees();
    }

    @PostMapping("/employees")
    public ResponseEntity<Employee> addEmployee(@Valid @RequestBody Employee employee) {
        if (!employeeManager.addEmployee(employee)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        URI location = UriComponentsBuilder.fromPath("/employees/{id}")
                .buildAndExpand(employee.getEmployeeId()).encode().toUri();
        return ResponseEntity.created(location).body(employee);
    }

    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable("employeeId") String employeeId) {
        return employeeManager.findEmployee(employeeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/employees/{employeeId}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable("employeeId") String employeeId,
            @Valid @RequestBody Employee employee) {
        if (!employeeId.equals(employee.getEmployeeId())) {
            return ResponseEntity.badRequest().build();
        }
        if (!employeeManager.updateEmployee(employee)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee);
    }
}