package de.disk0.dbutil.impl;

import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import de.disk0.dbutil.api.entities.BaseGuidEntity;
import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.util.ParsedEntity;
import de.disk0.dbutil.impl.util.ParsedEntity.ParsedColumn;


public abstract class AbstractGuidRepository<T extends BaseGuidEntity> extends AbstractRepository<T,String> implements RowMapper<T> {

	private static Log log = LogFactory.getLog(AbstractGuidRepository.class);
	
	public T save(T t) throws SqlException {
		try {
			beforeSave(t);
			NamedParameterJdbcTemplate templ = new NamedParameterJdbcTemplate(dataSource);
			if(t.getId()==null) {
				t.setId(UUID.randomUUID().toString());
				String sql = getInsertOneStatement();
				Map<String,Object> params = unmap(t); 
				log.debug("saving entity: insert!");
				log.debug("INSERT: "+sql);
				log.debug("INSERT: "+params);
				templ.update(sql, params);
			} else {
				String sql = getUpdateOneStatement();
				Map<String,Object> params = unmap(t); 
				log.debug("saving entity: update!");
				log.debug("UPDATE: "+sql);
				log.debug("UPDATE: "+params);
				templ.update(sql, params);
			}
			afterSave(t);
			return get(t.getId());
		} catch (Exception e) {
			throw new SqlException("SQL.REPO.SAVE_FAILED",new Object[] { e.getMessage() },  e);
		}
	}	
	
}
