-- You can use this file to load seed data into the database using SQL statements
insert into Project (id) values (0)
insert into Project (id) values (1)
insert into Project (id) values (2)

insert into ProjectEmployee (id, name, salary) values (0, 'John Smith', 12345)
insert into ProjectEmployee (id, name, salary) values (1, 'Jack Smith', 12345)
insert into ProjectEmployee (id, name, salary) values (2, 'Bill Smith', 12345)

insert into Emp_proj (emp_id, proj_id) values (0, 0)

insert into Emp_proj (emp_id, proj_id) values (1, 0)
insert into Emp_proj (emp_id, proj_id) values (1, 1)
insert into Emp_proj (emp_id, proj_id) values (1, 2)

-- no projects for bill
