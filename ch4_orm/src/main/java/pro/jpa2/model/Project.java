package pro.jpa2.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

/**
 * A simple Entity, taken from the Pro JPA2 book.
 *
 * Has a ManyToMany relation to the {@link ProjectEmployee}
 *
 * @author kostja
 *
 */
@Entity
public class Project {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	// this is the reverse side of the relationship
	// TODO : do I need a mappedBy attribute without join column?
	@ManyToMany
	// (mappedBy="projects")
	private List<ParkingEmployee> employees;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<ParkingEmployee> getEmployees() {
		return employees;
	}

	public void setEmployees(List<ParkingEmployee> employees) {
		this.employees = employees;
	}

	@Override
	public String toString() {
		return "Project [id=" + id + ", employees=" + employees + "]";
	}
}