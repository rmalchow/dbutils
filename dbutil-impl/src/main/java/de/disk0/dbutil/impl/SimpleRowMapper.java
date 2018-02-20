package de.disk0.dbutil.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import de.disk0.dbutil.impl.util.ParsedEntity;
import de.disk0.dbutil.impl.util.ParsedEntity.ParsedColumn;

public class SimpleRowMapper<T> implements RowMapper<T> {

	protected ParsedEntity<T> parsedEntity;
	
	protected Class<T> clazz;
	
	private static Map<Class,SimpleRowMapper> mappers = new HashMap<>(); 
	
	
	private SimpleRowMapper(Class clazz) {
		this.clazz = clazz;
	}
	
	protected ParsedEntity<T> getParsedEntity() {
		if(parsedEntity==null) {
			parsedEntity = new ParsedEntity<>(clazz); 
		}
		return parsedEntity;
	}

	
	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {
			T out = clazz.newInstance();
			for(ParsedColumn pc : getParsedEntity().getColumns()) {
				try {
					pc.set(out, rs.getObject(pc.getColumnName()));
				} catch (Exception e) {
					throw new RuntimeException("failed to map column: "+pc.getColumnName(),e);
				}
			}
			return out;
		} catch (Exception e) {
			throw new SQLException("failed to map and entity ("+e.getMessage()+")",e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> SimpleRowMapper<T> get(Class<T> clazz) {
		SimpleRowMapper<?> out = mappers.get(clazz);
		if(out == null) {
			out = new SimpleRowMapper<>(clazz);
			mappers.put(clazz, out);
		}
		return (SimpleRowMapper<T>)out;
	}
	
	

}
