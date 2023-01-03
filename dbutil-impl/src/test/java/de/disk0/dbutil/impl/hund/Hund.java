package de.disk0.dbutil.impl.hund;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

import de.disk0.dbutil.api.entities.BaseGuidEntity;

@Table(name="hund")
public class Hund extends BaseGuidEntity {

	@Column(name="name")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
