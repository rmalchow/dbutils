package de.disk0.dbutil.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class EnumUtil {
	
	public static Object resolveEnums(Object in) {
		if(in == null) {
			return in;
		} else if(in.getClass().isEnum()) {
			return ((Enum)in).name();
		} else if(in instanceof Collection) {
			
			if(((Collection)in).size()==0) {
				return in;
			} else if(!((((Collection)in).iterator()) instanceof Enum)) {
				return in;
			}
			List<String> x = new ArrayList<>();
			Iterator<Enum> e = (Iterator<Enum>)((Collection)in).iterator();
			while(e.hasNext()) {
				x.add(e.next().name());
			}
			return x;
		}
		return in;
	}
	

}
