package de.disk0.dbutil.api;

import de.disk0.dbutil.api.entities.BaseEntity;

public interface TableReference extends SqlFragment {
	
	public String getAlias();

	public String getName();

	public JoinTable leftJoin(String table);
	public JoinTable leftJoin(Class<? extends BaseEntity<?>> table);
	
	public JoinTable join(String table);
	public JoinTable join(Class<? extends BaseEntity<?>> table);

	public Field field(String fieldname);
	public Field value(Object value);
	public Field field(Aggregate a, Field... fr);
	public Field field(Aggregate a, String fieldName);
	
}
