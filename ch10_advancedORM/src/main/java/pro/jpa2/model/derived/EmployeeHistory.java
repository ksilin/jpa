package pro.jpa2.model.derived;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 */
@Entity
public class EmployeeHistory implements Serializable {

    //the actual PK in the table is going to have the same type as the PK of the EMployee
    @Id
    @OneToOne
    @JoinColumn(name = "EMP_ID")
    private Employee employee;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
