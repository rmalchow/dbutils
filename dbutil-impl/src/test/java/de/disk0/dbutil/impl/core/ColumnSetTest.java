package de.disk0.dbutil.impl.core;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.disk0.dbutil.impl.mysql.TestBase;

public class ColumnSetTest extends TestBase {

	private ItemRepository repo;

	@Override
	public void setup(Connection c) throws Exception {
		c.createStatement().execute("CREATE TABLE item (id VARCHAR(36) PRIMARY KEY, name VARCHAR(128), size INT)");
		c.createStatement().execute("INSERT INTO item (id, name, size) VALUES ('a', 'x', 7)");
	}

	@BeforeEach
	public void repo() {
		repo = new ItemRepository();
		repo.setDataSource(dataSource);
	}

	private Item one(String sql) throws Exception {
		List<Item> l = repo.find(sql, Map.of());
		Assertions.assertEquals(1, l.size());
		return l.get(0);
	}

	@Test
	public void fullSelectAfterProjection() throws Exception {
		Assertions.assertNull(one("SELECT id, name FROM item").getSize());
		Assertions.assertEquals(7, one("SELECT * FROM item").getSize());
	}

	@Test
	public void projectionAfterFullSelect() throws Exception {
		Assertions.assertEquals(7, one("SELECT * FROM item").getSize());
		Item i = one("SELECT id, name FROM item");
		Assertions.assertEquals("x", i.getName());
		Assertions.assertNull(i.getSize());
	}
}
