package de.disk0.dbutil.impl.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class ReaderConfig {


	@Bean(name="readerDSprops")
	@ConfigurationProperties("spring.reader")
	@ConditionalOnProperty(name = "spring.reader.enabled", havingValue = "true", matchIfMissing = false)
	public static DataSourceProperties readerDSProperties() {
		return new DataSourceProperties();
	}
	
	@Bean(name="readerDS")
	@ConditionalOnProperty(name = "spring.reader.enabled", havingValue = "true", matchIfMissing = false)
	public static DataSource readerDS( @Qualifier("readerDSprops") DataSourceProperties readerDSprops) {
		return readerDSprops.initializeDataSourceBuilder().build();
	}

	@Bean(name = "readerTemplate")
	@ConditionalOnProperty(name = "spring.reader.enabled", havingValue = "true", matchIfMissing = false)
	public static NamedParameterJdbcTemplate readerTemplate( @Qualifier("readerDS") DataSource readerDS) {
		return new NamedParameterJdbcTemplate(readerDS);
	}	
	
}
