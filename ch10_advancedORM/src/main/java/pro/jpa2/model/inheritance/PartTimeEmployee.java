package pro.jpa2.model.inheritance;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * A simple Entity, taken from the Pro JPA2 book.
 *
 * @author kostja
 *
 */
@Entity
public abstract class PartTimeEmployee extends CompanyEmployee {
	@Column(name="H_RATE")
	private long hourlyRate;

	public long getHourlyRate() {
		return hourlyRate;
	}

	public void setHourlyRate(long vacation) {
		this.hourlyRate = vacation;
	}

}