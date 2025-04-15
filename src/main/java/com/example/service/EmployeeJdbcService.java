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
    
    // Example of using Oracle's analytical functions
    public List<Map<String, Object>> getDepartmentSalaryStats() {
        String sql = """
            SELECT 
                DEPARTMENT_ID, 
                AVG(SALARY) AS AVG_SALARY,
                MIN(SALARY) AS MIN_SALARY,
                MAX(SALARY) AS MAX_SALARY,
                COUNT(*) AS EMP_COUNT,
                PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY SALARY) AS MEDIAN_SALARY
            FROM EMPLOYEES
            GROUP BY DEPARTMENT_ID
            ORDER BY DEPARTMENT_ID
        """;
        
        return jdbcTemplate.queryForList(sql);
    }
    
    // Example of using named parameters with Oracle DATE functions
    public List<Employee> findEmployeesHiredInRange(String startDate, String endDate) {
        String sql = """
            SELECT * FROM EMPLOYEES 
            WHERE TRUNC(HIRE_DATE) BETWEEN TO_DATE(:startDate, 'YYYY-MM-DD') 
            AND TO_DATE(:endDate, 'YYYY-MM-DD')
            ORDER BY HIRE_DATE
        """;
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);
        
        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> mapEmployeeFromResultSet(rs));
    }
    
    // Example of calling Oracle stored procedure using SimpleJdbcCall
    public void updateEmployeeSalary(Long employeeId, BigDecimal percentIncrease) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("update_employee_salary");
            
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_employee_id", employeeId);
        inParams.put("p_percent", percentIncrease);
        
        jdbcCall.execute(inParams);
    }
    
    // Example of using Oracle's hierarchical query (CONNECT BY)
    public List<Map<String, Object>> getEmployeeHierarchy(Long managerId) {
        String sql = """
            SELECT 
                EMPLOYEE_ID, FIRST_NAME, LAST_NAME, 
                LEVEL as HIERARCHY_LEVEL, 
                SYS_CONNECT_BY_PATH(LAST_NAME, '/') as EMP_PATH
            FROM EMPLOYEES
            START WITH MANAGER_ID = ?
            CONNECT BY PRIOR EMPLOYEE_ID = MANAGER_ID
            ORDER SIBLINGS BY LAST_NAME
        """;
        
        return jdbcTemplate.queryForList(sql, managerId);
    }
    
    // Example using Oracle's CASE expression and subquery
    public List<Map<String, Object>> getEmployeeSalaryCategories() {
        String sql = """
            SELECT 
                e.EMPLOYEE_ID, e.FIRST_NAME, e.LAST_NAME, e.SALARY,
                CASE 
                    WHEN e.SALARY < 5000 THEN 'Low'
                    WHEN e.SALARY BETWEEN 5000 AND 10000 THEN 'Medium'
                    ELSE 'High'
                END AS SALARY_CATEGORY,
                (SELECT AVG(SALARY) FROM EMPLOYEES WHERE DEPARTMENT_ID = e.DEPARTMENT_ID) AS DEPT_AVG_SALARY
            FROM EMPLOYEES e
            ORDER BY e.DEPARTMENT_ID, e.SALARY DESC
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
