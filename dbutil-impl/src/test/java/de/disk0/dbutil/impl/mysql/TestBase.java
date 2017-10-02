package de.disk0.dbutil.impl.mysql;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;

public abstract class TestBase {

	protected DataSource dataSource;
	private File db;

	
	public abstract void setup(Connection c) throws Exception;
	
	@Before
	public void prepare() throws Exception {
		db = File.createTempFile("junit_", "");
		
		BasicDataSource bds =  new BasicDataSource();
		try {
			Class.forName("org.h2.Driver");
			bds.setUrl("jdbc:h2:"+db.getAbsolutePath());
			bds.setMaxIdle(5);
			bds.setMaxTotal(50);
			bds.getConnection().close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("error connecting to database");
		}
		
		dataSource = bds;
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

	@After
	public void teardown() throws IOException {
		Path rootPath = Paths.get(db.getAbsolutePath());
		Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile)
				.peek(System.out::println).forEach(File::delete);
	}

}
