package de.disk0.dbutil.impl;

import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

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
		NamedParameterJdbcTemplate templ = null;
		try {
			beforeSave(t);
			templ = getTemplate();
			if(StringUtils.isEmpty(t.getId())) {
				t.setId(uuid);
				String sql = getInsertOneStatement();
				
				MapSqlParameterSource params = unmap(t);
				if(log.isDebugEnabled()) {
					log.debug("saving entity: insert!");
					log.debug("INSERT: "+sql);
					log.debug("INSERT: "+params.getValues());
				}
				int done = templ.update(sql, params);
				log.debug("saving entity: "+done+" rows inserted");
			} else {
				String sql = getUpdateOneStatement();
				MapSqlParameterSource params = unmap(t);
				if(log.isDebugEnabled()) {
					log.debug("saving entity: update!");
					log.debug("UPDATE: "+sql);
					log.debug("UPDATE: "+params.getValues());
				}
				int done = templ.update(sql, params);
				log.debug("saving entity: "+done+" rows updated");
			}
			log.debug("saving entity: calling after save ... ");
			afterSave(t);
			log.debug("saving entity: calling after save ... done!");
			log.debug("saving entity: retrieving object as saved: >>>"+t.getId()+"<<<");
			T out = get(t.getId());
			log.debug("saving entity: retrieving object as saved: "+(out==null?"NULL":"FOUND"));
			return out;
		} catch (Exception e) {
			throw new SqlException("SQL.REPO.SAVE_FAILED",new Object[] { e.getMessage() },  e);
		} finally {
		}
	}	
	
}
