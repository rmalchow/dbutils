package de.disk0.dbutil.impl.core;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.mysql.TestBase;

public class QueryLoggingTest extends TestBase {

	private static final String VALUE = "jane@example.org";
	private static final String SQL = "SELECT * FROM item WHERE name = :name";

	// strong references: JUL only keeps loggers weakly, and a collected logger loses its level
	private static final Logger IMPL = Logger.getLogger("de.disk0.dbutil.impl");
	private static final Logger PARAMS = Logger.getLogger("de.disk0.dbutil.sql.params");

	private final List<LogRecord> records = new ArrayList<>();
	private final Handler handler = new Handler() {
		@Override
		public void publish(LogRecord r) {
			records.add(r);
		}
		@Override
		public void flush() {
		}
		@Override
		public void close() {
		}
	};
	private Level implLevel;
	private Level paramsLevel;
	private ItemRepository repo;

	@Override
	public void setup(Connection c) throws Exception {
		c.createStatement().execute("CREATE TABLE item (id VARCHAR(36) PRIMARY KEY, name VARCHAR(128) NOT NULL, size INT)");
		c.createStatement().execute("CREATE ALIAS SLEEP FOR \"java.lang.Thread.sleep(long)\"");
		c.createStatement().execute("INSERT INTO item (id, name, size) VALUES ('a', '" + VALUE + "', 7)");
	}

	@BeforeEach
	public void attach() {
		implLevel = IMPL.getLevel();
		paramsLevel = PARAMS.getLevel();
		handler.setLevel(Level.ALL);
		IMPL.addHandler(handler);
		PARAMS.addHandler(handler);
		repo = new ItemRepository();
		repo.setDataSource(dataSource);
	}

	@AfterEach
	public void detach() {
		IMPL.removeHandler(handler);
		PARAMS.removeHandler(handler);
		IMPL.setLevel(implLevel);
		PARAMS.setLevel(paramsLevel);
	}

	private void assertNoValue() {
		for (LogRecord r : records) {
			Assertions.assertFalse(String.valueOf(r.getMessage()).contains(VALUE), r.getLoggerName() + ": " + r.getMessage());
		}
	}

	private List<LogRecord> warnings() {
		return records.stream().filter(r -> r.getLevel() == Level.WARNING).toList();
	}

	@Test
	public void verySlowFind() throws Exception {
		Assertions.assertEquals(1, repo.find("SELECT * FROM item WHERE SLEEP(1100) IS NULL AND name = :name", Map.of("name", VALUE)).size());
		List<LogRecord> w = warnings();
		Assertions.assertEquals(1, w.size());
		String m = w.get(0).getMessage();
		Assertions.assertTrue(m.contains("SLEEP(1100)") && m.contains("ms") && m.contains("1 rows") && m.contains("[name]"), m);
		assertNoValue();
	}

	@Test
	public void failingFind() {
		Assertions.assertThrows(SqlException.class, () -> repo.find("SELECT nope FROM item WHERE name = :name", Map.of("name", VALUE)));
		String m = warnings().get(0).getMessage();
		Assertions.assertTrue(m.contains("SELECT nope") && m.contains("[name]"), m);
		assertNoValue();
	}

	@Test
	public void failingSave() throws Exception {
		Item i = new Item();
		i.setName(VALUE);
		repo.save(i, "dup");
		Item j = new Item();
		j.setName(VALUE);
		records.clear();
		Assertions.assertThrows(SqlException.class, () -> repo.save(j, "dup"));
		String m = warnings().get(0).getMessage();
		Assertions.assertTrue(m.contains("INSERT INTO") && m.contains("name") && m.contains("id"), m);
		assertNoValue();
	}

	@Test
	public void debugFind() throws Exception {
		IMPL.setLevel(Level.FINE);
		Assertions.assertEquals(1, repo.find(SQL, Map.of("name", VALUE)).size());
		Assertions.assertFalse(records.isEmpty());
		assertNoValue();
	}

	@Test
	public void traceParams() throws Exception {
		PARAMS.setLevel(Level.FINEST);
		repo.find(SQL, Map.of("name", VALUE));
		Assertions.assertTrue(records.stream().anyMatch(r -> r.getLoggerName().equals(PARAMS.getName()) && r.getMessage().contains(VALUE)));
	}
}
