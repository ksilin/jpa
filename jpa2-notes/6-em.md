Chapter 6 - Entity Manager
=======================

Core terms - 

Persistence Unit (PU) - a named configuration of entity classes
Persistence Context (PC) - managed set of entity instances
Entity manager (EM) - manages a PC
managed bean = contained in a PC

If a PC is in a TX, it's contents are synchronized with the DB. The PC is not visible to the App and has to be addressed over the EM.

JPA defines 3 types of EMs:

### Transaction managed EMs

retrieved using the @PersistenceContext annotation

#### transaction scoped

PC lifecycle spans the active JTA transaction. The EM is stateless and is safe to be stored in any bean

Every time an operation is invoked on the EM, the proxy checks with the TX if a PC is available. If not, a new one is created

#### extended

PC lifecycle is determined by the lifecycle of the controlling stateful bean. The extended PC is designed to keep entitiy fields inside stateful beans attached (otherwise they must be looked up for each TX)

The type of PC is controlled by the **type** attribute of the @PersistenceContext annotation:
    @PersistenceContext(type=PersistenceContextType.EXTENDED) 
    PersistenceContextType.TRANSACTION is the other option.

Stateful beans were shunned for a long time for performance problems. This is no longer true, so using stateful beans with the EM will probably emerge as a best practice

### application-managed (p.136)

EMs created by calling EMF.createEntityManager are called application-managed.

There are differences in acquiring the EM in SE and EE nevironments:

#### SE:

EMF emf = Persistence.createEntityManagerFactory("PUName");
EM em = emf.createEntityManager();
...
em.close();
emf.close();

.createEntityManagerFactory can accept either the name of the PU (then you need a persistence.xml) or a Map of propetries for the persistence, adding to or overriding the ones defined in the persistence.xml

The active set of properties can be retrieved by callign .getProperties() on the EM.

#### EE:

@PersistenceContext
EMF emf;

EM em = emf.getEntityManager();
...
em.close();

Note - always close the application-managed EM!
In SE applications, the EMF must be closed as well.

The PC of an application managed EM will remain until the EM is explicitly closed, independent of TXs

Application-amanaged EMs are rarely needed in EE apps.

## TX management

There are 2 types of transactions - JTA TXs and resource-local TXs (native to JDBC drivers)
Container-managed EMs use JTA, application-managed EMs can use either one.

### JTA TX - key concepts:

TX synchronisation: PC registers with the TX to be notified on commits (for flushing)
TX association: binding a PC to a TX - defining the active PC
TX propagation: sharing the PC between multiple EMs in a single TX
 
There can be only one PC across a TX, that is associated and propagated.

### TX-scoped PCs

Created by the container and closed when TX ends. PC creatin is lazy - the EM will onyl create one if a method is called on teh EM and there is no PC available (all subsequent operations will then use this PC). This works for bean and container TX.

Propagating a PC makes sharing an EM instance unnecessary.

Here we have to be carefy with REQUIRES_NEW. If a bean method will open a new TX, the PC of the encompassing TX is not available.

### Extended PC

The transaction association for extended PCs is eager - the container associates the PC with the TX as soon as a method call starts on the bean (for container-managed TX, on TX.begin() otherwise)

It is possible to provide the extended PC of a stateless bean as the propagated PC for other,  transaction scoped EMs - the extended PC has to be the first PC that is encountered by the service call. (p. 141)

### PC collision with extended PCs

Collisions will occur if a bean with a TX-scoped PC calls the stateful bean with the extended PC. The EM will check try to eagerly attach it's own extended PC and will fail, as there is already one available -> Exception (TODO: model this! - p.142)

Thus, Stateful beans with extended PCs should be the first persistence-related beans being called in service call.

This limits the use of stateful beans wiht extended PC. A workaround to allow other beans call the stateful bean is to anotate the methods of the Stateful bean with REQUIRES_NEW - then it will always lift the current TX and provide the extended PC for all sequential calls. Using REQUIRES_NEW a lot may have a negative impact on the performance.

If the stateful bean does not call other EJBs, it might be woth it to annotate it's methods with NOT_SUPPORTED (p.142)

### PC inheritance (with multiple extended PCs)

When a stateful bean with an extended PC is injected into another stateful bean with an extended PC, the external bean inherits its PC, so both use the same PC

### Application managed PCs (p. 144-146)

---left out - some issues comnining app-managed PCs with TX-managed ones - 144-146

Combining application-managed and conteiner-managed PCs may lead to issues, if same entites, or even entites with same PKs are contained in different PCs at one time. There is no defined order for the flushing of multiple PCs on TX commit, so this would lead to unexpected, inconsistent results.

It is considered best to stick to either container- or application-managed PCs in a single app.

### Resource-local TX - (EntityTransaction)

Developed to mimic the UserTX. Accesses by em.getTransaction - this method will throw if called on a container-managed EM.

In containers, resource-local TX are rarely used - maybe for persistent logging - to ensure that logging to DB takes place despite rollback of container-managed TX. In order to obtain an app-managed EM, get an EMF from @PersistenceContext ang call createEntityManager.

### TX rollback and entity state

On TX rollback, the DB is returned to it's original state. While the DB changes are reverted, th in-memory changes (object states) are not.

Immediately before a commit, the changes are translated into SQL and sent to DB.

TX rollback actually means more than one thing happening:
* the DB TX is rolled back
* the PC is cleared - all entities are detached. In TX-scoped PCs, the PC is then dropped.

The detached entites are left in the state immediately before the rollback. Before staring over with a merge and commit, consider:

* If a PK was generated for a new entity, do not use it, remove it - the operaton that assigned teh key has been rolled back oand the same key can be already given to a different entity.
* if the entity contains a version field for locking, it may have an incorrect value (see ch.11)

It may be better to copy the data from the detached entities to new entites and commit them in order to avoid merga conflicts with stale data.

### Choosing the EM

The best practice i to use Container-managed EMs with TX-scoped PCs

Container-manged EMs with extended PCs are interesting too, but they advise a different application structure.

### EM operations

#### persist

accepts a new entity and makes it managed. The contains method should rarely be used - it should be known which entites are managed. If using contains often - look at the design.

The actual persisting takes place at TX commit.

When persist is called outside a TX,:
* a transaction-scoped EM will throw a TransactionRequiredEx
* app-managed and extenden EMs will accept the entity, put into th PC and wait for th next TX to commit - the change is queued up.

If the entity already exists in the DB, an EntityExistsException is thrown 

TODO : P.151 - an interesting case. Simpla adding an employee to a (managed) department does not persist it, since employee is th eowning side of the relationship - you need to explicitly persist the employee after adding it to the department.

### find

Is the most efficient way to retrieve entities. Find returns a manages instance (except when invoked outside a TX on a TX-scoped EM)

In cases where we need an entity only to update other entites, we can call em.getReference(entity.class, id). We then get a proxy to the instance without actually fetching anything from the DB - it'
s a performane optimization technique.
If the entity referred by the id does not exist and a property of the proxy is accessed (other than the Id), an EntityNotFoundException is thrown. It is also not safe to use this proxy if it is detached.

Generally, the marginal performance increase never warrants the use of getReference. Forget it.

### remove

Remove may result in violation of constraints, mostly FK contraints. Maintaining the constraints is the responsibility of the application.

The references (FKs) to the object that is to be removed must be set to null, in order to prevent violations and stale references (and see p.153)

Only a managed entity can be removed. If using an app-managed or extended EM, the call to remove will result in a DB change after a TX has been committed.

The in-mem instances of the removed entites will remain in the state immediately before TX commit. A removed antity can be persisted again with a call to persist(), but watch out for stale data (see TX rollback & object state).

### Cascading

The default setting is - no cascading

//TODO : how is cascading realized in cases with a bidirectional relation with a join table or two join columns - my intuition says it might run into a hen/egg problem


The cascadeable operations are: PERSIST, REMOVE, REFRESH, MERGE, DETACH

Cascade settings are unidirectional. Cascades often imply ownership.

### persist

//TODO : small example last paragraph before cascade remove p155

### remove

Cascading removal is much more sensitive and might result in unexpeced behaviour if applied incorrectly.

There are 2 cases, where cascading remove makes sense:
* 1-1 and 1-* relationship with a clear parent-child relationships and where the entities do no participate in additional relationships.

The in-memory objects will remain linked. There is no operation that could safely remove a child and simultaneously update the in-memory parent entity as well.

###clearing the PC

This is mostly required only for the longer-lived app-managed and extended PCs. In case you don't want to close the PC, you can call em.clear(). This is a bit like rolling back a TX: all entities are detached and in the state immediately before the clear() call.

Clearing the PC while there are uncommited changes is unsafe - if cleared in a TX, while some changes are already written to the DB, they will not be rolled back. The detached entities may suffer from stale data.

## Synchronization with DB (p.157)

A flush of the changes will occur before the DB TX is sompleted
If there are entites with pending changes, a fluch is guaranteed to occur when:
* the TX commits 
* em.flush() is called

OTOH a flush may occur any time the provider sees fit - there is no guarantee that a flush will not occur. Most providers defer SQL generation to the last possible moment though.

A flush consists of 3 components:

* new entities -persist
* changed entities - update
* deleted entities - remove

The process for the EM:

* which new entites have been added to relationshis with cascade PERSIST - equivlent to calling persist() on each entity just before the flsuh
* Check the integrity of relationships (and throw if detached or removed entities found)

An elaborate example of a fluch with new and detached entites on p.158

It is always safer to update the relations to the entities that will be removed before calling remove()

## Detachment and Merge

A detached entitiy is no longer connected to a PC. The opposite of detachmentis merging - hte EM then integrates the entity into a PC. On merge, any values in teh detached entity overwrite those on teh managed entity.

How does an entity become detached?

* When the PC is bound to a TX and the TX commits - all entities  in the PC will be detached
* An app.managed PC is closed - all entities will be detached
* A stateful bean holding an extended PC is removed - all entities will be detached
* clear() is invoked - all entities of the current PC will be detached
* detach() is called - a single entity will become detached
* TX rollbac - all entities in all attached PCs are detached
* entity is serialized - the serialized form is detached

Call to detach() may activate according cascades. Is detach() is called on a new or removed entity, ONLY cascades are activated to detach related entities.

//TODO : are one-to-many relationships LAZY be default?? (p.160)

Beware of lazy loading - if lazy loading properties of an entity were no accessed (and thus loaded) while the entity was managed - the behaviour of lazy associations on detach is undefined. They may be there or not. If the entity has been detached during serialization, the lazy properties will not be loaded

### Merging detached entities

A call ot merge is special. Calling em.merge(entity) will NOT cause the entity instance to become managed. Rather a new, managed instance is returned. 

If an entity instance with the same ID exists in the PC, it's state will be overridden.

Calling merge on a new entity will cause a copy of this entity to be persisted and returned.

It gets more complicates when relationships are involed: see complicated example on pp162-163

Merge, like other oerations should be cascaded only from parent to child.
Merging entities with relationships may get difficult. Ideally, the root af an object graph should be merged, so that all related entities will be merged automatically. This can be made work wiht cascading MERGE. Without is, each entity that is a target of a relationship in the graph must be merged individually

## working with detached entities

### preparing for detachment

in order to prevent th risk of missing lazy relateionships, several thing can be done:

* access the lazy loading proerties to assure that they are loaded - in case we need properties of the lazy properies, we need to access them too (the non-nested lazy property might be returned as an empty proxy, so we need to force the provider to load the real object by accessing it's properties)
* 

Note: the default fetch setting for Many-To-One relationship is EAGER, but it's LAZY for One-To_Many

All collection-valued relationships are LAZY loaded per default 


### avoiding detachment

two approaches:

* do not work with entities in the UI, which itself consists of two approaches: 
    * use DTOs in the UI - doubtful usefulness, given the POJO nature of JPA entities
    * use projection queries (subject in ch 7 & 8)
* keep the TX open while the UI is rendering (and what about user input?) - it does not work if the entities are to be serialized, but for one-tier webapps, it might work

#### Transaction view pattern

the TX is started, then rendering occurs and then the TX is commited in the Controller (may work for JSP pages with exlicit rendering, but I doubt that for JSF pages)

#### Entity manager per request

in the entry method (Servlet's doGet in the example on p170), create an application-managed EM, make queries and close() it when the method ends, flushing the PC.

We can advance this approach by extracting the EM  in to a Stateful bean adn using an EXTENDED PC. The lifecycle of the bean would be restricted by the calling method - we will also have to call a @Remove-annotated method on the bean itself.

This approach does introduce som eoverhead , but is extensible and can be used in merge scenarios as well - as the Stateful bean would hold the PC for the duration of a find-edit-merge workflow.

### merge strategies

### Session facade pattern

The changed entity is handed to a stateless bean for merging.
In case where the changes encompass only a small portion of a larger entity, a dedicated method for these properties might be better - finding the managed instance of the entity and then updating it with the values instead of merging the complete entity.
 
### Edit session pattern (p.175)

Using the EXTENDEN PC, we can approach the issue differently. If the Stateful bean holding the PC is stored during the session, we will not have to merge. The bean will be of very narrow use - mainly for retrieving the right entity and persisting it implicitly

 The bean is annotated with TransactionAtributeType.NOT_SUPPORTED - we don't need TX here, except for the saving:

@Remove
@TransactionAtribute(TransactionAtributeType.REQUIRES_NEW)
public void save(){}

once the editing session is complete, we need to remove the Stateful bean.

Here the patter explained:

1. For each use case that modifies data, a stateful bean with extended PC is created. 
2. The request triggers the creation of the bean (and binds it to the session)
3. On request completion the bean is obtained and data is written into the managed entites. Then an operation is called to persist the data (adn in teh example, to dispose the bean)

This looks like an excessive use of stateful session beans, but this approach scales well for complex (multiple) edits.

The stateful bean instances are ot threadsafe (might be a problem on rapid repeated resubmitting)

 






