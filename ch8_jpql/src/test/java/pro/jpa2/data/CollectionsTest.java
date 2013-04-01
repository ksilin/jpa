package pro.jpa2.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.*;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import pro.jpa2.model.Address;
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
public class CollectionsTest {
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

	// TODO: actually, the book states that this should be impossible - p.214
	// The following query is illegal:
	// SELECT d.employees
	// FROM Department d
	@Test
	public void gettingCollectionPropertyIsIllegal() throws Exception {

		String queryString = "SELECT d.employees FROM Department d";

		TypedQuery<Collection> query = em.createQuery(queryString,
				Collection.class);
		List<Collection> allResults = query.getResultList();

		log.info("retrieved employees: {}", allResults);

		assertEquals(3, allResults.size());
	}

	// asserting that the external variable e is accessible inside the subquery
	// and can be navigated
	@Test
	public void testInWithSubqueries() throws Exception {
		log.warn("------------------------------------------------------------------");

		// the subquery returns all departments that have employees working on
		// projects
		String subqueryString = "SELECT DISTINCT d FROM Department d JOIN d.employees de JOIN de.projects p WHERE p.name LIKE 'qual%'";
		String queryString = "SELECT e FROM Employee e WHERE e.department IN ("
				+ subqueryString + ")";

		TypedQuery<Department> subquery = em.createQuery(subqueryString,
				Department.class);

		List<Department> dept = subquery.getResultList();
		log.info("found the departments: {}", dept);

		TypedQuery<Employee> query = em
				.createQuery(queryString, Employee.class);

		List<Employee> emp = query.getResultList();
		log.info("found the employees: {}", emp);
	}

	// whats teh difference between MEMBER OF and IN - can we use them
	// interchangeably
	@Test
	public void differennceBetweenInAndMemberOf() throws Exception {
		log.warn("------------------------------------------------------------------");

		// the subquery returns all departments that have employees working on
		// projects
		String projectByNameQueryString = "SELECT p FROM Project p WHERE p.name LIKE :name";

		Project p = em.createQuery(projectByNameQueryString, Project.class)
				.setParameter("name", "qual%").getSingleResult();
		log.info("found the project: {}", p);

		// find all employees in a certain project
		String queryString = "SELECT e FROM Employee e WHERE :project IN (SELECT p FROM e.projects p)";

		TypedQuery<Employee> query = em
				.createQuery(queryString, Employee.class);
		query.setParameter("project", p);

		List<Employee> emp = query.getResultList();
		log.info("found the employees: {}", emp);

		String memebrQueryString = "SELECT e FROM Employee e WHERE :project MEMBER OF e.projects";
		TypedQuery<Employee> query2 = em.createQuery(memebrQueryString,
				Employee.class);
		query2.setParameter("project", p);

		List<Employee> emp2 = query2.getResultList();
		log.info("found the employees again: {}", emp2);

		assertTrue(emp2.containsAll(emp));
		assertTrue(emp.containsAll(emp2));

		// this is illegal - literal or subquery only for IN
		String memberQueryString3 = "SELECT e FROM Employee e WHERE :project IN e.projects";

		// this is illegal - id var/path only for MEMBER OF
		String memberQueryString4 = "SELECT e FROM Employee e WHERE :project MEMBER OF (SELECT p FROM e.projects p)";

	}

	// is it possible to retreive the results of a subquery first, and then use
	// them in a IN statement
	@Test
	public void usingParametersWithIn() {

		// String projectByNameQueryString =
		// "SELECT p FROM Project p WHERE p.name LIKE :name";
		//
		// Project p = em.createQuery(projectByNameQueryString, Project.class)
		// .setParameter("name", "qual%").getSingleResult();
		// log.info("found the project: {}", p);

		String singleLiteral = "John Smith";// , 'Jack Smith'";

		String queryString = "SELECT e.name FROM Employee e WHERE e.name = :names";
		TypedQuery<String> query = em.createQuery(queryString, String.class);
		query.setParameter("names", singleLiteral);
		log.info("parameter from query: {}", query.getParameter("names"));
		List<String> results = query.getResultList();
		log.info("found the employee names: {}", results);
		assertEquals(1, results.size());

		@SuppressWarnings("serial")
		List<String> literalList = new ArrayList<String>() {
			{
				add("John Smith");
				add("Jack Smith");
			}
		};

		String queryString2 = "SELECT e.name FROM Employee e WHERE e.name IN :names";
		TypedQuery<String> query2 = em.createQuery(queryString2, String.class);
		query2.setParameter("names", literalList);
		log.info("parameter from query: {}", query2.getParameter("names"));
		List<String> results2 = query2.getResultList();
		log.info("found the employee names: {}", results2);
		assertEquals(2, results2.size());

	}

	// is it possible to retreive the results of a subquery first, and then use
	// them in a IN statement
	@Test
	public void passSubqueryResultsToIn() {

		String projectByNameQueryString =
		"SELECT p FROM Project p WHERE p.name LIKE :name";

		Project p = em.createQuery(projectByNameQueryString, Project.class)
		.setParameter("name", "qual%").getSingleResult();
		log.info("found the project: {}", p);

		String queryString = "SELECT e FROM Employee e WHERE :project IN :projects";
		TypedQuery<Employee> query3 = em.createQuery(queryString,
				Employee.class);
		query3.setParameter("project", p);

        List<Project> projectSubqueryResults =
                em.createQuery("SELECT p FROM Project p WHERE p.employees IS NOT EMPTY",
                        Project.class).getResultList();

        List<Project> queryByExampleProjects = new ArrayList<Project>();
        for (Project attachedProject : projectSubqueryResults) {
            // detaching seems to be insufficient
            em.detach(attachedProject);
            Project example = new Project();
            example.setId(attachedProject.getId());
            queryByExampleProjects.add(example);
        }

		query3.setParameter("projects", queryByExampleProjects);

		List<Employee> emp3 = query3.getResultList();
		log.info("found the employees again: {}", emp3);
	}

    @Test
    public void passListOfNewEntitesToIn() throws HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException, NotSupportedException {

        Project p2 = new Project();
        p2.setId(1);

        String queryString = "SELECT e FROM Employee e WHERE :project IN e.projects";
        TypedQuery<Employee> query3 = em.createQuery(queryString,
                Employee.class);
        query3.setParameter("project", p2);

        List<Project> queryByExampleProjects = new ArrayList<Project>();
        for (int i = 0; i < 3; i++) {
            Project example = new Project();
            example.setId(i);
            queryByExampleProjects.add(example);
        }

        query3.setParameter("projects", queryByExampleProjects);

        List<Employee> emp3 = query3.getResultList();
        log.info("found the employees again: {}", emp3);
    }

	@Test
	public void exists() {

		// selecting all employees without mobile phones
		String queryString = "SELECT e FROM Employee e WHERE NOT EXISTS (SELECT p FROM e.phones p WHERE p.type='mobile')";

		TypedQuery<Employee> q = em.createQuery(queryString, Employee.class);

		List<Employee> results = q.getResultList();

		assertEquals(2, results.size());
	}

	// setting the class name per parameter does not work :(
	public <T> void assertEntity(Class<T> klazz, final int expectedSize) {

		List<T> allResults = getAll(klazz);
		assertNotNull(allResults);
		assertEquals(expectedSize, allResults.size());
	}

	private <T> List<T> getAll(Class<T> klazz) {

		// you cannot parametrize the class of the query using regular params,
		// like in the WHEN clause
		// String queryString = "SELECT e FROM :className  e";
		String queryString = "SELECT e FROM " + klazz.getSimpleName() + "  e";

		TypedQuery<T> query = em.createQuery(queryString, klazz);
		// does not work in the FROM clause, seee above
		// query.setParameter("className", klazz.getSimpleName());
		List<T> allResults = query.getResultList();
		return allResults;
	}
}
