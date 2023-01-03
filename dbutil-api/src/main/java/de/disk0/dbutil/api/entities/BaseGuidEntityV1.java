package de.disk0.dbutil.api.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BaseGuidEntityV1  implements BaseGuidEntityV1 {

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@Getter @Setter private String id;

}
