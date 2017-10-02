package de.disk0.dbutil.impl.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Condition;
import de.disk0.dbutil.api.Field;
import de.disk0.dbutil.api.Insert;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlInsert implements Insert {

	private TableReference trInto;
	private List<FieldValue> values = new ArrayList<>();
	private Condition wc;

	private AliasGenerator aliasGenerator;
	private Map<String, Object> params = new HashMap<>();

	public MysqlInsert() {
		this.aliasGenerator = new AliasGenerator();
	}

	public MysqlInsert(AliasGenerator aliasGenerator) {
		this.aliasGenerator = aliasGenerator;
	}

	@Override
	public TableReference into(String table) {
		if (trInto != null) {
			throw new RuntimeException("into is already set");
		}
		trInto = new MysqlTableReferenceSimple(aliasGenerator, table);
		return trInto;
	}

	@Override
	public void addField(TableReference tr, String field, Object value) {
		Field f = tr.field(field);
		Field v = tr.value(value);
		values.add(new FieldValue(f,v));
	}

	@Override
	public void addField(TableReference tr, String field, TableReference tr2, String field2) {
		Field f = tr.field(field);
		Field v = tr2.value(field2);
		values.add(new FieldValue(f,v));
	}

	@Override
	public String getSql() {
		List<String> parts = new ArrayList<>();
		
		parts.add("INSERT INTO");

		parts.add("`" + trInto.getName() + "`");

		List<String> fs = new ArrayList<>();
		List<String> vs = new ArrayList<>();
		
		if (values.size() > 0) {
			for (FieldValue p : values) {
				fs.add(p.getLeft().getSql());
				vs.add(p.getRight().getSql());
			}
			parts.add("(" + StringUtils.join(fs, ", ") + ")");
			parts.add("VALUES");
			parts.add("(" + StringUtils.join(vs, ", ") + ")");
		}
		if (wc != null) {
			parts.add(wc.getSql());
		}

		return StringUtils.join(parts, " ");
	}

	@Override
	public Map<String, Object> getParams() {
		Map<String, Object> out = new HashMap<>();
		out.putAll(params);
		if (wc != null) {
			out.putAll(wc.getParams());
		}
		return out;
	}

	@Override
	public Condition condition(Operator op, Field left, Comparator c, Field right) {
		if(wc==null) {
			wc = new MysqlCondition(aliasGenerator);
		}
		return wc.condition(op, left, c, right);
	}
	@Override
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value) {
		Field left = new MysqlField(table1, field1);
		Field right = new MysqlField(aliasGenerator, value);
		return this.condition(op, left, c, right);
	}
	
	@Override
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2) {
		Field left = new MysqlField(table1, field1);
		Field right = new MysqlField(table2, field2);
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
	public String toString() {
		return getSql() + " " + getParams();
	}

	private class FieldValue {

		private Field left;
		private Field right;

		public FieldValue(Field left, Field right) {
			super();
			this.left = left;
			this.right = right;
		}

		public Field getLeft() {
			return left;
		}

		public void setLeft(Field left) {
			this.left = left;
		}

		public Field getRight() {
			return right;
		}

		public void setRight(Field right) {
			this.right = right;
		}

	}

}
