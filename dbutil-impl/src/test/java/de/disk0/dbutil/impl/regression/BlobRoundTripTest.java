package de.disk0.dbutil.impl.regression;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.disk0.dbutil.api.entities.BaseGuidEntityV2;
import de.disk0.dbutil.impl.AbstractGuidRepositoryV2;
import de.disk0.dbutil.impl.mysql.TestBase;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

/** byte[] on an H2 BLOB column: exercises the java.sql.Blob branch of ParsedColumn.set if H2 returns a Blob. */
public class BlobRoundTripTest extends TestBase {

	@Table(name = "reg_blob")
	public static class RegBlob extends BaseGuidEntityV2 {
		@Column(name = "data") public byte[] data;
	}

	public static class RegBlobRepository extends AbstractGuidRepositoryV2<RegBlob> { }

	private RegBlobRepository repo;

	@Override
	public void setup(Connection c) throws Exception {
		c.createStatement().execute("CREATE TABLE reg_blob (id VARCHAR(36) PRIMARY KEY, data BLOB)");
	}

	@BeforeEach
	public void repo() {
		repo = new RegBlobRepository();
		repo.setDataSource(dataSource);
	}

	private byte[] roundTrip(byte[] in) throws Exception {
		RegBlob e = new RegBlob();
		e.data = in;
		return repo.get(repo.save(e).getId()).data;
	}

	@Test
	public void h2ReturnsBlobObjectForBlobColumn() throws Exception {
		// guards the premise of the other tests: the Blob branch really is taken
		try (Connection c = dataSource.getConnection()) {
			c.createStatement().execute("INSERT INTO reg_blob VALUES ('x', X'0102')");
			var rs = c.createStatement().executeQuery("SELECT data FROM reg_blob");
			assertTrue(rs.next());
			assertInstanceOf(java.sql.Blob.class, rs.getObject("data"));
		}
	}

	@Test
	public void nullBlob() throws Exception {
		assertNull(roundTrip(null));
	}

	@Test
	public void emptyBlob() throws Exception {
		assertArrayEquals(new byte[0], roundTrip(new byte[0]));
	}

	@Test
	public void randomKiB() throws Exception {
		byte[] b = new byte[1024];
		new Random(42).nextBytes(b);
		assertArrayEquals(b, roundTrip(b));
	}
}
