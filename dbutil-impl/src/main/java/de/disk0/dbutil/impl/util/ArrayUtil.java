package de.disk0.dbutil.impl.util;

import java.util.List;
import java.util.Map;

public class ArrayUtil {
	
	public static Map<String,?>[] array(List<Map<String,Object>> list) {
		Map<String,?>[] out = (Map<String,?>[]) new Map[list.size()];
		list.toArray(out);
		return out;
	}
	

}
