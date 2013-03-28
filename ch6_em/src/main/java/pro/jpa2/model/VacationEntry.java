package pro.jpa2.model;

import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A simple Embeddable, used to illustrate {@link @ElementCollection} use
 *
 * @author kostja
 *
 */
@Embeddable
public class VacationEntry {

	@Temporal(TemporalType.DATE)
	private Date startDate;

	private int daysTaken;

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public int getDaysTaken() {
		return daysTaken;
	}

	public void setDaysTaken(int daysTaken) {
		this.daysTaken = daysTaken;
	}

	@Override
	public String toString() {
		return "VacationEntry [startDate=" + startDate + ", daysTaken="
				+ daysTaken + "]";
	}
}