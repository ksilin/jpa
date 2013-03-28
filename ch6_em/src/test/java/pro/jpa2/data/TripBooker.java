package pro.jpa2.data;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;

import pro.jpa2.model.Employee;


@Stateful
public class TripBooker {

	@PersistenceContext
	EntityManager em;
	
	@Inject
	Logger log;
	
	public void bookTrip(){
		
		Employee emp = new Employee();
		emp.setName("Rob Anybody");
		em.persist(emp);
		
		bookHotel();

        printAllPersistedEmployees();
		
		bookFlight();
	}

    private void printAllPersistedEmployees() {
        TypedQuery<Employee> tq = em.createQuery("SELECT e FROM Employee e", Employee.class);

        log.info("-----------------------");
        for(Employee e: tq.getResultList()){
            log.info("saved empoyee: {}", e);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private void bookHotel() {

		Employee emp = new Employee();
		emp.setName("Dingus");
		em.persist(emp);
		
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private void bookFlight() {
		Employee emp = new Employee();
		emp.setName("Frank Sinatra");
		em.persist(emp);
		
		throwUp();
	}

	private void throwUp() {
		throw new RuntimeException("hihi");
	}
	
}
