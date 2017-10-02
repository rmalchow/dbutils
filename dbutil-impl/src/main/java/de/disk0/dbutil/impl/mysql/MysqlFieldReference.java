package de.disk0.dbutil.impl.mysql;

public class MysqlFieldReference {

	private String alias;

	public MysqlFieldReference(String alias) {
		this.alias = alias;
	}
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
}
