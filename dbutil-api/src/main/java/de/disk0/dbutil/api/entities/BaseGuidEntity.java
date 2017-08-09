package de.disk0.dbutil.api.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BaseGuidEntity extends BaseEntity<String> {

	@Id
	@Column(name = "id", unique = true, nullable = false)
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
