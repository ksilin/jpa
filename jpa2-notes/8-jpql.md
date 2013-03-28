JPQL
====

## Into

### Terminology
Queries fall into 4 categories

* select
* aggergate - variation of select, summarizing the select result
* update
* delete

Aggregate and select queries are sometimes called report queries. THe set of entities and embeddables the queries operate on are called "abstact persistence schema". 

Entities may be named using the `name` attribute of the `@Entity` annotation. The unqualified class name is the default entity name. Simple properties of entities are called "state fields", properties that are relations are called "association fields".

Queries are not case-sensitive except for entity and propery names - they must be written exactly as in the object.

## Select queries

```(sql)
SELECT
FROM
[WHERE]
[ORDER BY]
```
Important differnces to SQL - the domain is not the table but the entity.

In `SELECT e FROM Employee e`, the `e` (called alias in SQL) is called **identification variable**. Unlike SQL, the id var. is mandatory. The generated SQL from the above query may look like this : `SELECT id, name, salary, manager_id, dept_id, address_id FROM emp`

### Path expressions

The building blocks of queries. They are used to navigate along relationships or to properties. 

Navigations to a property is called a **state field path**.
Navigation to a singe entity is called  **single-valued association path**
Navigation to a collection of entities is called  **collection-valued association path** 

The navigation is performed using the dot(**.**) operator. The navigation stops at state field paths and collection-valued association paths. 

The keyword `OBJECT` has no impact on the query but may be used as a visual clue to signify that an entity is expected. Unfortunately, `OBJECT` is restricted to id vars (though e.department would return an entity, you cannot use `OBJECT(e.department)` - only `e`) and thus not very useful. It exists mainy for compatibilty reasons, as it was assumed that SQL would support it.

The `DISTINCT` operator is used to exclude duplicates:
`SELECT DISTINCT e.department FROM Employee e`

The result type of a select query may not include collection-valued paths:
`SELECT d.employees FROM Department d` is illegal **see BasicJpqlTest - it sems to be possible after all**. This restriction prevent the provider from combining successive DB rows to a single result (and why would that be bad?) -> SO

When a select query returns an embeddable , like in `SELECT e.address FROM Employee e`, it's important to note that the embeddable **will not be managed**. In order to obtain managed embeddables, retrieve the managed embedding entities and navigate from there.

### combining expressions (Projection queries)
### Ctor expressions
### Ingeritance and polymorphism 

Restricting queries to a particular class of a hierarchy:

`SELECT p FROM Project p WHERE TYPE(p)=DesignProject` -- there are no quotes around the type name, but string params can be used as params.

## FROM clause

Consists of id vars and join clause declarations. Every query must have at least one id var in the FROM clause, corresponding to an entity. When the id var is not a path but a single entity - e.g. `e` - it is called a **range variable declaration** (confusing, but it comes from the set theory). An optional `AS` keyword may be used. The id must follow standard java naming rules (case insensititve)

### Joins

Joins are queries combining results from ultiple entities. They occur whenever:

* more than one range var decls are listed in the `FROM` and appear in the `SELECT`
* `JOIN` operator is used to extend the entitiy through a path expression (how?)
* a path expr. navigates along an association field to the same or different entity. 
*  `WHERE` conditions compare attributes of different id vars.

Joins may be specified explicitly using the `JOIN` keyword, or implicitly as result of path navigation.

**Inner joins** return entites satisfying all join conditions. Path navigation from one entity to another is a form of inner join. An **outer join** is a set of all entities satisfying all join condtions plus all instances of one of the entities (called the **left** type). Absence of join conditions will produce a cartesian product (all possible combinations of the entities -> M*N results). Cartesian products are rarely used in JPQL. 

 More on the `JOIN` operator wiht examples: 217-222:
#### Inner joins
##### Join op. on collection association fields

`SELECT p FROM Employee e JOIN e.phones p` eqivalent to 
`SELECT p FROM Employee e, Phone p WHERE  e.emp_id = e.id`
 And once more using the old `IN()` operator from EJBQL:
`SELECT DISTINCT p FROM Employee e, IN(e.phones) p`

Of the above example, using the `JOIN` op is recommended.

##### Join op and single valued association fields

`SELECT d FROM Employee e JOIN e.department d` 
// what woudl be the difference to `SELECT d FROM Employee e JOIN Department d`? would hte association be used?
identical query w/o explicit joins: `SELECT e.department FROM Employee e`
identical query with join conditions in the `WHERE` clause: `SELECT d FROM Employee e, Department d WHERE d = e.department`


As we see, there is not much to gain in preferring explicit joins for single valued association fields to regular navigation. Path navigation is equivalent to inner joins. An exception are outer joins:

`SELECT DISTINCT e.department FROM Project p JOIN p.employees e WHERE p.name='Release1' AND e.address.state='CA'` 

Selecting the departments of all employees from California who work on the project Release1.
This query conatins 4 logical joins (and could involve even more tables if relations with join tables are involved). +p219

#### Join consitions in WHERE clause

These are often used if there is no explicit relation between the entities. E.g.: Selecting all departments and their managers (the employees who direct other employees):
`SELECT d, m FROM Department d, Employee m WHERE d=m.department AND m.manager IS NOT EMPTY`

#### Multiple joins

`SELECT DISTINCT p FROM Department d JOIN d.employees e JOIN e.projects p`

#### Map joins

Use `KEY` and `VALUE` operators to access the enties of a map. `VALUE` is the default. See p.221.
To access the whole entry, use the `ENTRY` keyword. It can be only used in `SELECT` clauses. `KEY` and `VALUE` can also be used in `WHERE` and `HAVING` clauses. JPA is not able to join the source entitiy against map values.

#### Outer joins p.221

#### Fetch joins

Used to optimize DB access and prepare query results to detachment. Fetch joins can be useed to enforce eager loading of lazily loading properties (assuming `address` is lazy):

`SELECT e FROM Employee e JOIN FETCH e.address`

This query will be replaced by the provider wiht a query with a regular join, where one of the returned entities would then be dropped:

` SELECT e, a FROM Employee e JOIN e.address a` 

Selecting all departments and eagerly fetching the employees requires an outer join, as departments with no employees should be returned as well (and would not be included due to a join).

`SELECT d, FROM Department d LEFT JOIN FETCH d.employees` would be turned by the provider into:
`SELECT d, e FROM Department d LEFT JOIN d.employees e` ...it' a bit more complicated in fact, as we would like to select disctinct departments, so there is no SQL equivalent to a fetch query, thats why the employees are fetched too and then dropped.
This "extra" work w´may result in a performance issue for large result sets. Due to this implications, if reations require eager fetching, consider marking them as such.

## WHERE clause

### Input params (already tried in ch.7)
### Basic expression form

Operator precedence:

Navigation/dot `.` > Unary `+` and `-` > `*` and `/` > `+` and `-` (add&subtract) > 
Comparison (<, >, = etc, BETWEEN, LIKE, IN, IS NULL, IS EMPTY, MEMBER, including the variants with `NOT`) > `AND`, `OR`, `NOT`

#### BETWEEN

`SELECT e FROM Employee e WHERE e.salary BETWEEN 40000 AND 50000` is identical to
 
`SELECT e FROM Employee e WHERE e.salary >= 40000 AND e.salary <= 50000`

#### LIKE

The legal wildcards are `_` for a single char and `%` for any number of chars. In cases when the undescore and the percent sign should be trated literally, the `ESCAPE` keyword can be used to signify the escape character:

E.g.: the `\` is the escape character.

`WHERE d.name LIKE 'QA\_% ESCAPE '\'`

#### Subqueries

Subqueries are complete queries (in parens) that are embedded in a conditional expression.

`SELECT e FROM Employee e WHERE e.salary = (SELECT MAX(emp.salary) FROM Employee emp)`

The scope of an id var extends to all subqueries. By defining an id var, a subquery can override the id var of the same name from the main query. However this is not required to be implemented by the provider, so it' better not to rely on such overrriding.

Get all employees whi have a cell phone:
` SELECT e FROM Employee e WHERE EXISTS (SELECT 1 FROM Phone p.employee = p AND p.type = 'cell')`

This query also illustrates an interesting technique. When we are only interested in existance of sth, and are using EXISTS, returning a literal `1` is common to signify that the actual results of the subquery are not relevant.

This query could have been written using joins and the `DISTINCT` operator. Often, when a join is used for iltering, there is an equivalent subquery condition.

` SELECT e FROM Employee e WHERE EXISTS (SELECT 1 FROM e.phones p WHERE p.type = 'cell')`
 
#### IN expressions

Used to check if a single valued path expression is member of a collection. The collection is either literal or the result of a subquery. -> p.226

#### Collection expressions

##### IS EMPTY

`IS EMPTY` is the counterpart of `IS NULL` for collections.

`SELECT e FROM Employee e WHERE NOT EMPTY e.directs` equivalent:
`SELECT m FROM Employee m WHERE (SELECT COUNT(e) FROM Employee e WHERE e.manager = m) > 0`

##### MEMBER OF

`SELECT e FROM Employee e WHERE :project MEMBER OF e.projects` equivalent:
`SELECT e FROM Employee e WHERE :project IN (SELECT p FROM e.projects p)`

##### EXISTS

##### ANY, ALL, SOME

Compares WHERE clause to results of a subquery. ANY and SOME are aliases - identical.

// finding a manager who has subordinates commanding a larger salary then himself
`SELECT e FROM Employee e WHERE e.directs IS NOT EMPTY AND e.salary < ANY (SELECT d.salary FROM e.directs d)`

### Scalar expressions

Basically anything that results on a single scalar value. Subqueries resulting in single scalar values may be used only in the WHERE clause, never in the SELECT.

#### Literals

Single quotes are used to demarcate string literals. Boolean literals are `TRUE` and `FALSE`. Nubers can be expressed similar to the regular java syntax.

Queries also can reference `Enum` types. `... WHERE e.type = com.example.EmployeeType.FULLTIME`

Temporal literals use the JDBC escape syntax. `d` is for date, `t` is for time, `ts` - for timestamp:

`{d '2009-11-05}`
`{t '12-45-52'}`

`{ts '2009-11-05 12-45-52.325'}` . The millisecond part is optional.

#### Function expressions - p.230

ABS(num)
LENGTH(string)
LOCATE(string1, string2, [start])
LOWER(string)
SIZE(collection)
SUBSTRING(string, start, end)

and many more

#### CASE expressions

Conditional logic for queries. Case expressions are often used to transform data in report queries.

There are 4 forms of the CASE expressions.

1. The most general (all other forms can be expressed as a general case expression)
`CASE {WHEN <condition> THEN <scalar> }+ ELSE <scalar> END`

The condition variable is defined in each WHEN clause:

`SELECT p.name, CASE WHEN TYPE(p) = DesignProject THEN 'Development'...`

2. the second type, in general form:
`CASE <value> {WHEN <scalar_expr1> THEN <scalar_expr2>}+ ELSE <scalar_expr> END`

the condition variable is defined for all WHEN clauses:
`SELECT p.name, CASE TYPE(p) WHEN DesignProject THEN 'Development'...`

3. COALESCE
`COALESCE(<scalar_expr> {,<scalar_expr>}+)`

The first expression to return a non-null value becomes the result of the expression.

Here we actually want a name, but if the name is not defined, we use the id as fallback.
`SELECT COALESCE(d.name, d.id) FROM Department d`

4. NULLIF

`NULLIF(<scalar_expr1>, <scalar_expr2>)`

It accepts two scalar expressions and resolves both. If the results are equal, the result of the expression is null. Otherwise it is the result of the first expression.

NULLIF can be used to exclude results (see p.233)

#### ORDER BY (p.233)

Default sort order is ASC

Sorting by a single field:
`SELECT e FROM Employee e ORDER BY e.name DESC`

Sorting by multiple fields:
`SELECT e FROM Employee e ORDER BY e.salary DESC, e.name DESC`

Note the DESC keyword, on e.salary. If we had separated the ORDER BY arguments by a comma, ASC order would be used per default.

Using aliases for better performance (the variables have to be resolved only once)
`SELECT e.name, e.salary * 0.05 AS bonus FROM Employee e JOIN e.department d ORDER BY bonus DESC`

Sorting by attributes that are not accessible from the SELECT clause is supposed to be illegal, but it does work for some reason - TODO - clarify on SO

`SELECT e.name FROM Employee e ORDER BY e.salary DESC`

#### Aggregate queries

Aggregate queries are the ones containing either an aggregate function or a GROUP BY clause and/or a HAVING clause

```sql
    SELECT <select_expression> 
    FROM <from_clause>
    [WHERE <conditional_expression>]
    [GROUP BY <group_by_clause>]
    [HAVING <conditional_expression>]
    [ORDER BY <order_by_clause>]
```

There are 5 aggregate functions: 

**AVG, MIN, MAX, COUNT, SUM**

All employees are in the single group that defines the range of the aggregate query:

`SELECT AVG(e.salary) FROM Employee e`

`SELECT d.name, AVG(e.salary) FROM Department d, Employee e GROUP BY d.name`

A complex query is executed in multiple steps:

```sql
SELECT d.name, AVG(e.salary)
FROM Department d JOIN d.employees e
WHERE e.directs IS EMPTY
GROUP BY d.name
HAVING AVG(e.salary) > 50000
```

is executed like this: first the "vanilla" select part is executed:

```sql
SELECT d.name, AVG(e.salary)
FROM Department d JOIN d.employees e
WHERE e.directs IS EMPTY
```

then the results are iterated over and the grouping occurs, which passes the data to the AVG function. The result of the AVG is then passed to the HAVING clause.


#### GROUP BY clause p.236

Determines the aggregetion/breaking down of results.

//TODO - The book says, you cannot group by what is not included in the select clause, but it works. clarify on SO

<Because there are two grouping expressions, the department name and employee salary must be listed in both the SELECT clause and GROUP BY clause>

#### HAVING clause p.236

Defines a filter to be applied to the grouped results - effectively a secondary WHERE clause. Contrary to the original WHERE clause, the argumants are limited by singlevalused associations listed in the GROUP BY clause....it seems Im misunderstanding this restriction - the query below works just fine (adn is atually listen in the book) but p is not listed in the GROUP BY clause:

```sql
SELECT e, COUNT(p)
FROM Employee e JOIN e.projects p
GROUP BY e
HAVING COUNT(p) >= 2
```
#### Update & Delete queries - nothing special p.237-238 

Delete queries are polymorphic - when deleting a base class, the entites of derived classe are also affected.
Delete queries do not honor cascading - they only delete their target entity, but no associations. 




