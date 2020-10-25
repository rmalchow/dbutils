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


public abstract class AbstractGuidRepository<T extends BaseGuidEntity> extends AbstractRepository<T,String> implements RowMapper<T> {

	private static Log log = LogFactory.getLog(AbstractGuidRepository.class);
	private static char[] hex      = new char[] { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'}; 
	private static SplittableRandom sr = new SplittableRandom();

	private static String generateUUID() {
		/**
		return UUID.randomUUID().toString();
		 **/
		char[] x = new char[36];
		int l=0;
		l=sr.nextInt();
		// 8-block
		x[0] = (char)hex[l>>0  &0xF];
		x[1] = (char)hex[l>>4  &0xF];
		x[2] = (char)hex[l>>8  &0xF];
		x[3] = (char)hex[l>>12 &0xF];
		x[4] = (char)hex[l>>16 &0xF];
		x[5] = (char)hex[l>>20 &0xF];
		x[6] = (char)hex[l>>24 &0xF];
		x[7] = (char)hex[l>>28 &0xF];
		x[8] = '-';
		l=sr.nextInt();
		// 4-block
		x[9] = (char)hex[l>>0  &0xF];
		x[10]= (char)hex[l>>4  &0xF];
		x[11]= (char)hex[l>>8  &0xF];
		x[12]= (char)hex[l>>12 &0xF];
		x[13] = '-';
		// 4-block
		x[14]= (char)hex[l>>16 &0xF];
		x[15]= (char)hex[l>>20 &0xF];
		x[16]= (char)hex[l>>24 &0xF];
		x[17]= (char)hex[l>>28 &0xF];
		x[18] = '-';
		l=sr.nextInt();
		// 4-block
		x[19]= (char)hex[l>>0  &0xF];
		x[20]= (char)hex[l>>4  &0xF];
		x[21]= (char)hex[l>>8  &0xF];
		x[22]= (char)hex[l>>12 &0xF];
		x[23] = '-';
		// 10-block
		x[24]= (char)hex[l>>16 &0xF];
		x[25]= (char)hex[l>>20 &0xF];
		x[26]= (char)hex[l>>24 &0xF];
		x[27]= (char)hex[l>>28 &0xF];
		l=sr.nextInt();
		x[28]= (char)hex[l>>0  &0xF];
		x[29]= (char)hex[l>>4  &0xF];
		x[30]= (char)hex[l>>8  &0xF];
		x[31]= (char)hex[l>>12 &0xF];
		x[32]= (char)hex[l>>16 &0xF];
		x[33]= (char)hex[l>>20 &0xF];
		x[34]= (char)hex[l>>24 &0xF];
		x[35]= (char)hex[l>>28 &0xF];
		return new String(x);
	}

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
	
	public static void main(String[] args) {
		int iter = 2000000;

		long time = System.nanoTime();
		for(int i = 0; i < iter; i++) {
			generateUUID();
		}
		System.err.println("using splitrandom:");
		System.err.println((System.nanoTime()-time)/(iter));

		time = System.nanoTime();
		for(int i = 0; i < iter; i++) {
			UUID.randomUUID().toString();
		}
		System.err.println("using UUID:");
		System.err.println((System.nanoTime()-time)/(iter));
	}

	
}
