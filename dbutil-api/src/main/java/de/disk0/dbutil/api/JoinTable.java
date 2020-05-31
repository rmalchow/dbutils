package de.disk0.dbutil.api;

public interface JoinTable extends TableReference {
	
	Condition addOn(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2);
	Condition addOn(Operator op, TableReference table1, String field1, Comparator c, Object value);
	Condition addOn(Operator op, Field left, Comparator c, Field right);
	Condition addOn(Operator op);

}
