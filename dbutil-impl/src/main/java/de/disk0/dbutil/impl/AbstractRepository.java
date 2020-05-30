package de.disk0.dbutil.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import de.disk0.dbutil.api.entities.BaseEntity;
import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.util.ParsedEntity;
import de.disk0.dbutil.impl.util.ParsedEntity.ParsedColumn;


public abstract class AbstractRepository<T extends BaseEntity<S>,S> extends AbstractMappingRepository<T> {

	private static Log log = LogFactory.getLog(AbstractRepository.class);
	
	private String findOneStatement;
	private String findAllStatement;
	private String insertOneStatement;
	private String updateOneStatement;
	private String deleteOneStatement;
	
	public AbstractRepository() {
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
		Map<String,Object> params = new HashMap<>();
		String sql = getFindOneStatement();
		try {
			NamedParameterJdbcTemplate t = new NamedParameterJdbcTemplate(getDataSource());
			params.put("id", id);
			List<T> ts = t.query(getFindOneStatement(), params, this);
			if(ts.size()>0) {
				return ts.get(0);
			}
			return null;
		} catch (Exception e) {
			log.warn("-------- query failed: "+sql+" / "+params);
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
			insertOneStatement = "INSERT INTO `"+pe.getTableName()+"` ("+(String.join(", ", columns))+") VALUES ("+(String.join(", ", values))+")";
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
			updateOneStatement = "UPDATE `"+pe.getTableName()+"` SET "+(String.join(", ", columns))+" WHERE `"+pe.getIdColumn()+"`=:id";
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
	
	public void delete(T t) throws SqlException {
		String sql = "[none]";
		try {
			if(t==null || t.getId()==null) {
				return;
			}
			beforeDelete(t);
			NamedParameterJdbcTemplate templ = new NamedParameterJdbcTemplate(getDataSource());
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

	
}
