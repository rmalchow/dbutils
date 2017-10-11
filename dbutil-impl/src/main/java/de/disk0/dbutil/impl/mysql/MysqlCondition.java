package de.disk0.dbutil.impl.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Condition;
import de.disk0.dbutil.api.Field;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlCondition implements Condition {


	private Operator op;
	private Field leftField;
	private Field rightField;
	private Comparator c;
	
	private List<Condition> conditions = new ArrayList<>();
	private AliasGenerator aliasGenerator;
	
	public MysqlCondition(AliasGenerator aliasGenerator) {
		this.aliasGenerator = aliasGenerator;
	}

	public MysqlCondition(AliasGenerator aliasGenerator, Operator op) {
		this.aliasGenerator = aliasGenerator;
		this.op = op;
	}
	
	public MysqlCondition(AliasGenerator aliasGenerator, Operator op, TableReference table1, String field1, Comparator c, Object value) {
		this.op = op;
		Field left = new MysqlField(table1,field1);
		Field right = new MysqlField(aliasGenerator,value);
		this.conditions.add(new MysqlCondition(aliasGenerator, op, left, c, right));
	}

	public MysqlCondition(AliasGenerator aliasGenerator, Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2) {
		this.aliasGenerator = aliasGenerator;
		this.op = op;
		Field left = new MysqlField(table1,field1);
		Field right = new MysqlField(table2,field2);
		this.conditions.add(new MysqlCondition(aliasGenerator, op, left, c, right));
	}
	
	public MysqlCondition(AliasGenerator aliasGenerator, Operator op, Field fr1, Comparator c, Field fr2) {
		this.aliasGenerator = aliasGenerator;
		this.leftField = fr1;
		this.rightField = fr2;
		this.op = op;
		this.c = c;
	}
	
	public Map<String,Object> getParams() {
		Map<String,Object> out = new HashMap<>();
		for(Condition mc : conditions) {
			out.putAll(mc.getParams());
		}
		if(leftField!=null) out.putAll(leftField.getParams());
		if(rightField!=null) out.putAll(rightField.getParams());
		return out; 
	}

	public Operator getOp() {
		return op;
	}

	public void setOp(Operator op) {
		this.op = op;
	}

	private Condition addCondition(Condition c) {
		if(leftField!=null) {
			MysqlCondition cE = new MysqlCondition(this.aliasGenerator, this.op, this.leftField, this.c, this.rightField);
			this.conditions.add(cE);
			leftField = null;
		}
		this.conditions.add(c);
		return c;
	}
	
	@Override
	public Condition condition(Operator op) {
		return addCondition(new MysqlCondition(aliasGenerator,op));
	}
	
	
	@Override
	public Condition condition(Operator op, Field left, Comparator c, Field right) {
		return addCondition(new MysqlCondition(aliasGenerator,op,left,c,right));
	}

	@Override
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value) {
		Field left = table1.field(field1);
		Field right = table1.value(value);
		return this.condition(op, left, c, right);
	}
	
	@Override
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2) {
		Field left = table1.field(field1);
		Field right = table2.value(field2);
		return this.condition(op, left, c, right);
	}

	@Override
	public Condition isNotNull(Operator op, Field left) {
		return this.condition(op, left, Comparator.NOT_NULL, null);
	}
	
	@Override
	public Condition isNull(Operator op, Field left) {
		return this.condition(op, left, Comparator.NULL, null);
	}
	
	@Override
	public String getSql() {
		List<String> parts = new ArrayList<>();
		
		if(conditions.size()>0) {
			
			List<Condition> cs = new ArrayList<>(conditions);
			
			while(cs.size()>0) {
				Condition c = cs.remove(0);
				if(c.getOp()==null || c.getSql().length()==0) {
					continue;
				}
				if(parts.size()>0) {
					parts.add(c.getOp().name());
				}
				parts.add(c.getSql());
			}

			if(parts.size()>1) {
				return "("+StringUtils.join(parts," ")+")";
			}
			
		} else if(leftField!=null) {
			
			parts.add(leftField.getSql());
			parts.add(c.getSymbol());
			if(c == Comparator.NOT_NULL || c == Comparator.NULL) {
				parts.add(c.getSymbol());
			} else if(c == Comparator.IN) {
				parts.add("("+rightField.getSql()+")");
			} else if(c == Comparator.NOT_IN) {
				parts.add("("+rightField.getSql()+")");
			} else {
				parts.add(rightField.getSql());
			}
			
			
		}
		
		String s = StringUtils.join(parts," "); 

		return s;

	}
	
}
