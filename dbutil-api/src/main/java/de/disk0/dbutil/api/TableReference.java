package de.disk0.dbutil.api;

import java.util.Map;

public interface TableReference {
	
	public Map<String,Object> getParams();
	
	public String getAlias();

	public String getName();

	public String getSql();

	public JoinTable leftJoin(String table);
	
	public JoinTable join(String table);

	public FieldReference field(String fieldname);
	public FieldReference value(Object value);
	public FieldReference field(Aggregate a, FieldReference... fr);
	
}
