package de.disk0.dbutil.impl.mysql;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.katze.Katze;
import de.disk0.dbutil.impl.katze.Katze.TYPE;
import de.disk0.dbutil.impl.katze.KatzeRepository;

public class MapEnumTest extends TestBase {

	@Override
	public void setup(Connection c) throws SQLException {
		c.createStatement().execute("CREATE TABLE katze (id VARCHAR(36) PRIMARY KEY, type VARCHAR(128))");
	}

	@Test
	public void testSetNullExpectNull() throws SqlException {
		KatzeRepository hr = new KatzeRepository();
		hr.setDataSource(dataSource);

		Katze k = new Katze();
		k = hr.save(k);
		Assertions.assertNotNull(k);
		Assertions.assertNotNull(k.getId());
		Assertions.assertNull(k.getType());
		
		k.setType(TYPE.MIEZE);
		k = hr.save(k);
		Assertions.assertEquals(TYPE.MIEZE, k.getType());
		
		
		
	}
	
}
