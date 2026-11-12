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
    void updateEmployeeReplacesExistingEmployee() {
        Employee updated = new Employee("E1001", "Ada", "Lovelace",
                "ada.lovelace@example.com", "Principal Engineer");

        assertTrue(manager.updateEmployee(updated));
        assertEquals("Principal Engineer", manager.findEmployee("E1001").get().getTitle());
        assertEquals(3, manager.getEmployees().getEmployeeList().size());
    }

    @Test
    void updateEmployeeReturnsFalseForUnknownId() {
        Employee unknown = new Employee("UNKNOWN", "No", "One",
                "no.one@example.com", "None");

        assertFalse(manager.updateEmployee(unknown));
    }

    @Test
    void deleteEmployeeHidesEmployeeFromListAndLookup() {
        assertTrue(manager.deleteEmployee("E1001"));

        assertEquals(2, manager.getEmployees().getEmployeeList().size());
        assertTrue(manager.findEmployee("E1001").isEmpty());
    }

    @Test
    void deleteEmployeeReturnsFalseForUnknownId() {
        assertFalse(manager.deleteEmployee("UNKNOWN"));
    }

    @Test
    void deleteEmployeeReturnsFalseWhenAlreadyDeleted() {
        manager.deleteEmployee("E1001");

        assertFalse(manager.deleteEmployee("E1001"));
    }

    @Test
    void deletedEmployeeIdCannotBeReused() {
        manager.deleteEmployee("E1001");
        Employee reused = new Employee("E1001", "New", "Person",
                "new.person@example.com", "Engineer");

        assertFalse(manager.addEmployee(reused));
    }

    @Test
    void updateEmployeeReturnsFalseForDeletedEmployee() {
        manager.deleteEmployee("E1001");
        Employee updated = new Employee("E1001", "Ada", "Lovelace",
                "ada.lovelace@example.com", "Principal Engineer");

        assertFalse(manager.updateEmployee(updated));
    }

    @Test
    void findEmployeeReturnsEmptyForUnknownId() {
        assertTrue(manager.findEmployee("UNKNOWN").isEmpty());
    }
}