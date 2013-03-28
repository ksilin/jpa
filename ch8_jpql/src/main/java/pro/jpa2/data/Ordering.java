/**
 *
 */
package pro.jpa2.data;

/**
 * @author kostja
 *
 */
public enum Ordering {

	ASC, DESC;

	private String propName;

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public String getPropName() {
		return propName;
	}
}
