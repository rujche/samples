-- Database schema creation script for PostgreSQL

-- Create sequences
CREATE SEQUENCE employees_seq
    START WITH 1000
    INCREMENT BY 1
    NO CYCLE;
    
CREATE SEQUENCE departments_seq
    START WITH 100
    INCREMENT BY 10
    NO CYCLE;

-- Create tables
CREATE TABLE departments (
    department_id    INTEGER PRIMARY KEY,
    department_name  VARCHAR(30) NOT NULL,
    manager_id       INTEGER,
    location_id      INTEGER
);

CREATE TABLE employees (
    employee_id      INTEGER PRIMARY KEY,
    first_name       VARCHAR(20),
    last_name        VARCHAR(25) NOT NULL,
    email            VARCHAR(25) NOT NULL UNIQUE,
    phone_number     VARCHAR(20),
    hire_date        DATE NOT NULL,
    job_id           VARCHAR(10) NOT NULL,
    salary           NUMERIC(8,2),
    commission_pct   NUMERIC(2,2),
    manager_id       INTEGER,
    department_id    INTEGER,
    CONSTRAINT emp_dept_fk FOREIGN KEY (department_id) REFERENCES departments (department_id)
);

-- Create indexes for better query performance
CREATE INDEX emp_department_ix ON employees (department_id);
CREATE INDEX emp_job_ix ON employees (job_id);
CREATE INDEX emp_manager_ix ON employees (manager_id);
CREATE INDEX emp_name_ix ON employees (last_name, first_name);

-- Create a view for common queries
CREATE OR REPLACE VIEW emp_details_view AS
SELECT
    e.employee_id,
    e.job_id,
    e.manager_id,
    e.department_id,
    d.location_id,
    e.first_name,
    e.last_name,
    e.salary,
    e.commission_pct,
    d.department_name
FROM
    employees e,
    departments d
WHERE e.department_id = d.department_id;

-- Create a stored procedure for employee salary update
CREATE OR REPLACE FUNCTION update_employee_salary(
    p_employee_id INTEGER,
    p_percent NUMERIC
) RETURNS VOID AS $$
BEGIN
    UPDATE employees
    SET salary = salary * (1 + p_percent/100)
    WHERE employee_id = p_employee_id;
EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END;
$$ LANGUAGE plpgsql;

-- Create a function to calculate employee annual compensation
CREATE OR REPLACE FUNCTION calculate_compensation(
    p_salary NUMERIC,
    p_commission_pct NUMERIC
) RETURNS NUMERIC AS $$
DECLARE
    v_total_comp NUMERIC;
BEGIN
    v_total_comp := p_salary * 12;
    IF p_commission_pct IS NOT NULL THEN
        v_total_comp := v_total_comp + (p_salary * p_commission_pct * 12);
    END IF;
    RETURN v_total_comp;
END;
$$ LANGUAGE plpgsql;
