package pro.jpa2.data;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

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
 * Testing setting parameters for JPA queries - named and numbered params
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
// @UsingDataSet("employeeTestData.yml")
public class ParametrizedQueriesTest {
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
	public void testNamedParams() throws Exception {
		log.warn("------------------------------------------------------------------");

		String queryString = "SELECT e FROM Employee e WHERE e.name = :name";
		log.warn("started named params query test: {}", queryString);

		TypedQuery<Employee> query = em
				.createQuery(queryString, Employee.class);
		// setting the param
		query.setParameter("name", "Jack Bauer");

		Collection<Employee> allEmployees = query.getResultList();

		assertEquals(1, allEmployees.size());
		log.info("found {} employees", allEmployees.size());
		for (Employee e : allEmployees) {
			log.info("found employee: {}", e);
		}
	}


	@Test(expected = Exception.class)
	public void testSetMissingParam() throws Exception {
		log.warn("------------------------------------------------------------------");

		String queryString = "SELECT e FROM Employee e";
		log.warn("started named params query test: {}", queryString);

		TypedQuery<Employee> query = em
				.createQuery(queryString, Employee.class);
		// setting the param
		query.setParameter("name", "Jack Bauer");
	}

	@Test
	public void testNumberedParams() throws Exception {
		log.warn("------------------------------------------------------------------");

		String queryString = "SELECT e FROM Employee e WHERE e.name = ?1";
		log.warn("started numbered params query test: {}", queryString);

		TypedQuery<Employee> query = em
				.createQuery(queryString, Employee.class);

		// setting the param
		query.setParameter(1, "Jack Bauer");

		Collection<Employee> result = query.getResultList();
		assertEquals(1, result.size());

		log.info("found {} employees", result.size());
		for (Employee e : result) {
			log.info("found employee: {}", e);
		}
	}
}
