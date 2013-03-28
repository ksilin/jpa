-- You can use this file to load seed data into the database using SQL statements
insert into Project (id, name, dtype) values (0, 'design project1', 'DP')
insert into Project (id, name, dtype) values (1, 'quality project1', 'QP')
insert into Project (id, name, dtype) values (2, 'design project2', 'DP')

insert into Department (id, name) values (0, 'The Hidden Department')
insert into Department (id, name) values (1, 'The Found Department')
insert into Department (id, name) values (2, 'The Very Useful Department')

insert into Employee (id, name, salary, dept_id, manager_id, zip, state) values (0, 'John Smith', 12345, 0, 0, '123', 'CA')
insert into Employee (id, name, salary, dept_id, manager_id, zip, state) values (1, 'Jack Smith', 55555, 1, 0, '222', 'NA')
insert into Employee (id, name, salary, dept_id, manager_id, zip, state) values (2, 'Bill Smith', 99999, 2, 1, '333', 'NY')

insert into Phone (id, number, type, employee_id) values (0, '12345', 'mobile', 1)
insert into Phone (id, number, type, employee_id) values (1, '22222', 'embedded', 2)
insert into Phone (id, number, type, employee_id) values (2, '333333', 'pocket', 0)
insert into Phone (id, number, type, employee_id) values (3, '444444', 'handy', 0)

insert into Emp_proj (emp_id, proj_id) values (0, 0)

insert into Emp_proj (emp_id, proj_id) values (1, 0)
insert into Emp_proj (emp_id, proj_id) values (1, 1)
insert into Emp_proj (emp_id, proj_id) values (1, 2)
-- no projects for bill
