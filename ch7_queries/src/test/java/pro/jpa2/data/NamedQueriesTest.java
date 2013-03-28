package pro.jpa2.data;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

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

/**
 * Testing named JPA queries - named and numbered params
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
// @UsingDataSet("employeeTestData.yml")
public class NamedQueriesTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				// .addClasses(Employee.class, GenericDao.class, Ordering.class,
				// Resources.class)
				.addPackages(true, "pro.jpa2")
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				// a safer way to seed with Hibernate - the @UsingDataSet breaks
				.addAsResource("testSeeds/3Employee3Dept.sql", "import.sql")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@PersistenceContext
	EntityManager em;

	@Inject
	Logger log;

	// just to see if test is working at all
	@Test
	public void testNamedQuery() throws Exception {
		log.warn("------------------------------------------------------------------");
		String queryString = "Employee.findAll";

		log.warn("started named query test: {}", queryString);

		TypedQuery<Employee> namedQuery = em.createNamedQuery(queryString,
				Employee.class);

		List<Employee> allEmployees = namedQuery.getResultList();

		assertEquals(3, allEmployees.size());
		log.info("found {} employees", allEmployees.size());
		for (Employee e : allEmployees) {
			log.info("found employee: {}", e);
		}
	}
}
