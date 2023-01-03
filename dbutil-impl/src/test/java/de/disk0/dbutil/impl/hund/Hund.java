package de.disk0.dbutil.impl.hund;

import de.disk0.dbutil.api.entities.BaseGuidEntityV1;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Table(name="hund")
public class Hund extends BaseGuidEntityV1 {

	@Column(name="name")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
