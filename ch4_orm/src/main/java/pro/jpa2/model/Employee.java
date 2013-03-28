package pro.jpa2.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * A simple Entity, taken from the Pro JPA2 book.
 *
 * Has a ManyToOne relation to the {@link Department}
 *
 * @author kostja
 *
 */
@Entity
public class Employee {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String name;
	private long salary;

	@ManyToOne
	//@JoinColumn is always on the owning side
	//overriding the default join column name - DEPARTMENT_ID
	@JoinColumn(name = "DEPT_ID")
	private Department department;

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

	public String toString() {
		return "Employee id: " + getId() + " name: " + getName()
				+ " with department: " + getDepartment();
	}
}