/**
 *
 */
package pro.jpa2.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author kostja
 *
 */
@Entity
@DiscriminatorValue("QP")
public class QualityProject extends Project {

}
