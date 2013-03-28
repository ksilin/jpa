package pro.jpa2.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

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

import pro.jpa2.model.Department;
import pro.jpa2.model.Employee;
import pro.jpa2.model.Phone;
import pro.jpa2.model.Project;

/**
 * The relationship model has grown complicated, so here we are testing it. See image on p.209
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
public class ModelTest {
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
	Logger log;

	// just to see if test is working at all
	@Test
	public void testEntitesInPlace() throws Exception {
		log.warn("------------------------------------------------------------------");

		assertEntity(Employee.class, 3);
		assertEntity(Department.class, 3);
		assertEntity(Project.class, 3);
		assertEntity(Phone.class, 4);

		for(Employee e: getAll(Employee.class)){
			assertNotNull(e.getAddress());
		}
	}

	// setting the class name per parameter does not work :(
	public <T> void assertEntity(Class<T> klazz, final int expectedSize) {

		List<T> allResults = getAll(klazz);
		assertNotNull(allResults);
		assertEquals(expectedSize, allResults.size());
	}

	private <T> List<T> getAll(Class<T> klazz) {

		// you cannot parametrize the class of the query using regular params, like in the WHEN clause
//		 String queryString = "SELECT e FROM :className  e";
		String queryString = "SELECT e FROM " + klazz.getSimpleName() + "  e";

		TypedQuery<T> query = em.createQuery(queryString, klazz);
		// does not work in the FROM clause, seee above
//		query.setParameter("className", klazz.getSimpleName());
		List<T> allResults = query.getResultList();
		return allResults;
	}
}
