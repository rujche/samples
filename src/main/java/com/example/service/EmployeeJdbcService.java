package com.example.service;

import com.example.model.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeJdbcService {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    // Example of direct SQL query using JdbcTemplate
    public List<Employee> findHighSalaryEmployees(BigDecimal minSalary) {
        String sql = "SELECT * FROM EMPLOYEES WHERE SALARY > ?";
        
        return jdbcTemplate.query(sql, 
            new Object[]{minSalary},
            new int[]{Types.NUMERIC}, 
            (rs, rowNum) -> mapEmployeeFromResultSet(rs));
    }
      // Example of using PostgreSQL's analytical functions
    public List<Map<String, Object>> getDepartmentSalaryStats() {
        String sql = """
            SELECT 
                department_id, 
                AVG(salary) AS avg_salary,
                MIN(salary) AS min_salary,
                MAX(salary) AS max_salary,
                COUNT(*) AS emp_count,
                PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary) AS median_salary
            FROM employees
            GROUP BY department_id
            ORDER BY department_id
        """;
        
        return jdbcTemplate.queryForList(sql);
    }
      // Example of using named parameters with PostgreSQL DATE functions
    public List<Employee> findEmployeesHiredInRange(String startDate, String endDate) {
        String sql = """
            SELECT * FROM employees 
            WHERE DATE_TRUNC('day', hire_date) BETWEEN to_date(:startDate, 'YYYY-MM-DD') 
            AND to_date(:endDate, 'YYYY-MM-DD')
            ORDER BY hire_date
        """;
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);
        
        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> mapEmployeeFromResultSet(rs));
    }
      // Example of calling PostgreSQL function using SimpleJdbcCall
    public void updateEmployeeSalary(Long employeeId, BigDecimal percentIncrease) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withFunctionName("update_employee_salary");
            
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_employee_id", employeeId);
        inParams.put("p_percent", percentIncrease);
        
        jdbcCall.execute(inParams);
    }
      // Example of using PostgreSQL's recursive query (instead of Oracle's CONNECT BY)
    public List<Map<String, Object>> getEmployeeHierarchy(Long managerId) {
        String sql = """
            WITH RECURSIVE emp_hierarchy AS (
                SELECT 
                    employee_id, first_name, last_name, 
                    manager_id, 1 AS hierarchy_level,
                    last_name::text AS emp_path
                FROM employees
                WHERE manager_id = ?
                UNION ALL
                SELECT 
                    e.employee_id, e.first_name, e.last_name, 
                    e.manager_id, h.hierarchy_level + 1,
                    h.emp_path || '/' || e.last_name
                FROM employees e
                JOIN emp_hierarchy h ON e.manager_id = h.employee_id
            )
            SELECT 
                employee_id, first_name, last_name, 
                hierarchy_level, emp_path 
            FROM emp_hierarchy
            ORDER BY hierarchy_level, last_name
        """;
        
        return jdbcTemplate.queryForList(sql, managerId);
    }
      // Example using PostgreSQL's CASE expression and subquery 
    public List<Map<String, Object>> getEmployeeSalaryCategories() {
        String sql = """
            SELECT 
                e.employee_id, e.first_name, e.last_name, e.salary,
                CASE 
                    WHEN e.salary < 5000 THEN 'Low'
                    WHEN e.salary BETWEEN 5000 AND 10000 THEN 'Medium'
                    ELSE 'High'
                END AS salary_category,
                (SELECT AVG(salary) FROM employees WHERE department_id = e.department_id) AS dept_avg_salary
            FROM employees e
            ORDER BY e.department_id, e.salary DESC
        """;
        
        return jdbcTemplate.queryForList(sql);
    }
    
    // Helper method to map ResultSet to Employee
    private Employee mapEmployeeFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getLong("EMPLOYEE_ID"));
        employee.setFirstName(rs.getString("FIRST_NAME"));
        employee.setLastName(rs.getString("LAST_NAME"));
        employee.setEmail(rs.getString("EMAIL"));
        employee.setPhoneNumber(rs.getString("PHONE_NUMBER"));
        employee.setHireDate(rs.getDate("HIRE_DATE").toLocalDate());
        employee.setJobId(rs.getString("JOB_ID"));
        employee.setSalary(rs.getBigDecimal("SALARY"));
        employee.setCommissionPct(rs.getBigDecimal("COMMISSION_PCT"));
        employee.setManagerId(rs.getLong("MANAGER_ID"));
        employee.setDepartmentId(rs.getLong("DEPARTMENT_ID"));
        return employee;
    }
}
