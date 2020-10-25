package de.disk0.dbutil.impl;

import java.util.SplittableRandom;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import de.disk0.dbutil.api.entities.BaseGuidEntity;
import de.disk0.dbutil.api.exceptions.SqlException;


public abstract class AbstractTokenRepository<T extends BaseGuidEntity> extends AbstractRepository<T,String> implements RowMapper<T> {

	private static Log log = LogFactory.getLog(AbstractTokenRepository.class);
	private static SplittableRandom sr = new SplittableRandom();
	
	private static char[] alphabet = new char[] { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','_','-' }; 

	public void delete(String id) throws SqlException {
		T t = get(id);
		super.delete(t);
	}
	
	private static String generateToken() {
		char[] x = new char[16];
		int l=0;
		l=sr.nextInt();
		x[0] = (char)alphabet[l>>0 &0x3F];
		x[1] = (char)alphabet[l>>6 &0x3F];
		x[2] = (char)alphabet[l>>12 &0x3F];
		x[3] = (char)alphabet[l>>18 &0x3F];
		l=sr.nextInt();
		x[4] = (char)alphabet[l>>0 &0x3F];
		x[5] = (char)alphabet[l>>6 &0x3F];
		x[6] = (char)alphabet[l>>12 &0x3F];
		x[7] = (char)alphabet[l>>18 &0x3F];
		l=sr.nextInt();
		x[8] = (char)alphabet[l>>0 &0x3F];
		x[9] = (char)alphabet[l>>6 &0x3F];
		x[10] = (char)alphabet[l>>12 &0x3F];
		x[11] = (char)alphabet[l>>18 &0x3F];
		l=sr.nextInt();
		x[12] = (char)alphabet[l>>0 &0x3F];
		x[13] = (char)alphabet[l>>6 &0x3F];
		x[14] = (char)alphabet[l>>12 &0x3F];
		x[15] = (char)alphabet[l>>18 &0x3F];
		return new String(x);
	}
	
	public T save(T t) throws SqlException {
		return save(t,generateToken());
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
	
	public static void main(String[] args) {
		long time = System.nanoTime();
		int iter = 2000000;
		for(int i = 0; i < iter; i++) {
			//System.err.println(generateUUID());
			generateToken();
		}
		System.err.println((System.nanoTime()-time)/(iter));
	}
	
}
