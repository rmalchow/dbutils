package de.disk0.dbutil.impl.mysql;

import java.util.HashMap;
import java.util.Map;

import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlFieldValuePair {

	private TableReference table1;
	private String field1;
	private TableReference table2;
	private String field2;
	private String valueName;
	private Object value;

	private Map<String,Object> params = new HashMap<>();

	public MysqlFieldValuePair(AliasGenerator aliasGenerator, TableReference table1, String field1, TableReference table2, String field2, Object value) {
		this.table1 = table1;
		this.field1 = field1;
		this.table2 = table2;
		this.field2 = field2;
		if(value != null) {
			this.valueName = aliasGenerator.generateAlias(field1);
			this.value = value;
			params.put(valueName, value);
		}
	}

	public String[] getSql() {
		StringBuffer sb0 = new StringBuffer();
		sb0.append('`');
		sb0.append(table1.getName());
		sb0.append('`');
		sb0.append('.');
		sb0.append('`');
		sb0.append(field1);
		sb0.append('`');

		
		StringBuffer sb1 = new StringBuffer();
		
		if(value!=null) {
			sb1.append(":");
			sb1.append(valueName);
		} else if (table2!=null && field2!=null){
			sb1.append('`');
			sb1.append(table2.getName());
			sb1.append('`');
			sb1.append('.');
			sb1.append('`');
			sb1.append(field2);
			sb1.append('`');
		}
		
		return new String[] { sb0.toString(), sb1.toString() };
	}
	
}
