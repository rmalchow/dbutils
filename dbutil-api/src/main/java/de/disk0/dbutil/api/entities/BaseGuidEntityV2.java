package de.disk0.dbutil.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@javax.persistence.Entity
public class BaseGuidEntityV2 extends BaseEntityV1<String> implements BaseGuidEntity {

	@Id
	@javax.persistence.Id
	@Column(name = "id", unique = true, nullable = false)
	@javax.persistence.Column(name = "id", unique = true, nullable = false)
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
