package pro.jpa2.em;

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

import pro.jpa2.model.EmployeeCustomId;

/**
 * Trying to isolate the unexpected behaviour of the contains method - the
 * persisted ofject is found but seems not to be managed - what the fuck?
 *
 * The relation between an EM and it's PersistenceContext should be ManyToOne.
 *
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
// @Stateful
public class TransactionAndContextTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addPackages(true, "pro.jpa2")
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Inject
	Logger log;

	@Inject
	UserTransaction tx;

	@PersistenceContext
	EntityManager em;

	@Test
	public void testTransactionAndContextScope() throws Exception {

		EmployeeCustomId e = new EmployeeCustomId();
		e.setId(42);
		// not managed before transaction
		log.info("ctx contains the entity before calling persist : {}",
				em.contains(e));

		tx.begin();
		em.persist(e);
		// already managed, but is it reliably so?
		log.info("ctx contains the entity before flush: {}", em.contains(e));
		em.flush();
		// only here the entity is actualy managed
		log.info("ctx contains the entity after flush: {}", em.contains(e));
		tx.commit();
		log.info("ctx contains the entity outside transaction: {}",
				em.contains(e));

		EmployeeCustomId found = em.find(EmployeeCustomId.class, 42);
		log.info("found : {}", found);
		log.info("ctx contains the found entity outside transaction: {}",
				em.contains(found));

		// the original and the found entity are not managed, even if a new
		// transaction is started
		tx.begin();
		log.info("ctx contains the original entity in new transaction: {}",
				em.contains(e));
		log.info("ctx contains the found entity in new transaction: {}",
				em.contains(found));
		tx.commit();

		// if the entity is found inside the transaction, it is managed
		tx.begin();
		found = em.find(EmployeeCustomId.class, 42);
		log.info("found : {}", found);
		log.info(
				"ctx contains the newly found entity inside new transaction: {}",
				em.contains(found));
		tx.commit();
	}
}
