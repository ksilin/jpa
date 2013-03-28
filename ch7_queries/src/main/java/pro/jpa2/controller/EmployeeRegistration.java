package pro.jpa2.controller;

import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.slf4j.Logger;

import pro.jpa2.model.Employee;

// The @Stateful annotation eliminates the need for manual transaction demarcation
@Stateful
// The @Model stereotype is a convenience mechanism to make this a
// request-scoped bean that has an
// EL name
// Read more about the @Model stereotype in this FAQ:
// http://sfwk.org/Documentation/WhatIsThePurposeOfTheModelAnnotation
@Model
public class EmployeeRegistration {

	@Inject
	private Logger log;

	@Inject
	EntityManager em;

	/**
	 * Fires an event, informing about a newly created Employee
	 */
	@Inject
	private Event<Employee> memberEventSrc;

	private Employee newEmployee;

	@Produces
	@Named
	public Employee getNewEmployee() {
		return newEmployee;
	}

	public void register() throws Exception {
		log.info("Registering " + newEmployee.getName());
		em.persist(newEmployee);
		memberEventSrc.fire(newEmployee);
		initNewEmployee();
	}

	@PostConstruct
	public void initNewEmployee() {
		newEmployee = new Employee();
	}
}
