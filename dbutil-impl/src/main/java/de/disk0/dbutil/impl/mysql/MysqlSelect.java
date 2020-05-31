package de.disk0.dbutil.impl.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Table;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Condition;
import de.disk0.dbutil.api.Field;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.SqlFragment;
import de.disk0.dbutil.api.SubSelect;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.api.entities.BaseEntity;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlSelect implements Select {

	private List<Field> se = new ArrayList<>();
	private List<TableReference> tr = new ArrayList<>();
	private Condition wc;
	private List<SortOrder> order = new ArrayList<>();
	private List<Field> group = new ArrayList<>();
	private List<Select> unions = new ArrayList<>();
	
	private int offset = -1;
	private int max = -1;
	
	private AliasGenerator aliasGenerator;
	
	public MysqlSelect() {
		this.aliasGenerator = new AliasGenerator();
	}
	
	public MysqlSelect(AliasGenerator aliasGenerator) {
		this.aliasGenerator = aliasGenerator;
	}
	
	@Override
	public Field addSelect(Aggregate aggregate, TableReference tableReference, String field, String alias) {
		if(alias == null) {
			alias = field;
		}
		Field f1 = new MysqlField(tableReference,field); 
		Field f2 = new MysqlField(aggregate,f1); 
		Field f3 = new MysqlField(f2,alias); 
		se.add(f3);
		return f3;
	}

	@Override
	public Field addSelect(TableReference tableReference, String field, String alias) {
		if(alias == null) {
			alias = field;
		}
		Field f1 = new MysqlField(tableReference,field); 
		Field f2 = new MysqlField(f1,alias); 
		se.add(f2);
		return f2;
	}

	@Override
	public Field addSelect(Aggregate a, String alias, Field... references) {
		Field f1 = new MysqlField(a, references); 
		Field f2 = new MysqlField(f1,alias); 
		se.add(f2);
		return f2;
	}
	
	@Override
	public Field addSelect(Field fr, String alias) {
		Field f2 = new MysqlField(fr,alias); 
		se.add(f2);
		return f2;
	}
	
	@Override
	public Field addSelect(Object value, String alias) {
		Field f1 = new MysqlField(aliasGenerator,value); 
		Field f2 = new MysqlField(f1,alias); 
		se.add(f2);
		return f2;
	}
	
	
	@Override
	public TableReference fromTable(Class<? extends BaseEntity<?>> table) {
		return fromTable(table.getAnnotation(Table.class).name());
	}
	
	@Override
	public TableReference fromTable(String table) {
		String tn = table;
		
		TableReference t = new MysqlTableReferenceSimple(this.aliasGenerator,tn);
		tr.add(t);
		return t;
	}

	@Override
	public SubSelect from() {
		MysqlTableReferenceSubSelect s = new MysqlTableReferenceSubSelect(this.aliasGenerator);
		tr.add(s);
		return s;
	}

	@Override
	public Select union() {
		Select s = new MysqlSelect(aliasGenerator);
		this.unions.add(s);
		return s;
	}
	
	@Override
	public Condition condition(Operator op) {
		if(wc==null) {
			wc = new MysqlCondition(aliasGenerator);
		}
		return wc.condition(op);
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
	
	private String join(List<? extends SqlFragment> fragments, String joinWith) {
		if(fragments == null || fragments.size()==0) return null;
		List<String> x = new ArrayList<>();
		for(SqlFragment f : fragments) {
			x.add(f.getSql());
		}
		return String.join(joinWith, x);
	}
	
	
	@Override
	public String getSql() {
		
		List<String> parts = new ArrayList<>();
		
		parts.add("SELECT");
		if(se.size()==0) {
			parts.add("*");
		} else {
			parts.add(join(se, ", "));
		}
		parts.add("FROM");
		parts.add(join(tr, ", "));

		if(wc!=null) {
			String where = wc.getSql(); 
			if(where.length()>0) {
				parts.add("WHERE");
				parts.add(where);
			}
		}

		if(group.size()>0) {
			parts.add("GROUP BY");
			parts.add(join(group, ", "));
		}
		
		if(order.size()>0) {
			parts.add("ORDER BY");
			parts.add(join(order, ", "));
		}
		
		if(offset > -1 && max > -1) {
			parts.add("LIMIT");
			parts.add(offset+","+max);
		} else if (max > -1) {
			parts.add("LIMIT");
			parts.add(max+"");
		}
		
		String out = String.join(" ", parts); 
		
		if(unions.size()>0) {
			parts.clear();
			parts.add("("+out+")");
			for(Select s : unions) {
				parts.add("("+s.getSql()+")");
			}
			out = String.join(" UNION ", parts);
		}
		
		return out;
	}

	@Override
	public void order(TableReference table, String field, boolean ascending) {
		order(table.field(field), ascending);
	}
	
	@Override
	public void group(TableReference table, String field) {
		group(table.field(field));
	}

	@Override
	public void group(Field reference) {
		group.add(reference);
		
	}
	
	@Override
	public void order(Field reference, boolean asc) {
		order.add(new SortOrder(reference, asc));
	}
	
	
	@Override
	public Map<String, Object> getParams() {
		Map<String, Object> out = new HashMap<>();

		for(SqlFragment sf : se) {
			out.putAll(sf.getParams());
		}
		for(SqlFragment sf : tr) {
			out.putAll(sf.getParams());
		}
		for(SqlFragment sf : order) {
			out.putAll(sf.getParams());
		}
		for(SqlFragment sf : group) {
			out.putAll(sf.getParams());
		}
		if(wc!=null) {
			out.putAll(wc.getParams());
		}
		
		for(Select s : unions) {
			out.putAll(s.getParams());
		}
		
		return out;
	}

	@Override
	public String getAlias() {
		if(aliasGenerator!=null) {
			return aliasGenerator.generateAlias("sub_select");
		}
		return "";
	}

	@Override
	public void limit(int offset, int max) {
		this.offset = offset;
		this.max = max;
	}
	
	
	@Override
	public String toString() {
		return getSql()+" "+getParams();
	}
	
	private class SortOrder implements SqlFragment {

		public SortOrder(SqlFragment sqlf, boolean ascending) {
			super();
			this.sqlf = sqlf;
			this.ascending = ascending;
		}

		private SqlFragment sqlf;
		private boolean ascending;
		
		
		@Override
		public String getSql() {
			return sqlf.getSql()+" "+(ascending?"ASC":"DESC");
		}

		@Override
		public Map<String, Object> getParams() {
			return sqlf.getParams();
		}
		
		
		
	}
	
	
}
