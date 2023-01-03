package de.disk0.dbutil.impl.katze;

import de.disk0.dbutil.api.entities.BaseGuidEntityV1;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Table(name="katze")
public class Katze extends BaseGuidEntityV1 {
	
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
