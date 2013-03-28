package pro.jpa2.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author kostja
 *
 */
@Entity
public class UploadFile {

	@Id
	@Column(name = "UPL_ID")
	private int uploadId;

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUploadId() {
		return uploadId;
	}

	public void setUploadId(int uploadId) {
		this.uploadId = uploadId;
	}

	@Override
	public String toString() {
		return "UploadFile [uploadId=" + uploadId + ", name=" + name + "]";
	}
}