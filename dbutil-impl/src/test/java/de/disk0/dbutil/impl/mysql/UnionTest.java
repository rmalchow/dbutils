package de.disk0.dbutil.impl.mysql;

import org.junit.Assert;
import org.junit.Test;

import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.TableReference;

public class UnionTest {

	
	@Test
	public void simpleUnion() {
		
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference t = s.fromTable("hund");
		s.condition(Operator.AND, t.field("id"), Comparator.EQ, t.value("1"));

		Select s2 = s.union();
		TableReference t2 = s2.fromTable("katze");
		s2.condition(Operator.AND, t2.field("id"), Comparator.EQ, t2.value("2"));
		
		Assert.assertEquals("(SELECT * FROM `hund` `hund_1` WHERE `hund_1`.`id` = :value_2) UNION (SELECT * FROM `katze` `katze_1` WHERE `katze_1`.`id` = :value_4)", s.getSql());
		
		
	}
}
