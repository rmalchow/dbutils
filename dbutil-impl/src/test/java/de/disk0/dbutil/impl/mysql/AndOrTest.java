package de.disk0.dbutil.impl.mysql;

import org.junit.Assert;
import org.junit.Test;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Condition;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.mysql.MysqlStatementBuilder;

public class AndOrTest {

	@Test
	public void testSetNullExpectNull() throws SqlException {
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference t = s.fromTable("hund");
		s.condition(Operator.AND, t.field("id"), Comparator.EQ, t.value("1"));
		Assert.assertEquals("SELECT * FROM `hund` `hund_1` WHERE `hund_1`.`id` = :value_1", s.getSql());
		
		Condition c = s.condition(Operator.AND, t.field("id"), Comparator.EQ, t.value("2"));
		Assert.assertEquals("SELECT * FROM `hund` `hund_1` WHERE (`hund_1`.`id` = :value_1 AND `hund_1`.`id` = :value_2)", s.getSql());
		
		c.condition(Operator.OR, t.field("id"), Comparator.EQ, t.value("3"));
		Assert.assertEquals("SELECT * FROM `hund` `hund_1` WHERE (`hund_1`.`id` = :value_1 AND (`hund_1`.`id` = :value_2 OR `hund_1`.`id` = :value_3))", s.getSql());
		
		
	}
	
	@Test
	public void testNestedEmptryExpectNoWhere() throws SqlException {
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference t = s.fromTable("hund");
		s.condition(Operator.AND).condition(Operator.AND).condition(Operator.AND);
		Assert.assertEquals("SELECT * FROM `hund` `hund_1`", s.getSql());
	}
	
	@Test
	public void testNestedPartlyEmptyExpectWhere() throws SqlException {
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference t = s.fromTable("hund");
		Condition c1 = s.condition(Operator.AND);
		c1.condition(Operator.AND, t.field("id"), Comparator.EQ, t.field("id1"));
		Condition c2 = s.condition(Operator.AND);
		Condition c3 = s.condition(Operator.OR);
		c3.condition(Operator.AND, t.field("id"), Comparator.EQ, t.field("id3"));
		//s.condition().condition().condition();
		Assert.assertEquals("SELECT * FROM `hund` `hund_1` WHERE (`hund_1`.`id` = `hund_1`.`id1` OR `hund_1`.`id` = `hund_1`.`id3`)", s.getSql());
	}
	
	@Test
	public void testNestedFilledExpectWhere() throws SqlException {
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference t = s.fromTable("hund");
		Condition c1 = s.condition(Operator.AND);
		c1.condition(Operator.AND, t.field("id"), Comparator.EQ, t.field("id1"));
		Condition c2 = s.condition(Operator.AND);
		Condition c2b = c2.condition(Operator.AND, t.field("id"), Comparator.EQ, t.field("id2"));
		c2b.condition(Operator.AND, t.field("id"), Comparator.EQ, t.field("id2b"));
		Condition c3 = s.condition(Operator.OR);
		c3.condition(Operator.AND, t.field("id"), Comparator.EQ, t.field("id3"));
		//s.condition().condition().condition();
		Assert.assertEquals("SELECT * FROM `hund` `hund_1` WHERE (`hund_1`.`id` = `hund_1`.`id1` AND (`hund_1`.`id` = `hund_1`.`id2` AND `hund_1`.`id` = `hund_1`.`id2b`) OR `hund_1`.`id` = `hund_1`.`id3`)", s.getSql());
	}
	
}
