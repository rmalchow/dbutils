package de.disk0.dbutil.impl.mysql.entities;

import java.math.BigDecimal;

import de.disk0.dbutil.api.entities.BaseGuidEntityV2;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Table(name="bd")
public class BigDecimalEntity extends BaseGuidEntityV2 {
	
	@Column(name="dec")
	private BigDecimal dec;

	public BigDecimal getDec() {
		return dec;
	}

	public void setDec(BigDecimal dec) {
		this.dec = dec;
	}
	
}
