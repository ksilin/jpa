package pro.jpa2.data;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import pro.jpa2.model.Employee;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.HashSet;
import java.util.Set;

/**
 * Testing an Employee entity with collection tables
 *
 * @author kostja
 */
@RunWith(Arquillian.class)
// @UsingDataSet("employeeTestData.yml")
public class BasicEmployeeTest {
    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addPackages(true, "pro.jpa2")
                .addAsResource("META-INF/persistence.xml",
                        "META-INF/persistence.xml")
                        // a safer way to seed with Hibernate - the @UsingDataSet breaks
                .addAsResource("testSeeds/1Employee.sql", "import.sql")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    GenericDao<Employee> dao;

    @Inject
    EntityManager em;

    @Inject
    UserTransaction tx;

    @Inject
    Logger log;

    @Before
    public void before() {
        dao.setKlazz(Employee.class);
    }

    // just to see if test is working at all
    @Test
    public void testFindAll() throws Exception {
        log.warn("------------------------------------------------------------------");
        log.warn("started findAll test");

        tx.begin();
        Employee employee = em.find(Employee.class, 0);
        tx.commit();


        log.info("found employee: {}", employee);
    }
}
