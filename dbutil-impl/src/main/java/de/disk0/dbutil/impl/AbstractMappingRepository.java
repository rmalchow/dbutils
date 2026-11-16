package de.disk0.dbutil.impl;

import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.disk0.dbutil.impl.util.PersistenceApiUtils;
import de.disk0.dbutil.impl.util.SqlLog;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
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
	
	@Autowired
	private NamedParameterJdbcTemplate template;

	private ObservationRegistry observationRegistry = ObservationRegistry.NOOP;
	
	@Autowired(required = false)
	public void setObservationRegistry(ObservationRegistry observationRegistry) {
		this.observationRegistry = observationRegistry;
	}
	
	@SuppressWarnings("unchecked")
	protected Class<T> getClazz() {
		if(clazz==null) {
			// not GenericTypeResolver.resolveTypeArgument: for a raw subclass it returns T's bound instead of null
			ResolvableType rt = ResolvableType.forClass(getClass()).as(AbstractMappingRepository.class);
			if(rt.hasUnresolvableGenerics() || rt.resolveGeneric(0)==null) {
				throw new IllegalStateException("cannot resolve entity type of " + getClass());
			}
			clazz = (Class<T>) rt.resolveGeneric(0);
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
			ResultSetMetaData rsmd = rs.getMetaData();
			Set<String> applicableColumns = new HashSet<>();
			for(int i=0;i<rsmd.getColumnCount();i++) {
				applicableColumns.add(rsmd.getColumnLabel(i+1).toUpperCase());
			}
			
			T out = getClazz().getDeclaredConstructor().newInstance();
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
		return Observation.createNotStarted("dbutil.sql.find", observationRegistry)
			.lowCardinalityKeyValue("object", getClazz().getSimpleName())
			.observeChecked(() -> doFind(sql, params));
	}
	
	private List<T> doFind(String sql, Map<String,Object> params) throws SqlException {
		String entity = getClazz().getSimpleName();
		try {
			NamedParameterJdbcTemplate t = getTemplate();
			long start = System.currentTimeMillis();
			log.debug("-------- query: "+sql+" params="+SqlLog.names(params));
			SqlLog.values(entity, params);
			List<T> out = t.query(sql, params, this);
			long ms = System.currentTimeMillis()-start;
			if(ms > 1000) {
				log.warn("VERY SLOW: "+ms+"ms, "+out.size()+" rows, "+entity+": "+sql+" params="+SqlLog.names(params));
			} else if(ms > 500) {
				log.info("     SLOW: "+ms+"ms, "+out.size()+" rows, "+entity+": "+sql+" params="+SqlLog.names(params));
			} else {
				log.debug("  NORMAL: "+ms+"ms, "+out.size()+" rows");
			}
			return out;
		} catch (Exception e) {
			log.warn("-------- query failed: "+sql+" params="+SqlLog.names(params),e);
			throw new SqlException("SQL.REPO.LIST_FAILED",new Object[] { e.getMessage() },  e);
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
			log.warn("-------- query failed: "+sql+" params="+SqlLog.names(params),e);
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
			log.debug("-------- delete: "+sql+" params="+SqlLog.names(params));
			SqlLog.values(getClazz().getSimpleName(), params);
			return templ.update(sql, params);
		} catch (Exception e) {
			log.warn("-------- delete failed: "+sql+" params="+SqlLog.names(params),e);
			throw new SqlException("SQL.REPO.DELETE_FAILED",new Object[] { e.getMessage() },  e);
		}
	}
	
	public int update(String sql, Map<String,Object> params) throws SqlException {
		try {
			NamedParameterJdbcTemplate templ = getTemplate();
			log.debug("-------- update: "+sql+" params="+SqlLog.names(params));
			SqlLog.values(getClazz().getSimpleName(), params);
			return templ.update(sql, params);
		} catch (Exception e) {
			log.warn("-------- update failed: "+sql+" params="+SqlLog.names(params),e);
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
