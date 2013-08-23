package pro.jpa2.model.derived;

import java.io.Serializable;
import java.util.Date;

public class ProjectId implements Serializable {

    private String name;
    private Date startDate;

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
}
