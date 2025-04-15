package com.example.mapper;

import com.example.model.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface EmployeeMapper {
    
    // Simple select by id
    Employee selectById(@Param("id") Long id);
    
    // Finding employees by name pattern
    List<Employee> findEmployeesByName(@Param("namePattern") String namePattern);
    
    // Complex query with join
    List<Employee> findEmployeesInDepartment(@Param("departmentName") String departmentName);
    
    // Using Oracle's ROWNUM for pagination
    List<Employee> findEmployeesWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
    // Dynamic SQL example
    List<Employee> findEmployeesByDynamicCriteria(Map<String, Object> criteria);
    
    // Using Oracle stored procedure
    void updateEmployeeSalary(@Param("employeeId") Long employeeId, @Param("percentIncrease") BigDecimal percentIncrease);
    
    // Insert statement
    int insertEmployee(Employee employee);
}
