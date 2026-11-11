package com.example.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeManager employeeManager;

    private EmployeeController controller;
    private Employee employee;

    @BeforeEach
    void setUp() {
        controller = new EmployeeController(employeeManager);
        employee = new Employee("E2001", "Linus", "Torvalds",
                "linus.torvalds@example.com", "Staff Engineer");
    }

    @Test
    void getEmployeesReturnsListFromManager() {
        Employees employees = new Employees();
        when(employeeManager.getEmployees()).thenReturn(employees);

        assertSame(employees, controller.getEmployees());
    }

    @Test
    void addEmployeeReturns201WhenAdded() {
        when(employeeManager.addEmployee(employee)).thenReturn(true);

        ResponseEntity<Employee> response = controller.addEmployee(employee);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(employee, response.getBody());
        verify(employeeManager).addEmployee(employee);
    }

    @Test
    void addEmployeeReturns409WhenIdAlreadyExists() {
        when(employeeManager.addEmployee(employee)).thenReturn(false);

        ResponseEntity<Employee> response = controller.addEmployee(employee);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
    }
}