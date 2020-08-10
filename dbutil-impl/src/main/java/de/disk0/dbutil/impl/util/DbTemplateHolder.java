package de.disk0.dbutil.impl.util;

public class DbTemplateHolder {

	private static ThreadLocal<DbTemplate> t = new ThreadLocal<>();
	
	public static DbTemplate get() {
		return t.get();
	}
	
	public static void init() {
		t.set(new DbTemplate());
	}
	
	public static void clear() {
		t.remove();
	}
	
}
