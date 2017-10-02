package de.disk0.dbutil.impl.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.Field;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlField implements Field {

	private Aggregate aggregate;
	private List<Field> fields = new ArrayList<>();
	private Map<String,Object> params = new HashMap<>();
	private String table;
	private String field;
	private String valueAlias;
	private String alias;
	
	public MysqlField(Aggregate aggregate, Field... references) {
		this.aggregate = aggregate;
		for(Field f : references) {
			fields.add(f);
			params.putAll(f.getParams());
		}
	}

	public MysqlField(AliasGenerator ag, Object value) {
		this.valueAlias = ag.generateAlias("value");
		this.params.put(this.valueAlias, value);
	}
	
	public MysqlField(TableReference tr, String field) {
		this.table = tr.getAlias();
		this.field = field;
	}
	
	public MysqlField(Field fr, String alias) {
		fields.add(fr);
		this.alias = alias;
	}
	
	@Override
	public String getSql() {
		
		List<String> parts = new ArrayList<>();
		if(table!=null) {
			parts.add("`"+table+"`.`"+field+"`");
		} else if(valueAlias!=null) {
			parts.add(":"+valueAlias);
		} else {
			for(Field f : fields) {
				parts.add(f.getSql());
			}
		}

		String t = StringUtils.join(parts,", ");
		
		List<String> parts2 = new ArrayList<>();
		
		if(aggregate==null) {
			parts2.add(t);
		} else if(aggregate==Aggregate.DISTINCT) {
			parts2.add("DISTINCT");
			parts2.add(t);
		} else if(
				aggregate==Aggregate.AVG ||
				aggregate==Aggregate.MIN ||
				aggregate==Aggregate.MAX ||
				aggregate==Aggregate.SUM ||
				aggregate==Aggregate.CONCAT ||
				aggregate==Aggregate.GROUP_CONCAT
			) {
			parts2.add(aggregate.name());
			parts2.add("("+t+")");
		} else {
			parts2.add(t);
		}

		if(alias!=null) {
			parts2.add("AS");
			parts2.add(alias);
			
		}
		
		String x = StringUtils.join(parts2," "); 
		
		
		
		System.err.println(x);
		
		return x;
		
	}
	
	@Override
	public Map<String, Object> getParams() {
		return params;
	}
	
}
