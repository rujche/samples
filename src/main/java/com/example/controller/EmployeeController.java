package com.example.controller;

import com.example.model.Employee;
import com.example.repository.EmployeeRepository;
import com.example.service.EmployeeJdbcService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final EmployeeJdbcService employeeJdbcService;
    
    // JPA repository examples
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    @GetMapping("/department/{deptId}")
    public List<Employee> getEmployeesByDepartment(@PathVariable Long deptId) {
        return employeeRepository.findByDepartmentId(deptId);
    }
    
    @GetMapping("/salary-greater-than/{salary}")
    public List<Employee> getEmployeesWithSalaryGreaterThan(@PathVariable BigDecimal salary) {
        return employeeRepository.findEmployeesWithSalaryGreaterThan(salary);
    }
    
    @GetMapping("/department-name/{deptName}")
    public List<Employee> getEmployeesByDepartmentName(@PathVariable String deptName) {
        return employeeRepository.findEmployeesByDepartmentNameOrderBySalary(deptName);
    }
    
    @GetMapping("/search/{namePattern}")
    public List<Employee> searchEmployeesByName(@PathVariable String namePattern) {
        return employeeRepository.findEmployeesByNamePattern(namePattern);
    }
    
    @GetMapping("/hired-between")
    public List<Employee> getEmployeesHiredBetweenDates(
            @RequestParam String startDate, 
            @RequestParam String endDate) {
        return employeeRepository.findEmployeesHiredBetweenDates(startDate, endDate);
    }
    
    // JDBC template examples
    @GetMapping("/high-salary/{minSalary}")
    public List<Employee> getHighSalaryEmployees(@PathVariable BigDecimal minSalary) {
        return employeeJdbcService.findHighSalaryEmployees(minSalary);
    }
    
    @GetMapping("/dept-salary-stats")
    public List<Map<String, Object>> getDepartmentSalaryStats() {
        return employeeJdbcService.getDepartmentSalaryStats();
    }
    
    @GetMapping("/hired-in-range")
    public List<Employee> getEmployeesHiredInRange(
            @RequestParam String startDate, 
            @RequestParam String endDate) {
        return employeeJdbcService.findEmployeesHiredInRange(startDate, endDate);
    }
    
    @PutMapping("/{employeeId}/update-salary/{percentIncrease}")
    public ResponseEntity<Void> updateEmployeeSalary(
            @PathVariable Long employeeId, 
            @PathVariable BigDecimal percentIncrease) {
        employeeJdbcService.updateEmployeeSalary(employeeId, percentIncrease);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/hierarchy/{managerId}")
    public List<Map<String, Object>> getEmployeeHierarchy(@PathVariable Long managerId) {
        return employeeJdbcService.getEmployeeHierarchy(managerId);
    }
    
    @GetMapping("/salary-categories")
    public List<Map<String, Object>> getEmployeeSalaryCategories() {
        return employeeJdbcService.getEmployeeSalaryCategories();
    }
}
