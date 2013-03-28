package pro.jpa2.data;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
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

import pro.jpa2.model.ConstructedEmployee;
import pro.jpa2.model.Department;
import pro.jpa2.model.Employee;
import pro.jpa2.util.Resources;

/**
 * Testing an Employee entity with generated Ids.
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
// @UsingDataSet("employeeTestData.yml")
public class ConstructorExpressionTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addClasses(Employee.class, Department.class,
						ConstructedEmployee.class, GenericDao.class,
						Ordering.class, Resources.class)
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

	@Test
	public void testSimpleProjection() throws Exception {
		log.warn("------------------------------------------------------------------");

		// don't forget to put a comma between the results
		String query = "SELECT e.name, e.salary, e.department FROM Employee e";
		log.warn("started filtering query test: {}", query);

		// if selecting more than one entity, the return type will be a List of
		// Object
		List projectedResult = em.createQuery(query).getResultList();

		assertEquals(3, projectedResult.size());
		log.info("found {} employee projections", projectedResult.size());
		int count = 0;
		for (Iterator i = projectedResult.iterator(); i.hasNext();) {
			Object[] values = (Object[]) i.next();
			System.out.println(++count + ": " + values[0] + ", " + values[1]);
		}
	}

	@Test
	public void testUntypedCtorExpression() throws Exception {
		log.warn("------------------------------------------------------------------");

		// don't forget to put a comma between the results
		String query = "SELECT NEW pro.jpa2.model.ConstructedEmployee(e.name, e.salary, e.department) FROM Employee e";
		log.warn("started filtering query test: {}", query);

		// if selecting more than one entity, the return type will be a List of
		List projectedEmployees = em.createQuery(query).getResultList();

		assertEquals(3, projectedEmployees.size());
		log.info("constructed {} objects", projectedEmployees.size());
		for (Object o : projectedEmployees) {
			log.info("constructed employee: {}", o);
		}
	}

	@Test
	public void testTypedCtorExpression() throws Exception {
		log.warn("------------------------------------------------------------------");

		// don't forget to put a comma between the results
		String query = "SELECT NEW pro.jpa2.model.ConstructedEmployee(e.name, e.salary, e.department) FROM Employee e";
		log.warn("started filtering query test: {}", query);

		// if selecting more than one entity, the return type will be a List of
		// Object
		List<ConstructedEmployee> projectedEmployees = em.createQuery(query,
				ConstructedEmployee.class).getResultList();

		assertEquals(3, projectedEmployees.size());
		log.info("constructed {} objects", projectedEmployees.size());
		for (Object o : projectedEmployees) {
			log.info("constructed employee: {}", o);
		}
	}

}
