package pro.jpa2.data;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
 * Getting a single result - the happy path, more than one result, no results
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
// @UsingDataSet("employeeTestData.yml")
public class SingleResultQueriesTest {
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

	/**
	 * if the query returns a single result, we can access it by calling
	 * {@link Query#getSingleResult}.
	 *
	 * @throws Exception
	 */
	@Test
	public void testGettingSingleResult() throws Exception {
		log.warn("------------------------------------------------------------------");

		String queryString = "SELECT e FROM Employee e WHERE e.name = :name";
		log.warn("started getSingleResult test: {}", queryString);

		TypedQuery<Employee> query = em
				.createQuery(queryString, Employee.class);
		// setting the param
		query.setParameter("name", "Jack Bauer");

		Employee singleResult = query.getSingleResult();

		log.info("found employee: {}", singleResult);
	}

	/**
	 * if the query returns a single result, we can access it by calling
	 * {@link Query#getSingleResult}.
	 *
	 * @throws Exception
	 */
	@Test
	public void testGettingResultListWithSingleResult() throws Exception {
		log.warn("------------------------------------------------------------------");

		String queryString = "SELECT e FROM Employee e WHERE e.name = :name";
		log.warn("started getSingleResult test: {}", queryString);

		TypedQuery<Employee> query = em
				.createQuery(queryString, Employee.class);
		// setting the param
		query.setParameter("name", "Jack Bauer");

		List<Employee> singleResult = query.getResultList();

		assertEquals(1, singleResult.size());
		log.info("found employee: {}", singleResult);
	}

	/**
	 * if no results can be found, a {@link NoResultException} is thrown
	 *
	 * @throws Exception
	 */
	@Test(expected = NoResultException.class)
	public void testNotoundSingleResult() throws Exception {
		log.warn("------------------------------------------------------------------");

		String queryString = "SELECT e FROM Employee e WHERE e.name = :name";
		log.warn("started getSingleResult test: {}", queryString);

		TypedQuery<Employee> query = em
				.createQuery(queryString, Employee.class);
		// setting the param - there is no such employee
		query.setParameter("name", "John Bauer");

		query.getSingleResult();
	}

	/**
	 * If asingle result is required but more than one results have been found,
	 * a {@link NonUniqueResultException} is thrown
	 *
	 * @throws Exception
	 */
	@Test(expected = NonUniqueResultException.class)
	public void testFailOnMultipleResults() throws Exception {
		log.warn("------------------------------------------------------------------");

		String queryString = "SELECT e FROM Employee e WHERE e.department.id = ?1";
		log.warn("started fail on multiple results test: {}", queryString);

		TypedQuery<Employee> query = em
				.createQuery(queryString, Employee.class);

		// setting the param
		query.setParameter(1, 2);
		query.getSingleResult();
	}
}
