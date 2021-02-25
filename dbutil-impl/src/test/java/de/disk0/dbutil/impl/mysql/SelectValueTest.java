package de.disk0.dbutil.impl.mysql;

import org.junit.Assert;
import org.junit.Test;

import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.api.exceptions.SqlException;

public class SelectValueTest {

	@Test
	public void testSetNullExpectNull() throws SqlException {
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference t = s.fromTable("hund");
		
		s.addSelect(t.value("foo"), "bar");
		
		Assert.assertEquals("foo", s.getParams().get("value_1"));
	}
	
}
