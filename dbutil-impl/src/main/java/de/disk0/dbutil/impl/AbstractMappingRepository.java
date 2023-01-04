package de.disk0.dbutil.impl;

import java.lang.reflect.ParameterizedType;
import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.disk0.dbutil.impl.micrometer.MicrometerAdapter;
import de.disk0.dbutil.impl.util.PersistenceApiUtils;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.util.ParsedEntity;
import de.disk0.dbutil.impl.util.ParsedEntity.ParsedColumn;

public abstract class AbstractMappingRepository<T> implements RowMapper<T> {

	private static Log log = LogFactory.getLog(AbstractMappingRepository.class);

	protected ParsedEntity<T> pe;
	
	protected Class<T> clazz;
	
	private List<String> applicableColumns;
	
	@Autowired
	private NamedParameterJdbcTemplate template;

	@Autowired(required = false)
	private MicrometerAdapter micrometerAdapter;
	
	@SuppressWarnings("unchecked")
	protected Class<T> getClazz() {
		if(clazz==null) {
			clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
		return clazz;
	}
		
	public ParsedEntity<T> getParsedEntity() {
		if(pe==null) {
			try {
				pe = new ParsedEntity<>(getClazz());
				if(pe.getTableName()==null) {
					throw new InvalidParameterException("entity "+getClazz()+" has no table");
				}
				if(pe.getColumns().size()==0) {
					throw new InvalidParameterException("entity "+getClazz()+" has no columns");
				}
			} catch (Exception e) {
				log.error("unable to parse entity: "+getClazz()+"! ",e);
				throw e;
			}
		}
		return pe;
	}
	
	@SuppressWarnings("rawtypes")
	protected MapSqlParameterSource unmapInsert(T t) throws IllegalArgumentException, IllegalAccessException {
		MapSqlParameterSource out = new MapSqlParameterSource();
		for(ParsedColumn pc : getParsedEntity().getColumns()) {
			if(!pc.isInsertable()) continue;
			Object o = pc.get(t);
			out.addValue(pc.getColumnName(),o);
		}
		return out;
	}
	
	@SuppressWarnings("rawtypes")
	protected MapSqlParameterSource unmapUpdate(T t) throws IllegalArgumentException, IllegalAccessException {
		MapSqlParameterSource out = new MapSqlParameterSource();
		for(ParsedColumn pc : getParsedEntity().getColumns()) {
			if(!pc.isUpdatable()) continue;
			Object o = pc.get(t);
			out.addValue(pc.getColumnName(),o);
		}
		return out;
	}
	
	@Override
	public T mapRow(ResultSet rs, int c) throws SQLException {
		try {
			if(applicableColumns==null) {
				synchronized (this) {
					applicableColumns = new ArrayList<>();
					for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
						applicableColumns.add(rs.getMetaData().getColumnLabel(i+1).toUpperCase());
					}
				}
			}
			
			T out = getClazz().newInstance();
			for(ParsedColumn pc : getParsedEntity().getColumns()) {
				log.debug("parsing: "+pc.getColumnName());
				if(!applicableColumns.contains(pc.getColumnName().toUpperCase())) {
					log.warn("column: "+pc.getColumnName()+" in object ("+getClazz().getSimpleName()+"), but not in result set");
					ResultSetMetaData md = rs.getMetaData();
					for(int i=0; i < md.getColumnCount();i++) {
						log.warn(" ---- "+md.getColumnName(i+1)+" / "+md.getColumnLabel(i+1));
					}
					continue;
				} else {
					log.debug("column: "+pc.getColumnName()+" in object AND in result set");
				}
				try {
					pc.set(out, rs);
				} catch (Exception e) {
					throw new RuntimeException("failed to map column: "+pc.getColumnName()+" of type "+getClazz().getSimpleName(),e);
				}
			}
			return out;
		} catch (Exception e) {
			log.error("failed to map entity: ",e);
			throw new SQLException("failed to map entity ("+e.getMessage()+")",e);
		}
	}

	public List<T> find(SimpleQuery q) throws SqlException {
		return find(q.getQuery(),q.getParams());
	}
	
	public List<T> find(Select s) throws SqlException {
		return find(s.getSql(),s.getParams());
	}
	
	public List<T> find(String sql, Map<String,Object> params) throws SqlException {
		Instant timerStart = Instant.now();
		try {
			NamedParameterJdbcTemplate t = getTemplate();
			long start = System.currentTimeMillis();
			log.debug("-------- query: "+sql+" / "+params);
			List<T> out = t.query(sql, params, this);
			log.debug("-------- found: "+out.size()+" / "+(System.currentTimeMillis()-start)+"ms");
			return out;
		} catch (Exception e) {
			log.warn("-------- query failed: "+sql+" / "+params,e);
			throw new SqlException("SQL.REPO.LIST_FAILED",new Object[] { e.getMessage() },  e);
		} finally {
			if (micrometerAdapter != null) {
				Instant timerEnd = Instant.now();
				micrometerAdapter.record(
						Duration.between(timerStart, timerEnd),
						"dbutil.sql.find",
						MicrometerAdapter.Tag.of("object", getClazz().getSimpleName())
				);
			}
		}
	}

	public T findOne(SimpleQuery q) throws SqlException {
		return findOne(q.getQuery(),q.getParams());
	}
	
	public T findOne(Select s) throws SqlException {
		return findOne(s.getSql(),s.getParams());
	}
	
	protected T findOne(String sql, Map<String,Object> params) throws SqlException {
		try {
			List<T> out = find(sql, params);
			if(out.size()==0) return null;
			if(out.size()==1) return out.get(0);
		} catch (Exception e) {
			log.warn("-------- query failed: "+sql+" / "+params,e);
			throw new SqlException("SQL.REPO.LIST_FAILED",new Object[] { e.getMessage() },  e);
		}
		throw PersistenceApiUtils.nonUniqueResultException();
	}

	public int delete(SimpleQuery q) throws SqlException {
		return this.delete(q.getQuery(),q.getParams());
	}
	
	public int update(SimpleQuery q) throws SqlException {
		return this.update(q.getQuery(),q.getParams());
	}
	
	
	public int delete(String sql, Map<String,Object> params) throws SqlException {
		try {
			NamedParameterJdbcTemplate templ = getTemplate();
			log.debug("-------- update: "+sql+" / "+params);
			return templ.update(sql, params);
		} catch (Exception e) {
			log.warn("-------- delete failed: "+sql+" / "+params,e);
			throw new SqlException("SQL.REPO.DELETE_FAILED",new Object[] { e.getMessage() },  e);
		}
	}
	
	public int update(String sql, Map<String,Object> params) throws SqlException {
		try {
			NamedParameterJdbcTemplate templ = getTemplate();
			log.debug("-------- update: "+sql+" / "+params);
			return templ.update(sql, params);
		} catch (Exception e) {
			log.warn("-------- delete failed: "+sql+" / "+params,e);
			throw new SqlException("SQL.REPO.DELETE_FAILED",new Object[] { e.getMessage() },  e);
		}
	}
	
	public NamedParameterJdbcTemplate getTemplate() {
		/**
		DbTemplate dt = DbTemplateHolder.get();
		if(dt != null) {
			return dt.getTemplate(getDataSource());
		}
		if(template==null) {
			template = new NamedParameterJdbcTemplate(getDataSource());
		}
		 **/
		return template;
	}

	public void setDataSource(DataSource dataSource) {
		this.template = new NamedParameterJdbcTemplate(dataSource);
	}
	
}
