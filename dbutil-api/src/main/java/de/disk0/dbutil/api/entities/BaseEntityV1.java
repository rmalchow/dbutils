package de.disk0.dbutil.api.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BaseEntityV1<T> implements BaseEntity<T> {

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@Getter @Setter private T id;

}
