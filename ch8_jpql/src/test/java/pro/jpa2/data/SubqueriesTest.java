package pro.jpa2.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

import ch.qos.logback.classic.db.SQLBuilder;

import pro.jpa2.model.Department;
import pro.jpa2.model.Employee;
import pro.jpa2.model.Phone;
import pro.jpa2.model.Project;

/**
 * Testing setting parameters for JPA queries - named and numbered params
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
public class SubqueriesTest {
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

	// asserting that different ways of join queries return the same result
	@Test
	public void testSimpleSubquery() throws Exception {
		log.warn("------------------------------------------------------------------");

		String subqueryString = "SELECT MAX(emp.salary) FROM Employee emp";
		String queryString = "SELECT e 	FROM Employee e WHERE e.salary = ("
				+ subqueryString + ")";

		TypedQuery<Long> subquery = em.createQuery(subqueryString, Long.class);
		Long highestPay = subquery.getSingleResult();
		log.info("found the max salary employees: {}", highestPay);

		TypedQuery<Employee> query = em
				.createQuery(queryString, Employee.class);

		// for cases where several employees are getting the same max amount,
		// all would be returned
		Employee ceo = query.getSingleResult();
		log.info("found the best paid employees: {}", ceo);
	}

	// asserting that the external variable e is accessible inside the subquery
	// and can be navigated
	@Test
	public void testInnerQueryVariableAccessAndExists() throws Exception {
		log.warn("------------------------------------------------------------------");

		// returning 1 is a common practice if the actual content of the result
		// is not relevant
		String subquery = "(SELECT 1 FROM e.phones p WHERE p.type = 'mobile')";
		String queryString = "	SELECT e FROM Employee e	WHERE EXISTS "
				+ subquery;

		TypedQuery<Employee> query = em
				.createQuery(queryString, Employee.class);

		// for cases where several employees are getting the same max amount,
		// all would be returned
		Employee emp = query.getSingleResult();
		log.info("found the employee with a mobile phone: {}", emp);
	}

	// TODO  ANY, ALL, SOME
	// ANY and SOME are identical

}
