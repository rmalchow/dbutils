package de.disk0.dbutil.impl.mysql;

import org.junit.Assert;
import org.junit.Test;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.mysql.MysqlStatementBuilder;

public class AggregateTest {

	@Test
	public void testSetNullExpectNull() throws SqlException {
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference t = s.fromTable("hund");
		s.addSelect(Aggregate.AVG, t, "value", "hund");
		Assert.assertEquals("SELECT avg(`hund_1`.`value`) AS `hund` FROM `hund` `hund_1`", s.getSql());
		
		s.condition(
				Operator.OR,
				t.field("hase"),
				Comparator.EQ,
				t.field(Aggregate.CONCAT, t.field("path"), t.value("%"))
			);

		Assert.assertEquals("SELECT avg(`hund_1`.`value`) AS `hund` FROM `hund` `hund_1` WHERE `hund_1`.`hase` = concat(`hund_1`.`path`, :value_1)", s.getSql());
		Assert.assertEquals("%", s.getParams().get("value_1"));
		
	}
	
}
