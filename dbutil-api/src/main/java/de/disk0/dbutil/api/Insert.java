package de.disk0.dbutil.api;

public interface Insert extends SqlFragment {

	TableReference into(String table);
	
	void addField(TableReference tr, String field, Object value);
	void addField(TableReference tr, String field, TableReference tr2, String field2);
	
	Condition condition(Operator op);
	Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2);
	Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value);

	Condition condition(Operator op, Field fr1, Comparator c, Field fr2);
	Condition isNull(Operator op, Field fr1);
	Condition isNotNull(Operator op, Field fr1);

}
