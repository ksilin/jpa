package pro.jpa2.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

/**
 * @author kostja
 *
 */
@Entity
public class UploadBidirectional {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String name;

	@OneToMany(mappedBy = "upload")
	// using a @JoinColumn fails - the deployment of the persistence unit fails
	private List<UploadFileBidirectional> files;

	public List<UploadFileBidirectional> getFiles() {
		return files;
	}

	public void setFiles(List<UploadFileBidirectional> files) {
		this.files = files;
	}

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
}