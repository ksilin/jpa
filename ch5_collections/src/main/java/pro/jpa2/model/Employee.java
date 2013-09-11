package pro.jpa2.model;

import javax.persistence.*;
import java.util.*;

/**
 * A simple Entity, used to illustrate {@link @ElementCollection} use
 *
 * @author kostja
 */
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private long salary;


    @ElementCollection(targetClass = VacationEntry.class, fetch = FetchType.EAGER)
    private Collection vacations;

    public List<String> getNicknames() {
        return nicknames;
    }

    public void setNicknames(List<String> nicknames) {
        this.nicknames = nicknames;
    }

    @ElementCollection//(fetch = FetchType.EAGER)
    @Column(name="NICKNAME")
    // nothing helps, not even FetchType=EAGER - still getting a
    // LazyInitializationExample in the logging statement in the
    // findAllTest of BasicEmployeeTest

    // further, a @CollectionTable annotation can be used to customize the
    // mapping p.108
    // the default name for the table is EMPLOYEE_NICKNAMES
    private List<String> nicknames = new ArrayList<String>();

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

    public long getSalary() {
        return salary;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }


    public Collection getVacations() {
        return vacations;
    }

    public void setVacations(Collection vacations) {
        this.vacations = vacations;
    }

    @Override
    public String toString() {
        return "Employee [id=" + id + ", name=" + name + ", salary=" + salary
                + ", vacations=" + vacations + "]";
    }
}