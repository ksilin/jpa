package pro.jpa2.model;

import javax.persistence.Embeddable;

/**
 * An Embeddable is not a complete Entity -it needs an entity to be identified.
 * The fields of an Embeddable will be persisted in the same table along with
 * the embedding entity
 *
 * @author kostja
 *
 */
@Embeddable
public class Address {

	private String zip;

	private String state;

	public String getZip() {
		return zip;
	}

	public void setZip(String city) {
		this.zip = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "Address [zip=" + zip + ", state=" + state + "]";
	}
}