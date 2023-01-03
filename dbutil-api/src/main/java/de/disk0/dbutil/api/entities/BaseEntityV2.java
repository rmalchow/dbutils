package de.disk0.dbutil.api.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class BaseEntityV2<T> implements BaseEntity<T> {

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@Getter @Setter private T id;

}
