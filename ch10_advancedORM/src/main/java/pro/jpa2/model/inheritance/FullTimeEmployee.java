package pro.jpa2.model.inheritance;

import javax.persistence.Entity;

/**
 * A simple Entity, taken from the Pro JPA2 book.
 *
 * @author kostja
 *
 */
@Entity
public abstract class FullTimeEmployee extends CompanyEmployee {

	private long pension;

	public long getPension() {
		return pension;
	}

	public void setPension(long pension) {
		this.pension = pension;
	}

}