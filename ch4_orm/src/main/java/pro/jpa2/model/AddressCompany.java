package pro.jpa2.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Embeds an {@link Address}, just like the {@link AddressEmployee}. Used to
 * check the implications of sharing an Embeddable between Entities
 *
 * @author kostja
 *
 */
@Entity
public class AddressCompany {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String name;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "state", column = @Column(name = "PROVINCE")),
			@AttributeOverride(name = "zip", column = @Column(name = "POSTAL_CODE")) })
	private Address address;

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

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "AddressCompany [id=" + id + ", name=" + name + ", address="
				+ address + "]";
	}
}