package pro.jpa2.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.UserTransaction;

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

import pro.jpa2.model.UploadBidirectional;
import pro.jpa2.model.UploadFileBidirectional;

/**
 * Having encountered a strange issue at work. Two entities - Upload and
 * UploadFile. Upload has an Id. Uploadile has the Upload's Id as a Foreign key
 * (Join column), but also as a part of the PK. When an Upload is removed, the
 * persistence provider tries to set the FK of the connected UploadFiles to
 * NULL. This fails - as a part of the PK, the IDs are not updateable.
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
public class ForeignKeyUpdateBidirectionalTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addPackages(true, "pro.jpa2")
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				// a safer way to seed with Hibernate - the @UsingDataSet breaks
				.addAsResource("testSeeds/1Upload1FileBidirectional.sql",
						"import.sql")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Inject
	GenericDao<UploadBidirectional> dao;

	@Inject
	GenericDao<UploadFileBidirectional> fileDao;

	@Inject
	UserTransaction tx;

	@Inject
	Logger log;

	@Before
	public void before() {
		dao.setKlazz(UploadBidirectional.class);
		fileDao.setKlazz(UploadFileBidirectional.class);
	}

	@Test
	public void testFindUpload() throws Exception {
		log.warn("------------------------------------------------------------------");
		log.warn("started findAll test");

		tx.begin();
		List<UploadBidirectional> allUploads = dao.findAll();

		assertEquals(1, allUploads.size());
		assertEquals(1, allUploads.get(0).getFiles().size());
		tx.commit();
	}

	@Test
	public void testFindFile() throws Exception {
		log.warn("------------------------------------------------------------------");
		log.warn("started findFile test");

		tx.begin();
		List<UploadFileBidirectional> allFiles = fileDao.findAll();

		assertEquals(1, allFiles.size());
		assertNotNull(allFiles.get(0).getUpload());
		tx.commit();
	}

	// for some reason, if the relation is shaped as it is, the removal of the
	// upload fails - Hibernate tries to keep the UploadFile entity consistent
	// by setting it'S FK to null
	@Test(expected = Exception.class)
	public void testRemoveUploadOnly() throws Exception {
		log.warn("------------------------------------------------------------------");
		log.warn("started remove upload test");

		tx.begin();
		List<UploadBidirectional> allUploads = dao.findAll();

		assertEquals(1, allUploads.size());
		UploadBidirectional upload = allUploads.get(0);
		assertEquals(1, upload.getFiles().size());

		// if the uploadFIle isnot removed here, an exception is thrown
		// dao.getEm().remove(upload.getFiles().get(0));
		dao.getEm().remove(upload);
		tx.commit();
	}

	/**
	 * simulating the removal cascade - removing the uploadfile first
	 *
	 * @throws Exception
	 */
	@Test
	public void testRemoveUploadFileThenUpload() throws Exception {
		log.warn("------------------------------------------------------------------");
		log.warn("started manual remove cascade test");

		tx.begin();
		List<UploadBidirectional> allUploads = dao.findAll();

		assertEquals(1, allUploads.size());
		UploadBidirectional upload = allUploads.get(0);
		assertEquals(1, upload.getFiles().size());

		dao.getEm().remove(upload.getFiles().get(0));
		dao.getEm().remove(upload);
		tx.commit();
	}
}
