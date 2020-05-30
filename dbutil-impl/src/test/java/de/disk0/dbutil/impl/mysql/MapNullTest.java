package de.disk0.dbutil.impl.mysql;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.hund.Hund;
import de.disk0.dbutil.impl.hund.HundRepository;
import de.disk0.dbutil.impl.pets.Pet;
import de.disk0.dbutil.impl.pets.PetRepository;

import org.junit.Assert;

public class MapNullTest extends TestBase {

	@Override
	public void setup(Connection c) throws SQLException {
		c.createStatement().execute("CREATE TABLE hund (id VARCHAR(36) PRIMARY KEY, name VARCHAR(128))");
		c.createStatement().execute("CREATE TABLE pet (id VARCHAR(36) PRIMARY KEY, age INT, size INT)");
	}

	@Test
	public void testSetNullExpectNull() throws SqlException {
		HundRepository hr = new HundRepository(dataSource);
		Hund h = new Hund();
		h = hr.save(h);
		Assert.assertNotNull(h);
		Assert.assertNotNull(h.getId());
		Assert.assertNull(h.getName());
		
		h.setName("hund");
		h = hr.save(h);
		Assert.assertNotNull(h.getName());
	}
	
	
	@Test
	public void testNullableFields() throws SqlException {
		PetRepository pr = new PetRepository(dataSource);
		Pet p = new Pet();
		
		String id = pr.save(p).getId();
		
		Pet p2 = pr.get(id);

		Assert.assertNull(p2.getAge());
		
	}

	@Test
	public void testNonNullableFields() throws SqlException, SQLException {
		PetRepository pr = new PetRepository(dataSource);
		Pet p = new Pet();
		
		String id = pr.save(p).getId();
		

		dataSource.getConnection().createStatement().execute("UPDATE pet SET size = NULL");

		
		Exception e = null;
		
		try {
			pr.get(id);
		} catch (Exception e2) {
			e = e2;
		}
		
		Assert.assertNotNull(e);
		
		e.printStackTrace();
		
		Assert.assertEquals("Cannot set NULL value to field with type int", e.getCause().getCause().getCause().getCause().getCause().getMessage());
		
	}
	
}
