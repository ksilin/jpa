package pro.jpa2.model.inheritance;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;


/**
 * A simple Entity, taken from the Pro JPA2 book.
 *
 * @author kostja
 *
 */
@MappedSuperclass
public class CompanyEmployee extends Employee{

	@Column(name="D_RATE")
	private int dailyRate;
	private int term;

	public int getDailyRate() {
		return dailyRate;
	}

	public void setDailyRate(int dailyRate) {
		this.dailyRate = dailyRate;
	}

	public int getTerm() {
		return term;
	}
	public void setTerm(int term) {
		this.term = term;
	}
}
