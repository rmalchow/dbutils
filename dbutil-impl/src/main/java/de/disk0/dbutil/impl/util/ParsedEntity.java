package de.disk0.dbutil.impl.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.util.PersistenceApiUtils.Column;

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
				Column column = PersistenceApiUtils.getColumn(f);
				if(column!=null) {
					log.debug("found a field: "+f.getName());
					String name = f.getName();
					if(!column.getName().equals("")) {
						name = column.getName();
					}
					columns.add(new ParsedColumn(f, name, column.isInsertable(), column.isUpdatable()));
				} else {
					log.debug("found a non-field: "+f.getName());
				}
			}
			c = c.getSuperclass();
			if(c==null) break;
			
		} while(clazz.getSuperclass()!=null);
		tableName = PersistenceApiUtils.getTableName(clazz);
		
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
		private boolean updatable;
		private boolean insertable;
		
		public ParsedColumn(Field field, String column, boolean insertable, boolean updatable) {
			this.field = field;
			this.column = column;
			this.setInsertable(insertable);
			this.setUpdatable(updatable);
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

				} else if(field.getType()==BigInteger.class) {
				
					field.set(target,new BigInteger(rs.getString(column)));

				} else if(field.getType()==Integer.class || field.getType()==Integer.TYPE) {
                    int value = rs.getInt(column);
                    if (field.getType().isPrimitive() && rs.wasNull()) {
                        throw new NullPointerException("Cannot set NULL value to field with type int");
                    }
                    field.set(target, rs.wasNull() ? null : value);

                } else if(field.getType()==Long.class || field.getType()==Long.TYPE) {
					long value = rs.getLong(column);
                    if (field.getType().isPrimitive() && rs.wasNull()) {
                        throw new NullPointerException("Cannot set NULL value to field with type long");
                    }
					field.set(target, rs.wasNull() ? null : value);

				} else if(field.getType()==Boolean.class || field.getType()==Boolean.TYPE) {
					boolean value = rs.getBoolean(column);
                    if (field.getType().isPrimitive() && rs.wasNull()) {
                        throw new NullPointerException("Cannot set NULL value to field with type boolean");
                    }
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
					try {
						Object o = rs.getObject(column);
						if (o !=null && o.getClass().getName().compareTo("org.mariadb.jdbc.MariaDbBlob")==0) {
							Class c = Class.forName("org.mariadb.jdbc.MariaDbBlob");
							try {
								Method ms = c.getMethod("getBinaryStream");
								InputStream is = (InputStream)ms.invoke(o);
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								IOUtils.copy(is, baos);
								byte[] buff = baos.toByteArray();
								field.set(target, buff);
							} catch (Exception e) {
								throw new SqlException("implicit mariadb conversion failed", e);
							}
						} else {
							field.set(target, o);
						}
					} catch (Exception e) {
						log.warn("mapping "+field.getName()+" ("+rs.getObject(column).getClass().getCanonicalName()+")",e);
						throw new SqlException("error mapping field", e);
					}
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

		public boolean isUpdatable() {
			return updatable;
		}

		public void setUpdatable(boolean updatable) {
			this.updatable = updatable;
		}

		public boolean isInsertable() {
			return insertable;
		}

		public void setInsertable(boolean insertable) {
			this.insertable = insertable;
		}
		
	}
	

}
