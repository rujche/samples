# PostgreSQL Spring Boot Integration Demo

This project demonstrates various ways to integrate PostgreSQL Database with Spring Boot 3.4.4. It showcases different methods of database interaction and PostgreSQL-specific features.

## Project Overview

This is a Spring Boot application demonstrating comprehensive PostgreSQL Database integration using:
- Spring Data JPA
- MyBatis XML mapping
- JDBC Template
- Native SQL queries
- PostgreSQL-specific features (PL/pgSQL functions, recursive queries, INSERT ON CONFLICT statements, etc.)

## Features

### 1. JPA Repository with @Query
- JPQL and native SQL queries
- PostgreSQL-specific functions and syntax

### 2. MyBatis XML SQL Mapping
- XML-based SQL mapping
- ResultMap configurations
- Dynamic SQL generation
- PostgreSQL pagination using LIMIT/OFFSET

### 3. JDBC Template Direct SQL Access
- Direct SQL execution
- PostgreSQL analytical functions
- Named parameters
- Function calls

### 4. Database Schema
- SQL scripts for schema creation
- Functions in PL/pgSQL
- Sample data with PostgreSQL INSERT ON CONFLICT and recursive queries

## Prerequisites

- JDK 21+
- Maven 3.8+
- PostgreSQL Database (accessible via the connection details in application.yml)

## Getting Started

### 1. Configure Database Connection

Update the database connection details in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver
```

### 2. Build the Application

```powershell
mvn clean package
```

### 3. Run the Application

```powershell
mvn spring-boot:run
```

Or run the JAR file:

```powershell
java -jar target/postgresql-spring-demo-0.0.1-SNAPSHOT.jar
```

## API Endpoints

The application provides REST API endpoints to demonstrate PostgreSQL DB interactions:

- `GET /api/employees` - Get all employees (JPA)
- `GET /api/employees/department/{deptId}` - Get employees by department ID (JPA @Query)
- `GET /api/employees/salary-greater-than/{salary}` - Get employees with salary greater than specified amount (Native SQL)
- `GET /api/employees/department-name/{deptName}` - Get employees by department name (JPA with joins)
- `GET /api/employees/search/{namePattern}` - Search employees by name pattern (PostgreSQL LIKE with UPPER)
- `GET /api/employees/hired-between?startDate=X&endDate=Y` - Get employees hired between dates (PostgreSQL DATE functions)
- `GET /api/employees/high-salary/{minSalary}` - Get high salary employees (JDBC Template)
- `GET /api/employees/dept-salary-stats` - Get department salary statistics (PostgreSQL analytical functions)
- `PUT /api/employees/{employeeId}/update-salary/{percentIncrease}` - Update employee salary (PostgreSQL function)
- `GET /api/employees/hierarchy/{managerId}` - Get employee hierarchy (Recursive WITH query)
- `GET /api/employees/salary-categories` - Get employee salary categories (CASE expressions and subqueries)

## Project Structure

- `src/main/java/com/example/model` - Entity models
- `src/main/java/com/example/repository` - JPA repositories
- `src/main/java/com/example/mapper` - MyBatis mapper interfaces
- `src/main/java/com/example/service` - Service classes with JDBC examples
- `src/main/java/com/example/controller` - REST controllers
- `src/main/resources/db` - SQL scripts for schema and data
- `src/main/resources/mybatis/mapper` - MyBatis XML mapping files
