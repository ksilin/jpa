package pro.jpa2.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import pro.jpa2.model.Project;
import pro.jpa2.model.ProjectEmployee;

@RunWith(Arquillian.class)
public class BidirectionalManyToManyProjectTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "manyToManyTest.war")
				// .addClasses(Employee.class, Department.class,
				// GenericDao.class,
				// Ordering.class, Resources.class)
				.addPackages(true, "pro.jpa2")
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				.addAsResource("testSeeds/3ProjectEmployees3Projects.sql",
						"import.sql")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Inject
	GenericDao<ProjectEmployee> empDao;

	@Inject
	GenericDao<Project> projDao;

	@Inject
	Logger log;

	@Before
	public void before() {
		empDao.setKlazz(ProjectEmployee.class);
		projDao.setKlazz(Project.class);
	}

	@Test
	public void testFindAll() throws Exception {

		log.warn("------------------------------------------------------------------");
		log.warn("started ProjectEmployeeAndSpace test:3 projects and 3 employees through import");

		Collection<ProjectEmployee> allEmployees = empDao.findAll();
		assertEquals(3, allEmployees.size());

		Collection<Project> allSpaces = projDao.findAll();
		assertEquals(3, allSpaces.size());
	}

	// verifying that the relation and the traversal from the owning to the
	// owned side works
	@Test
	public void testGettingProjectsFromEmployee() {

		List<ProjectEmployee> allEmployees = empDao.findAll();
		ProjectEmployee e = allEmployees.get(0);

		Map<String, String> findingBill = new HashMap<String, String>();
		findingBill.put("name", "Bill%");
		List<ProjectEmployee> billList = empDao.find(findingBill);

		// make sure it's just Bill
		assertEquals(1, billList.size());
		ProjectEmployee bill = billList.get(0);
		assertEquals("Bill Smith", bill.getName());

		assertNotNull(e.getProjects());
	}

	// verifying that the relation and the traversal from the owned to the
	// owning side works
	@Test
	public void testGettingEmployeeFromProject() {

		List<Project> allProjects = projDao.findAll();
		Project d = allProjects.get(0);

		assertNotNull(d.getEmployees());
	}

}
