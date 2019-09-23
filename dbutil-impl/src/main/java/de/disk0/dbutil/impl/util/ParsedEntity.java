package de.disk0.dbutil.impl.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ParsedEntity<T> {
	
	private static Log log = LogFactory.getLog(ParsedEntity.class);
	
	private String tableName;
	private List<ParsedColumn> columns = new ArrayList<>();
	
	public ParsedEntity(Class<T> clazz) {
		Class<?> c = clazz;
		List<String> x = new ArrayList<>();
		do {
			for(Field f : c.getDeclaredFields()) {
				if(x.contains(f.getName())) continue;
				x.add(f.getName());
				if(f.getAnnotation(Column.class)!=null) {
					Column column = f.getAnnotation(Column.class);
					String name = f.getName();
					if(!column.name().equals("")) {
						name = column.name();
					}
					columns.add(new ParsedColumn(f, name));
				}
			}
			c = c.getSuperclass();
			if(c==null) break;
			
		} while(clazz.getSuperclass()!=null);
		tableName = clazz.getAnnotation(Table.class).name();
		
	}
	
	public String getTableName() {
		return this.tableName;
	}
	
	public String getIdColumn() {
		return "id";
	}
	
	public List<ParsedColumn> getColumns() {
		return new ArrayList<>(columns);
	}

	public class ParsedColumn {
		
		private Field field;
		private String column;
		
		public ParsedColumn(Field field, String column) {
			this.field = field;
			this.column = column;
			this.field.setAccessible(true);
		}
		
		public String getColumnName() {
			return column;
		}
		
		public void set(T target, ResultSet rs) throws IllegalArgumentException, IllegalAccessException {
			try {

				if(field.getType()==String.class) {
					field.set(target,rs.getString(column));
				} else if(field.getType()==BigDecimal.class) {
					field.set(target,rs.getBigDecimal(column));
				} else if(field.getType()==Integer.class || field.getType()==Integer.TYPE) {
					int value = rs.getInt(column);
					field.set(target, rs.wasNull() ? null : value);
				} else if(field.getType()==Long.class || field.getType()==Long.TYPE) {
					long value = rs.getLong(column);
					field.set(target, rs.wasNull() ? null : value);
				} else if(field.getType()==Boolean.class || field.getType()==Boolean.TYPE) {
					boolean value = rs.getBoolean(column);
					field.set(target, rs.wasNull() ? null : value);
				} else if(Enum.class.isAssignableFrom(field.getType())) {
					String value = rs.getString(column);
					if(value==null) {
						field.set(target, null);
					} else {
						for(Object o : field.getType().getEnumConstants()) {
							if(o.toString().compareTo(value)==0) {
								field.set(target, o);
							}
						}
					}
				} else {
					field.set(target, rs.getObject(column));
				}
				
				log.debug("mapping column: "+column+" - "+rs.getObject(column));
				
			} catch (Exception e) {
				log.error("error mapping "+column+": ",e);
				throw new IllegalArgumentException("error mapping "+column+": ",e);
			}
		}
		
		public Object get(T target) throws IllegalArgumentException, IllegalAccessException {
			if(Enum.class.isAssignableFrom(field.getType())) {
				Object o = field.get(target);
				if(o==null) return null;
				return ((Enum)o).name();
			}
			return field.get(target);
		}
		
	}
	

}
