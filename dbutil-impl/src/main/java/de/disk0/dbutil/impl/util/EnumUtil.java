package de.disk0.dbutil.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EnumUtil {
	
	public static Object resolveEnums(Object in) {
		List<Object> out = new ArrayList<>();
		if(in == null) {
			return in;
		} else if(in.getClass().isEnum()) {
			return ((Enum)in).name();
		} else if(in instanceof Collection) {
			for(Object o : ((Collection)in)) {
				out.add(o);
			}
		} else if (in.getClass().isArray()) {
			for(Object o : ((Object[])in)) {
				out.add(o);
			}
		} else {
			return in;
		}

		if(out.size()==0) {
			return out;
		}
		if(out.get(0) instanceof Enum) {
			List<String> x = new ArrayList<>();
			for(Object o : out) {
				x.add(((Enum)o).name());
			}
			return x;
		}
		return out;
	}
	

}
