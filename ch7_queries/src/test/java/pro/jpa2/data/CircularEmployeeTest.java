package pro.jpa2.data;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

import pro.jpa2.model.CircularEmployee;
import pro.jpa2.util.Resources;

@RunWith(Arquillian.class)
// @UsingDataSet("employeeTestData.yml")
public class CircularEmployeeTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addClasses(CircularEmployee.class, Resources.class)
				// .addPackages(true, "pro.jpa2")
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				.addAsResource("testSeeds/2CircularEmployees.sql", "import.sql")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@PersistenceContext
	EntityManager em;

	@Inject
	UserTransaction tx;

	@Inject
	Logger log;

	// just to see if test is working at all
	@Test(expected = Exception.class)
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void testFindAll() throws Exception {
		String query = "SELECT e FROM CircularEmployee e WHERE e.id = 1";

		tx.begin();
		CircularEmployee employee = em.createQuery(query,
				CircularEmployee.class).getSingleResult();
		tx.commit();
		log.info("retrieving the boss: {}", employee.getBoss());
	}
}
