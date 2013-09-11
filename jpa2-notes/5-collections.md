# Chapter 5 - Collection Mapping (p. 107)

Collections of Entities, Embeddables and basic types are supported.
Collections of Embeddables and basic types are not relationships, but **element collections**
The difference is in the used annotations: element collections use the `@ElementCollection` annotations

`@ElementCollection` can define the `targetClass` (the class of the collection elements) and the `fetch` (`LAZY` or `EAGER`) attributes.

To store the collections, a separate table is required. Each collection table must have a join column referring to it's entity table

the `@CollectionTable` annotation can be used to customize the mapping - p.108 -110

`Collection`, `Set`, `List` and `Map` can be used. The access to these fields should only be made using the according interface - because the provider can exchange the actual implementation with a different one when the entity is managed.

As ordering of rows inside the database is not commonly defined, we can use the `@OrderBy` annotation to ensure ordering of `List`s

The attribute is a comma-separated list of properties, followed by `ASC` or `DESC`. `ASC` is the default. Nested properties are allowed, like `employee.name`

```
    @OrderBy("name DESC, salary ASC")
    List<Employees> employees;
```

If the relationship is an entity, `@OrderBy` without attributes will order teh collection by the PKs of the listed entities.

If the relationship is an Embeddable, the order will be defined by the order of retrieving the rows from DB (not really helpful)

Reordering the `List`s in memory will not result in a reordering in the DB. The order for sequential reads of the enity will be defined by the `@OrderBy` annotation. In general, it's a good idea not to change the initial ordering in memory.

A second option to order `List`s is to order them persistently - with an order column in the database. This is used if the entities do not have a sensible ordering property - i.e. if the ordering is external.

For this, `@OrderColumn` should be used. When the `List` is rearranged in the managed entity, the new order is persistent. This column is transparent to the user.

```
    @OneToMany(mappedBy="queue")
    @OrderColumn(name="PRINT_ORDER")//the default would be JOBS_ORDER
    private List<PrintJob> jobs;
```

You can use **either** `OrderColumn` **or** `OrderedBy`, not both.

Though the order column is contained in the table of the `PrintJob` (owning side), it is defined on the Collection attribute of the inverse side (p. 114)

Having a persistently ordered List may result in significant costs for keeping the List in sync - for each entity added to the list of size N, N additional queries must be emitted.

### Maps

Entities, embeddables and basic types can be used both as keys and as values.

When used as keys, the types must be `Comparable` and implement `hashCode` and `equals` properly. The instances should also be unique, at least for the map in question. While the object is acting as a key in a `Map`, the properties relevant to it's identity (tho ones used in `hashCode` and `equals`) must not be changed. 

When keys are entities, only the FKs are stored in the referred table. If the keys are embeddables or basics, they are stored inside the referrred table (either an entity table, or a collection table or a join table) 

The value type of the `Map` determines the mapping (entity or collection table).

---leaving the boring chapter for later - enjoy from page 116 - 128

### Best practices

* when using a `List`, do not excpect it to be ordered if no ordering is specified. Without an explicit ordering, thre are no guarantees for repeatable results.

* using an `OrderBy` is always be better than an order column. Use the column only if it is not possible to do otherwise.

* Maps can be hard to configure right. Once mastered, they can be very useful.

* It is most efficient to use an attribute of the target object as the key, when using a `Map`, preferably basic attributes.

* The identitiy of embedded objects is usually not defined, so avoid using them in a `Map`. Never use them as keys.

* Support for duplicate and NULL values in collections is not guaranteed, and even where possiblle, it is better not to use them for performance reasons. 




