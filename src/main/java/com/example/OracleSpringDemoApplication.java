package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.example.mapper") // For MyBatis mapper scanning
public class OracleSpringDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OracleSpringDemoApplication.class, args);
    }
}
