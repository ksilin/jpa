Advanced ORM
==========

### Table and column names

Some DBs support case-sensitive table names (most DBs don't). So we might have table named EMPLOYEE, and another named Employee. To distiguish between the two, we need to delimit the table names:

The naive approach does not work as expected, as the following annotations are identical:
`@Table(name="employee")`
`@Table(name="Employee")`
`@Table(name="EMPLOYEE")`

Use a second set of escaped quotes inside the regular string quotes to signify case-sensitiveness:

`@Table(name="\"Employee\"")`
`@Table(name="\"EMPLOYEE\"")`

The escaping looks a bit different in XML:
`<column name="&quot;ID&quot;"/>`

When a DB is set to generally accept case-specific identifiers, it might be worth it to use a special setitng in the XML mapping file, instead of escaping all the ids:

`<delimited-identifiers/>`

Remember to remove the existing double qoutes when switching the feature on.


## Complex embedded objects

### Advanced embedded mappings

Embedded objects can embed other objects, have element collections of basic or embeddable types, as well as have relationships to entities.

The complete embedded hierarchy depends on the embedding object. Any relations within and between the embedded objects exist within the owning entity on DB level.

See code examples for entity structure - an embeded ContactInfo, containing an embedded Address and a bunch of phones.

**TODO: the mapping as displayed on page 277 didnt really work. See NestedEmbeddableExample in ch10**

**an example here would have been nice, I dont quite understand what this means:**

--
If an embedded object is a part of an element collection then the embedded object in the collection can only include mappings where the foreign key is stored in the source table. It can contain owned relationships, such as one-to-one and many-to-one, but it cannot contain one-to-many or many-to-many relationships where the foreign key is in either the target table or a join table. 

Similarly, collection table-based mappings like element collections are unsupported.
--

### Overriding embedde relationships

Embeddables can be reused in multiple entities. The embedding entity can override teh mapping of an embeddable, using `@AttributeOverride`. When Embeddables contain relations, these mappings may be overriden too, using `@AssociationOverride`.
Page 278 left out.

## Compound primary keys

There are two options. Both of them require defining a class for the key.

PKs must implemente `hasCode()` and `equals()`. They must be public, implement `Serializable` and have a no-arg ctor.

Example: Employee with country.wide unique Ids, where teh full key is the Country code end the Id.

### Id class 

The basic type of PK as an **id class**. All fields that mark the entitiy are marked with the `@Id` annotation. The entitiy defines it's Id class with the `@IdClass(EmployeeId.class)` annotation.

When implementing the class, we can omit the settings for the attributes of the IdClass (p.279 ok, why wouldnt I want it to be possible to change a country or the id of an employee?).

## Embedded Id class

If the entity contains a single field with the type of the PK class. It is indicated with `@EmbeddeId`. The PK embeddable must contain all attributes that constitute the PK.
The PK class is annotated with `@Embedable`. It's attributes do not have to be anotated with `@Id`.

An embeddable id class can be reused by multiple entities. They can also override the mapping attributes.

The difference is a matter of personal preference and has no further impact other then
having to reference the attributes of the Id with a dot operator: "employee.id.country".

---
## derived identifier

If the PK contains FKs to other entities, the identifier is called **derived**. The entitiy containing the FK is called **dependent**. The **parent** entity is target of a `*-1` or `1-1` relation. The relationship may be the only PK, or it may consist of several attributes.

A dependent entitiy cannot be persisted without a parent entity. It is undefined to modify the PK  of an existing entity. Once the entity is persisted, the relation should not be reset.

//TODO - I wonder what happens then

### Basic rules for derived Ids

* dependent entities may have multiple parents.

* dep. entity must have all it's relationships set before it can be persisted.

* If the PK is constituted from multiple attributes, the dependent entity must have a ID class. This class must contain all the id attributes.

* Id attributes in an entity might be of a simple type, or of an entity type that is the
target of a many-to-one or one-to-one relationship

* If an id attribute in an entity is of a simple type, then the type of the matching
attribute in the id class must be of the same simple type.

* If an id attribute in an entity is a relationship, then the type of the matching
attribute in the id class is of the same type as the primary key type of the target
entity in the relationship (whether the primary key type is a simple type, an id
class, or an embedded id class).

* If the derived identifier of a dependent entity is in the form of an embedded id
class, then each attribute of that id class that represents a relationship should be
referred to by a `@MapsId` annotation on the corresponding relationship attribute.

### Shared Primary key

...fast forward to p300 - preparing to implement a composite
## Inheritance

### Classs hierarchies

#### Mapped superclass

A mapped superclass (MSC) is convenient for storing data that inheriting entities share, while not being an entity itself. Thus annotations like @Table are illegal for maped superclasses. MSCs can be viewed as a JPA parallel to abstract classes. It is good practive ot make mapped superclasses abstract.

When do we need a MSC? 
 - will we be needing to query across it or acces a not-inherted instance of it?
- will we be needing to have a relation to an instance of it?
If you can answer NO to both these questions, you may safely use a mapped superclass.

#### Transient classes

Classes in the hierarchy that are neither entities nor MSC are called transient classes. The state iof this class is availabel but not persistent (ignored by the persistence). It is a good practice to make them abstract.

#### Abstract and conrete classes

At any level of the inheritance tree, transient classes, MSCs and entities may be abstract (really, abstract entities?). 

Abstract entities can be queried adn will be isntantiated by an instance of a subclass

### Inheritance models

JPA supports 3 different representations, Two of them are required to be implemented, while the third one is well-defined, but not required.

Every entity must either define or inherit an Id, effectively meaning that the Id will be defined wither by the root entity or MSC.

#### Single-Table Strategy

The default settitng. This is also the most common and performant way. The table contains a superset of all state in all classes of the hierarchy. An important consequence is, that for most rows, there will be empty columns - the ones that are not applicable to the entity type represented by the row. This means that all columns that are not shared by all entities must be nullable.

To (redundantly) define single-table strategy, the root entity can be annotated:
`@Inheritance(strategy=InheritanceType.SINGLE_TABLE)`

// TODO - do we need the inheritance annotation at all? How would the tbales look if we had entities left without annotation?


To describe the type of hte stored entities, the provider generates a **discriminator column**, which is named DTYPE per default. The type of the discriminator column may be String, Integer or Char. String is default.
`@DiscriminatorColumn(name="EMP_TYPE")`

The value in the discriminator columns is called **discriminator value** or **class indicator**. If unspecified, the provider will use a provider-specific way . IF the discriminator type is String, the provider will iuse the entity name. If the type is integer, we either must specify it for all relevant entities or for none, otherwise the numbers cold overlap with the provider-generated ones.
`@DiscriminatorValue("FTEMP")`

Discriminator values should not be specified for MSC, transient classes and in general abstract classes.

#### Joined Strategy

Mapping a table per entity gives us a normalized dd schema. The data is stored more space-efficiently. But it has performance issues as to create an instance of a subclass, we need to join with all teh superclass tables. It is also more expensive to create an entity, as it will add a row to each table up the inheritance hierarchy.

We have same id types for all entities, as with teh single-table approach and ids of subclasses are also used os FKs for joining to the superclass table. We use the `@PrimaryKeyJoinColumn` annotation for this.

Broad hierarchies tend to be expensive for queries across a superclass. Deep hierarchies tend to be expensive for construction of subclasses.

MSCs do get mapped to tables (despite their misleading name).
Abstract entities are mapped to separate tables.

To use the joined model, annotate the root entity wiht
`@Inheritance(strategy=InheritanceType.JOINED)`

#### Table per conrete class strategy

The data is not normalizedm as each concrete entity has a complete table, containting also teh inherited attributes. This strategy is not required to be implemented by providers.

This strategy makes polymorphic queries across the class hierarchy is more expensive. The query must be split by the provider inseparate queries for each table, or to use the UNION op, which is considered expensive.

Subclasses of concrete classes will have to store the inherited data redundantly in their own table.

Querying across a sinle concrete subclass is less expensive as with the joined strategy. There is no need for a discriminator column, as no data is shared.

To use this strategy, type
`@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)`

Fot the TPCC strategy, `@AttributeOverride` and `@AssociationOverride` annotations are often used with legacy databases.

#### Mixed inheritance p.311

Using a mixed inheritance mode is not in the specification as of now and thus might not be portable. Only single-table anf Joined strategies should be mixed.











 
