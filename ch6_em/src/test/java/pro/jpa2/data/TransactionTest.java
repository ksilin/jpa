package pro.jpa2.data;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import pro.jpa2.model.Employee;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

/**
 * Testing setting parameters for JPA queries - named and numbered params
 *
 * @author kostja
 */
@RunWith(Arquillian.class)
public class TransactionTest {
    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addPackages(true, "pro.jpa2")
                .addAsResource("META-INF/persistence.xml",
                        "META-INF/persistence.xml")
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

    @Inject
    TripBooker ejb;

    //was trying to test what happens with multiple nested exceptions if one of them throws
    // some of the nested tx will have committed before teh exception
    // will all preceding nested tx be rolled back or just the last one?
    //since in the second test the db is empty, i think all tx are rolled back
    @Test(expected = RuntimeException.class)
    public void bookTrip() {
        try {
            ejb.bookTrip();
        } catch (RuntimeException e) {
            log.info("caught exception");

            printAllEmployees();
            throw e;
        }
    }

    private void printAllEmployees() {
        TypedQuery<Employee> tq = em.createQuery("SELECT e FROM Employee e", Employee.class);
        log.info("-----------------------");
        for (Employee emp : tq.getResultList()) {
            log.info("saved employee: {}", emp);
        }
    }

    @Test
    public void seeWhatHappened() {
        printAllEmployees();
    }


//	@Test
//	public void testNestedTX() throws SystemException, NotSupportedException,
//			HeuristicRollbackException, HeuristicMixedException,
//			RollbackException {
//
//		tx.begin();
//
//		log.info("tx1 begins");
//
//		tx2.begin();
//
//		log.info("tx2 begins");
//
//		log.info("throwing");
//		throwSome();
//
//		log.info("thrown");
//
//		tx2.commit();
//
//		log.info("tx2 committed");
//
//		tx.commit();
//	}

    private void throwSome() {
        throw new RuntimeException("inner tx ");
    }
}
