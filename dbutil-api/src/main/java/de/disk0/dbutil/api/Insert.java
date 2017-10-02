package de.disk0.dbutil.api;

public interface Insert extends SqlFragment {

	public TableReference into(String table);
	
	public void addField(TableReference tr, String field, Object value);
	public void addField(TableReference tr, String field, TableReference tr2, String field2);
	
	public Condition condition(Operator op);
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2);
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value);

	public Condition condition(Operator op, Field fr1, Comparator c, Field fr2);
	public Condition isNull(Operator op, Field fr1);
	public Condition isNotNull(Operator op, Field fr1);


	
}
