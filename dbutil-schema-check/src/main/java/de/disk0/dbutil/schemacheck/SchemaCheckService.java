package de.disk0.dbutil.schemacheck;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class SchemaCheckService {
	
	@Autowired
	private DataSource dataSource;
	
	@Scheduled(fixedDelay=60000)
	public void checkSchema() {
		List<Table> tables = new ArrayList<>();
		try {
			NamedParameterJdbcTemplate templ = new NamedParameterJdbcTemplate(dataSource);
			String dbName = templ.queryForObject("SELECT DATABASE()", new HashMap<String,Object>(), String.class);
			Map<String,String> p = new HashMap<>();
			p.put("dbName", dbName);
			
			
			for(String tableName : templ.queryForList("select table_name from information_schema.TABLES where TABLE_SCHEMA = :dbName", p, String.class)) {
				p.put("tableName", tableName);
				Table t = new Table();
				t.setName(tableName);
				t.setColumns(templ.query("show columns from :tableName", p, new ColumnRowMapper()));
				t.setKeys(templ.query("show columns from :tableName", p, new KeyRowMapper()));
				tables.add(t);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.err.println(new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(tables));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
	}

	public class KeyRowMapper implements RowMapper<Key> {

		@Override
		public Key mapRow(ResultSet arg0, int arg1) throws SQLException {
			return null;
		}
		
	}
	
	public class ColumnRowMapper implements RowMapper<Column> {
		
		@Override
		public Column mapRow(ResultSet arg0, int arg1) throws SQLException {
			return null;
		}
		
	}
	
	

}
