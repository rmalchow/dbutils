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
import de.disk0.dbutil.api.SubSelect;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.api.entities.BaseEntity;
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
		return String.join(" ", cs);
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
	
	public Field addSelect(TableReference tableReference, String field, String alias) {
		return addSelect(null,tableReference, field, alias);
	}

	public Field addSelect(Object value, String alias) {
		return addSelect(value,alias);
	}

	public Field addSelect(Aggregate a, TableReference tableReference, String field, String alias) {
		return select.addSelect(a, tableReference, field, alias);
	}

	@Override
	public Field addSelect(Field fr, String alias) {
		return select.addSelect(fr, alias);
	}

	@Override
	public Field addSelect(Aggregate a, String alias, Field... references) {
		return select.addSelect(a, alias, references);
	}
	
	
	public TableReference fromTable(String table) {
		return select.fromTable(table);
	}

	@Override
	public TableReference fromTable(Class<? extends BaseEntity<?>> table) {
		return fromTable(table.getAnnotation(Table.class).name());
	}
	
	public SubSelect from() {
		return select.from();
	}

	public Select union() {
		return select.union();
	}
	
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2,
			String field2) {
		return select.condition(op, table1, field1, c, table2, field2);
	}

	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value) {
		return select.condition(op, table1, field1, c, value);
	}

	@Override
	public Condition condition(Operator op) {
		return select.condition(op);
	}
	
	@Override
	public Condition condition(Operator op, Field fr1, Comparator c, Field fr2) {
		return select.condition(op, fr1, c, fr2);
	}

	@Override
	public Condition isNull(Operator op, Field fr1) {
		return select.isNull(op, fr1);
	}

	@Override
	public Condition isNotNull(Operator op, Field fr1) {
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

	public void group(Field reference) {
		select.group(reference);
	}

	public void order(Field reference, boolean ascending) {
		select.order(reference, ascending);
	}


	
}
