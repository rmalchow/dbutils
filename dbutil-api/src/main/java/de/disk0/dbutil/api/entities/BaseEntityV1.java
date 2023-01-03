package de.disk0.dbutil.api.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BaseEntityV1<T> {

	@Id
	@Column(name = "id", unique = true, nullable = false)
	private T id;

	public T getId() {
		return id;
	}

	public void setId(T id) {
		this.id = id;
	}

}
