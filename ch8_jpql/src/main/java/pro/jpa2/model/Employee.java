package pro.jpa2.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * A simple Entity, taken from the Pro JPA2 book.
 *
 * @author kostja
 *
 */
@Entity
@NamedQueries({ @NamedQuery(name = "Employee.findAll", query = "SELECT e FROM Employee e") })
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String name;
	private long salary;

	@ManyToOne
	@JoinColumn(name = "DEPT_ID")
	private Department department;

	@ManyToMany(mappedBy = "employees")
	private List<Project> projects = new ArrayList<Project>();

	@OneToMany(mappedBy = "employee")
	private List<Phone> phones = new ArrayList<Phone>();;

	@Embedded
	private Address address;

	@ManyToOne
	private Employee manager;

	@OneToMany(mappedBy = "manager")
	List<Employee> directs = new ArrayList<Employee>();

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSalary() {
		return salary;
	}

	public void setSalary(long salary) {
		this.salary = salary;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public List<Phone> getPhones() {
		return phones;
	}

	public void setPhones(List<Phone> phone) {
		this.phones = phone;
	}

	public Employee getManager() {
		return manager;
	}

	public void setManager(Employee manager) {
		this.manager = manager;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public List<Employee> getDirects() {
		return directs;
	}

	public void setDirects(List<Employee> directs) {
		this.directs = directs;
	}

	@Override
	public String toString() {
		return "Employee [id=" + id + ", name=" + name + ", salary=" + salary
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Employee other = (Employee) obj;
		if (id != other.id)
			return false;
		return true;
	}
}