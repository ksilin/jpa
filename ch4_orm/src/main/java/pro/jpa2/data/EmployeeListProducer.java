package pro.jpa2.data;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import pro.jpa2.model.Employee;

@RequestScoped
public class EmployeeListProducer {
   @Inject
   private EntityManager em;

   private List<Employee> employees;

   @Inject
   Logger log;

   // @Named provides access the return value via the EL variable name "members" in the UI (e.g.,
   // Facelets or JSP view)
   @Produces
   @Named
   public List<Employee> getEmployees() {
	   return employees;
   }

   /**
    * Listens to an {@link Employee} {@link Event}
    * @param newEmployee
    */
   public void onMemberListChanged(@Observes(notifyObserver = Reception.IF_EXISTS) final Employee newEmployee) {
      retrieveAllEmployeesOrderedByName();
   }

   @PostConstruct
   public void retrieveAllEmployeesOrderedByName() {
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<Employee> criteria = cb.createQuery(Employee.class);
      Root<Employee> member = criteria.from(Employee.class);
      // Swap criteria statements if you would like to try out type-safe criteria queries, a new
      // feature in JPA 2.0
      // criteria.select(member).orderBy(cb.asc(member.get(Member_.name)));
      criteria.select(member).orderBy(cb.asc(member.get("name")));
      employees = em.createQuery(criteria).getResultList();
   }
}
