package pro.jpa2.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * The inverse side of the Employee-Department relation.
 *
 * @author kostja
 *
 */
@Entity
public class Department {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String name;

	// Bidirectional OneToMany relation,
	@OneToMany(mappedBy = "department", fetch=FetchType.EAGER)
	// if not using generics but plain Collection instead, you must use the
	// {@code targetEntity=Employee.class} attribute of the OneToMany annotation
	private Collection<Employee> employees;

	public Department() {
		employees = new ArrayList<Employee>();
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

	public void setName(String deptName) {
		this.name = deptName;
	}

	public void addEmployee(Employee employee) {
		if (!getEmployees().contains(employee)) {
			getEmployees().add(employee);
			if (employee.getDepartment() != null) {
				employee.getDepartment().getEmployees().remove(employee);
			}
			employee.setDepartment(this);
		}
	}

	public Collection<Employee> getEmployees() {
		return employees;
	}

	public String toString() {
		return "Department id: " + getId() + ", name: " + getName();
	}
}