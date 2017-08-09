package de.disk0.dbutil.impl.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Condition;
import de.disk0.dbutil.api.Insert;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.impl.util.AliasGenerator;

public class MysqlInsert implements Insert {

	private TableReference trInto;
	private List<MysqlFieldValuePair> values = new ArrayList<>();
	private Condition wc;
	
	private Select select;
	
	
	private AliasGenerator aliasGenerator;
	private Map<String,Object> params = new HashMap<>();

	
	public MysqlInsert() {
		this.aliasGenerator = new AliasGenerator();
	}
	
	public MysqlInsert(AliasGenerator aliasGenerator) {
		this.aliasGenerator = aliasGenerator;
	}
	
	@Override
	public TableReference into(String table) {
		if(trInto!=null) {
			throw new RuntimeException("into is already set");
		}
		trInto = new MysqlTableReferenceSimple(aliasGenerator, table);
		return trInto;
	}

	@Override
	public void addField(TableReference tr, String field, Object value) {
		if(tr==null) {
			throw new NullPointerException("table reference");
		}
		if(field==null) {
			throw new NullPointerException("field");
		}
		values.add(new MysqlFieldValuePair(aliasGenerator,tr,field,null,null,value));
	}

	@Override
	public void addField(TableReference tr, String field, TableReference tr2, String field2) {
		if(tr==null) {
			throw new NullPointerException("table reference");
		}
		if(field==null) {
			throw new NullPointerException("field");
		}
		values.add(new MysqlFieldValuePair(aliasGenerator,tr,field,tr2,field2,null));
	}
	
	@Override
	public String getSql() {
		List<String> components = new ArrayList<>();
		components.add("INSERT INTO");
		
		components.add("`"+trInto.getName()+"`");

		if(values.size()>0) {
			List<String> fieldList = new ArrayList<>();
			List<String> valueList = new ArrayList<>();
			for(MysqlFieldValuePair fvp : values) {
				String[] x = fvp.getSql();
				fieldList.add(x[0]);
				valueList.add(x[1]);
			}
			components.add("("+StringUtils.join(fieldList.toArray(),", ")+")");
			components.add("VALUES");
			components.add("("+StringUtils.join(valueList.toArray(),", ")+")");
		}
		if(wc!=null) {
			components.add(wc.getSql());
		}
			
		return StringUtils.join(components.toArray()," ");		
	}

	@Override
	public Map<String, Object> getParams() {
		Map<String, Object> out = new HashMap<>();
		out.putAll(params);
		if(wc!=null) {
			out.putAll(wc.getParams());
		}
		return out;
	}
	
	@Override
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, TableReference table2, String field2) {
		if(wc==null) {
			wc = new MysqlCondition(this.aliasGenerator);
		}
		wc.condition(op, table1, field1, c, table2,field2);
		return wc;
	}

	@Override
	public Condition condition(Operator op, TableReference table1, String field1, Comparator c, Object value) {
		if(wc==null) {
			wc = new MysqlCondition(this.aliasGenerator);
		}
		wc.condition(op, table1, field1, c, value);
		return wc;
	}

	@Override
	public String toString() {
		return getSql()+" "+getParams();
	}
	
	

}
