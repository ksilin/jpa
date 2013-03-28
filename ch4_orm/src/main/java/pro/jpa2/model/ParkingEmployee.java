package pro.jpa2.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * A simple Entity, taken from the Pro JPA2 book.
 *
 * Has a ManyToOne relation to the {@link ParkingSpace}
 *
 * @author kostja
 *
 */
@Entity
public class ParkingEmployee {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String name;
	private long salary;

	@OneToOne
	// @JoinColumn is always on the owning side
	// leaving the default join column name - PARKING_ID
	@JoinColumn
	private ParkingSpace parking;

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

	public ParkingSpace getParking() {
		return parking;
	}

	public void setParking(ParkingSpace parking) {
		this.parking = parking;
	}

	@Override
	public String toString() {
		return "ParkingEmployee [id=" + id + ", name=" + name + ", salary="
				+ salary + ", parking=" + parking + "]";
	}

}