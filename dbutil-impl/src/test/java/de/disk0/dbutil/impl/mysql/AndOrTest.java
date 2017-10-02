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
		s.condition(Operator.AND, t, "id", Comparator.EQ, "1");
		Assert.assertEquals("SELECT * FROM `hund` `hund_1` WHERE `hund_1`.`id` = :value_1", s.getSql());
		
		Condition c = s.condition(Operator.AND, t, "id", Comparator.EQ, "2");
		Assert.assertEquals("SELECT * FROM `hund` `hund_1` WHERE (`hund_1`.`id` = :value_1 AND `hund_1`.`id` = :value_2)", s.getSql());
		
		c.condition(Operator.OR, t, "id", Comparator.EQ, "3");
		Assert.assertEquals("SELECT * FROM `hund` `hund_1` WHERE (`hund_1`.`id` = :value_1 AND (`hund_1`.`id` = :value_2 OR `hund_1`.`id` = :value_3))", s.getSql());
		
		
	}
	
}
