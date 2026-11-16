package de.disk0.dbutil.impl.mysql;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Comparator;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

public abstract class TestBase {

	protected DataSource dataSource;
	private File db;

	
	public abstract void setup(Connection c) throws Exception;
	
	@BeforeEach
	public void prepare() throws Exception {
		db = File.createTempFile("junit_", "");
		
		dataSource = new SimpleDriverDataSource(new org.h2.Driver(), "jdbc:h2:" + db.getAbsolutePath());
		Connection c = null;
		try {
			c = dataSource.getConnection();
			setup(c);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				c.close();
			} catch (Exception e2) {
				
			}
		}
	}

	@AfterEach
	public void teardown() throws IOException {
		Path rootPath = Paths.get(db.getAbsolutePath());
		Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
				.peek(System.out::println).forEach(File::delete);
	}

}
