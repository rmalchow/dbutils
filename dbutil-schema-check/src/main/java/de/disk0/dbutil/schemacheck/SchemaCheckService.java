package de.disk0.dbutil.schemacheck;

import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class SchemaCheckService {
	
	private static Log log = LogFactory.getLog(SchemaCheckService.class);
	
	@Autowired
	private DataSource dataSource;
	
	@Scheduled(fixedDelay=600000)
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
			String s = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(tables);
			MessageDigest md = MessageDigest.getInstance("MD5");
		    md.update(s.getBytes());
		    byte[] digest = md.digest();
		    String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
			log.info(" ---> DB state: "+myHash);
		} catch (Exception e) {
			log.error("error checking db state: ",e);
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
