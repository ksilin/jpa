package pro.jpa2.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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

import pro.jpa2.model.Address;
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
public class BasicJpqlTest {
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

	// just to see if test is working at all
	@Test
	public void testEntitesInPlace() throws Exception {
		log.warn("------------------------------------------------------------------");

		assertEntity(Employee.class, 3);
		assertEntity(Department.class, 3);
		assertEntity(Project.class, 3);
		assertEntity(Phone.class, 4);

		for (Employee e : getAll(Employee.class)) {
			assertNotNull(e.getAddress());
		}
	}

	@Test
	public void getSimpleProperty() throws Exception {

		String queryString = "SELECT e.name FROM Employee  e";

		TypedQuery<String> query = em.createQuery(queryString, String.class);
		List<String> allResults = query.getResultList();

		assertEquals(3, allResults.size());
	}

	@Test
	public void getRelation() throws Exception {

		String queryString = "SELECT e.department FROM Employee  e";

		TypedQuery<Department> query = em.createQuery(queryString,
				Department.class);
		List<Department> allResults = query.getResultList();

		assertEquals(3, allResults.size());
	}

	@Test
	public void embeddedShouldBeDetached() throws Exception {

		String queryString = "SELECT e.address FROM Employee e";

		TypedQuery<Address> query = em.createQuery(queryString, Address.class);

		tx.begin();
		List<Address> allResults = query.getResultList();
		String state = allResults.get(0).getState();
		// this change will not be propagates, as the addresses are not managed
		// if the embeddeds would have been pulled from an attached entity, the
		// changes would be managed
		allResults.get(0).setState("NoState");
		tx.commit();

		allResults = query.getResultList();
		assertEquals(state, allResults.get(0).getState());
	}

	// setting the class name per parameter does not work :(
	public <T> void assertEntity(Class<T> klazz, final int expectedSize) {

		List<T> allResults = getAll(klazz);
		assertNotNull(allResults);
		assertEquals(expectedSize, allResults.size());
	}

	private <T> List<T> getAll(Class<T> klazz) {

		// you cannot parametrize the class of the query using regular params,
		// like in the WHEN clause
		// String queryString = "SELECT e FROM :className  e";
		String queryString = "SELECT e FROM " + klazz.getSimpleName() + "  e";

		TypedQuery<T> query = em.createQuery(queryString, klazz);
		// does not work in the FROM clause, seee above
		// query.setParameter("className", klazz.getSimpleName());
		List<T> allResults = query.getResultList();
		return allResults;
	}
}
