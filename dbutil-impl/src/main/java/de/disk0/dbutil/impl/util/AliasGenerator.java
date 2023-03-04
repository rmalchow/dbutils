package de.disk0.dbutil.impl.util;

import java.util.HashMap;
import java.util.Map;

public class AliasGenerator {

	private Map<String,Integer> map = new HashMap<>();
	
	public String generateAlias(String prefix) {
		prefix = prefix.replaceAll("[^A-Za-z0-9_]+", "_");
		Integer i = map.get(prefix);
		if(i==null) {
			i = new Integer(0);
		}
		i++;
		map.put(prefix, i);
		prefix = prefix +"_"+i;
		return prefix;
	}
	
}
