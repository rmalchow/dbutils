package de.disk0.dbutil.api;

import java.util.Map;

public interface Select {

	public SelectExpression addSelect(Object value, String alias);
	public SelectExpression addSelect(TableReference tableReference, String field, String alias);
	public SelectExpression addSelect(Aggregate a, TableReference tableReference, String field, String alias);

	public TableReference fromTable(String table);
	public SubSelect from();

	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2);
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value);

	public void limit(int offset, int max);
	public void order(TableReference table, String field, boolean ascending);
	public void group(TableReference table, String field);

	public String getSql();
	
	public Map<String,Object> getParams();

	public String getAlias();


}
