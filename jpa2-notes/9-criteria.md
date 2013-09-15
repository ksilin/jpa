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


