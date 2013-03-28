package pro.jpa2.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import pro.jpa2.model.Department;
import pro.jpa2.model.Employee;
import pro.jpa2.util.Resources;

/**
 * Testing bulk updates
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
public class BulkUpdateTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addClasses(Employee.class, Department.class, Ordering.class,
						Resources.class)
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				// a safer way to seed with Hibernate - the @UsingDataSet breaks
				.addAsResource("testSeeds/3Employee3Dept.sql", "import.sql")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@PersistenceContext
	EntityManager em;

	@Inject
	UserTransaction tx;

	@Inject
	Logger log;

	@Test
	public void testBulkUpdate() throws Exception {
		log.warn("------------------------------------------------------------------");
		String query = "SELECT e FROM Employee e";
		log.warn("started bulk update test: {}", query);

		tx.begin();
		List<Employee> projectedEmployees = em.createQuery(query,
				Employee.class).getResultList();

		assertEquals(3, projectedEmployees.size());

		for (Employee e : projectedEmployees) {
			assertFalse(e.getSalary() == 99999);
		}

		String updateQuery = "UPDATE Employee e SET e.salary = 99999";
		// WHERE e.name = :name";
		em.createQuery(updateQuery)
		// .setParameter("name", projectedEmployees.get(0).getName())
				.executeUpdate();
		for (Employee e : projectedEmployees) {
			em.refresh(e);
			assertTrue(e.getSalary() == 99999);
		}
		tx.commit();
	}

	@Test(expected = Exception.class)
	public void testBulkDelete() throws Exception {
		log.warn("------------------------------------------------------------------");
		tx.begin();
		String updateQuery = "DELETE FROM Department d";
		// will result in a ConstaintVolationException
		em.createQuery(updateQuery).executeUpdate();
		tx.commit();
	}

}
