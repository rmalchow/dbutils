package de.disk0.dbutil.schemacheck;

import java.util.List;

public class Table {

	private String name;
	private List<Column> columns;
	private List<Key> keys;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Column> getColumns() {
		return columns;
	}
	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}
	public List<Key> getKeys() {
		return keys;
	}
	public void setKeys(List<Key> keys) {
		this.keys = keys;
	}
	
}
