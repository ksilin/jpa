package pro.jpa2.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;

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

import pro.jpa2.model.ParkingEmployee;
import pro.jpa2.model.ParkingSpace;

@RunWith(Arquillian.class)
public class BidirectionalOneToOneParkingTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "empPTest.war")
				// .addClasses(Employee.class, Department.class,
				// GenericDao.class,
				// Ordering.class, Resources.class)
				.addPackages(true, "pro.jpa2")
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				.addAsResource("testSeeds/1ParkingEmployee1Space.sql",
						"import.sql")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Inject
	GenericDao<ParkingEmployee> empDao;

	@Inject
	GenericDao<ParkingSpace> parkDao;

	@Inject
	Logger log;

	@Before
	public void before() {
		empDao.setKlazz(ParkingEmployee.class);
		parkDao.setKlazz(ParkingSpace.class);
	}

	@Test
	public void testFindAll() throws Exception {

		log.warn("------------------------------------------------------------------");
		log.warn("started ParkingEmployeeAndSpace test: a single parking space and single employee through import");

		Collection<ParkingEmployee> allEmployees = empDao.findAll();
		assertEquals(1, allEmployees.size());

		Collection<ParkingSpace> allSpaces = parkDao.findAll();
		assertEquals(1, allSpaces.size());
	}

	// verifying that the relation and the traversal from the owning to the
	// owned side works
	@Test
	public void testGettingParkingFromEmployee() {

		List<ParkingEmployee> allEmployees = empDao.findAll();
		ParkingEmployee e = allEmployees.get(0);

		assertNotNull(e.getParking());
	}

	// verifying that the relation and the traversal from the owned to the
	// owning side works
	@Test
	public void testGettingEmployeeFromParking() {

		List<ParkingSpace> allSpaces = parkDao.findAll();
		ParkingSpace d = allSpaces.get(0);

		assertNotNull(d.getEmployee());
	}

}
