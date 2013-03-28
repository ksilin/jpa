package pro.jpa2.data;

import static org.junit.Assert.assertEquals;

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

import pro.jpa2.model.Employee;

/**
 * Testing setting parameters for JPA queries - named and numbered params
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
public class FunctionExpressionsTest {
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
	public void getSimpleProperty() throws Exception {

		String queryString = "SELECT e FROM Employee e WHERE SIZE(e.phones) > 1";

		TypedQuery<Employee> query = em.createQuery(queryString, Employee.class);
		List<Employee> allResults = query.getResultList();

		assertEquals(1, allResults.size());

		//equivalent to first query
		String queryString2 = "SELECT e FROM Employee e WHERE (SELECT COUNT(p) FROM e.phones p) > 1";
		TypedQuery<Employee> query2 = em.createQuery(queryString2, Employee.class);
		List<Employee> allResults2 = query2.getResultList();

		assertEquals(1, allResults2.size());
		assertEquals(allResults.get(0), allResults2.get(0));
	}

	//TODO : add more tests for further functions:
	// ABS(num)
	// LENGTH(string)
	// LOCATE(string1, string2, [start])
	// LOWER(string)
	// SIZE(collection)
	// SUBSTRING(string, start, end)

}
