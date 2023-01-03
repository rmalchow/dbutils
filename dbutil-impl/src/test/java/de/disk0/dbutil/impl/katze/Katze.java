package de.disk0.dbutil.impl.katze;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

import de.disk0.dbutil.api.entities.BaseGuidEntity;

@Table(name="katze")
public class Katze extends BaseGuidEntity {
	
	public enum TYPE { MIEZE, SCHMUSE, RAUB };
	
	@Column(name="type")
	private TYPE type;

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}
	

}
