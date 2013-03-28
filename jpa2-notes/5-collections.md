# Chapter 5 - Collection Mapping (p. 107)

Collections of Entites, Embeddables and basic types are supported.
Collections of Embeddables and basic types are not relationships, but **element collections**
The difference is in the used annotations: element collections use the **@ElementCollection** annotations

@ElementCollection can define the **targetClass** (the class of the collection elements) and the **fetch** (LAZY or EAGER) attributes.

To store the collections, a separate table is required. Each collection table must have a join column referring to it's entity table

the **@CollectionTable** annotation can be used to customize the mapping - p.108 -110

Collection, Set, List and Map can be used. The access to these fields should only be made using the according interface - because the provider can exchange the actual implementation with a different one when the entity is managed.

As ordering of rows inside the database is nnot commpnly defined, we can use the **@OrderBy** annotation to ensure ordering of _Lists_

The attribute is a comm-separated lsit of properties, followed by ASC or DESC. ASC is the default. Nested properties are allowed, like "employee.name"

    @OrderBy("name DESC, salary ASC")
    List<Employees> employees;

If the relationship is an entity, @OrderBy without attributes will order it by the PKs of the listen entities

If the relationship is an embeddable, the order will be defined by the order of retrieving the rows from DB (not really helpful)

Reordering the Lists in memory will not result in reordering in DB. The order for sequential reads of teh enity will be defined by the @OrderBy annotation. In general, it's good idea not to change the initial ordering in memory.

A second option to order Lists is to order tehm persistently - with an order column in the database. This is used if the entities do not have a sensible ordering property - i.e. if the ordering is external.

For this, **@OrderColumn** should be used. When the List is rearranged in the managed entity, the new order is persistent. This column is transparent to the user.

    @OneToMany(mappedBy="queue")
    @OrderColumn(name="PRINT_ORDER")//the default would be JOBS_ORDER
    private List<PrintJob> jobs;

You can use **either** OrderColumn **or** OrderedBy, not both.

Join column and owning side (p. 114)

Having a persistently ordered List may result in significant costs for keeping the List in sync - for each entity added to the list of size N, N additional queries must be emitted.

### Maps

Entities, embeddables and basic types can be used both as keys and values.

When used as keys, the types must be `comparable` and implement `hashCode` and `equals` properly. THey should also be unique, at least for the map in question. While the object is acting as a key in a Map, the properties relevant to it's identity (tho ones used in `hashCode` and `equals`) must not be changed. 

When keys are entities, only the FKs are stored in teh referred table. If the keys are embeddables or basics, they are stored inside the referrred table (cab either an entity table, or a collection table or a join table) 

The value object of the map determines the mapping (entity or collection table).

---leaving the boring chapter for later - enjoy from page 116 - 130 





