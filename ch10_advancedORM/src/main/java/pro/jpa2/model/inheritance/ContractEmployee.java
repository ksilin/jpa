package pro.jpa2.model.inheritance;

import javax.persistence.Entity;


/**
 * A simple Entity, taken from the Pro JPA2 book.
 *
 * @author kostja
 *
 */
@Entity
public abstract class ContractEmployee extends Employee{

	private int vacation;

	public int getVacation() {
		return vacation;
	}

	public void setVacation(int vacation) {
		this.vacation = vacation;
	}

}