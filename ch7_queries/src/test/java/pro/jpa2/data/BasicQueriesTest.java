package pro.jpa2.data;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
 * Testing an Employee entity with generated Ids.
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
// @UsingDataSet("employeeTestData.yml")
public class BasicQueriesTest {
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
	public void testFindAll() throws Exception {
		log.warn("------------------------------------------------------------------");

		String query = "SELECT e FROM Employee e";
		log.warn("started findAll query test: {}", query);

		Collection<Employee> allEmployees = em.createQuery(query,Employee.class).getResultList();
		assertEquals(3, allEmployees.size());

		log.info("found {} employees", allEmployees.size());
		for (Employee e : allEmployees) {
			log.info("found employee: {}", e);
		}
	}

	@Test
	public void testFindProperty() throws Exception {
		log.warn("------------------------------------------------------------------");

		String query = "SELECT e.name FROM Employee e";
		log.warn("started find property query test: {}", query);

		Collection<String> allEmployeeNames = em.createQuery(query,String.class).getResultList();
		assertEquals(3, allEmployeeNames.size());

		log.info("found {} employees", allEmployeeNames.size());
		for (String e : allEmployeeNames) {
			log.info("found employee: {}", e);
		}
	}

	@Test
	public void testFiltering() throws Exception {
		log.warn("------------------------------------------------------------------");

		String query = "SELECT e.name FROM Employee e WHERE e.department.name='The Very Useful Department'";
		log.warn("started filtering query test: {}", query);

		Collection<String> filteredEmployees = em.createQuery(query,String.class).getResultList();
		assertEquals(2, filteredEmployees.size());
		log.info("found {} employees", filteredEmployees.size());
		for (String e : filteredEmployees) {
			log.info("found employee: {}", e);
		}
	}

	@Test
	public void testProjection() throws Exception {
		log.warn("------------------------------------------------------------------");

		//don't forget to put a comma between the results
		String query = "SELECT e.name, e.salary FROM Employee e";
		log.warn("started filtering query test: {}", query);

		//if selecting more than one entity, the return type will be a List of Object
		List projectedEmployees = em.createQuery(query).getResultList();

		assertEquals(3, projectedEmployees.size());
		log.info("found {} employees", projectedEmployees.size());
		for (Object o : projectedEmployees) {
			log.info("found employee name and salary: {}", o);
		}
	}

	@Test
	public void testCollectionQuery() throws Exception {
		log.warn("------------------------------------------------------------------");

		// don't forget to put a comma between the results
		String query = "SELECT d.employees FROM Department d";// WHERE d.name LIKE '%Hidden%'";
		log.warn("started collection query test: {}", query);

		// the query, returning collections works with implicit conversion
		Collection<Employee> employees = em.createQuery(query).getResultList();

		// and would fail here, when the Entity class is used
		//Collection<Employee> employees = em.createQuery(query, Employee.class).getResultList();

		assertEquals(3, employees.size());
		log.info("found {} employees", employees.size());
		for (Object o : employees) {
			log.info("found employees: {}", o);
		}
	}


	@Test
	public void testImplicitJoin() throws Exception {
		log.warn("------------------------------------------------------------------");

		//don't forget to put a comma between the results
		String query = "SELECT e FROM Employee e, Department d WHERE d = e.department AND d.name LIKE '%Hidden%'";
		log.warn("started join query test: {}", query);

		//if selecting more than one entity, the return type will be a List of Object
		List<Employee> employees = em.createQuery(query, Employee.class).getResultList();

		assertEquals(1, employees.size());
		log.info("found {} employees", employees.size());
		for (Object o : employees) {
			log.info("found employees: {}", o);
		}
	}

}
