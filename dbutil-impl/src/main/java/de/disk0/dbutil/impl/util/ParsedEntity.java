package de.disk0.dbutil.impl.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

public class ParsedEntity<T> {
	
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
		
		public void set(T target, Object value) throws IllegalArgumentException, IllegalAccessException {
			if(Enum.class.isAssignableFrom(field.getType())) {
				if(value==null) {
					field.set(target, null);
					return;
				}
				for(Object o : field.getType().getEnumConstants()) {
					if(o.toString().compareTo(value.toString())==0) {
						field.set(target, o);
						return;
					}
				}
			}
			field.set(target, value);
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
