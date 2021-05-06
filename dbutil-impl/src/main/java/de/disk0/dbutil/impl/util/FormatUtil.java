package de.disk0.dbutil.impl.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FormatUtil {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	public static Object resolve(Object value) {
		if(value instanceof Boolean) {
			value = ((Boolean)value).booleanValue()?1:0;
		}
		
		if(value instanceof Calendar) {
			value = new Date(((Calendar)value).getTimeInMillis());
		}
		
		if(value instanceof Date) {
			value = sdf.format(value);
		}
		return value;
	}
	
}
