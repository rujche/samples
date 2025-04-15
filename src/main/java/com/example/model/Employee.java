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
@Table(name = "employees")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EMP_SEQ")
    @SequenceGenerator(name = "EMP_SEQ", sequenceName = "employees_seq", allocationSize = 1)
    @Column(name = "employee_id")
    private Long id;
      @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;
    
    @Column(name = "job_id", nullable = false)
    private String jobId;
    
    @Column(name = "salary")
    private BigDecimal salary;
    
    @Column(name = "commission_pct")
    private BigDecimal commissionPct;
    
    @Column(name = "manager_id")
    private Long managerId;
    
    @Column(name = "department_id")
    private Long departmentId;
}
