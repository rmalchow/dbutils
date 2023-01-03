package de.disk0.dbutil.impl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "spring.reader.enabled", havingValue = "true")
public class ReaderConfig {

	@Bean
	@ConfigurationProperties("spring.reader")
	public static DataSourceProperties readerDSprops() {
		return new DataSourceProperties();
	}

	@Bean
	public static DataSource readerDS() {
		return readerDSprops().initializeDataSourceBuilder().build();
	}

	@Bean
	public static NamedParameterJdbcTemplate readerTemplate() {
		return new NamedParameterJdbcTemplate(readerDS());
	}

}
