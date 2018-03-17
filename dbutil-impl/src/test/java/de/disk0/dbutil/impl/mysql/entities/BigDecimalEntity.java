package de.disk0.dbutil.impl.mysql.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Table;

import de.disk0.dbutil.api.entities.BaseGuidEntity;

@Table(name="bd")
public class BigDecimalEntity extends BaseGuidEntity {
	
	@Column(name="dec")
	private BigDecimal dec;

	public BigDecimal getDec() {
		return dec;
	}

	public void setDec(BigDecimal dec) {
		this.dec = dec;
	}
	
}
