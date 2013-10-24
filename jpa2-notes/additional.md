### in the mock exams, exceptions seem to be more important than in the book - 3.9

java.lang.Object
  extended by java.lang.Throwable
      extended by java.lang.Exception
          extended by java.lang.RuntimeException
              extended by javax.persistence.PersistenceException

Thrown by the persistence provider when a problem occurs. All instances of PersistenceException except for instances of `NoResultException`, `NonUniqueResultException`, `LockTimeoutException`, and `QueryTimeoutException` will cause the current transaction, if one is active, to be marked for rollback. 

                 extended by javax.persistence.EntityExistsException

Thrown by the persistence provider when `EntityManager.persist(Object)` is called and the entity already exists. The current transaction, if one is active, will be marked for rollback.

If the entity already exists, the `EntityExistsException` may be thrown when the persist operation is invoked, or the `EntityExistsException` or another `PersistenceException` may be thrown at flush or commit time.

The current transaction, if one is active, will be marked for rollback. 

                  extended by javax.persistence.EntityNotFoundException

 Thrown by the persistence provider when an entity reference obtained by `EntityManager.getReference` is accessed but the entity does not exist. Thrown when `EntityManager.refresh` is called and the object no longer exists in the database. Thrown when `EntityManager.lock` is used with pessimistic locking is used and the entity no longer exists in the database.

The current transaction, if one is active, will be marked for rollback. 

                  extended by javax.persistence.LockTimeoutException

Thrown by the persistence provider when an pessimistic locking conflict occurs that does not result in transaction rollback. This exception may be thrown as part of an API call, at, flush or at commit time. The current transaction, if one is active, will be **not** be marked for rollback. 

 the `LockTimeoutException` will be thrown if the database locking failure causes only statement-level rollback 

                  extended by javax.persistence.NonUniqueResultException

Thrown by the persistence provider when `Query.getSingleResult()` or `TypedQuery.getSingleResult()` is executed on a query and there is more than one result from the query. This exception will **not** cause the current transaction, if one is active, to be marked for rollback. 

                  extended by javax.persistence.NoResultException

Thrown by the persistence provider when Query.getSingleResult() or TypedQuery.getSingleResult()is executed on a query and there is no result to return. This exception will **not** cause the current transaction, if one is active, to be marked for rollback. 

                  extended by javax.persistence.OptimisticLockException

Thrown by the persistence provider when an optimistic locking conflict occurs. This exception may be thrown as part of an API call, a flush or at commit time. The current transaction, if one is active, will be marked for rollback.

                  extended by javax.persistence.PessimisticLockException

Thrown by the persistence provider when an pessimistic locking conflict occurs. This exception may be thrown as part of an API call, a flush or at commit time. The current transaction, if one is active, will be marked for rollback. 

 the `PessimisticLockException` will be thrown if the database locking failure causes transaction-level rollback 

                  extended by javax.persistence.RollbackException

Thrown by the persistence provider when EntityTransaction.commit() fails. 

                  extended by javax.persistence.TransactionRequiredException

Thrown by the persistence provider when a transaction is required but is not active. 

### transaction rollback

Transaction rollback will probably leave your PC in a broken state and will detach all mamaged entities. This may not be a problem with a transaction-scoped PCs, but it is for an EXTENDED EM. 

Extended EM is not threadsafe, so you should only use it in a stateful bean.

http://stackoverflow.com/questions/13638944/jpa-rollbackexception-persist-transaction-causes-subsequent-valid-transactions-t 

Rolling back a TX in a CMT bean:

        EJBContext.setRollbackOnly()

http://www.developerscrappad.com/547/java/java-ee/ejb3-x-jpa-when-to-use-rollback-and-setrollbackonly/


### how to prevent tx rollback from interfering with your evil plans:

use nested tx:

http://piotrnowicki.com/2013/03/jpa-and-cmt-why-catching-persistence-exception-is-not-enough/

### JPA exam post mortem

http://www.coderanch.com/t/564161/sr/certification/OCE-Java-EE-JPA-Developer

There were only 6 questions on the Criteria API (which was the hardest part for me). A lot of questions about locking and about using Maps

Some questions were about EJBs transactionality and L2 cache

### application exception vs system exception

Generally, an application exception is an exception that your code throws, explicitly or implicitly. A system exception is probably an exception thrown by the persistence provider, i.e. `java.persistence.PersistenceException`

### exception wrapping and transaction rollback:

http://palkonyves.blogspot.de/2013/04/exceptions-and-transactions-in-ejb.html

there is an annotation for that

### What happens to the propagated PC if a nested TX is rolled back?

Will it be corrupted and all entities detached?

### Retrieving a collection-valued attribute

The `SELECT` clause must be specified to return **only single-valued** expressions. The query below is therefore invalid:

        SELECT o.lineItems from Order AS o

The correct query would be: 

        SELECT li from Order o inner join o.lineItems li
or:
        SELECT li from Order o IN(o.lineItems) li


### the 'mappedBy' attribute appears only on the inverse (non-owning) side

### JoinTable annotation appears only on the owning side
