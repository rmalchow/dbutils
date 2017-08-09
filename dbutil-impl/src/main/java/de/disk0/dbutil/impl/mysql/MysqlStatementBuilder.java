package de.disk0.dbutil.impl.mysql;

import de.disk0.dbutil.api.Insert;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.StatementBuilder;

public class MysqlStatementBuilder implements StatementBuilder {

	@Override
	public Select createSelect() {
		return new MysqlSelect();
	}

	@Override
	public Insert createInsert() {
		return new MysqlInsert();
	}
	

	
	
}
