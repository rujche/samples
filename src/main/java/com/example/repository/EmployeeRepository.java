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
    @Query(value = "SELECT * FROM EMPLOYEES e WHERE e.SALARY > :minSalary", nativeQuery = true)
    List<Employee> findEmployeesWithSalaryGreaterThan(@Param("minSalary") BigDecimal minSalary);
    
    // Complex native query with joins
    @Query(value = "SELECT e.* FROM EMPLOYEES e " +
           "JOIN DEPARTMENTS d ON e.DEPARTMENT_ID = d.DEPARTMENT_ID " +
           "WHERE d.DEPARTMENT_NAME = :deptName " +
           "ORDER BY e.SALARY DESC", nativeQuery = true)
    List<Employee> findEmployeesByDepartmentNameOrderBySalary(@Param("deptName") String departmentName);
    
    // Using Oracle specific functions in a query
    @Query(value = "SELECT * FROM EMPLOYEES WHERE " +
           "UPPER(FIRST_NAME) LIKE UPPER('%' || :namePattern || '%') OR " +
           "UPPER(LAST_NAME) LIKE UPPER('%' || :namePattern || '%')", nativeQuery = true)
    List<Employee> findEmployeesByNamePattern(@Param("namePattern") String namePattern);
    
    // Example using Oracle specific date functions
    @Query(value = "SELECT * FROM EMPLOYEES WHERE " +
           "TRUNC(HIRE_DATE) BETWEEN TO_DATE(:startDate, 'YYYY-MM-DD') AND TO_DATE(:endDate, 'YYYY-MM-DD')", 
           nativeQuery = true)
    List<Employee> findEmployeesHiredBetweenDates(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
