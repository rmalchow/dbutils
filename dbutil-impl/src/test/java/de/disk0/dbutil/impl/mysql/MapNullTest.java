package de.disk0.dbutil.impl.mysql;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.hund.Hund;
import de.disk0.dbutil.impl.hund.HundRepository;
import org.junit.Assert;

public class MapNullTest extends TestBase {

	@Override
	public void setup(Connection c) throws SQLException {
		c.createStatement().execute("CREATE TABLE hund (id VARCHAR(36) PRIMARY KEY, name VARCHAR(128))");
	}

	@Test
	public void testSetNullExpectNull() throws SqlException {
		HundRepository hr = new HundRepository();
		hr.setDataSource(dataSource);
		Hund h = new Hund();
		h = hr.save(h);
		Assert.assertNotNull(h);
		Assert.assertNotNull(h.getId());
		Assert.assertNull(h.getName());
		
		h.setName("hund");
		h = hr.save(h);
		Assert.assertNotNull(h.getName());
		
		
	}
	
}
