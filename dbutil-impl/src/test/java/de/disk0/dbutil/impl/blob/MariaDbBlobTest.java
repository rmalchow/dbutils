package de.disk0.dbutil.impl.blob;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.Driver;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

public class MariaDbBlobTest {

	@Test
	public void nullEmptyAndRandomBytesRoundTrip() throws Exception {
		String url = System.getProperty("dbutil.it.mariadb.url");
		Assumptions.assumeTrue(url != null, "dbutil.it.mariadb.url not set");

		SimpleDriverDataSource ds = new SimpleDriverDataSource(new Driver(), url);
		try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
			s.execute("DROP TABLE IF EXISTS blob_entity");
			s.execute("CREATE TABLE blob_entity (id VARCHAR(36) PRIMARY KEY, data LONGBLOB)");
		}
		BlobEntityRepository repo = new BlobEntityRepository();
		repo.setDataSource(ds);

		byte[] random = new byte[1024];
		new Random().nextBytes(random);
		byte[][] inputs = { null, new byte[0], random };
		for (byte[] in : inputs) {
			BlobEntity e = new BlobEntity();
			e.setData(in);
			repo.save(e);
			Assertions.assertArrayEquals(in, repo.get(e.getId()).getData());
		}
	}

}
