package pro.jpa2.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * A simple Entity, taken from the Pro JPA2 book.
 *
 * Has a ManyToOne relation to the {@link Department}
 *
 * @author kostja
 *
 */
@Entity
public class CircularEmployee {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String name;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private CircularEmployee boss;

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

	public CircularEmployee getBoss() {
		return boss;
	}

	public void setBoss(CircularEmployee boss) {
		this.boss = boss;
	}

	@Override
	public String toString() {
		return "CircularEmployee [id=" + id + ", name=" + name + ", boss="
				+ boss.getId() + "]";
	}
}