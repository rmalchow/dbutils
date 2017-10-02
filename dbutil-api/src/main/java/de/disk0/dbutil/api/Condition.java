package de.disk0.dbutil.api;

import java.util.Map;

public interface Condition {

	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2);
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value);
	public Condition condition(Operator op, FieldReference fr1, Comparator c, FieldReference fr2);
	public Condition isNull(Operator op, FieldReference fr1);
	public Condition isNotNull(Operator op, FieldReference fr1);

	public Map<String,Object> getParams();
	String getSql();
	
}
