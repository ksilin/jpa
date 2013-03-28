-- You can use this file to load seed data into the database using SQL statements
insert into BaseEmployee (id, name, salary, startDate) values (0, 'John Smith', 12345, '2012-05-19 12:00:00')
insert into BaseEmployee (id, name, salary, startDate) values (1, 'Jack Smith', 55555, '2012-05-19 12:00:00')
insert into BaseEmployee (id, name, salary, startDate) values (2, 'Bill Smith', 99999, '2012-05-19 12:00:00')

--persisiting an entitiy of the derived class
insert into FullTimeEmployee (id, name, salary, startDate, dailyRate, term pension) values (2, 'Bill Smith', 99999, '2012-05-19 12:00:00', 1, 1, 999)
