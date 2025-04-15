package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "EMPLOYEES")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EMP_SEQ")
    @SequenceGenerator(name = "EMP_SEQ", sequenceName = "EMPLOYEES_SEQ", allocationSize = 1)
    @Column(name = "EMPLOYEE_ID")
    private Long id;
    
    @Column(name = "FIRST_NAME")
    private String firstName;
    
    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;
    
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;
    
    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;
    
    @Column(name = "HIRE_DATE", nullable = false)
    private LocalDate hireDate;
    
    @Column(name = "JOB_ID", nullable = false)
    private String jobId;
    
    @Column(name = "SALARY")
    private BigDecimal salary;
    
    @Column(name = "COMMISSION_PCT")
    private BigDecimal commissionPct;
    
    @Column(name = "MANAGER_ID")
    private Long managerId;
    
    @Column(name = "DEPARTMENT_ID")
    private Long departmentId;
}
