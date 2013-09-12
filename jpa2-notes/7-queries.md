Using Queries
============

## Overview

JPQL is msotly similar to pure SQL - the first difference is that instead of selecting a table, we selct an entity:

    `SELECT e FROM Employee e`

#### . operator

We can navigate entity relationshis using the dot operator

    `SELECT e.name FROM Employee e`

We don't have to list an entity to navigate to it:
    
    `SELECT e.department FROM Employee e`

### Projections

We can retrieve a projection, using only portions of found data:
    
    `SELECT e.name, e.salary FROM Employee e`

//TODO : how do we work with projected results - how are they returned from a query?

### Filtering results 

using the WHERE clause:

    `SELECT e FROM Employee e WHERE e.name = "John Smith"`

#### Joins

Collection return types are illegal in JPQL. To return the collection we must use Joins (p. 181).

I have found collection queries to work, despite the warning - see `BasicQueriesTest.testCollectionQuery` in the jpa2-ch7-queries project.

Joins can be implicit and explicit. Explicit joins are used either for eager prefetching or for different types of joins (more in ch.8)

The default join type is the inner join.

#### Aggregate Queries

These five aggregate function are supported: `AVG`, `COUNT`, `MIN`, `MAX`, `SUM`. The results may be grouped using the `GROUP BY` and filtered with the `HAVING` clause.

```
        SELECT COUNT(e), MAX(e.salary), AVG(e.salary) FROM Department d JOIN d.employees e
        GROUP BY d HAVING COUNT(e) > 3
```
#### Query Params

There are 2 types of parameter binding syntax - positional and named:

```
        SELECT e FROM Employee e WHERE e.department = ?1 AND e.salary > ?2

        SELECT e FROM Employee e WHERE e.department = :dept AND e.salary > :base
```
## Defining Queries

`Query` & `TypedQuery`. `TypeQuery` extends `Query`. Queries are created using the 4 factory methods of the EM. Here we handle only the JPQL queries. SQL -> see ch.11, criteria -> see ch.9

2 Approaches to deifne an JPQL query: 

* dynamically specified at runtime
* configured in the PU metadata (annotation or XML) and referenced by name

Dynamic queries are simple strings, named queries are static and more efficient as they are precompiled only once.

### Dynamic Queries


    `em.createQuery(queryString);`

Translated queries are often cached. To exploit the caching, use parametrized queries in place of simple strings. If using simply concatenated queries, they will have to be translated again each time.

When concatentaing queries from user input, be aware that users could alter the queries maliciously.

Use parametrized queries to avoid such attacks. Here the quotes used in the parameter are escaped and the whole parameter is treated as a string - the malicious SQL will not be executed.

In general, static named queries are preferrable for queries that are executed often.

### Named Queries

```java
    @NamedQuery(name="findSalaryForNameAndDepartment", query="SELECT e.salary FROM employee e WHERE e.department.name = :deptName AND e.name = :empName")
```

Named queries are typically annotated on the entitiy class. Here string concatenation for formatting is acceptable because of it's low and one-time cost. The name of the query must be unique across the complete PU. A common practice is to prefix the queries with the name of the entity: `name=Employee.findSalaryByName`

For multiple queries, use the `@NamedQueries` annnotation:

```java
    @NamedQueries({@NamedQuery(...), @NamedQuery(...)})

    em.createNamedQuery("Employee.findByName", Employee.class).setParameter("name", name);
```

### Parameter types

Parameters are set using the `setParameter(name/number, value)`. `Date` and `Calendar` types require a third method parameter that specifies the type (`java.sql.Date` or `.Datetime` or `.Timesamp`).

Entity types may be used as parameters as well. When translating to SQL, the PK columns of the entities are used.

    setParameter("start", currentDate, TemporalType.DATE)

A Pparameter may be used several times in the query, but needs to be set only once. 

### Executing queries

3 ways to execute a query:

* `getSingleResult`
* `getResultList`
* `executeUpdate` - for bulk updates, no result is returned

If the query executed by `getResultList` does not find any results, the list is empty. The return type is specified as `List` to support sorting (using `ORDER BY`, queries are unordered by default).

If the query executed by `getSingleResult` finds no result, it throws a `NoResultExcepion`.
If multiple results are available, it will throw a `NonUniqueResultException`.

These exceptions, unlike other exceptions thrown by the EM, will not cause the TX to rollback.

Both `get*` queries may specify locking for the affected rows using the `setLockMode` method (more in ch. 11).

`Query` and `TypedQuery` objects may be reused as long as the same PC is active. For TX-scoped EM, the lifetime of a `Query` is limited to the TX. Other EM types may use the Query instances until the EM is closed or removed.

## Working with query results

The entities returned from a query are managed. The only exception is when using an EM outside any TX - then the entities are returned in detached state.

### Untyped results

If the query is not parametrized by the return type, `getResultList` will return an untyped `List` and `getSingleResult` will return an `Object`.

### Optimizing read-only queries

Using TX-scoped EM outside of TX for read-only queries may be more efficient. For managed entites, a copy is created to compare the initial state with the one on TX commit. In cases where the entities are detached immediately, the provider may be able to optimize this overhead away. This trick does not work for app-managed or extended EMs, as their PC is not discarded on TX commit. To disable transactions, use `TransactionAttribute.NOT_SUPPORTED`

### Special result types

When returning multiple results (projection and aggregate queries), the return type will be a `List` of `Object[]`. This can be used in **Constructor expressions** to create custom objects with the JPQL `NEW` operator. -> see tests

### Pagination

JPA supports pagination with the `setFirstResult` and `setMaxResults` methods of the `Query`. These values can be accessed via `getFirstResult` and `getMaxResults`.

Do not use pagination for queries that use joins across collection relationships (1-* and *-*) as these queris may return duplicate values. The duplicates make it impossible to use a position.
-> example?

### Queries and uncommited changes

Queries are executed directly on the DB, so the provider cannot participate and use the PC. If the PC has not been flushed, the query may return stale data.

This is not a concern when using the `find()` method - it always checks the PC first.

The provider will then attempt to make query results consistent, regardless of the flush state - either flushing the PC before the query or using the PC to modify the results. Ensuring the integrity is not easy - the provider cannot reliably determine which entities have changed. 

So if queries that consider changed entites are common, and the integrity strategy of the provider is to flush before the query, this might lead to a performance impact.

To control the integrity requirements of the queries, the flush mode can be set on the `Query` instance or on the EM. The **flush mode** tells the provider how to handle pending changes and queries.

`FlushModeType.AUTO/COMMIT`

`AUTO` is the default - the provider has to ensure that pending changes get included in the query results. If the query might involve data from the PC, the provider will ensure the correctness of the results.

`COMMIT` - nothing will be done to ensure the inclusion of the PC in the query results. Flush is not guaranteed.

The flush mode is set on the EM, but is rather a property of the PC. For TX-scoped EMs this means that the flush mode has to be set on each TX - for the current PC. Extended EMs will retain their flush setting.

When conflicted, the `Query` setting overrides the EM setting.

When executing queries where data is being changed, use `AUTO`. For performance reasons, you can change to `COMMIT` on some queries.

## Query timeouts

A TX timeout will cause the TX to be rolled back. For real query timeouts, after which the query will be aborted, but the TX will be fine, use the `javax.persistence,query.timeout` property. It can be set as a hint on a query or configured in the PU (see ch13.)

`query.setHint("javax.persistence.query.timeout", 5000);`

Using this hint is not portable - it may not be supported by the DB or the provider - it is not required to. It is further not guaranteed that the timeout will not lead to a TX rollback.

## Bulk update and delete

Bulk updates on entites are complex to implement for a provider and therefore, some restrictions apply.

Bulk updates are executed by calling `executeUpdate()` on the `Query`. They are directly translated to SQL and do not affect the PC.

The PC is not updated on execution, so only entities retrieved after the update will have the correct data.

Because of this, bulk updates need either to be the first operation in a TX or be executed in an own TX - in a method annotated with
`@TransactionAttribute(@TransactionAttributeType.REQUIRES_NEW)` - a new TX is the preferred method.

Usually, a bulk update causes the data related to the target entity to be invalidated (//TODO and detached?), but the range of invalidation may vary - a small bulk update may invalidate only a selection of in-memory entities.

DO not perform native SQL updates and deletions on tables mapped to entities. This can lead to stale in-memory cache data. The PC is not invalidated - only the provider cache.

If bulk updates happen after the relevant operations in a TX, the PC will have the priority and overwrite the data changed by the bulk updates (see p.202 example) 

Bulk updates and extended PCs don't play nice together - the provider will never refresh the PC to reflect the changes of the bulk update. For relevant locking and refreshing strategies - see ch.11

#### DELETE and FK integrity

Delete statements are applied to a set of entities. No cascading occurs. The persistence provider is further oblivious of FK relationships, so a delete violation of an FK relationship will result in `PersistenceException`.

To avoid it, update the entites with the FK first, then delete.

If the FK integrity were not enforced, it would result in a `PeristenceException` the next time the entity containing the FK would be retrieved.

### Query hints

Hints are the extension point of JPA - features not available or specified may be activated over the hints. There are also some standard features that can be manipulated over the hints - like the query timeout. Providers are required to ignore hints they don't understand.

### best practices

Use named queries where possible - they are almost always precompiled. They also enforce using parameters - this keeps the number of distinct strings parsed by the DB to a minimum. As most DB cache their queries, this will enhance performance. Parameters also improve security as they are escaped.

Prefix named queries with the entity name or use some other sort of namespace scheme.

#### report queries

For queries that will not change the retrieved entities, using a TX-scoped EM outside a TX is advised. The provider then may be able to optimize the query omitting some measures.

If you need only several properties of the entity, use projection queries - they are more efficient.

#### query hints

If you decide to use query hints, better place them in the xml config, or at least into named queris.

#### stateless session beans

SSB seems to be the best way to organize queries.

* Clients will be able to execute queries using business methods.
* Can optimize their TX usage
* using TX-scoped PCs keeps them lean

#### bulk update&delete

Should be executed in isolated TX, as they may negatively impact the current PC.

