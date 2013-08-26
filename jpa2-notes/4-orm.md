Ch 4 - ORM
=========

mapping annotations can be divided in two groups 
* physical - relate to the concrete model in the DB (table, column etc)
* logical - describe the entity from the object modeling view 

Field access uses reflection, property access uses the JavaBean setters and getters
--> does this mean that accessors are not needed if field access is used?

Prop access: the annotation must be placed on the getter method. The type is determined by the return type of this method, the default name of the column is derived from the getter method and does not have to correspond to the actual field name (although this would break the JavaBeans convention)

`@Access` annotation and mixed access was introduced with JPA 2.0

Access types can be mixed inside an entity. 
The default access type is `Field`. `Property` access is often defined when some kind of processing is needed on the value.

If you mix access modes and define Property access for a method, mark the according field as `@Transient` to prevent the property to be persisted twice.

Default names - table or column -  are not specified to have a case. In case you work with a case-sensitive DB, consult ch.10

`@Table` annotation can be used to define the schema as well as the table name

`@Table(name=EMP, schema=HR)`
`@Table(name=HR.EMP) - nonstandard, supported only by some vendors`

## simple types

Supported types for simple mapping:

* primitive java types: `byte, short, int, long, boolean, char, float, double`
* Wrapper classes of primitives
* Byte and character arrays: `byte[], Byte[], char[], Character[]`
* Large numeric types: `BigInteger, BigDecimal`
* `String`
* Temporal types: `Date, Calendar` (also the according JDBC types : `java.sql.Date, java.sql.Timestamp`)
* Any `Enums`
* Any `Serializable` Object

When reading, and encounteringa type that does not match a JDBC type, an automatic conversion takes place. An exception may be thrown if the conversion fails, but is not required to.

When persisting a non-serializable `Object`, the behavior is undefined. An exception may be thrown, or the object may even be persisted nevertheless.

An optional `@Basic` annotation may be used to explicitly mark a field as persistent - this is mostly done for documentation purposes.

A general term for persistent field or property is **attribute**.

### column mapping

`@Column` annotation - most attributes with the exception of the **name** are used for schema generation: `@Column(name="EMP_ID")`

### Lazy fetching

defines portions of the entity that may be expensive to load to be loaded on first access.
In order to be able to define the fetching, we may also need the `@Basic` annotation, where `fetch` is an attribute: 

`@Basic(fetch=FetchType.LAZY)`

This is all that has to be done to ensure lazy loading - it is done transparently as long as the entity is attached.

`FetchType.LAZY` is only a hint.
`FetchType.EAGER` must be observed.
more details on lazy loading and detachment - see ch.6

It is almost never a good idea to lazily fetch simple types - there is usually nothing to be gained by returning only a part of a row and makes sense only if there are very many columns or the objects in the column are very big. Lazy fetching is only really relevant in the context of relation mapping.

### large objects - @Lob

`@Lob` annotation - just marker purposes, often used in conjunction with lazy fetching.

BLOBS - mapped by `byte[]` and `Byte[]`
CLOBS - mapped by `char[]`, `Character[]` and `String`

### Enums - @Enumerated

the default mapping for `Enums` is integer (`@Enumerated(EnumType.ORDINAL)`), for which the enum ordinal number is used. This is prone to break if the enum is changed (it's ok to add new values at the end of the type though), so it is advisable to map to String if changes are anticipated:
`@Enumerated(EnumType.STRING)`

String mapping still leaves the Enum mapping vulnerable to changes in the actual values. However this is significantly less likely to happen.

JPA only supports "plain" `Enums`, without attached values/state.

### Temporal - @Temporal

supports several types:
`@Temporal(TemporalType.DATE/TIME/TIMESTAMP)`
Explicit TemporalType definition is not necessary when using the sql temporal types.

### transient state

transient fields may be defined either by the `transient` keyword or by the `@Transient` annotation - the keyword has the additional meaning for regular serialization, while `@Transient` is only meaningful for persistence.
 
## Primary keys

assumed to be insertable, but neither nullable nor updateable. Do not change this (using `@Column`)

The legal types are a subset of the types for the basic mapping

* primitive java types: `byte, short, int, long, char`
* Wrapper classes of the above primitives
* Large numeric type: `BigInteger`
* `String`
* Temporal type: `Date` (also the according JDBC types : `java.sql.Date`)

Floating point values are permitted but discouraged because of rounding issues (the near-to-broken equality being one of them)

### ID generation

There are several strategies for letting the provider to generate an id for the entity:
`AUTO, TABLE, SEQUENCE, IDENTITY`

`@Id @GeneratedValue(strategy=GenerationType.*)`

`AUTO` is rather for dev and prototyping, since the provider has to create/already have a persistent resource for generation. The creation of this resource is usually a restricted/privileged operation that cannot be performed by the app.

#### table id - more on pp.83

this is the most flexible and portable generation strategy. It allows storing multiple id sequences for different entities within the same table.

`@Id @GeneratedValue(strategy=GenerationType.TABLE)`

more explicit approach is to define the table itself:

`@TableGenerator(name="Emp_Gen", table="ID_GEN", pkColumnName="GEN_NAME", valueColumnName="GEN_VAL", initalValue=10000, allocationSize=100)`
The elements beyond the name are not mandatory. 

This annotation can be placed anywhere, but it is advisable to place it on the entity that uses the generator.

in this case, the generator must be explicitly specified:
`@Id @GeneratedValue(generator="Emp_Gen")`

If the auto schema gen feature (ch. 13) is not used, the table must already exist. see p.85 for according SQL.

`Sequence` and `Identity` are supported only by some DBs. - pp85, 86

Identity or "autonumber" is often used as a fallback, if the DB does not support `Sequence`. It is less efficient as it does not support allocation in blocks

The accessibility of the id field is not defined for all generation strategies except for `Identity` - while it is uncommon but possible that the entity will actually have a generated id before it is persisted with all other gen strategies, it is not possible with Identity. It will be accessible after the inserting transaction has been committed.

## Relationships - pp.87 

* Roles - have been mandatory up to EJB 2.1.
* Directionality - bidirectional rel. should be seen as a pair of unidirectional relationships for technical reasons
* Cardinality
* Ordinality/optionality- does the target need to be specified at source creation

### single-valued associations (cardinality of target 1)

#### @ManyToOne - logical mapping

`@JoinColumns` - physical mapping - when the relation is defined by the foreign key of the target, the join column can be defined:
`@JoinColumn(name="DEPT_ID")` - always **defined on the owning side**.

If no join column is defined, the column name is defaulted to the field name plus the name of the primary key column of the target (here: ID), separated by a `_` :
`DEPARTMENT_ID`

#### @OneToOne - logical

`@JoinColumn` - same as with `@ManyToOne`

On the DB level, having a 1-1 rel results in having a **uniqueness constraint on the foreign key column of the owning side**

The source side is called **owning**, the target side is called **inverse** or non-owning

#### bidirectional 1-1

One side has to be the owner and control the mapping. This is a data modeling decision, where the important factor is the frequency of traversal in each dir. It makes sense to make the side where traversal is likely to start the mapping/owning side. 

The inverse side has to specify the field name that it is mapped by on owning side:

```
        class Owner{
          @OneToOne
          @JoinColumn(name="REV_ID")
          Reverse rev;}

        class Reverse{
          @OneToOne(mappedBy="rev")
          Owner o;}
```


### Collection-values associations

bidirectional *-1 relation implies a 1-* relation on the other side.

The 1 - side of the relation has no scalable way of storing the mapping information (it would have to go in one cell), so the mapping is held on the *-side (in a join column or otherwise). This is why 1-* rel are almost always bidirectional and the 1-side is never the owner.

This in turn is the reason that the target (the * side) entities have to have a reference back to the reverse side. [Having a foreign key without an association to the foreign entity is not supported by the API - p.96 - what does it mean - not supported - where does it break then?

As the reverse side, the 1-side has to define the mappedBy prop:

        class Department{
          @OneToMany(mappedBy="department")
          Collection<Employee> employees;} 

--If **mappedBy** is absent on both sides, two independent unidirectional relations are provided and a join table will be used.


#### ManyToMany mappings

@ManyToMany annotation on both sides

There is no way to implement a *-* relationship with join columns. The only way is a join table. Thus there is no way to determine the owner side, so we just have to pick one, arbitrarily by providing a **mappedBy** attribute to the mapping.

the **@JoinTable** annotation is not necessary and is used only to configure the table. BTW, other mappings can use a join table as well, but it's far less common.

example:
	@ManyToMany
	@JoinTable(name = "EMP_PROJ", joinColumns = @JoinColumn(name = "EMP_ID"), inverseJoinColumns = @JoinColumn(name = "PROJ_ID"))
	private List<Project> projects;

joinColumns can encompass multiple join columns - for multipart PKeys

Default name for the join table : <Owner>_<Inverse>. The join column name defaults remain (field name plus the name of the primary key column of the target)



####OneToMany - unidirectional

if the @OneToMany side does not include the mappedBy attribute, the relation is unidirectional. 
The join table must still be used, but it is used only by one entity

#### Lazy relationships

lazy fetching is more performance relevant for relations than it is for properties. IF not specified, the loading is guaranteed to be eager, if specified, it' a hint to the provider.
It is common for bidirectional relationships to be lazy on the one side and eager on the other.

//TODO : what is the case for lazy relationships after detaching - are they loaded on detachment or do they remain set to null?

### Embedded objects

EOs have no Id of their own and depend on an entity for that

Embedded types can be reused, but not the instances (denoted as composition in UML)! (so what happens if you try?)

Do not define Embedded Objects as part of inheritance hierarchies - it is unportable and requires lots of effort to get right (hmm, how is it meant?)

As a single Embeddable may be used in different entities, we might need to adapt the mapping using the **@AttributeOvierrides** annotation inside the embedding entity.


 








