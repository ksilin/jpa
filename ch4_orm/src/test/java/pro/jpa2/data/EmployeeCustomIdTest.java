package pro.jpa2.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.hamcrest.Description;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import pro.jpa2.model.EmployeeCustomId;

/**
 * Testing an entity with non-generated (custom) Ids.
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
// @UsingDataSet("employeeTestData.yml")
public class EmployeeCustomIdTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				// .addClasses(Employee.class, GenericDao.class, Ordering.class,
				// Resources.class)
				.addPackages(true, "pro.jpa2")
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@PersistenceContext
	EntityManager em;

	@Inject
	UserTransaction tx;

	@Inject
	Logger log;

	@Test
	public void testFindAll() throws Exception {
		log.warn("------------------------------------------------------------------");
		log.warn("started EmployeeCustomId persistence test: empty db");

		final CriteriaQuery<EmployeeCustomId> query = em.getCriteriaBuilder()
				.createQuery(EmployeeCustomId.class);
		final Root<EmployeeCustomId> root = query.from(EmployeeCustomId.class);
		query.select(root);

		Collection<EmployeeCustomId> allEmployees = em.createQuery(query)
				.getResultList();
		assertTrue(allEmployees.isEmpty());
	}

	/**
	 * If the Id is not generated and not set, but still has a default value, no
	 * exception is thrown and no id is generated. A generated id would normally
	 * start with 1, here 0 is perfectly OK
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateNewWithDefaultId() throws Exception {
		log.warn("------------------------------------------------------------------");
		log.warn("started test: creating a new employee without setting an id explicitly");

		EmployeeCustomId e = new EmployeeCustomId();
		e.setName("noname");
		log.info("employee id before persisting : {}", e.getId());
		assertThat(e, hasId(0));
		tx.begin();
		em.persist(e);
		assertTrue(em.contains(e));
		tx.commit();
		log.info("employee id after persisting : {}", e.getId());
		assertThat(e, hasId(0));
	}

	/**
	 * Cannot persist entity with same Id
	 *
	 * @throws Exception
	 */
	@Test
	// (expected = PersistenceException.class)
	public void testCreateNewWithDefaultIdTwice() {
		log.warn("------------------------------------------------------------------");
		log.warn("started test: creating a new employee wit the same id twice");

		EmployeeCustomId e = new EmployeeCustomId();
		e.setName("first");
		log.info("first employee id before persisting : {}", e.getId());
		assertThat(e, hasId(0));
		e.setId(23);

		try {
			tx.begin();

			em.persist(e);
			assertTrue(em.contains(e));

			log.info("first employee id after persisting : {}", e.getId());
			assertThat(e, hasId(23));

			// expected to merge, effectively replacing the data of the first
			// entity with the second one
			e = new EmployeeCustomId();
			e.setName("second");
			log.info("second employee id before persisting : {}", e.getId());
			assertThat(e, hasId(0));
			e.setId(23);

			em.persist(e);
			assertTrue(em.contains(e));

			// will throw here - the entity is already managed
			tx.commit();
		} catch (Throwable e1) {
			try {
				tx.rollback();
				// just swallow them
			} catch (IllegalStateException e2) {
			} catch (SecurityException e2) {
			} catch (SystemException e2) {
			}
		}
	}

	/**
	 * Merging entities
	 */
	@Test
	public void testMergeEntities() throws Exception {
		log.warn("------------------------------------------------------------------");
		log.warn("started test: merging entities");

		// creating the first entity - this one will be merged into
		EmployeeCustomId e = new EmployeeCustomId();
		e.setName("first");
		log.info("first employee id before persisting : {}", e.getId());
		assertThat(e, hasId(0));
		e.setId(42);

		tx.begin();

		em.persist(e);
		assertTrue(em.contains(e));
		log.info("first employee id after persisting : {}", e.getId());
		assertThat(e, hasId(42));

		// creating the second entity - this one will overwrite the data of the
		// first one
		e = new EmployeeCustomId();
		e.setName("second");
		assertThat(e, hasId(0));
		e.setId(42);
		log.info("second employee id before persisting : {}", e.getId());

		// expected to merge, effectively replacing the data of the first entity
		// with the second one
		EmployeeCustomId managedEntity = em.merge(e);
		// merging does not make the passed entity managed
		assertFalse(em.contains(e));
		assertThat(e, hasId(42));
		assertThat(managedEntity, hasId(42));
		// be sure that it's actually the merged entity
		assertThat(e, hasName("second"));

		tx.commit();
	}

	/**
	 * User-defined Id should remain unchanged
	 */
	@Test
	public void testCreateNewWithSetId() throws Exception {

		log.warn("------------------------------------------------------------------");
		log.warn("started test: creating a new employee and setting an id explicitly");

		EmployeeCustomId e = new EmployeeCustomId();
		e.setName("noname");
		log.info("employee id before persisting : {}", e.getId());

		assertThat(e, hasId(0));
		e.setId(99);
		tx.begin();

		em.persist(e);

		tx.commit();
		assertThat(e, hasId(99));
	}

	// Using hamcrest as a DSL provider for the tests
	public static TypeSafeMatcher<EmployeeCustomId> hasId(final int expectedId) {

		return new TypeSafeMatcher<EmployeeCustomId>() {

			protected int expected = expectedId;

			@Override
			public void describeTo(Description description) {
				description.appendText(Integer.toString(expectedId));
			}

			@Override
			public boolean matchesSafely(EmployeeCustomId e) {
				return e.getId() == expected;
			}
		};
	}

	// Using hamcrest as a DSL provider for the tests
	public static TypeSafeMatcher<EmployeeCustomId> hasName(
			final String expectedName) {

		return new TypeSafeMatcher<EmployeeCustomId>() {

			protected String expected = expectedName;

			@Override
			public void describeTo(Description description) {
				description.appendText(expectedName);
			}

			@Override
			public boolean matchesSafely(EmployeeCustomId e) {
				return e.getName() == expected;
			}
		};
	}
}
