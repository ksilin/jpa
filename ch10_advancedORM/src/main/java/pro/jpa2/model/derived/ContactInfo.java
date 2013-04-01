package pro.jpa2.model.derived;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * An Embeddable is not a complete Entity - it needs an entity to be identified.
 * The fields of an Embeddable will be persisted in the same table along with
 * the embedding entity
 *
 * @author kostja
 */
@Embeddable
public class ContactInfo {

    @Embedded
    private Address residence;

    @ManyToOne
    @JoinColumn(name = "PRI_NUM")
    private Phone primaryPhone;

    @ManyToMany
    @MapKey(name = "type")
    @JoinTable(name = "EMP_PHONES")
    private Map<String, Phone> phones = new HashMap<String, Phone>();

    public Address getResidence() {
        return residence;
    }

    public void setResidence(Address residence) {
        this.residence = residence;
    }

    public Phone getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(Phone primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public Map<String, Phone> getPhones() {
        return phones;
    }

    public void setPhones(Map<String, Phone> phones) {
        this.phones = phones;
    }

    @Override
    public String toString() {
        return "ContactInfo{" +
                "residence=" + residence +
                //", primaryPhone=" + primaryPhone +
                ", phones=" + phones +
                '}';
    }
}