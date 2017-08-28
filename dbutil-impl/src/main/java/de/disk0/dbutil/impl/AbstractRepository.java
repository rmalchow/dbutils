package de.disk0.dbutil.impl;

import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import de.disk0.dbutil.api.entities.BaseEntity;
import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.util.ParsedEntity;
import de.disk0.dbutil.impl.util.ParsedEntity.ParsedColumn;


public abstract class AbstractRepository<T extends BaseEntity<S>,S> implements RowMapper<T> {

	private static Log log = LogFactory.getLog(AbstractRepository.class);
	
	private Class<T> clazz;
	
	@Autowired
	protected DataSource dataSource;

	private String findOneStatement;
	private String findAllStatement;
	private String insertOneStatement;
	private String updateOneStatement;
	private String deleteOneStatement;
	
	private ParsedEntity<T> parsedEntity;
	
	public AbstractRepository() {
	}
	
	@SuppressWarnings("unchecked")
	protected Class<T> getClazz() {
		if(clazz==null) {
			clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
		return clazz;
	}

	protected ParsedEntity<T> getParsedEntity() {
		if(parsedEntity==null) {
			parsedEntity = new ParsedEntity<>(getClazz()); 
		}
		return parsedEntity;
	}
	
	protected List<T> find(String sql, Map<String,Object> params) throws SqlException {
		try {
			log.debug(sql);
			log.debug(params);
			NamedParameterJdbcTemplate t = new NamedParameterJdbcTemplate(dataSource);
			List<T> out = t.query(sql, params, this);
			log.debug("-------- found: "+out.size());
			return out;
		} catch (Exception e) {
			throw new SqlException("SQL.REPO.LIST_FAILED",new Object[] { e.getMessage() },  e);
		}
	}
	
	protected String getFindOneStatement() {
		if(findOneStatement == null) {
			ParsedEntity<T> pe = getParsedEntity();
			findOneStatement = "SELECT * FROM `"+pe.getTableName()+"` WHERE `"+pe.getIdColumn()+"` = :id";
		}
		return findOneStatement; 
	}
	
	protected List<T> findAll() throws SqlException {
		return find(getFindAllStatement(), new HashMap<>());
	}
	
	protected String getFindAllStatement() {
		if(findAllStatement == null) {
			ParsedEntity<T> pe = getParsedEntity();
			findAllStatement = "SELECT * FROM `"+pe.getTableName()+"`";
		}
		return findAllStatement; 
	}
	
	public T get(S id) throws SqlException {
		try {
			NamedParameterJdbcTemplate t = new NamedParameterJdbcTemplate(dataSource);
			Map<String,Object> params = new HashMap<>();
			params.put("id", id);
			List<T> ts = t.query(getFindOneStatement(), params, this);
			if(ts.size()>0) {
				return ts.get(0);
			}
			return null;
		} catch (Exception e) {
			throw new SqlException("SQL.REPO.LIST_FAILED",new Object[] { e.getMessage() },  e);
		}
	}
	
	protected String getInsertOneStatement() {
		if(insertOneStatement == null) {
			ParsedEntity<T> pe = getParsedEntity();
			List<String> columns = new ArrayList<>();
			List<String> values = new ArrayList<>();
			for(ParsedColumn pc : pe.getColumns()) {
				columns.add("`"+pc.getColumnName()+"`");
				values.add(":"+pc.getColumnName());
			}
			insertOneStatement = "INSERT INTO `"+pe.getTableName()+"` ("+(StringUtils.join(columns,", "))+") VALUES ("+(StringUtils.join(values,", "))+")";
		}
		return insertOneStatement; 
	}
	
	protected String getUpdateOneStatement() {
		if(updateOneStatement == null) {
			ParsedEntity<T> pe = getParsedEntity();
			List<String> columns = new ArrayList<>();
			for(ParsedColumn pc : pe.getColumns()) {
				columns.add("`"+pc.getColumnName()+"`=:"+pc.getColumnName());
			}
			updateOneStatement = "UPDATE `"+pe.getTableName()+"` SET "+(StringUtils.join(columns,", "))+" WHERE `"+pe.getIdColumn()+"`=:id";
		}
		return updateOneStatement; 
	}
	
	protected void beforeSave(T t) throws Exception {
	}
	
	protected void afterSave(T t) throws Exception {
	}
	
	
	public abstract T save(T t) throws SqlException;
	
	protected String getDeleteOneStatement() {
		if(deleteOneStatement == null) {
			ParsedEntity<T> pe = getParsedEntity();
			deleteOneStatement = "DELETE FROM `"+pe.getTableName()+"` WHERE `"+pe.getIdColumn()+"`=:id";
		}
		return deleteOneStatement; 
	}

	protected void beforeDelete(T t) throws Exception {
	}
	
	protected void afterDelete(T t) throws Exception {
	}
	

	
	public void delete(S id) throws SqlException {
		delete(get(id));
	}
	
	public void delete(String sql, Map<String,Object> params) throws SqlException {
		try {
			NamedParameterJdbcTemplate templ = new NamedParameterJdbcTemplate(dataSource);
			templ.update(sql, params);
		} catch (Exception e) {
			throw new SqlException("SQL.REPO.DELETE_FAILED",new Object[] { e.getMessage() },  e);
		}
	}
	
	
	public void delete(T t) throws SqlException {
		String sql = "[none]";
		try {
			if(t==null || t.getId()==null) {
				return;
			}
			beforeDelete(t);
			NamedParameterJdbcTemplate templ = new NamedParameterJdbcTemplate(dataSource);
			Map<String,Object> params = new HashMap<>();
			params.put("id", t.getId());
			sql = getDeleteOneStatement();
			templ.update(sql, params);
			afterDelete(t);
		} catch (Exception e) {
			log.warn("delete failed: "+sql);
			throw new SqlException("SQL.REPO.DELETE_FAILED",new Object[] { e.getMessage() },  e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected HashMap<String, Object> unmap(T t) throws IllegalArgumentException, IllegalAccessException {
		HashMap<String, Object> out = new HashMap<>();
		for(ParsedColumn pc : getParsedEntity().getColumns()) {
			log.debug("unmapping ---> "+pc.getColumnName());
			Object o = pc.get(t);
			out.put(pc.getColumnName(), o==null?java.sql.Types.NULL:o);
		}
		return out;
	}
	
	@Override
	public T mapRow(ResultSet rs, int c) throws SQLException {
		try {
			T out = getClazz().newInstance();
			for(ParsedColumn pc : getParsedEntity().getColumns()) {
				log.debug("mapping ---> "+pc.getColumnName());
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
	
}
