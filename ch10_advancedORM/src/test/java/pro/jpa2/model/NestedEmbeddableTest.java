package pro.jpa2.model;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Testing setting parameters for JPA queries - named and numbered params
 *
 * @author kostja
 */
@RunWith(Arquillian.class)
public class NestedEmbeddableTest {
    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addPackages(false, "pro.jpa2.model")
                .addPackages(true, "pro.jpa2.data")
                .addPackages(true, "pro.jpa2.util")
                .addAsResource("META-INF/persistence.xml",
                        "META-INF/persistence.xml")
                        // a safer way to seed with Hibernate - the @UsingDataSet breaks
                .addAsResource("testSeeds/embeddables.sql",
                        "import.sql")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction tx;

    @Inject
    private UserTransaction tx2;

    @Inject
    private Logger log;

    // just to see if test is working at all
    @Test
    public void testEntitiesInPlace() throws Exception {
        log.warn("------------------------------------------------------------------");
        assertEntity(Employee.class, 3);
        assertEntity(Phone.class, 4);

        for (Employee e : getAll(Employee.class)) {
            assertNotNull(e.getContactInfo());
        }
    }

    @Test
    public void contactInfoShouldBeNavigableFromEmployeeQuery() throws Exception {

        String queryString = "SELECT e.contactInfo FROM Employee  e";

        TypedQuery<ContactInfo> query = em.createQuery(queryString, ContactInfo.class);
        List<ContactInfo> allResults = query.getResultList();

        for (ContactInfo info : allResults) {
            log.info("contact info: {}", info);
        }
        assertEquals(3, allResults.size());
    }

    @Test
    public void phonesShouldBeNavigableFromEmployee() throws Exception {

        //working with detached entities, so prefetch the association
        String queryString = "SELECT DISTINCT e FROM Employee  e JOIN FETCH e.contactInfo.phones";

        TypedQuery<Employee> query = em.createQuery(queryString, Employee.class);
        List<Employee> allResults = query.getResultList();

        for (Employee employee : allResults) {
            log.info("employee: {}", employee);
            Collection<Phone> entries = employee.getContactInfo().getPhones().values();
            for (Phone p : entries) {
                log.info("employee phone: {}", p);
            }
        }
        assertEquals(3, allResults.size());
    }

    //this throws an exception due to a bug in hibernate before 4.1.4.Final
    //https://hibernate.onjira.com/browse/HHH-5396
    @Test(expected = Exception.class)
    public void phonesShouldBeNavigableFromEmployeeQueryOverContactInfo() throws Exception {

        String queryString = "SELECT KEY(p) FROM Employee e JOIN e.contactInfo.phones AS p ";
//        String queryString = "SELECT KEY(e.contactInfo.phones) FROM Employee e";

        tx.begin();
        TypedQuery<Phone> query = em.createQuery(queryString, Phone.class);
        List<Phone> allResults = query.getResultList();

        for (Phone phone : allResults) {
            log.info("found phone: {}", phone);
        }
        tx.commit();
    }

    //
    @Test
    public void changesOnContactInfoShouldBeReflectedOnEmployee() throws Exception {

        String queryString = "SELECT e.contactInfo FROM Employee  e";

        tx.begin();
        TypedQuery<ContactInfo> query = em.createQuery(queryString, ContactInfo.class);
        List<ContactInfo> allResults = query.getResultList();

        for (ContactInfo info : allResults) {
            assertNull(info.getPrimaryPhone());

            Map<String, Phone> phones = info.getPhones();
            if (phones != null && !phones.isEmpty()) {
                Phone primaryPhone = phones.entrySet().iterator().next().getValue();
                info.setPrimaryPhone(primaryPhone);
            }
        }
        tx.commit();

//        tx.begin();
//        allResults = query.getResultList();
//
//        for (ContactInfo info : allResults) {
//            assertNotNull(info.getPrimaryPhone());
//        }
//        tx.commit();
    }


    @Test(expected = RuntimeException.class)
    public void testNestedTX() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {

        tx.begin();

        //cannot open a nested user tX
        // BaseTransaction.checkTransactionState - ARJUNA016051: thread is already associated with a transaction!
//        tx2.begin();

        throwSome();

//        tx2.commit();

        tx.commit();
    }

    private void throwSome() {
        throw new RuntimeException("inner tx");
    }


    //The embedded is not a queryable directly.
    @Test(expected = Exception.class)
    public void contactInfoShouldNotBeQueryable() throws Exception {

        String queryString = "SELECT ci FROM ContactInfo  ci";

        TypedQuery<ContactInfo> query = em.createQuery(queryString, ContactInfo.class);
        query.getResultList();
    }

    //querying for attachment to the PU is illegal
    @Test(expected = Exception.class)
    public void contactInfoIsNotAnEntity() throws Exception {

        String queryString = "SELECT e.contactInfo FROM Employee  e";

        TypedQuery<ContactInfo> query = em.createQuery(queryString, ContactInfo.class);
        List<ContactInfo> allResults = query.getResultList();

        log.info("contact info is attached: {}", em.contains(allResults.get(0)));
    }

    // The Phone has a reference on Employee. The ContactInfo, as part of Employee
    // has a reference to the primaryPhone
    // This creates a circular FK relation and one of the sides has to go empty initially
    @Test
    public void contactInfoShouldNotHaveThePrimaryPhoneSet() throws Exception {

        String queryString = "SELECT e.contactInfo FROM Employee  e";

        TypedQuery<ContactInfo> query = em.createQuery(queryString, ContactInfo.class);
        List<ContactInfo> allResults = query.getResultList();

        for (ContactInfo info : allResults) {
            assertNull(info.getPrimaryPhone());
        }
    }


    // setting the class name per parameter does not work :(
    public <T> void assertEntity(Class<T> klazz, final int expectedSize) {

        List<T> allResults = getAll(klazz);
        assertNotNull(allResults);
        assertEquals(expectedSize, allResults.size());
    }

    private <T> List<T> getAll(Class<T> klazz) {

        // you cannot parametrize the class of the query using regular params,
        // like in the WHEN clause
        // String queryString = "SELECT e FROM :className  e";
        String queryString = "SELECT e FROM " + klazz.getSimpleName() + "  e";

        TypedQuery<T> query = em.createQuery(queryString, klazz);
        // does not work in the FROM clause, see above
        // query.setParameter("className", klazz.getSimpleName());
        List<T> allResults = query.getResultList();
        return allResults;
    }
}
