-- Sample data for PostgreSQL database demo

-- Insert departments
INSERT INTO departments (department_id, department_name, manager_id, location_id) VALUES (10, 'Administration', 200, 1700);
INSERT INTO departments (department_id, department_name, manager_id, location_id) VALUES (20, 'Marketing', 201, 1800);
INSERT INTO departments (department_id, department_name, manager_id, location_id) VALUES (30, 'Purchasing', 114, 1700);
INSERT INTO departments (department_id, department_name, manager_id, location_id) VALUES (40, 'Human Resources', 203, 2400);
INSERT INTO departments (department_id, department_name, manager_id, location_id) VALUES (50, 'Shipping', 121, 1500);
INSERT INTO departments (department_id, department_name, manager_id, location_id) VALUES (60, 'IT', 103, 1400);

-- Insert employees
INSERT INTO employees (employee_id, first_name, last_name, email, phone_number, hire_date, job_id, salary, commission_pct, manager_id, department_id)
VALUES (100, 'Steven', 'King', 'SKING', '515.123.4567', to_date('2003-06-17', 'YYYY-MM-DD'), 'AD_PRES', 24000, NULL, NULL, 90);

INSERT INTO employees (employee_id, first_name, last_name, email, phone_number, hire_date, job_id, salary, commission_pct, manager_id, department_id)
VALUES (101, 'Neena', 'Kochhar', 'NKOCHHAR', '515.123.4568', to_date('2005-09-21', 'YYYY-MM-DD'), 'AD_VP', 17000, NULL, 100, 90);

INSERT INTO employees (employee_id, first_name, last_name, email, phone_number, hire_date, job_id, salary, commission_pct, manager_id, department_id)
VALUES (102, 'Lex', 'De Haan', 'LDEHAAN', '515.123.4569', to_date('2001-01-13', 'YYYY-MM-DD'), 'AD_VP', 17000, NULL, 100, 90);

INSERT INTO employees (employee_id, first_name, last_name, email, phone_number, hire_date, job_id, salary, commission_pct, manager_id, department_id)
VALUES (103, 'Alexander', 'Hunold', 'AHUNOLD', '590.423.4567', to_date('2006-01-03', 'YYYY-MM-DD'), 'IT_PROG', 9000, NULL, 102, 60);

INSERT INTO employees (employee_id, first_name, last_name, email, phone_number, hire_date, job_id, salary, commission_pct, manager_id, department_id)
VALUES (104, 'Bruce', 'Ernst', 'BERNST', '590.423.4568', to_date('2007-05-21', 'YYYY-MM-DD'), 'IT_PROG', 6000, NULL, 103, 60);

INSERT INTO employees (employee_id, first_name, last_name, email, phone_number, hire_date, job_id, salary, commission_pct, manager_id, department_id)
VALUES (107, 'Diana', 'Lorentz', 'DLORENTZ', '590.423.5567', to_date('2008-02-07', 'YYYY-MM-DD'), 'IT_PROG', 4200, NULL, 103, 60);

INSERT INTO employees (employee_id, first_name, last_name, email, phone_number, hire_date, job_id, salary, commission_pct, manager_id, department_id)
VALUES (124, 'Kevin', 'Mourgos', 'KMOURGOS', '650.123.5234', to_date('2007-11-16', 'YYYY-MM-DD'), 'ST_MAN', 5800, NULL, 100, 50);

INSERT INTO employees (employee_id, first_name, last_name, email, phone_number, hire_date, job_id, salary, commission_pct, manager_id, department_id)
VALUES (141, 'Trenna', 'Rajs', 'TRAJS', '650.121.8009', to_date('2003-10-17', 'YYYY-MM-DD'), 'ST_CLERK', 3500, NULL, 124, 50);

-- Example of using PostgreSQL INSERT with ON CONFLICT (upsert)
INSERT INTO departments (department_id, department_name, manager_id, location_id)
VALUES (70, 'Public Relations', 204, 1700)
ON CONFLICT (department_id) 
DO UPDATE SET 
    department_name = EXCLUDED.department_name,
    manager_id = EXCLUDED.manager_id,
    location_id = EXCLUDED.location_id;

-- Example of PostgreSQL's recursive queries (replacement for Oracle's CONNECT BY)
-- This creates a log of the sample data load with timestamps
CREATE TABLE data_load_log (
    log_id SERIAL PRIMARY KEY,
    log_message VARCHAR(200),
    log_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    parent_log_id INTEGER
);

-- Log hierarchy example
INSERT INTO data_load_log (log_message, parent_log_id) VALUES ('Started data load process', NULL);
INSERT INTO data_load_log (log_message, parent_log_id) VALUES ('Loaded department data', 1);
INSERT INTO data_load_log (log_message, parent_log_id) VALUES ('Loaded employee data', 1);
INSERT INTO data_load_log (log_message, parent_log_id) VALUES ('Loaded IT department employees', 3);
INSERT INTO data_load_log (log_message, parent_log_id) VALUES ('Loaded HR department employees', 3);
INSERT INTO data_load_log (log_message, parent_log_id) VALUES ('Data load completed', 1);

-- Foreign data wrapper example (PostgreSQL's equivalent to Oracle's database links)
-- CREATE EXTENSION postgres_fdw;
-- CREATE SERVER remote_server FOREIGN DATA WRAPPER postgres_fdw
--   OPTIONS (host 'remote_host', port '5432', dbname 'remote_db');
-- CREATE USER MAPPING FOR local_user SERVER remote_server
--   OPTIONS (user 'remote_user', password 'remote_password');
