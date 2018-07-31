package de.disk0.dbutil.impl.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.Field;
import de.disk0.dbutil.api.JoinTable;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.api.entities.BaseEntity;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlTableReferenceSimple implements TableReference {

	protected Map<String,Object> params = new HashMap<>();
	
	protected List<TableReference> refs = new ArrayList<>();
	
	private String table;
	protected String alias;
	
	
	protected AliasGenerator aliasGenerator;
	
	protected MysqlTableReferenceSimple(AliasGenerator aliasGenerator) {
		this.aliasGenerator = aliasGenerator;
	}
	
	protected MysqlTableReferenceSimple(AliasGenerator aliasGenerator, String table) {
		this(aliasGenerator);
		this.table = table;
		this.alias = aliasGenerator.generateAlias(table);
	}
	
	@Override
	public String getSql() {
		StringBuilder sb = new StringBuilder();
		if(table!=null) {
			sb.append('`');
			sb.append(table);
			sb.append('`');
		}
		sb.append(' ');
		sb.append('`');
		sb.append(getAlias());
		sb.append('`');
		if(refs.size()>0) {
			sb.append(' ');
			List<String> rSql = new ArrayList<>();
			for(TableReference r : refs) {
				rSql.add(r.getSql());
			}
			sb.append(StringUtils.join(rSql.toArray()," "));
		}
		return sb.toString();
	}

	@Override
	public Map<String, Object> getParams() {
		Map<String, Object> out = new HashMap<>();
		out.putAll(params);
		for(TableReference tr : refs) {
			out.putAll(tr.getParams());
		}
		return out;
	}

	@Override
	public JoinTable leftJoin(Class<? extends BaseEntity<?>> table) {
		return leftJoin(table.getAnnotation(Table.class).name());
	}
	
	@Override
	public JoinTable leftJoin(String table) {
		MysqlTableReferenceSimple s = new MysqlTableReferenceSimple(this.aliasGenerator,table);
		MysqlTableReferenceJoinTable j = new MysqlTableReferenceJoinTable(this.aliasGenerator, s, true);
		refs.add(j);
		return j;
	}

	@Override
	public JoinTable join(Class<? extends BaseEntity<?>> table) {
		return join(table.getAnnotation(Table.class).name());
	}
	
	@Override
	public JoinTable join(String table) {
		MysqlTableReferenceSimple s = new MysqlTableReferenceSimple(this.aliasGenerator,table);
		MysqlTableReferenceJoinTable j = new MysqlTableReferenceJoinTable(this.aliasGenerator, s, false);
		refs.add(j);
		return j;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public String getName() {
		return table;
	}

	@Override
	public Field field(String fieldname) {
		return new MysqlField(this, fieldname);
	}

	@Override
	public Field value(Object value) {
		return new MysqlField(aliasGenerator,value);
	}

	@Override
	public Field field(Aggregate a, Field... references) {
		return new MysqlField(a, references);
	}
	
	@Override
	public Field field(Aggregate a, String fieldname) {
		return new MysqlField(a, this, fieldname);
	}
	
}
