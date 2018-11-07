package de.disk0.dbutil.impl.mysql;

import org.junit.Assert;
import org.junit.Test;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.impl.hund.Hund;

public class TableFromObjectTest {
	
	
	@Test
	public void testTableNameFromField() {
		
		Select s = new MysqlStatementBuilder().createSelect();
		TableReference tr = s.fromTable(Hund.class);
		
		s.addSelect(tr.field(Aggregate.AVG, "value"),"avg_value");
		
		Assert.assertEquals("SELECT avg(`hund_1`.`value`) AS `avg_value` FROM `hund` `hund_1`", s.getSql());
		
		
	}
	

}
