package pro.jpa2.data;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import pro.jpa2.model.Department;
import pro.jpa2.model.Employee;
import pro.jpa2.util.Resources;

/**
 * And while we're at it - not exactly JPA-tech-relevant, simple testing a
 * concept of a more dynamic Dao operating on string properties. So from this
 * POV, the Metamodel API is a step back to needing to have a separate Dao for
 * each model
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
// @UsingDataSet("emps.yml")
public class GenericDaoTest {
	@Deployment
	public static Archive<?> createTestArchive() {

		Archive<?> archive = ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addClasses(Employee.class, Department.class, GenericDao.class,
						Ordering.class, Resources.class)
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				// importing the seed sql
				.addAsResource("testSeeds/1EmployeeNoDepts.sql", "import.sql")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		// System.out.println("test archive contents : "
		// + archive.toString(Formatters.VERBOSE));

		return archive;
	}

	@Inject
	GenericDao<Employee> dao;

	@Inject
	Logger log;

	@Before
	public void before() {
		dao.setKlazz(Employee.class);
	}

	@Test
	public void testFindAll() throws Exception {
		log.warn("------------------------------------------------------------------");
		log.warn("started findAll test");
		Collection<Employee> allEmployees = dao.findAll();

		for (Employee e : allEmployees) {
			log.info("found employee: {}", e);
		}
	}

	@Test
	public void testFindPaged() throws Exception {
		log.warn("------------------------------------------------------------------");
		log.warn("started findPaged test");
		Collection<Employee> foundEmployees = dao.find(0, 1);

		assertEquals(1, foundEmployees.size());
	}

	@Test
	public void testFindById() throws Exception {
		log.warn("------------------------------------------------------------------");
		log.warn("started findById test");
		Map<String, String> predicates = new HashMap<String, String>();
		predicates.put("id", "0");
		List<Employee> foundEmployees = dao.find(predicates, 0, 1);

		assertEquals(1, foundEmployees.size());
		assertEquals(0, foundEmployees.get(0).getId());
	}
}
