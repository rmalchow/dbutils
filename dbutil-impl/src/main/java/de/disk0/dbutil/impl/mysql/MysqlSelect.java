package de.disk0.dbutil.impl.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Condition;
import de.disk0.dbutil.api.FieldReference;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.SubSelect;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlSelect implements Select {

	private List<FieldReference> se = new ArrayList<>();
	private List<TableReference> tr = new ArrayList<>();
	private Condition wc;
	private List<String> order = new ArrayList<>();
	private List<String> group = new ArrayList<>();
	
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
	public FieldReference addSelect(Aggregate aggregate, TableReference tableReference, String field, String alias) {
		if(alias == null) {
			alias = field;
		}
		FieldReference f1 = new MysqlField(tableReference,field); 
		FieldReference f2 = new MysqlField(aggregate,f1); 
		FieldReference f3 = new MysqlField(f2,alias); 
		se.add(f3);
		return f3;
	}

	@Override
	public FieldReference addSelect(TableReference tableReference, String field, String alias) {
		if(alias == null) {
			alias = field;
		}
		FieldReference f1 = new MysqlField(tableReference,field); 
		FieldReference f2 = new MysqlField(f1,alias); 
		se.add(f2);
		return f2;
	}

	@Override
	public FieldReference addSelect(Aggregate a, String alias, FieldReference... references) {
		FieldReference f1 = new MysqlField(a, references); 
		FieldReference f2 = new MysqlField(f1,alias); 
		se.add(f2);
		return f2;
	}
	
	@Override
	public FieldReference addSelect(FieldReference fr, String alias) {
		FieldReference f2 = new MysqlField(fr,alias); 
		se.add(f2);
		return f2;
	}
	
	@Override
	public FieldReference addSelect(Object value, String alias) {
		FieldReference f1 = new MysqlField(aliasGenerator,value); 
		FieldReference f2 = new MysqlField(f1,alias); 
		se.add(f2);
		return f2;
	}
	
	@Override
	public TableReference fromTable(String table) {
		TableReference t = new MysqlTableReferenceSimple(this.aliasGenerator,table);
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
	public Condition condition(Operator op, FieldReference left, Comparator c, FieldReference right) {
		if(wc==null) {
			wc = new MysqlCondition(aliasGenerator);
		}
		return wc.condition(op, left, c, right);
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
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		if(se.size()==0) {
			sb.append("*");
		} else {
			List<String> seSql = new ArrayList<>();
			for(FieldReference s : se) {
				seSql.add(s.getSql());
			}
			sb.append(StringUtils.join(seSql.toArray(),", "));
		}
		sb.append(" FROM ");
		{
			List<String> trSql = new ArrayList<>();
			for(TableReference t : tr) {
				trSql.add(t.getSql());
			}
			sb.append(StringUtils.join(trSql.toArray(),", "));
		}
		if(wc!=null) {
			sb.append(" WHERE ");
			sb.append(wc.getSql());
		}

		if(group.size()>0) {
			sb.append(" GROUP BY ");
			sb.append(StringUtils.join(group.toArray(),", "));
		}
		
		if(order.size()>0) {
			sb.append(" ORDER BY ");
			sb.append(StringUtils.join(order.toArray(),", "));
		}
		
		if(offset > -1 && max > -1) {
			sb.append(" LIMIT ");
			sb.append(offset);
			sb.append(',');
			sb.append(max);
		} else if (max > -1) {
			sb.append(" LIMIT ");
			sb.append(max);
		}
		
		return sb.toString();
	}

	@Override
	public void order(TableReference table, String field, boolean ascending) {
		StringBuilder sb = new StringBuilder();
		sb.append('`');
		sb.append(table.getAlias());
		sb.append('`');
		sb.append('.');
		sb.append('`');
		sb.append(field);
		sb.append('`');
		sb.append(' ');
		sb.append(ascending?"ASC":"DESC");
		order.add(sb.toString());
	}
	
	@Override
	public void group(TableReference table, String field) {
		StringBuilder sb = new StringBuilder();
		sb.append('`');
		sb.append(table.getAlias());
		sb.append('`');
		sb.append('.');
		sb.append('`');
		sb.append(field);
		sb.append('`');
		group.add(sb.toString());
	}
	
	@Override
	public Map<String, Object> getParams() {
		Map<String, Object> out = new HashMap<>();
		for(FieldReference se : se) {
			out.putAll(se.getParams());
		}
		for(TableReference t : tr) {
			out.putAll(t.getParams());
		}
		if(wc!=null) {
			out.putAll(wc.getParams());
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
	
}
