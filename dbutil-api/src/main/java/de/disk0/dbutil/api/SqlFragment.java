package de.disk0.dbutil.api;

import java.util.Map;

public interface SqlFragment {

	String getSql();
	Map<String, Object> getParams();
	
}
