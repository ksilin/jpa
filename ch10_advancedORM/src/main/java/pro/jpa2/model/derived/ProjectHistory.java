package pro.jpa2.model.derived;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 */
@Entity
@IdClass(ProjectId.class)
public class ProjectHistory implements Serializable {

//    @Id
//    String name;
//
//    @Id
//    Date startDate;

    //the actual PK in the table is going to have the same type as the PK of the Employee
    // in this case two columns defined by the ProjectId class
    @Id
    @OneToOne
    private Project project;

    private String notes;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public Date getStartDate() {
//        return startDate;
//    }
//
//    public void setStartDate(Date startDate) {
//        this.startDate = startDate;
//    }
}
