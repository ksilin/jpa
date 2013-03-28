Ch 3 - enterprise applications
===============================

do not use static variables with session beans - stateful or stateless (or singleton) - they cause problems at redeployment. Is it forbidden or just discouraged? -> TODO?

**Stateful** beans have a **@Remove** annotation to signify the method  for detachign teh bean from the user session and returning to the pool - I have never seen it yet. And not sure if this is a good idea with DI - once you have removed the bean - what do your client beans have instead of the injected instance? (the book states theat at least one Remove-annotated method is necessary - have to check on that)

**Stateful** beans have two more lifecycle callbacks - **PrePassivate and PostActivate**. These are used for  serialization - either for purposes of passivationn or for sending the bean instance to other server instances in a cluster. If the bean manages resources explicitly, the PrePassivate is analogous to PreDestroy and PostActivate is analogous to PostConstruct - used for resource allocation and release.

**Singleton** beans - introduced with EJB 3.1
-- is a singleton unique across a server cluster?
If the @Startup annottation is not used, the bean is instantiated when the server sees it fit. In case multiple Sngleton depend on each other, the @DependsOn annotation should be used. The @PreDestroy method is called only once - when the appplication is ended. 
As Singleton beans are accessed concurrently. Per default, the methods of the bean are managed with Write-lcoks, but can be managed with read.level locks, or programmatically - bean-level - p.44

MDB - p.45. For JMS.based MDBs, the business interface is javax.jms.MessageListener with the single void onMessage method. See @ActivationConfigProperty

The Dependency lookup - p.47-49 si a bit confusing. First, the dependency is defined on the class level as @EJB(name...) and then the dependency is looked up manually - with JNDI or wiht the SessionContext - what about the container?
-> Explanation: rhoth class-level dependency decl, no DI occurs, then the follw-up question - why shoudl  define it at all? I coudl just define a field and look it up

---
To declare a dependency on a EntityManager in an eneterprise ctx, the dependency of tha according persisstence ctx is declared, and the EM is generated automatically:

@PersistenceContext(unitName="EmpService")
EntityManager em;

once one know the relations if the persistence concepts (p23), it does make sense, but I still would prefer to declare @EntityManager(uName=), but maybe it was just too obvious. 
--> Explanantion - the EntityManager is not what you actually get, but a container maneged proxy that acquires and releases PCtx on behalf of the app. Thus, an injection into a Stateless session bean is OK:
---> another one - @PU can also be used to get an EMF injected, ehich in turn iwill be used to produce the EM. Fot differences between injecting the EM and the EMF, lokk up ch. 6

@Stateless
class SB{
@PersistenceContext
EM em // or EMF emf
}

If no name is defined, the injection behaviour is bendor specific (but likely to succeed for a solitary PU and fail for multiple PUs)

MDB cannot be injected using the @EJB annotation as they have no client interface

---
JEE transactions are generally ACID, but offering a degree of freedom in the strictness of the ACID requirements, e.g isolation. JEE mostly uses container transactions backed by JTA. AS they may span multiple resources, they are called global transactions

Container managed transactions - the support level is defined by the @TransactionAttribute annotation with one of the TransactionAttributeTypes:
MANDATORY, REQUIRED, REQUIRES_NEW, SUPPORTS, NOT_SUPPORTED, NEVER
The default is REQUIRED

--p 60 - 67 left out
