-- You can use this file to load seed data into the database using SQL statements


insert into Employee (id, name, salary, zip, state) values (0, 'John Smith', 12345, '123', 'CA')
insert into Employee (id, name, salary, zip, state) values (1, 'Jack Smith', 55555, '222', 'NA')
insert into Employee (id, name, salary, zip, state) values (2, 'Bill Smith', 99999, '333', 'NY')

insert into Phone (id, number, type) values (0, '12345', 'mobile')
insert into Phone (id, number, type) values (1, '22222', 'embedded')
insert into Phone (id, number, type) values (2, '333333', 'pocket')
insert into Phone (id, number, type) values (3, '444444', 'handy')

insert into EMP_PHONES (employee_id, phones_id) values (0, 0)
insert into EMP_PHONES (employee_id, phones_id) values (1, 1)
insert into EMP_PHONES (employee_id, phones_id) values (2, 2)
insert into EMP_PHONES (employee_id, phones_id) values (0, 3)

