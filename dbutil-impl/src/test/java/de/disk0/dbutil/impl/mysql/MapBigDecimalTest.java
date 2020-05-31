package de.disk0.dbutil.impl.mysql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.mysql.entities.BigDecimalEntity;
import de.disk0.dbutil.impl.mysql.entities.BigDecimalEntityRepository;

public class MapBigDecimalTest extends TestBase {

	@Override
	public void setup(Connection c) throws SQLException {
		c.createStatement().execute("CREATE TABLE bd (id VARCHAR(36) PRIMARY KEY, dec DECIMAL(10,4))");
	}

	@Test
	public void testSetNullExpectNull() throws SqlException {
		BigDecimalEntityRepository r = new BigDecimalEntityRepository();
		r.setDataSource(dataSource);

		BigDecimalEntity bde = new BigDecimalEntity();
		BigDecimalEntity bde2 = null;
		
		bde.setDec(new BigDecimal("999900.0009"));
		bde = r.save(bde);
		bde2 = r.get(bde.getId());
		Assert.assertEquals("999900.0009", bde2.getDec().toPlainString());
		
		bde.setDec(new BigDecimal("99900.00091"));
		bde = r.save(bde);
		bde2 = r.get(bde.getId());
		Assert.assertEquals("99900.0009", bde2.getDec().toPlainString());
		
		
	}
	
}
