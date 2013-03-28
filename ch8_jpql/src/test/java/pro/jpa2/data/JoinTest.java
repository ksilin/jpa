package pro.jpa2.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
public class JoinTest {
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

	// asserting that different ways of join queries return the same result
	@Test
	public void testImplicitAndExplicitJoinCollections() throws Exception {
		log.warn("------------------------------------------------------------------");

		String explicit = "SELECT p	FROM Employee e JOIN e.phones p";

		TypedQuery<Phone> query = em.createQuery(explicit, Phone.class);
		List<Phone> explResults = query.getResultList();
		log.info("found phones explicitly: {}", explResults);
		assertEquals(4, explResults.size());

		// not quite the same, just similar
		String implicit = "SELECT e.phones FROM Employee e";

		TypedQuery<Collection> queryImpl = em.createQuery(implicit,
				Collection.class);
		List<Collection> implResults = queryImpl.getResultList();

		log.info("found phones implicitly: {}", implResults);
		assertEquals(4, implResults.size());

		// TODO: I'm not quite sure why this is true, the second result list is
		// of type Collection
		assertTrue(implResults.containsAll(explResults));
	}

	// asserting that different ways of join queries return the same result
	@Test
	public void testImplicitAndExplicitJoinSingleValues() throws Exception {
		log.warn("------------------------------------------------------------------");

		String explicit = "SELECT d	 FROM Employee e JOIN e.department d";

		TypedQuery<Department> query = em.createQuery(explicit,
				Department.class);
		List<Department> explResults = query.getResultList();
		log.info("found departments explicitly: {}", explResults);
		assertEquals(3, explResults.size());

		// should result in the same SQL
		String implicit = "SELECT e.department FROM Employee e";

		TypedQuery<Department> queryImpl = em.createQuery(implicit,
				Department.class);
		List<Department> implResults = queryImpl.getResultList();

		log.info("found departments implicitly: {}", implResults);
		assertEquals(3, implResults.size());

		assertTrue(implResults.containsAll(explResults));

		// should result in the same SQL
		// used of no explicit relation is specified in the model - join
		// codition in the WHERE clause
		String overId = "SELECT d FROM Department d, Employee e WHERE d = e.department";

		TypedQuery<Department> queryId = em.createQuery(overId,
				Department.class);
		List<Department> idResults = queryId.getResultList();

		log.info("found departments with id join: {}", idResults);
		assertEquals(3, idResults.size());
		assertTrue(idResults.containsAll(explResults));
	}

	// testing fetch join - here we would like to be able to navigate from the
	// dept to it's employees after detachment
	@Test(expected = Exception.class)
	public void joinFetch() throws Exception {
		log.warn("------------------------------------------------------------------");

		// we use left join to retrieve departments without employees as well
		// (we have none, so this does not change anything)
		String explicit = "SELECT d FROM Department d LEFT JOIN FETCH d.employees";

		TypedQuery<Department> query = em.createQuery(explicit,
				Department.class);
		tx.begin();
		List<Department> explResults = query.getResultList();
		tx.commit();

		for (Department d : explResults) {
			log.info("employees: of dept {}, {}", d, d.getEmployees());
		}

		// should result in the same SQL
		String noFetch = "SELECT d	FROM Department d";
		TypedQuery<Department> queryNoFetch = em.createQuery(noFetch,
				Department.class);
		tx.begin();
		List<Department> results = queryNoFetch.getResultList();
		tx.commit();

		// will throw a LazyInitializationException
		log.info("{}", results.get(0).getEmployees());
	}

}
