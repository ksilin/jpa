package pro.jpa2.model.inheritance;

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

/**
 * Testing setting parameters for JPA queries - named and numbered params
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
public class MappedSuperclassTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addPackages(true, "pro.jpa2.model.inheritance")
				.addPackages(true, "pro.jpa2.data")
				.addPackages(true, "pro.jpa2.util")
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				// a safer way to seed with Hibernate - the @UsingDataSet breaks
				.addAsResource("testSeeds/MappedSuperclass.sql", "import.sql")
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

		String queryString = "SELECT e FROM FullTimeEmployee e";

		TypedQuery<FullTimeEmployee> query = em
				.createQuery(queryString, FullTimeEmployee.class);
		List<FullTimeEmployee> allResults = query.getResultList();
		log.info("no matter what the inheritance strategy was, I have managed to retrieve teh needed entities:");
		for(FullTimeEmployee e: allResults){
			log.info("{}", e);
		}

	}
}
