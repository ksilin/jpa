package pro.jpa2.model.derived;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * A simple Entity, taken from the Pro JPA2 book.
 *
 * Has a ManyToMany relation to the {@link Employee}
 *
 * @author kostja
 *
 */
@Entity
@IdClass(ProjectId.class)
public class Project {

    @Id
	private Date startDate;

    @Id
	private String name;

	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name = "EMP_PROJ", joinColumns = @JoinColumn(name = "PROJ_ID"), inverseJoinColumns = @JoinColumn(name = "EMP_ID"))
	private List<Employee> employees;

	public List<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(List<Employee> employees) {
		this.employees = employees;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;

        if (name != null ? !name.equals(project.name) : project.name != null) return false;
        if (startDate != null ? !startDate.equals(project.startDate) : project.startDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = startDate != null ? startDate.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Project{" +
                "startDate=" + startDate +
                ", name='" + name + '\'' +
                '}';
    }
}