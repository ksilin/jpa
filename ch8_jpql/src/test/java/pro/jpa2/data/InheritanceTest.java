package pro.jpa2.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
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
import pro.jpa2.model.DesignProject;
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
public class InheritanceTest {
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

		// selecting a derived type using TYPE
		String query = "SELECT p FROM Project p WHERE TYPE(p) IN (DesignProject, QualityProject)";

		// must use the base class from the query type
		// the result may be safely cast afterwards
		TypedQuery<Project> projectQuery = em.createQuery(query, Project.class);

		List<Project> resultList = projectQuery.getResultList();

		assertEquals(2, resultList.size());
	}

}
