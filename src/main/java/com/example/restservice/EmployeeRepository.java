package com.example.restservice;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, String> {

    List<Employee> findByActiveTrueOrderByEmployeeId();

    Optional<Employee> findByEmployeeIdAndActiveTrue(String employeeId);
}