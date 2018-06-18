package de.disk0.dbutil.schemacheck;

public class Key {
	
	private boolean unique;
	private int seqInIndex;

	private String table;
	private String keyname;
	private String column;
	public boolean isUnique() {
		return unique;
	}
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	public int getSeqInIndex() {
		return seqInIndex;
	}
	public void setSeqInIndex(int seqInIndex) {
		this.seqInIndex = seqInIndex;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getKeyname() {
		return keyname;
	}
	public void setKeyname(String keyname) {
		this.keyname = keyname;
	}
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	

}
