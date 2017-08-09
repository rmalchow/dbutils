package de.disk0.dbutil.api.exceptions;

public class SqlException extends Exception {

	private static final long serialVersionUID = 2872221145854622939L;
	
	public SqlException(String message, Throwable cause) {
		super(message,cause);
	}

	public SqlException(String code, Object[] params, Throwable cause) {
		super(code,cause);
	}
	
}
