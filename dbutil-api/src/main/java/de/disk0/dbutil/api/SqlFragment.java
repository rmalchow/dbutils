package de.disk0.dbutil.api;

import java.util.Map;

public interface SqlFragment {

	public String getSql();
	public Map<String, Object> getParams();
	
}
