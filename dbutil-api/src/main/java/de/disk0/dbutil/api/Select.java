package de.disk0.dbutil.api;

import java.util.Map;

import de.disk0.dbutil.api.entities.BaseEntity;

public interface Select {

	//@Deprecated
	Field addSelect(Object value, String alias);
	//@Deprecated
	Field addSelect(TableReference tableReference, String field, String alias);
	//@Deprecated
	Field addSelect(Aggregate a, TableReference tableReference, String field, String alias);
	Field addSelect(Aggregate a, String alias, Field... references);
	Field addSelect(Field fr, String alias);

	TableReference fromTable(String table);
	TableReference fromTable(Class<? extends BaseEntity<?>> table);
	SubSelect from();
	Select union();

	//@Deprecated
	Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2);
	//@Deprecated
	Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value);

	Condition condition(Operator op);
	Condition condition(Operator op, Field fr1, Comparator c, Field fr2);
	Condition isNull(Operator op, Field fr1);
	Condition isNotNull(Operator op, Field fr1);

	void limit(int offset, int max);

	//@Deprecated
	void group(TableReference table, String field);
	//@Deprecated
	void order(TableReference table, String field, boolean ascending);

	void group(Field reference);
	void order(Field reference, boolean ascending);

	String getSql();

	Map<String,Object> getParams();

	String getAlias();

}
