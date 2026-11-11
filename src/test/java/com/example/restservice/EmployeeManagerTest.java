package com.example.restservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

class EmployeeManagerTest {

    private EmployeeManager manager;

    @BeforeEach
    void setUp() {
        manager = new EmployeeManager();
    }

    @Test
    void startsWithThreeEmployees() {
        assertEquals(3, manager.getEmployees().getEmployeeList().size());
    }

    @Test
    void addEmployeeAddsNewEmployee() {
        Employee newEmployee = new Employee("E2001", "Linus", "Torvalds",
                "linus.torvalds@example.com", "Staff Engineer");

        assertTrue(manager.addEmployee(newEmployee));
        assertEquals(4, manager.getEmployees().getEmployeeList().size());
        assertTrue(manager.getEmployees().containsId("E2001"));
    }

    @Test
    void addEmployeeRejectsDuplicateId() {
        Employee duplicate = new Employee("E1001", "Someone", "Else",
                "someone.else@example.com", "Intern");

        assertFalse(manager.addEmployee(duplicate));
        assertEquals(3, manager.getEmployees().getEmployeeList().size());
    }

    @Test
    void employeeListCannotBeModifiedFromOutside() {
        Employee intruder = new Employee("E9999", "No", "Access",
                "no.access@example.com", "None");

        assertThrows(UnsupportedOperationException.class,
                () -> manager.getEmployees().getEmployeeList().add(intruder));
    }

    @Test
    void findEmployeeReturnsExistingEmployee() {
        Optional<Employee> found = manager.findEmployee("E1001");

        assertTrue(found.isPresent());
        assertEquals("Ada", found.get().getFirstName());
    }

    @Test
    void findEmployeeReturnsEmptyForUnknownId() {
        assertTrue(manager.findEmployee("UNKNOWN").isEmpty());
    }
}