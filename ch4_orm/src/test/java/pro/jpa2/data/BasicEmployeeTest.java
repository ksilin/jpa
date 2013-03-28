package pro.jpa2.data;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import javax.ejb.EJBException;
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

import pro.jpa2.model.Employee;

/**
 * Testing an Employee entity with generated Ids.
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
// @UsingDataSet("employeeTestData.yml")
public class BasicEmployeeTest {
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
				.addAsResource("testSeeds/1EmployeeNoDepts.sql", "import.sql")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Inject
	GenericDao<Employee> dao;

	@Inject
	Logger log;

	@Before
	public void before() {
		dao.setKlazz(Employee.class);
	}

	// just to see if test is working at all
	@Test
	public void testFindAll() throws Exception {
		log.warn("------------------------------------------------------------------");
		log.warn("started findAll test");

		Collection<Employee> allEmployees = dao.findAll();

		log.info("found {} employees", allEmployees.size());
		for (Employee e : allEmployees) {
			log.info("found employee: {}", e);
		}
	}

	// The id of a newly instantiated Entity is 0. There already is a persisted
	// entity with Id 0.
	// A simple call to em.persist() will create a new persistent entity with a
	// different generated Id
	@Test
	public void testCreateNewWithGeneratedId() {
		log.warn("------------------------------------------------------------------");
		log.warn("started test: creating employee with existing id ");

		Employee e = new Employee();
		e.setName("noname");

		// there is already an employee with id 0
		log.info("employee id before persisting : {}", e.getId());
		assertEquals(0, e.getId());
		// calling unchecked create (direct proxy to th EM) - there is already
		// an entity with Id 0
		dao.create(e);
		log.info("employee id after persisting : {}", e.getId());
		assertThat(e.getId(), not(0));
	}

	// since the Ids are generated, the id of 99 will cause the EM to assume
	// that this Entity is a detached one
	@Test(expected = EJBException.class)
	public void testCreateNewWithSetId() {
		log.warn("------------------------------------------------------------------");
		log.warn("started test: creating a new employee and setting an id explicitly");

		Employee e = new Employee();
		e.setName("noname");
		log.info("employee id before persisting : {}", e.getId());
		assertEquals(0, e.getId());
		e.setId(99);
		dao.checkedCreate(e, 99);
	}

	// since the Ids are generated, the id of 99 will cause the EM to assume
	// that this Entity is a detached one
	@Test(expected = EJBException.class)
	public void testCreateNewWithSetId2() {
		log.warn("------------------------------------------------------------------");
		log.warn("started test: creating a new employee and setting an id explicitly");

		Employee e = new Employee();
		e.setName("noname");
		log.info("employee id before persisting : {}", e.getId());
		assertEquals(0, e.getId());
		e.setId(99);
		dao.create(e);
	}
}
