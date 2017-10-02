package de.disk0.dbutil.impl.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Condition;
import de.disk0.dbutil.api.FieldReference;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlCondition implements Condition {


	private Operator op;
	private FieldReference leftField;
	private FieldReference rightField;
	private Comparator c;
	
	private List<MysqlCondition> conditions = new ArrayList<>();
	private AliasGenerator aliasGenerator;
	
	public MysqlCondition(AliasGenerator aliasGenerator) {
		this.aliasGenerator = aliasGenerator;
	}

	public MysqlCondition(AliasGenerator aliasGenerator, Operator op, TableReference table1, String field1, Comparator c, Object value) {
		this.op = op;
		FieldReference left = new MysqlField(table1,field1);
		FieldReference right = new MysqlField(aliasGenerator,value);
		this.conditions.add(new MysqlCondition(aliasGenerator, op, left, c, right));
	}

	public MysqlCondition(AliasGenerator aliasGenerator, Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2) {
		this.aliasGenerator = aliasGenerator;
		this.op = op;
		FieldReference left = new MysqlField(table1,field1);
		FieldReference right = new MysqlField(table2,field2);
		this.conditions.add(new MysqlCondition(aliasGenerator, op, left, c, right));
	}
	
	public MysqlCondition(AliasGenerator aliasGenerator, Operator op, FieldReference fr1, Comparator c, FieldReference fr2) {
		this.aliasGenerator = aliasGenerator;
		this.leftField = fr1;
		this.rightField = fr2;
		this.op = op;
		this.c = c;
	}
	
	public Map<String,Object> getParams() {
		Map<String,Object> out = new HashMap<>();
		for(MysqlCondition mc : conditions) {
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

	@Override
	public Condition condition(Operator op, FieldReference left, Comparator c, FieldReference right) {
		if(leftField!=null) {
			MysqlCondition cE = new MysqlCondition(this.aliasGenerator, this.op, this.leftField, this.c, this.rightField);
			this.conditions.add(cE);
		}
		MysqlCondition cN = new MysqlCondition(this.aliasGenerator, op, left, c, right);
		this.conditions.add(cN);
		return cN;
	}

	@Override
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value) {
		FieldReference left = new MysqlField(table1, field1);
		FieldReference right = new MysqlField(aliasGenerator, value);
		return this.condition(op, left, c, right);
	}
	
	@Override
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2) {
		FieldReference left = new MysqlField(table1, field1);
		FieldReference right = new MysqlField(table2, field2);
		return this.condition(op, left, c, right);
	}

	@Override
	public Condition isNotNull(Operator op, FieldReference left) {
		return this.condition(op, left, Comparator.NOT_NULL, null);
	}
	
	@Override
	public Condition isNull(Operator op, FieldReference left) {
		return this.condition(op, left, Comparator.NULL, null);
	}
	
	@Override
	public String getSql() {
		List<String> parts = new ArrayList<>();
		
		if(leftField!=null) {
			
		}
		if(conditions.size()>0) {

			if(conditions.size()==1) {
				return conditions.get(0).getSql();
			}
			
			List<MysqlCondition> cs = new ArrayList<>(conditions);
			parts.add(cs.remove(0).getSql());
			
			while(cs.size()>0) {
				MysqlCondition c = cs.remove(0); 
				parts.add(c.getOp().name());
				parts.add(c.getSql());
			}
			
			return "("+StringUtils.join(parts," ")+")";
			
		} else {
			
			parts.add(leftField.getSql());

			parts.add(c.getSymbol());
			
			if(c == Comparator.NOT_NULL) {
			} else if(c == Comparator.NOT_NULL) {
			} else if(c == Comparator.IN) {
				parts.add("("+rightField.getSql()+")");
			} else if(c == Comparator.NOT_IN) {
				parts.add("("+rightField.getSql()+")");
			} else {
				parts.add(rightField.getSql());
			}
			
			
		}
		return StringUtils.join(parts," ");

	}
	
}
