package de.disk0.dbutil.api.exceptions;

public class NonUniqueResultException extends SqlException {

	private static final long serialVersionUID = -1592918734851712070L;

	public NonUniqueResultException() {
		super("SQL.NON_UNIQUE", null);
	}

}
