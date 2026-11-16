package de.disk0.dbutil.impl.regression;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.disk0.dbutil.api.entities.BaseGuidEntityV2;
import de.disk0.dbutil.impl.AbstractGuidRepositoryV2;
import de.disk0.dbutil.impl.mysql.TestBase;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

/** D3: one repository instance must map projections and full selects correctly under concurrency. */
public class ParallelProjectionTest extends TestBase {

	@Table(name = "reg_proj")
	public static class Proj extends BaseGuidEntityV2 {
		@Column(name = "name") public String name;
		@Column(name = "size") public Integer size;
		@Column(name = "note") public String note;
	}

	public static class ProjRepository extends AbstractGuidRepositoryV2<Proj> { }

	private ProjRepository repo;

	@Override
	public void setup(Connection c) throws Exception {
		c.createStatement().execute("CREATE TABLE reg_proj (id VARCHAR(36) PRIMARY KEY, name VARCHAR(50), size INT, note VARCHAR(50))");
		for (int i = 0; i < 5; i++) {
			c.createStatement().execute("INSERT INTO reg_proj VALUES ('id" + i + "', 'n" + i + "', " + i + ", 'note" + i + "')");
		}
	}

	@BeforeEach
	public void repo() {
		repo = new ProjRepository();
		repo.setDataSource(dataSource);
	}

	@Test
	public void concurrentProjectionAndFullSelect() throws Exception {
		final int iterations = 200;
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService ex = Executors.newFixedThreadPool(2);
		try {
			Callable<Void> projection = () -> {
				start.await();
				for (int i = 0; i < iterations; i++) {
					List<Proj> l = repo.find("SELECT id, name FROM reg_proj", Map.of());
					assertEquals(5, l.size());
					for (Proj p : l) {
						assertNotNull(p.getId());
						assertNotNull(p.name);
					}
				}
				return null;
			};
			Callable<Void> full = () -> {
				start.await();
				for (int i = 0; i < iterations; i++) {
					List<Proj> l = repo.find("SELECT * FROM reg_proj", Map.of());
					assertEquals(5, l.size());
					for (Proj p : l) {
						assertNotNull(p.getId());
						assertNotNull(p.name);
						assertNotNull(p.size);
						assertEquals("note" + p.size, p.note);
					}
				}
				return null;
			};
			Future<Void> a = ex.submit(projection);
			Future<Void> b = ex.submit(full);
			start.countDown();
			a.get(30, TimeUnit.SECONDS);
			b.get(30, TimeUnit.SECONDS);
		} finally {
			ex.shutdownNow();
		}
	}
}
