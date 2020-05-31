package de.disk0.dbutil.impl;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import de.disk0.dbutil.api.entities.BaseGuidEntity;
import de.disk0.dbutil.api.exceptions.SqlException;


public abstract class AbstractGuidRepository<T extends BaseGuidEntity> extends AbstractRepository<T,String> implements RowMapper<T> {

	private static Log log = LogFactory.getLog(AbstractGuidRepository.class);

	public void delete(String id) throws SqlException {
		T t = get(id);
		super.delete(t);
	}

	public T save(T t) throws SqlException {
		return save(t,UUID.randomUUID().toString());
	}
	
	public T save(T t, String uuid) throws SqlException {
		try {
			beforeSave(t);
			NamedParameterJdbcTemplate templ = new NamedParameterJdbcTemplate(getDataSource());
			if(t.getId()==null) {
				t.setId(uuid);
				String sql = getInsertOneStatement();
				
				MapSqlParameterSource params = unmap(t);
				
				log.debug("saving entity: insert!");
				log.debug("INSERT: "+sql);
				log.debug("INSERT: "+params.getValues());
				templ.update(sql, params);
			} else {
				String sql = getUpdateOneStatement();
				MapSqlParameterSource params = unmap(t);
				log.debug("saving entity: update!");
				log.debug("UPDATE: "+sql);
				log.debug("UPDATE: "+params.getValues());
				templ.update(sql, params);
			}
			afterSave(t);
			T out = get(t.getId());
			return out;
		} catch (Exception e) {
			throw new SqlException("SQL.REPO.SAVE_FAILED",new Object[] { e.getMessage() },  e);
		}
	}	
	
}
