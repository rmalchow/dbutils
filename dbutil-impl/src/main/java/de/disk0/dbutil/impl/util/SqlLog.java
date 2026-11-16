package de.disk0.dbutil.impl.util;

import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Keeps parameter values out of the default logs: other loggers get names, values go only to
 * de.disk0.dbutil.sql.params at TRACE.
 */
public final class SqlLog {

	private static final Log PARAMS = LogFactory.getLog("de.disk0.dbutil.sql.params");

	private SqlLog() {
	}

	/** Parameter names only, sorted: "[email, unitId]". Safe at any level. */
	public static String names(Map<String, ?> params) {
		return params == null ? "[]" : new TreeSet<>(params.keySet()).toString();
	}

	/** Values, only on de.disk0.dbutil.sql.params at TRACE. */
	public static void values(String context, Map<String, ?> params) {
		if (PARAMS.isTraceEnabled()) PARAMS.trace("params " + context + ": " + params);
	}
}
