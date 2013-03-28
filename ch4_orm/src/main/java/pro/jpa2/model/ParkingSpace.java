package pro.jpa2.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * A simple Entity, taken from the Pro JPA2 book.
 *
 * Has a OneToOne relation to the {@link Employee}
 *
 * @author kostja
 *
 */
@Entity
public class ParkingSpace {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	// this is the reverse side of the relationship - only the owning sida may
	// declare the join column
	// without the mappedBy attribute, the employee is not reachable from here
	@OneToOne(mappedBy = "parking")
	private ParkingEmployee employee;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ParkingEmployee getEmployee() {
		return employee;
	}

	public void setEmployee(ParkingEmployee employee) {
		this.employee = employee;
	}

	@Override
	public String toString() {
		return "ParkingSpace [id=" + id + ", employee=" + employee + "]";
	}

}