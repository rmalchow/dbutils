package de.disk0.dbutil.impl.mysql;

import java.util.HashMap;
import java.util.Map;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.SelectExpression;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlSelectExpression implements SelectExpression {

	
	private Aggregate aggregate;
	
	private Object value;
	private String table;
	private String field;
	private String alias;
	private String valueAlias;
	
	private Map<String,Object> params = new HashMap<>();
	
	public MysqlSelectExpression(Aggregate aggregate, String table, String field, String alias) {
		this.aggregate = aggregate;
		this.table = table;
		this.field = field;
		this.alias = alias;
	}

	public MysqlSelectExpression(AliasGenerator ag, Object value, String alias) {
		this.alias = alias;
		this.valueAlias = ag.generateAlias(alias);
		this.value = value;
		params.put(valueAlias, value);
	}
	
	@Override
	public String getSql() {
		StringBuilder sb = new StringBuilder();
		if(value!=null) {
			sb.append(':');
			sb.append(valueAlias);
			sb.append(" as `");
			sb.append(alias);
			sb.append('`');
		} else {
			String t = "`"+table+"`.`"+field+"`";
			if(aggregate==null) {
				// none
				sb.append(t);
			} else if(aggregate==Aggregate.DISTINCT) {
				sb.append("DISTINCT "+t);
			} else if(aggregate==Aggregate.AVG) {
				sb.append("AVG("+t+")");
			} else if(aggregate==Aggregate.MIN) {
				sb.append("MIN("+t+")");
			} else if(aggregate==Aggregate.MAX) {
				sb.append("MAX("+t+")");
			} else if(aggregate==Aggregate.SUM) {
				sb.append("SUM("+t+")");
			} else if(aggregate==Aggregate.GROUP_CONCAT) {
				sb.append("GROUP_CONCAT("+t+")");
			}
			sb.append(" as `"+alias+"`");
		}
		return sb.toString();
	
	}
	
	@Override
	public Map<String, Object> getParams() {
		return params;
	}
	
}
