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

import pro.jpa2.model.AddressCompany;
import pro.jpa2.model.AddressEmployee;

@RunWith(Arquillian.class)
public class AddressEmbeddableTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "embeddableTest.war")
				.addPackages(true, "pro.jpa2")
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				.addAsResource("testSeeds/1AddressEmployee1Company.sql",
						"import.sql")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Inject
	GenericDao<AddressEmployee> empDao;

	@Inject
	GenericDao<AddressCompany> compDao;

	@Inject
	Logger log;

	@Before
	public void before() {
		empDao.setKlazz(AddressEmployee.class);
		compDao.setKlazz(AddressCompany.class);
	}

	@Test
	public void testFindAll() throws Exception {

		log.warn("------------------------------------------------------------------");
		log.warn("started AddressEmbeddable test: 1 company, 1 employee through import");

		Collection<AddressEmployee> allEmployees = empDao.findAll();
		assertEquals(1, allEmployees.size());

		Collection<AddressCompany> allSpaces = compDao.findAll();
		assertEquals(1, allSpaces.size());
	}

	// verifying that the relation and the traversal from the owning to the
	// owned side works
	@Test
	public void testGettingAddressFromEmployee() {

		List<AddressEmployee> allEmployees = empDao.findAll();
		AddressEmployee e = allEmployees.get(0);

		assertEquals("Florida", e.getAddress().getState());
	}

	// verifying that the relation and the traversal from the owned to the
	// owning side works
	@Test
	public void testGettingAddressFromCompany() {

		List<AddressCompany> allCompanies = compDao.findAll();
		AddressCompany d = allCompanies.get(0);

		assertEquals("Quebec", d.getAddress().getState());
	}

	// can two entities share a single eddress? yes they can. maybe it would not
	// be possible inside a common transaction?
	@Test
	public void testSharingSingleInstanceBetweenEntities() {

		List<AddressCompany> allCompanies = compDao.findAll();
		AddressCompany d = allCompanies.get(0);

		List<AddressEmployee> allEmployees = empDao.findAll();
		AddressEmployee e = allEmployees.get(0);

		e.setAddress(d.getAddress());

		empDao.create(e);

		Map<String, String> ourManInQuebec = new HashMap<String, String>();
		ourManInQuebec.put("address.state", "Quebec");

		List<AddressEmployee> foundList = empDao.find(ourManInQuebec);
		AddressEmployee found = foundList.get(0);

		assertEquals("Quebec", found.getAddress().getState());
	}

}
