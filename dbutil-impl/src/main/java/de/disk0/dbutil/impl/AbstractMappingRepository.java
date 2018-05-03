package de.disk0.dbutil.impl;

import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import de.disk0.dbutil.api.exceptions.NonUniqueResultException;
import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.util.ParsedEntity;
import de.disk0.dbutil.impl.util.ParsedEntity.ParsedColumn;

public abstract class AbstractMappingRepository<T> implements RowMapper<T> {
	
	private static Log log = LogFactory.getLog(AbstractRepository.class);

	@Autowired
	protected DataSource dataSource;

	protected ParsedEntity<T> parsedEntity;
	
	protected Class<T> clazz;
	
	private List<String> applicableColumns;
	
	@Autowired
	private NamedParameterJdbcTemplate template;
	
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
	
	@SuppressWarnings("rawtypes")
	protected MapSqlParameterSource unmap(T t) throws IllegalArgumentException, IllegalAccessException {
		MapSqlParameterSource out = new MapSqlParameterSource();
		for(ParsedColumn pc : getParsedEntity().getColumns()) {
			Object o = pc.get(t);
			out.addValue(pc.getColumnName(),o);
		}
		return out;
	}
	
	@Override
	public T mapRow(ResultSet rs, int c) throws SQLException {
		try {

			log.info("mapping row ... ");
			
			if(applicableColumns==null) {
				synchronized (this) {
					applicableColumns = new ArrayList<>();
					for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
						applicableColumns.add(rs.getMetaData().getColumnName(i+1).toUpperCase());
					}
				}
			}
			
			T out = getClazz().newInstance();
			for(ParsedColumn pc : getParsedEntity().getColumns()) {
				if(!applicableColumns.contains(pc.getColumnName().toUpperCase())) {
					log.warn("column: "+pc.getColumnName()+" in object, but not in result set");
					continue;
				}
				try {
					pc.set(out, rs);
				} catch (Exception e) {
					throw new RuntimeException("failed to map column: "+pc.getColumnName(),e);
				}
			}
			return out;
		} catch (Exception e) {
			log.error("failed to map entity: ",e);
			throw new SQLException("failed to map entity ("+e.getMessage()+")",e);
		}
	}
	
	public List<T> find(String sql, Map<String,Object> params) throws SqlException {
		try {
			NamedParameterJdbcTemplate t = new NamedParameterJdbcTemplate(getDataSource());
			long start = System.currentTimeMillis();
			log.debug("-------- query: "+sql+" / "+params);
			List<T> out = t.query(sql, params, this);
			log.debug("-------- found: "+out.size()+" / "+(System.currentTimeMillis()-start)+"ms");
			return out;
		} catch (Exception e) {
			log.warn("-------- query failed: "+sql+" / "+params);
			throw new SqlException("SQL.REPO.LIST_FAILED",new Object[] { e.getMessage() },  e);
		}
	}

	protected T findOne(String sql, Map<String,Object> params) throws SqlException {
		try {
			List<T> out = find(sql, params);
			if(out.size()==0) return null;
			if(out.size()==1) return out.get(0);
		} catch (Exception e) {
			log.warn("-------- query failed: "+sql+" / "+params);
			throw new SqlException("SQL.REPO.LIST_FAILED",new Object[] { e.getMessage() },  e);
		}
		throw new NonUniqueResultException();
	}

	public void delete(SimpleQuery q) throws SqlException {
		this.delete(q.getQuery(),q.getParams());
	}
	
	public void update(SimpleQuery q) throws SqlException {
		this.update(q.getQuery(),q.getParams());
	}
	
	
	public void delete(String sql, Map<String,Object> params) throws SqlException {
		try {
			NamedParameterJdbcTemplate templ = new NamedParameterJdbcTemplate(getDataSource());
			log.debug("-------- update: "+sql+" / "+params);
			templ.update(sql, params);
		} catch (Exception e) {
			log.warn("-------- delete failed: "+sql+" / "+params);
			throw new SqlException("SQL.REPO.DELETE_FAILED",new Object[] { e.getMessage() },  e);
		}
	}
	
	public void update(String sql, Map<String,Object> params) throws SqlException {
		try {
			NamedParameterJdbcTemplate templ = new NamedParameterJdbcTemplate(getDataSource());
			log.debug("-------- update: "+sql+" / "+params);
			templ.update(sql, params);
		} catch (Exception e) {
			log.warn("-------- delete failed: "+sql+" / "+params);
			throw new SqlException("SQL.REPO.DELETE_FAILED",new Object[] { e.getMessage() },  e);
		}
	}
	
	
	public NamedParameterJdbcTemplate getTemplate() {
		if(template==null) {
			template = new NamedParameterJdbcTemplate(getDataSource());
		}
		return template;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}	
}
