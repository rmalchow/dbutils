package de.disk0.dbutil.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.hund.Hund;
import de.disk0.dbutil.impl.hund.HundRepository;
import org.junit.Assert;

public class InTest extends TestBase {

	@Override
	public void setup(Connection c) throws SQLException {
		c.createStatement().execute("CREATE TABLE hund (id VARCHAR(36) PRIMARY KEY, name VARCHAR(128))");
	}

	@Test
	public void testSetNullExpectNull() throws SqlException {
		HundRepository hr = new HundRepository();
		hr.setDataSource(dataSource);

		for(String s : new String[] { "a", "b", "c", "d", "e", "f", "g" } ) {
			Hund h = new Hund();
			h.setName(s);
			hr.save(h);
		}
		
		Map<String,Object> p = new HashMap<>();
		List<String> l = new ArrayList<>();
		l.add("a");
		l.add("c");
		l.add("f");
		l.add("h");
		p.put("name", l);
		
		List<Hund> hs = hr.find("SELECT * FROM hund WHERE name IN ( :name )", p);
		Assert.assertEquals(3,hs.size());
		
		
		
	}
	
}
