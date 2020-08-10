package de.disk0.dbutil.impl.util;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DbTemplate {

	private NamedParameterJdbcTemplate templ;
	
	public NamedParameterJdbcTemplate getTemplate(DataSource ds) {
		if(templ==null) {
			templ = new NamedParameterJdbcTemplate(ds);
		}
		return templ;
	}
	
}
