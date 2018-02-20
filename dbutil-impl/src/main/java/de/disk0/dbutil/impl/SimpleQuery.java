package de.disk0.dbutil.impl;

import java.util.HashMap;
import java.util.Map;

public class SimpleQuery {

	private Map<String,Object> params = new HashMap<>();
	private String query;
	
	public SimpleQuery(String query) {
		this.setQuery(query);
	}
	

	public Object put(String key, Object value) {
		return getParams().put(key, value);
	}

	public Object remove(Object key) {
		return getParams().remove(key);
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		getParams().putAll(m);
	}

	public void clear() {
		getParams().clear();
	}


	public String getQuery() {
		return query;
	}


	public void setQuery(String query) {
		this.query = query;
	}


	public Map<String,Object> getParams() {
		return params;
	}
	
	
}
