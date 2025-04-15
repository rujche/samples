package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DEPARTMENTS")
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEPT_SEQ")
    @SequenceGenerator(name = "DEPT_SEQ", sequenceName = "DEPARTMENTS_SEQ", allocationSize = 1)
    @Column(name = "DEPARTMENT_ID")
    private Long id;
    
    @Column(name = "DEPARTMENT_NAME", nullable = false)
    private String name;
    
    @Column(name = "MANAGER_ID")
    private Long managerId;
    
    @Column(name = "LOCATION_ID")
    private Long locationId;
}
