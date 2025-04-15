-- Database schema creation script
-- Run as SYS or other admin user with schema creation privileges

-- Create sequences
CREATE SEQUENCE EMPLOYEES_SEQ
    START WITH 1000
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;
    
CREATE SEQUENCE DEPARTMENTS_SEQ
    START WITH 100
    INCREMENT BY 10
    NOCACHE
    NOCYCLE;

-- Create tables
CREATE TABLE DEPARTMENTS (
    DEPARTMENT_ID   NUMBER(4) PRIMARY KEY,
    DEPARTMENT_NAME VARCHAR2(30) NOT NULL,
    MANAGER_ID      NUMBER(6),
    LOCATION_ID     NUMBER(4)
);

CREATE TABLE EMPLOYEES (
    EMPLOYEE_ID     NUMBER(6) PRIMARY KEY,
    FIRST_NAME      VARCHAR2(20),
    LAST_NAME       VARCHAR2(25) NOT NULL,
    EMAIL           VARCHAR2(25) NOT NULL UNIQUE,
    PHONE_NUMBER    VARCHAR2(20),
    HIRE_DATE       DATE NOT NULL,
    JOB_ID          VARCHAR2(10) NOT NULL,
    SALARY          NUMBER(8,2),
    COMMISSION_PCT  NUMBER(2,2),
    MANAGER_ID      NUMBER(6),
    DEPARTMENT_ID   NUMBER(4),
    CONSTRAINT EMP_DEPT_FK FOREIGN KEY (DEPARTMENT_ID) REFERENCES DEPARTMENTS (DEPARTMENT_ID)
);

-- Create indexes for better query performance
CREATE INDEX EMP_DEPARTMENT_IX ON EMPLOYEES (DEPARTMENT_ID);
CREATE INDEX EMP_JOB_IX ON EMPLOYEES (JOB_ID);
CREATE INDEX EMP_MANAGER_IX ON EMPLOYEES (MANAGER_ID);
CREATE INDEX EMP_NAME_IX ON EMPLOYEES (LAST_NAME, FIRST_NAME);

-- Create a view for common queries
CREATE OR REPLACE VIEW EMP_DETAILS_VIEW AS
SELECT
    e.EMPLOYEE_ID,
    e.JOB_ID,
    e.MANAGER_ID,
    e.DEPARTMENT_ID,
    d.LOCATION_ID,
    e.FIRST_NAME,
    e.LAST_NAME,
    e.SALARY,
    e.COMMISSION_PCT,
    d.DEPARTMENT_NAME
FROM
    EMPLOYEES e,
    DEPARTMENTS d
WHERE e.DEPARTMENT_ID = d.DEPARTMENT_ID;

-- Create a stored procedure for employee salary update
CREATE OR REPLACE PROCEDURE update_employee_salary(
    p_employee_id IN EMPLOYEES.EMPLOYEE_ID%TYPE,
    p_percent IN NUMBER
) IS
BEGIN
    UPDATE EMPLOYEES
    SET SALARY = SALARY * (1 + p_percent/100)
    WHERE EMPLOYEE_ID = p_employee_id;
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END update_employee_salary;
/

-- Create a function to calculate employee annual compensation
CREATE OR REPLACE FUNCTION calculate_compensation(
    p_salary IN EMPLOYEES.SALARY%TYPE,
    p_commission_pct IN EMPLOYEES.COMMISSION_PCT%TYPE
) RETURN NUMBER IS
    v_total_comp NUMBER;
BEGIN
    v_total_comp := p_salary * 12;
    IF p_commission_pct IS NOT NULL THEN
        v_total_comp := v_total_comp + (p_salary * p_commission_pct * 12);
    END IF;
    RETURN v_total_comp;
END calculate_compensation;
/
