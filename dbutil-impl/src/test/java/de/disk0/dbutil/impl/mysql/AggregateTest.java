package de.disk0.dbutil.impl.mysql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.api.exceptions.SqlException;

public class AggregateTest {

	@Test
	public void testSetNullExpectNull() throws SqlException {
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference t = s.fromTable("hund");
		s.addSelect(Aggregate.AVG, t, "value", "hund");
		Assertions.assertEquals("SELECT avg(`hund_1`.`value`) AS `hund` FROM `hund` `hund_1`", s.getSql());
		
		s.condition(
				Operator.OR,
				t.field("hase"),
				Comparator.EQ,
				t.field(Aggregate.CONCAT, t.field("path"), t.value("%"))
			);

		Assertions.assertEquals("SELECT avg(`hund_1`.`value`) AS `hund` FROM `hund` `hund_1` WHERE `hund_1`.`hase` = concat(`hund_1`.`path`, :value_1)", s.getSql());
		Assertions.assertEquals("%", s.getParams().get("value_1"));
		
	}
	
}
