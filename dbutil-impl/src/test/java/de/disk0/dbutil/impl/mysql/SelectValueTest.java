package de.disk0.dbutil.impl.mysql;

import org.junit.Assert;
import org.junit.Test;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Field;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.SubSelect;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.api.exceptions.SqlException;

public class SelectValueTest {

	@Test
	public void testSimpleValueExpectValue() throws SqlException {
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference t = s.fromTable("hund");
		
		s.addSelect(t.value("foo"), "bar");
		
		Assert.assertEquals("foo", s.getParams().get("value_1"));
		
		System.err.print(s.getSql());
		
	}

	@Test
	public void testAggregatedValueExpectValue() throws SqlException {
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference t = s.fromTable("hund");
		
		s.addSelect(Aggregate.COUNT, "bar", t.value("foo"));
		
		Assert.assertEquals("foo", s.getParams().get("value_1"));
		
		System.err.print(s.getSql());
		
	}
	
	@Test
	public void testAggregatedValueInSubselectExpectValue() throws SqlException {
		Select s = new MysqlStatementBuilder().createSelect();
		
		SubSelect ss = s.from();
		
		TableReference t = ss.fromTable("hund");
		
		ss.addSelect(t.value(1), "foo");
		
		Field f = t.value("foo");
		
		ss.condition(Operator.AND,t.value("a"),Comparator.EQ,t.field("b"));
		
		s.addSelect(f,"bar");
		s.addSelect(Aggregate.COUNT, "bar", f);
		
		System.err.println(s.getSql());
		System.err.println("----");
		System.err.println(s.getParams());
		System.err.println("----");
		System.err.println(ss.getSql());
		System.err.println("----");
		System.err.println(ss.getParams());

		Assert.assertEquals(1, ss.getParams().get("value_1"));
		Assert.assertEquals("foo", s.getParams().get("value_2"));
		
		
	}
	
	@Test
	public void testNamedValuesExpectSuccess() {
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference t = s.fromTable("hund");

		s.condition(Operator.AND,t.field("id"), Comparator.EQ, t.value(123,"id"));
		Assert.assertEquals("SELECT * FROM `hund` `hund_1` WHERE `hund_1`.`id` = :id_1", s.getSql());

	}
	
	
}
