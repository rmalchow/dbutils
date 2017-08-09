package de.disk0.dbutil.api;

import java.util.Map;

public interface SelectExpression {

	public String getSql();
	
	public Map<String,Object> getParams();

}
