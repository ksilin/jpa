package pro.jpa2.model;

import java.util.Collection;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * A simple Entity, used to illustrate {@link @ElementCollection} use
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

	// nothing helps, not even FetchType=EAGER - still getting a
	// LazyInitializationExample in the loggin statement in the
	// findAllTest of BasicEmployeeTest
	@ElementCollection(targetClass = VacationEntry.class, fetch = FetchType.EAGER)
	private Collection vacations;

	@ElementCollection
	// further, a @CollectionTable annotation can be used to customize the
	// mapping p.108
	// the default name for the table is EMPLOYEE_NICKNAMES
	private Set<String> nicknames;

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

	public Set<String> getNicknames() {
		return nicknames;
	}

	public void setNicknames(Set<String> nicknames) {
		this.nicknames = nicknames;
	}

	public Collection getVacations() {
		return vacations;
	}

	public void setVacations(Collection vacations) {
		this.vacations = vacations;
	}

	@Override
	public String toString() {
		return "Employee [id=" + id + ", name=" + name + ", salary=" + salary
				+ ", vacations=" + vacations + ", nicknames=" + nicknames + "]";
	}
}