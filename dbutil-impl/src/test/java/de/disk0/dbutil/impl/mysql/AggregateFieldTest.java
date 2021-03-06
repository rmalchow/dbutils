package de.disk0.dbutil.impl.mysql;

import org.junit.Assert;
import org.junit.Test;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.TableReference;

public class AggregateFieldTest {
	
	
	@Test
	public void testSimpleAggregateField() {
		
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference tr = s.fromTable("hund");
		
		s.addSelect(tr.field(Aggregate.AVG, "value"),"avg_value");
		
		s.addSelect(tr.field(Aggregate.COUNT, "value"),"cnt_value");

		Assert.assertEquals("SELECT avg(`hund_1`.`value`) AS `avg_value`, count(`hund_1`.`value`) AS `cnt_value` FROM `hund` `hund_1`", s.getSql());
		
		
	}
	

}
