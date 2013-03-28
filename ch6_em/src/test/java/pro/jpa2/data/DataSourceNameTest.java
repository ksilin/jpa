package pro.jpa2.data;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Map;

/**
 * Testing an Employee entity with generated Ids.
 *
 * @author kostja
 *
 */
@RunWith(Arquillian.class)
public class DataSourceNameTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class, "DataSourceNameTest.war")
				.addPackages(true, "pro.jpa2")
				.addAsResource("META-INF/persistence.xml",
                        "META-INF/persistence.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

    @PersistenceContext
    EntityManager em;

	@Inject
	Logger log;

	// trying to get the name of teh used datasource
	@Test
	public void testGetDataSourceName() throws Exception {
		log.info("------------------------------------------------------------------");
		log.info("started testGetDataSourceName test");

        DataSource datasource1 = (DataSource)em.getEntityManagerFactory().getProperties().get("javax.persistence.jtaDataSource");
        DataSource datasource2 = (DataSource)em.getEntityManagerFactory().getProperties().get("javax.persistence.nonJtaDataSource");
        log.info("datasource 1: {}", datasource1);
        log.info("datasource 2: {}", datasource2);
    }

    @Test
    public void testGetEMFProperties() throws Exception {
        log.info("------------------------------------------------------------------");
        log.info("started testGetEMFProperties test");

        Map<String,Object> props = em.getEntityManagerFactory().getProperties();
            for(Map.Entry<String, Object> prop : props.entrySet())  {
                log.info(" {} : {}", prop.getKey(), prop.getValue());
            }
    }
}
