package pro.jpa2.model.derived;

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
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;
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
public class DerivedIdTest {
    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addPackages(true, "pro.jpa2.model.derived")
                .addPackages(true, "pro.jpa2.util")
                .addAsResource("META-INF/persistence.xml",
                        "META-INF/persistence.xml")
                        // a safer way to seed with Hibernate - the @UsingDataSet breaks
                .addAsResource("testSeeds/derivedId.sql",
                        "import.sql")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction tx;

    @Inject
    private Logger log;

    @Test(expected = Exception.class)
    public void employeeHistoryShouldNotBePersistableUnlessRelationToFKIsSet() throws Exception {
        EmployeeHistory history = new EmployeeHistory();
        tx.begin();
        // throws a ConstraintViolationException - NULL is not allowed in column EMP_ID
        em.persist(history);
        tx.commit();
    }

    @Test
    public void employeeHistoryShouldBePersistableWithRelationToFK() throws Exception {
        EmployeeHistory history = new EmployeeHistory();

        tx.begin();
        Employee employee = em.createQuery("SELECT e FROM Employee e", Employee.class).getResultList().get(0);
        history.setEmployee(employee);
        em.persist(history);
        tx.commit();
    }
}
