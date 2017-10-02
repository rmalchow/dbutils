package de.disk0.dbutil.api;

public enum Comparator {

	EQ("="),
	NE("<>"),
	GTE(">="),
	LTE("<="),
	GT(">"),
	LT("<"),
	NULL("IS NULL"),
	NOT_NULL("IS NOT NULL"),
	LIKE("LIKE"),
	IN("IN"),
	NOT_IN("NOT IN");

	private String symbol;
	
	private Comparator(String symbol) {
		this.setSymbol(symbol);
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	
}
