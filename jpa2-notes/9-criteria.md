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

A `CriteraQuery`, contrary to the JPQL `Query` cannot be executed. It is rather a query definition and has to be passed to the EM using `createQuery` instead of a JPQL string.
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

The majority of objects created through the Criteria API (including almost all objects created via the CB) are immutable.

Only the query definitions created by CB are mutable, as they are intended to. Here, some caution is advised, as multiple invocation of methods may have one of the 2 possible effects:

* when invoking the same method like `select` twice, the parameters of the second call will override the ones of the first call. 

* when invoking `from` twice, the second root is added to the query (resulting in a join)


## Query roots and path expressions

### Query roots

In JPQL, the id var is central in tying different parts of the  query together. With the Criteria API, we dont have aliases.

The method `from` of `AbstractQuery` accepts an entity type and returns (and adds to the query) a **root**. Roots correspond to the id vars of JPQL, which in turn correspond to a range var declaration or a join expression. 

        CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
        Root<Employee> emp = c.from(Employee.class);

**From -> Path -> Expression -> Selection**


        Root -\ 
               From
        Join -/


The `From` interface exposes join functionality. Each call to `from` adds a new root to the query, resulting in a cartesian product, if no further constraints are applied in the `WHERE`  clause.

        SELECT DISTINCT d 
        FROM Department d, Employee e
        WHERE d = e.department

        CriteriaQuery<Department> c = cb.createQuery(Department.class);
        Root<Department> dept = c.from(Department.class);
        Root<Employee> emp = c.from(Employee.class);
        c.select(dept).distinct(true).where(cb.equal(dept, emp.get("department")));
 
### Path expressions

Roots are (just) a special case of path expressions. 

        SELECT e FROM Employee e
        WHERE e.addresss.city = 'New York'

The `get` method is derived from the `Path` interface and is equivalent to the dot operator in JPQL. `get` returns a `Path` object and thus calls to  `get` can be chained.

        CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
        Root<Employee> emp = c.from(Employee.class);
        c.select(emp).where(cb.equal(emp.get("address").get("city"), "New York"));

Like JPQL, path expressions can be used in different clauses of the query. Always get hold of the root in a local variable and use it to form path expressions. Do not try to 'recreate' roots by repeatedly calling `from`.

## SELECT

### Selecting single expressions

All forms of the SELECT clause cam be represented by using `CriteriaQuery#select(Selection s)`, although convenience methods exist. `Selection` is extended by `Expression` and `CompoundSelection` for tuples. This means that instead of passing the `Root` as argument to `select`, we can also pass an `Expression` like 

`emp.<String>get("name")`

Note the unusual syntax, spcifying the return type of the `get` method. The type of the expression passed to `select` must be compatible with the result type of the query object. When the return type of a method can not be determined automatically, we can prefix the method call with the return type like above. We need to do it as the signature of `select` is:

        CriteriaQuery<T> select(Selection<? extends T> selection);

The `get` method returns a `Path<Object>` instance, because the compiler cannot infer the type based on the attribute name. So, to declare that the attribute is really f type `String`, we need to qualify the call. This syntax must be used wherever a `Path`  is used as the argument of `select` or some other CB expression methods. You do not need to qualify a call to `equal` for example, because the return type is `Expressison<?>` and thus compatible to `Path<Object>`.

### Selecting multiple expressions

If the result is a `Tuple`, we must pass a `CompoundSelection<Tuple>` to `select`. Create one with `CB#tuple`.

        CriteriaQuery<Tuple> c = cb.createTupleQuery();
        Root<Employee> emp = c.from(Employee.class);
        c.select(cb.tuple(emp.get("name"), emp.get("id"))); 

or (equivalent)

        c.multiselect(emp.get("name"), emp.get("id"));

If the result is a non-persistent class that will be created using a constuctor expression, we must use `CompoundSelection<[T]>`. Create one with `CB#construct`.

        CriteriaQuery<EmployeeInfo> c = cb.createQuery(EmployeeInfo.class);
        Root<Employee> emp = c.from(Employee.class);
        c.select(cb.construct(EmployeeInfo.class, emp.get("name"), emp.get("id")));

or (equivalent)

        c.multiselect(emp.get("name"), emp.get("id"));

`multiselect` will determine the appropriate ctor of the result type.

If the the result type is an `Array`,we must use `CompoundSelection<Object[]>`. Create one with `CB#array` (example pending).

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

There is also a strongly typed version of `join` that is able to handle all joins through a single method -> later.

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

`AbstractQuery#where` accepts 0 or more `Predicate` objects or a single `Expression<Boolean>` object. Each call to `where` will discard previous `where` expressions.

### Building expressions

see list pp.254-256

### Predicates 

to create a conjunction `Predicate` objects, pass an `Predicate[]` to the `and` method:

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


There are two sets of methods for numeric comparison. For example, `greaterThan` and `gt`. Two-letter forms are strongly typed to work only with number types. In all other cases, the long form must be used.

### Literals

In virtually all cases, criteria API can work with both `Expression` objects and and Java literals. Should you ever encounter a case, when only an `Expression` is allowed, use the `CB#literal` method to wrap the literals. `NULL` literals are created with the `CB#nullLiteral(MyClass.class)` method, that prodices a typed version of `NULL`. It's incredible how absurdly far the quest for strong typing can lead... 

### Parameters

**Predicate -> Expression -> ParameterExpression -> Parameter** 

In JPQL, parameters were simple prefixed by a semicolon. With criteria API, you will need a typed `ParameterExpression` -> `CB#parameter(MyClass.class, "name")`.

##### I am still not too sure why we would need to get hands on a Parameter.



### Subqueries

        AbstractQuery#subquery

Subqueries that reference a `Root`, `Path` or `Join` from the parent query are called "correlated" (making all others "non-correlated")

        Subquery<MyEntity> sq = query.subquery(MyEntity.class);

        SELECT e
        FROM Employee e
        WHERE e IN(SELECT emp
                   FROM Project p JOIN p.employees emp
                   WHERE p.name = :project)


criteria API:

        CritereiaQuery<Employee> q = cb.createQuery(Employee.class);
        Root<Employee> emp = q.from(Employee.class);
        c.select(emp);
        ...
        Subquery<Employee> sq = q.subquery(Employee.class);
        Root<Project> project = sq.from(Project.class);
        Join<Project, Employee> sqEmp = project.join("employees");
        sq.select(sqEmp)
        sq.where(cb.equal(project.get("name"), cb.parameter(String.class, "project")));
        criteria.add(cb.in(emp).value(sq));


The subquery above is non-correlated.

With subqueries there is often more than one wy to achieve a particular result. The same query, now correlated:

        SELECT e FROM Employee e
        WHERE EXISTS (SELECT p FROM Project p JOIN p.employees emp
                      WHERE emp = e AND p.name = :name)


criteria API:

        ...
        Subquery<Project> sq = q.subquery(Project.class);
        Root<Project> project = sq.from(Project.class); 
        Join<Project, Employee> sqEmp = project.join("employees");
        sq.select(project);
        sq.where(cb.equal(sqEmp, emp), 
                 cb.equal(project.get("name"), cb.parameter(String.class, "project")));
        criteria.add(cb.exists(sq));


Taking the example further - moving the reference to the `Employee` root to the `FROM` clause of the subquery and joining directly on the list of projects of the employee.

        SELECT e FROM Employee e
        WHERE EXISTS (SELECT p FROM e.projects p
                      WHERE p.ane = :name)

criteria API (using `Subquery#correlate`)

        Subquery<Project> sq = q.subquery(Project.class);
        Root<Project> sqEmp = sq.correlate(emp); 
        Join<Employee, Project> sqEmp = sqEmp.join("projects");
        sq.select(project);
        sq.where(cb.equal(project.get("name"), cb.parameter(String.class, "project")));
        criteria.add(cb.exists(sq));


One cornercase is referencing a join expression of the parent query in the FROM clause of the subquery. Finding all projects, with managers....

continued on p260

## Strongly types query definitions

### The Metamodel API (p265)

The metamodel of a PU is a descriptoin of the persistent type, state and relationsship of the entities, embeddables and managed classes.

        Metamodel mm = em.getMetaModel();
        EntityType<Employee> emp_ = mm.entity(Employee.class);

Use `mm.embeddable(MyEmbeddable.class)` and `mm.managedType(MyManagedType.class)` for non-entity classes.

The `EntityType` instance is not created by this call, only retrieved if it exists. The metamodel must be initialized by the provider when t o`EntityMenegerFactory` is created for the PU. If the class is not a persistent class, an `IllegalArgumentException` is thrown.


                                                              BasicType -\
                                            EmbeddableType-\              Type
                    MappedSuperclassType-\                  ManagedType -/
                                          IdentifyableType-/
                              EntityType-/

-

        EntityType<T> type = ...;
        for(Attribute<? super T, ?> attr : type.getAttributes()){ 
          syso(attr.getName() + 
               attr.getJavaType().getName() + 
               attr.getPersistentAttributeType());
        }

        id int BASIC
        name java.lang.String BASIC
        dept com.acme.PDepartment MANY_TO_ONE
        ...

The persistence metamodel has been available to earlier JPA version, but only internally.

### Strongly typed API overview

The strongly typed API is can be used well with the CB. For example, the `join` method also accepts `SingularAttribute`, `CollectionAttribute`, `SetAttribute`, `ListAttribute` and `MapAttribute`. We will no longer need the ugly type hints.Compare with the version usign `joinMap` from teh start of the chapter.

 



