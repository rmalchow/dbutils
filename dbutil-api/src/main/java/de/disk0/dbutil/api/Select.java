package de.disk0.dbutil.api;

import java.util.Map;

public interface Select {

	
	//@Deprecated
	public Field addSelect(Object value, String alias);
	//@Deprecated
	public Field addSelect(TableReference tableReference, String field, String alias);
	//@Deprecated
	public Field addSelect(Aggregate a, TableReference tableReference, String field, String alias);
	public Field addSelect(Aggregate a, String alias, Field... references);
	public Field addSelect(Field fr, String alias);

	public TableReference fromTable(Object table);
	public SubSelect from();
	public Select union();

	//@Deprecated
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2);
	//@Deprecated
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value);

	public Condition condition(Operator op);
	public Condition condition(Operator op, Field fr1, Comparator c, Field fr2);
	public Condition isNull(Operator op, Field fr1);
	public Condition isNotNull(Operator op, Field fr1);

	public void limit(int offset, int max);

	//@Deprecated
	public void group(TableReference table, String field);
	//@Deprecated
	public void order(TableReference table, String field, boolean ascending);

	public void group(Field reference);
	public void order(Field reference, boolean ascending);

	public String getSql();
	
	public Map<String,Object> getParams();

	public String getAlias();


}
