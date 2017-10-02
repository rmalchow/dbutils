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
import de.disk0.dbutil.api.SelectExpression;
import de.disk0.dbutil.api.SubSelect;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlTableReferenceSubSelect extends MysqlTableReferenceSimple implements SubSelect {

	protected Map<String,Object> params = new HashMap<>();
	
	private String table;
	private Select select;
	
	protected MysqlTableReferenceSubSelect(AliasGenerator aliasGenerator) {
		super(aliasGenerator);
		this.alias = aliasGenerator.generateAlias("subselect");
		select = new MysqlSelect(this.aliasGenerator);
	}
	
	@Override
	public String getSql() {
		List<String> cs = new ArrayList<>();

		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(select.getSql());
		sb.append(")");
		sb.append(' ');
		sb.append('`');
		sb.append(getAlias());
		sb.append('`');
		
		cs.add(sb.toString());
		
		if(refs.size()>0) {
			for(TableReference r : refs) {
				cs.add(r.getSql());
			}
		}
		return StringUtils.join(cs.toArray()," ");
	}

	@Override
	public Map<String, Object> getParams() {
		Map<String, Object> out = new HashMap<>();
		out.putAll(params);
		if(select!=null) {
			out.putAll(select.getParams());
		}
		for(TableReference tr : refs) {
			out.putAll(tr.getParams());
		}
		return out;
	}
	
	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public String getName() {
		return table;
	}
	
	public FieldReference addSelect(TableReference tableReference, String field, String alias) {
		return addSelect(null,tableReference, field, alias);
	}

	public FieldReference addSelect(Object value, String alias) {
		return addSelect(value,alias);
	}

	public FieldReference addSelect(Aggregate a, TableReference tableReference, String field, String alias) {
		return select.addSelect(a, tableReference, field, alias);
	}

	@Override
	public FieldReference addSelect(FieldReference fr, String alias) {
		return select.addSelect(fr, alias);
	}

	@Override
	public FieldReference addSelect(Aggregate a, String alias, FieldReference... references) {
		return select.addSelect(a, alias, references);
	}
	
	
	public TableReference fromTable(String table) {
		return select.fromTable(table);
	}

	public SubSelect from() {
		return select.from();
	}

	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2,
			String field2) {
		return select.condition(op, table1, field1, c, table2, field2);
	}

	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value) {
		return select.condition(op, table1, field1, c, value);
	}
	
	public Condition condition(Operator op, FieldReference fr1, Comparator c, FieldReference fr2) {
		return select.condition(op, fr1, c, fr2);
	}

	public Condition isNull(Operator op, FieldReference fr1) {
		return select.isNull(op, fr1);
	}

	public Condition isNotNull(Operator op, FieldReference fr1) {
		return select.isNotNull(op, fr1);
	}

	public void limit(int offset, int max) {
		select.limit(offset, max);
	}

	public void order(TableReference table, String field, boolean ascending) {
		select.order(table, field, ascending);
	}

	public void group(TableReference table, String field) {
		select.group(table, field);
	}


	
}
