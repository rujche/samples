package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "departments")
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEPT_SEQ")
    @SequenceGenerator(name = "DEPT_SEQ", sequenceName = "departments_seq", allocationSize = 1)
    @Column(name = "department_id")
    private Long id;
    
    @Column(name = "department_name", nullable = false)
    private String name;
    
    @Column(name = "manager_id")
    private Long managerId;
    
    @Column(name = "location_id")
    private Long locationId;
}
