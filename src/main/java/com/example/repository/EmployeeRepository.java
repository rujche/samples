package com.example.repository;

import com.example.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Example of custom JPQL query
    @Query("SELECT e FROM Employee e WHERE e.departmentId = :deptId")
    List<Employee> findByDepartmentId(@Param("deptId") Long departmentId);
      // Example of native SQL query
    @Query(value = "SELECT * FROM employees e WHERE e.salary > :minSalary", nativeQuery = true)
    List<Employee> findEmployeesWithSalaryGreaterThan(@Param("minSalary") BigDecimal minSalary);
    
    // Complex native query with joins
    @Query(value = "SELECT e.* FROM employees e " +
           "JOIN departments d ON e.department_id = d.department_id " +
           "WHERE d.department_name = :deptName " +
           "ORDER BY e.salary DESC", nativeQuery = true)
    List<Employee> findEmployeesByDepartmentNameOrderBySalary(@Param("deptName") String departmentName);
      // Using PostgreSQL string functions in a query
    @Query(value = "SELECT * FROM employees WHERE " +
           "UPPER(first_name) LIKE UPPER('%' || :namePattern || '%') OR " +
           "UPPER(last_name) LIKE UPPER('%' || :namePattern || '%')", nativeQuery = true)
    List<Employee> findEmployeesByNamePattern(@Param("namePattern") String namePattern);
    
    // Example using PostgreSQL date functions
    @Query(value = "SELECT * FROM employees WHERE " +
           "DATE_TRUNC('day', hire_date) BETWEEN to_date(:startDate, 'YYYY-MM-DD') AND to_date(:endDate, 'YYYY-MM-DD')", 
           nativeQuery = true)
    List<Employee> findEmployeesHiredBetweenDates(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
