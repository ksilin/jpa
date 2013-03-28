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
public class OrderByTest {
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
	public void orederBy() throws Exception {

		String queryString = "SELECT e FROM Employee e ORDER BY SIZE(e.phones) DESC";

		TypedQuery<Employee> query = em.createQuery(queryString, Employee.class);
		List<Employee> allResults = query.getResultList();

		assertEquals(3, allResults.size());
		for(Employee e: allResults){
			log.info(e.toString());
		}
		assertEquals("John Smith", allResults.get(0).getName());

		// multiple orderings are OK (I am just assuming lexical precedence rules)
		String salaryDescString = "SELECT e FROM Employee e ORDER BY e.salary DESC, e.name DESC";
		TypedQuery<Employee> salaryDescQuery = em.createQuery(salaryDescString, Employee.class);
		List<Employee> salaryDescResults = salaryDescQuery.getResultList();

		assertEquals(3, salaryDescResults.size());
		for(Employee e: salaryDescResults){
			log.info(e.toString());
		}
		assertEquals("Bill Smith", salaryDescResults.get(0).getName());
	}

	// we can define aliases (AS) for computed and path variables to make sorting more efficient
	@Test
	public void orederByAlias() throws Exception {

		String queryString = "SELECT e.salary * 0.05 AS bonus FROM Employee e ORDER BY bonus DESC";

		TypedQuery<Double> query = em.createQuery(queryString, Double.class);
		List<Double> allResults = query.getResultList();

		assertEquals(3, allResults.size());
		for(Double e: allResults){
			log.info(e.toString());
		}
	}


	// TODO: The book says on p233 - the following query is not legal, but it works - ask on SO
	// Because the result type of the query is the employee name, which is of type String, the remainder of
	// the Employee state fields are no longer available for ordering

	// when using path expressions, in the select clause, oredering is restricted to the selected attributes
	@Test
	public void orederBySelectedOnly() throws Exception {

		String queryString = "SELECT e.name FROM Employee e ORDER BY e.salary DESC";

		TypedQuery<String> query = em.createQuery(queryString, String.class);
		List<String> allResults = query.getResultList();

		assertEquals(3, allResults.size());
		for(String e: allResults){
			log.info(e.toString());
		}
	}
}
