Criteria API
===========

#### simple example

JPQL:

`SELECT e FROM Employee e WHERE e.name = 'Jon Smith'`

Eqivalent query with the criteria API:

```java
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
        Root<Employee> emp = c.from(Employee.class);
        c.select(emp).where(cb.equal(emp.get("name"), "John Smith");
```

## Building Criteria API queries

### Creating a query definition

The CriteriaBuilder (**CB**), retrieved by calling `getCriteriaBuilder` on the EM is the gateway to the criteria API. It serves as the factory for the `CriteriaQuery` as well as for other components of the query, like conditional expressions.

* `createQuery(Class<T>)` - typed
* `createQuery()` - untyped - result type is `Object`
* `createTupleQuery()` - for projection queries, equivalent to `createQuery(Tuple.class)`

`Tuple` should be used whenever we need to return multiple objects from a query, packed in a single typed object.

A `CriteraQuery`, contrary to the JPQL `Query` cannot be executed. It is rather a query definition adn has to be passed to the EM using `createQuery` instead of a JPQL string.
A fully defined `CriteraQuery` is similar to the internal representation of a JPQL query after parsing.

### Basic structure

Corresponsing methods of the 6 possible JPQL clauses:

```
        SELECT: CriteriaQuery#select() / Subquery#select()
        FROM: AbstractQuery#from()
        WHERE: AbstractQuery#where()
        ORDER BY: CriteriaQuery#orderBy()
        GROUP BY: AbstractQuery#groupBy() 
        HAVING: AbstractQuery#having() 
```

### Criteria objects and mutability

The majority of objects created through the Criteria API (including almost all objects sreted via the CB) are immutable.

Only the query definition created by CB are mutable, as they are intended to. Here, some caution is advised, as multiple invocation of mathods may have one of the 2 possible effects:

* when invoking the same method like `select` twice, the parameters of the second call will override the ones of the first call. 

* when invoking `from` twice, the second root is added to the query.


## Query roots and path expressions

### Query roots

In JPQL, the id var is central in tying differents parts of thethe  query together. With Criteria API, we dont have aliases.

The method `from` of `AbstractQuery` accepty an entity type and returns (and adds to the query) a **root**. Roots correspond to the id vars of JPQL, which in turn corresponds to a range var declaration or a join expression. 

        CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
        Root<Employee> emp = c.from(Employee.class);

**Root -> From -> Path -> Expression -> Selection**

The `From` interface exposes join functionality. Each call to `from` adds a new root to the query, resulting in a cartesian product, if no further constraints are applied in teh WHERE clause.

        SELECT DISTINCT d 
        FROM Department d, Employee e
        WHERE d = e.department

        CriteriaQuery<Department> c = cb.createQuery(Department.class);
        Root<Department> dept = c.from(Department.class);
        Root<Employee> emp = c.from(Employee.class);
        c.select(emp).distinct(true).where(cb.equal(dept, emp.get("department")));
 
### Path expressions

Roots are just a special case of path expressions. 

        SELECT e FROM Employee e
        WHERE e.addresss.city = 'New York'

The `get` method is dervied from the `Path` interface and is equivalent to the dot operator in JPQL. `get` returns a `Path` object and thus calls to  `get` can be chained.

        CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
        Root<Employee> emp = c.from(Employee.class);
        c.select(emp).where(cb.equal(emp.get("address").get("city"), "New York"));

Like JPQL, path expressions can be used in different clauses of the query. Always get hold of the root in a local variable and use it to form path expressions. Do not try to 'recreate' roots by repeatedly calling `from`.

## SELECT

### Selecting single expressions

All forms of the SELECT clause cam be represented by using `CriteriaQuery#select(Selection s)`, although convenience methods exist. `Selection` is extended by `Expression` an `CompoundSelection` for tuples. This means that instead of passing the `Root` as argumetn to `select`, we can also pass an `Expression` like 

`emp.<String>get("name")`

Note the unnusual sytax, spcifying the return type of the `get` method. The type of the expression passed to `select` must be compatible with the result type of the query object. When the return type of a method can not be determined automatically, we can prefix the method call with the return type like above. We need ot do it as the signature of `select` is:

        CriteriaQuery<T> select(Selection<? extends T> selection);

The `get` method returns a `Path<Object>` instance, because the compiler cannot infer the type based on teh attribute name. So, to declare that the attribute is really f type `String`, we need to qualify the call. This syntax must be used wherever a `Path`  is used as the argument of `select` or some other CB expression methods. You do not need to qualify a call to `equal` for example, because the return type is `Expressison<?>` and thus compatible to `Path<Object>`.

### Selecting multiple expressions

If the result is a `Tuple`, we must pass a `CompondSelection<Tuple>` to `select`. Create one with `CB#tuple`.

        CriteriaQuery<Tuple> c = cb.createTupleQuery();
        Root<Employee> emp = c.from(Employee.class);
        c.select(cb.tuple(emp.get("name"), emp.get("id"))); 

or

        c.multiselect(emp.get("name"), emp.get("id"));

If the result is a non-persistent class that will be created using a constuctor expression, we must use `CompoundSelection<[T]>`. Create one with `CB#construct`.

        CriteriaQuery<EmployeeInfo> c = cb.createQuery(EmployeeInfo.class);
        Root<Employee> emp = c.from(Employee.class);
        c.select(cb.construct(EmployeeInfo.class, emp.get("name"), emp.get("id")));

or

        c.multiselect(emp.get("name"), emp.get("id"));

`multiselect` will determine the appropriate ctor of the result type.

If the the result type is an `Array`,we must use `CompoundSelection<Object[]>`. Create one with `CB#array`.

        CriteriaQuery<Object[]> c = cb.createQuery(Object[].class);
        Root<Employee> emp = c.from(Employee.class);
        c.multiselect(emp.get("name"), emp.get("id"));

Here, `Object` can be used instead of `Object[]`. If `multiselect` is called with multiple arguments, the resulting object must be cast to `Object[]` to access values.


In some cases you have to use `cb.construct` explicitly - e.g. when the result is of type `Object[]` and the constructor expression utilizes only part of the result:

        CriteriaQuery<Object[]> c = cb.createQuery(Object[].class);
        Root<Employee> emp = c.from(Employee.class);
        c.select(emp.get("department"), 
        cb.construct(EmployeeInfo.class, emp.get("name"), emp.get("id")));

### Using aliases

`Selection#alias` 

Aliases are only useful with the Criteria API when the result is of type `Tuple`. The alias will be availble through the resulting `Tuple`.

        c.multiselect(emp.get("id"), emp.get("name").alias("fullname"));

It is invalid to set the alias of a `Selection` object more than once.

        TypedQuery<Tuple> q = em.createQuery(c);
        for(Tuple t: q.getResultList){
          String id = t.get("id", String.class);
          String name = t.get("fullName", String.class);
          ..

## FROM

### Inner and outer joins

 `From#join(Path, [JoinType])`

`Root` and `Join` both extend `From`, meaning that any root may join and joins can be chained. 

`JoinType.INNER` and `JoinType.LEFT` (outer join). `JoinType.RIGHT` is not required to be implemented by the provider and is thus unportable.

Join is additive. Each call to `join` adds another join to the query. As `Join` extends `Path` it can be used like a `Root`. 

        Join<Employee, Project> project = emp.join("projects", JoinType.LEFT);

Multiple joins can be associated with the same `From` instance.

        Join<Employee, Employee> directs = emp.join("directs");
        Join<Employee, Project> projects = directs.join("projects");
        Join<Employee, Employee> dept = directs.join("dept"); 

Cascading joins:

        Join<Employee, Project> project = dept.join("employees").join("projects");

The result type will be the source and target of the last join statement.

Joins across relationships using `Map` are more complex. `MapJoin#key` and `MapJoin#value`.

        SELECT e.name, KEY(p), VALUE(p)
        FROM Employee e JOIN e.phones p

        `MapJoin<SourceType, KeyType, ValueType>`

        CriteriaQuery<Object> c = cb.createQuery();
        Root<Employee> emp = c.from(Employee.class);
        MapJoin<Employee, String, Phone> phone = emp.joinMap("phones");
        c.multiselect(emp.get("name"), phone.key(), phone.value());

We cannot use `join` and have to use `mapJoin` instead because we only pass the name of the attribute (in this case, 'phones') as a String.

`Collection`, `Set` and `List` are required to use `joinCollection`, `joinSet` and `joinList` methods respectively.

Ther is also a strongly typed version of `join` that is able to handle all joins through a single method -> later.

### Fetch joins

`FetchParent#fetch`

        SELECT e FROM Employee e JOIN FETCH e.address

        CriteriaQuery<Employee>c = cb.createQuery(EMployee.class);
        Root<Employee> emp = c.from(Employee.class);
        emp.fetch("address");
        c.select;

The return type of `fetch` is a `Fetch` object, which is not a `Path` and can not be extended or referenced anywhere else in the query.

        emp.fetch("phones", JoinType.LEFT);
        c.select(emp).distinct(true);

Here we use an outer join to prevent the query to skip emplyoees without phones.

## WHERE

`AbstractQuery#where` accepts 0 or more `Predicate` objects or a single `Expression<Boolean>` object. Each call to `ehere` will discard previous where expressions.

### Building expressions

see list pp.254-256

### Predicates 

to create a conjuction `Predicate` objects, pass an `Predicate[]` to the `and` method:

        c.where(cb.and(predicates.toArray(new Predicate[0])));

there is a shortcut for that - passing multiple arguments to `where` - they are combined using `and`:

        CriteriaQuery#where(Predicate... restrictions) 

A combined `predicate` can also be built incrementally, using the `CB#conjunction` and `CB#disjunction` methods initially:

        Predicate p = cb.conjunction();
        ParameterExpression<String> ex = cb.parameter(String.class, "name");
        p = cb.and(p, cb.equal(root.get("name"), p));
        ...
        if(p.getExpressions().size() == 0){
            throw ...        
        }


