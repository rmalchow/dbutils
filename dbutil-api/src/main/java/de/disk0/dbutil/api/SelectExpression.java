package de.disk0.dbutil.api;

import java.util.Map;

public interface SelectExpression {

	String getSql();
	
	Map<String,Object> getParams();

}
