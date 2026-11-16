package de.disk0.dbutil.impl.blob;

import de.disk0.dbutil.api.entities.BaseGuidEntityV2;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Table(name="blob_entity")
public class BlobEntity extends BaseGuidEntityV2 {

	@Column(name="data")
	private byte[] data;

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
