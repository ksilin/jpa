package pro.jpa2.data;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

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
public class AggregateTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addPackages(true, "pro.jpa2")
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				// a safer way to seed with Hibernate - the @UsingDataSet breaks
				.addAsResource("testSeeds/3ProjectsEmployeesDepts.sql",
						"import.sql")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@PersistenceContext
	EntityManager em;

	@Inject
	UserTransaction tx;

	@Inject
	Logger log;

	@Test
	public void average() throws Exception {

		String queryString = "SELECT AVG(e.salary) FROM Employee e ";

		TypedQuery<Number> query = em.createQuery(queryString, Number.class);
		List<Number> allResults = query.getResultList();

		for (Number e : allResults) {
			log.info("average salary: {}", e.toString());
		}
	}

	@Test
	public void averageWithGroups() throws Exception {

		String queryString = "SELECT d.name, AVG(e.salary) FROM Department d JOIN d.employees e GROUP BY d.name";

		// exactly the same result with explicit join conditions
		// "SELECT d.name, AVG(e.salary) FROM Department d, Employee e WHERE e.department = d GROUP BY d.name";

		TypedQuery<Object[]> query = em
				.createQuery(queryString, Object[].class);
		List<Object[]> allResults = query.getResultList();

		for (Object[] e : allResults) {
			log.info("average salary in {} : {}", e[0], e[1]);
		}
	}

	//
	@Test
	public void groupByAndSelectMustContainSameProps() throws Exception {

		// d.name is missing in the select
		String queryString = "SELECT AVG(e.salary) FROM Department d JOIN d.employees e GROUP BY d.name, e.name";

		// exactly the same result with explicit join conditions
		// "SELECT d.name, AVG(e.salary) FROM Department d, Employee e WHERE e.department = d GROUP BY d.name";

		TypedQuery<Number> query = em
				.createQuery(queryString, Number.class);
		List<Number> allResults = query.getResultList();

		for (Number e : allResults) {
			log.info("average salary {}", e);
		}
	}

	// TODO : is return type of COUNT queries Long? - test
}
