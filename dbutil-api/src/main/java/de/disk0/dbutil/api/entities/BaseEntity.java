package de.disk0.dbutil.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@javax.persistence.Entity
public class BaseEntity<T> {

	@Id
	@javax.persistence.Id
	@Column(name = "id", unique = true, nullable = false)
	@javax.persistence.Column(name = "id", unique = true, nullable = false)
	private T id;

	public T getId() {
		return id;
	}

	public void setId(T id) {
		this.id = id;
	}

}
