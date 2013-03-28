package pro.jpa2.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author kostja
 */
@Entity
public class UploadFileBidirectional {

	@Id
	private int id;

	@ManyToOne
	// not specifying the join column does not fail - why?
	@JoinColumn(name = "UPL_ID")
	//@Column(name="UPL_ID") fails here - the deployment of the persistence unit fails
	private UploadBidirectional upload;

	private String name;

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public UploadBidirectional getUpload() {
		return upload;
	}

	public void setUpload(UploadBidirectional upload) {
		this.upload = upload;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "UploadFileBidirectional [upload=" + upload + ", name=" + name
				+ "]";
	}

}