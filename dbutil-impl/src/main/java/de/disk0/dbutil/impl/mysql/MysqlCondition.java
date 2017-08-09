package de.disk0.dbutil.impl.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Condition;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlCondition implements Condition {


	private Operator op;
	private TableReference table1;
	private MysqlFieldMethod field1Method;
	private String[] field1Args;
	private String field1;
	private Comparator c;
	private TableReference table2;
	private String field2;
	private String valueName;
	private Object value;
	
	private List<MysqlCondition> conditions = new ArrayList<>();
	private AliasGenerator aliasGenerator;
	
	public MysqlCondition(AliasGenerator aliasGenerator) {
		this.aliasGenerator = aliasGenerator;
	}

	public MysqlCondition(AliasGenerator aliasGenerator, Operator op, TableReference table1, String field1, Comparator c, Object value) {
		this.aliasGenerator = aliasGenerator;
		this.conditions.add(new MysqlCondition(aliasGenerator, op, table1, field1, c, null, null, value, aliasGenerator.generateAlias(field1), null));
		this.op = op;
		this.table1 = table1;
		this.op = op;
		this.field1 = field1;
		this.c = c;
	}

	public MysqlCondition(AliasGenerator aliasGenerator, Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2) {
		this.aliasGenerator = aliasGenerator;
		this.op = op;
		this.conditions.add(new MysqlCondition(aliasGenerator, op, table1, field1, c, table2, field2, null, null, null));
	}
	
	private MysqlCondition(AliasGenerator aliasGenerator, Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2, Object value, String valueName, MysqlFieldMethod field1Method, String... agrs) {
		this.aliasGenerator = aliasGenerator;
		this.op = op;
		this.table1 = table1;
		this.op = op;
		this.field1 = field1;
		
		this.c = c;
		
		this.table2 = table2;
		this.field2 = field2;
		
		this.value = value;
		this.valueName = valueName;
		
		this.field1Method = field1Method;
		this.field1Args = agrs;
	}
	
	public Map<String,Object> getParams() {
		Map<String,Object> out = new HashMap<>();
		for(MysqlCondition mc : conditions) {
			out.putAll(mc.getParams());
		}
		if(valueName!=null) {
			out.put(valueName, value);
		}
		return out; 
	}

	public Operator getOp() {
		return op;
	}

	public void setOp(Operator op) {
		this.op = op;
	}

	@Override
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value) {
		MysqlCondition c1 = new MysqlCondition(this.aliasGenerator, op, table1, field1, c, value);
		this.conditions.add(c1);
		return c1;
	}
	
	@Override
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2) {
		MysqlCondition c1 = new MysqlCondition(this.aliasGenerator, op, table1, field1, c, table2, field2);
		this.conditions.add(c1);
		return c1;
	}

	@Override
	public String getSql() {
		StringBuilder sb = new StringBuilder();

		if(conditions.size()>0) {
			List<String> cSql = new ArrayList<>();
			boolean first = true;
			for(MysqlCondition mc : conditions) {
				if(!first && mc.getOp()!=null) {
					cSql.add(mc.getOp().name());
				}
				first = false;
				cSql.add(mc.getSql());
			}
			if(conditions.size()>1) {
				sb.append("(");
			}
			sb.append(StringUtils.join(cSql.toArray()," "));
			if(conditions.size()>1) {
				sb.append(")");
			}
		} else if (table1!=null) {
			if(field1Method!=null){
				sb.append(field1Method.name()+"(");
			}
			sb.append('`');
			sb.append(table1.getAlias());
			sb.append('`');
			sb.append('.');
			sb.append('`');
			sb.append(field1);
			sb.append('`');
			if(field1Method!=null){
				if(field1Args!=null){
					for(String arg:field1Args){
						sb.append(","+arg);
					}
				}
				sb.append(")");
			}
			if(c == Comparator.EQ) {
				sb.append(" = ");
			} else if(c == Comparator.GT) {
				sb.append(" > ");
			} else if(c == Comparator.GTE) {
				sb.append(" >= ");
			} else if(c == Comparator.LT) {
				sb.append(" < ");
			} else if(c == Comparator.LTE) {
				sb.append(" <= ");
			} else if(c == Comparator.NE) {
				sb.append(" <> ");
			} else if(c == Comparator.LIKE) {
				sb.append(" LIKE ");
			} else if(c == Comparator.IN) {
				sb.append(" IN (");
			} else if(c == Comparator.NULL) {
				sb.append(" IS NULL");
				return sb.toString();
			} else if(c == Comparator.NOT_NULL) {
				sb.append(" IS NOT NULL");
				return sb.toString();
			}
			
			if (table2!=null && field2!=null){
				sb.append('`');
				sb.append(table2.getAlias());
				sb.append('`');
				sb.append('.');
				sb.append('`');
				sb.append(field2);
				sb.append('`');
			} else {
				sb.append(":");
				sb.append(valueName);
			} 
			
			if(c == Comparator.IN) {
				sb.append(')');
			}
			
		}
		return sb.toString();
	}
	
}
