package pro.jpa2.model;


/**
 * Instances of this class will be constructed directly from wueries
 *
 * @author kostja
 *
 */
public class ConstructedEmployee {

	private String name;
	private long salary;

	private Department department;

	public ConstructedEmployee(String name, long salary, Department department) {
		super();
		this.name = name;
		this.salary = salary;
		this.department = department;
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

	@Override
	public String toString() {
		return "ConstructedEmployee [name=" + name + ", salary=" + salary
				+ ", department=" + department + "]";
	}
}