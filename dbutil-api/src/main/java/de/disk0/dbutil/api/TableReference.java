package de.disk0.dbutil.api;

public interface TableReference extends SqlFragment {
	
	public String getAlias();

	public String getName();

	public JoinTable leftJoin(String table);
	
	public JoinTable join(String table);

	public Field field(String fieldname);
	public Field value(Object value);
	public Field field(Aggregate a, Field... fr);
	public Field field(Aggregate a, String fieldName);
	
}
