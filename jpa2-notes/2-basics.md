Chapter2 - basics
===============

an entity is usually a rather fine-grained business object corresponding to a DB table, ofet having relationships to other entities and associated with some metainformation, in addition to otherwise being a POJO.

Characteristics:
* persistability = unique identity
* (transactionality)
* granularity

2 annotations are needed to turn a POJO with a no-arg ctor to a JPA entity @Entity and @Id

The default table name is the entity name
The default column is the name of the property

Persistence context - set of managed entity instances

There is a one-to-one corresponence between a persistence unit and it's EntityManangerFactory.
A persistence Unit can have multiple contexts, a context is tied to a single unit. an EMF can produce many EMs, each EM can have a single PC, a PC can be attached to many EMs - see p.23

* creating EMs - p23
* persisting, finding - p.24

em.find(Empoyee.class, 158) - if the object was not found, em returns **null**

removing entities is not very common
    ```java
    emp = em.find(Emp.class, 123);
    if (emp!= null) em.remove(emp)
    ```

using user transactions outside of the container - p27

don't forget to **close the em AND the emf** when working without a container

