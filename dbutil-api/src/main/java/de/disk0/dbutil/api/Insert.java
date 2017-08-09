package de.disk0.dbutil.api;

import java.util.Map;

public interface Insert {

	public TableReference into(String table);
	
	public void addField(TableReference tr, String field, Object value);
	public void addField(TableReference tr, String field, TableReference tr2, String field2);
	
	public String getSql();
	
	public Map<String,Object> getParams();
	
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2);
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value);


	
}
