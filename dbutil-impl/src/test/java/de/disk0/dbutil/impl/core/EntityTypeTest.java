package de.disk0.dbutil.impl.core;

import java.sql.Connection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.disk0.dbutil.impl.mysql.TestBase;

public class EntityTypeTest extends TestBase {

	@Override
	public void setup(Connection c) throws Exception {
		c.createStatement().execute("CREATE TABLE item (id VARCHAR(36) PRIMARY KEY, name VARCHAR(128), size INT)");
	}

	@Test
	public void deepHierarchy() throws Exception {
		ItemRepoDeep repo = new ItemRepoDeep();
		repo.setDataSource(dataSource);
		Item i = new Item();
		i.setName("deep");
		Item saved = repo.save(i);
		Assertions.assertEquals("deep", repo.get(saved.getId()).getName());
	}

	@Test
	public void rawRepository() {
		IllegalStateException e = Assertions.assertThrows(IllegalStateException.class, () -> new RawRepository().getParsedEntity());
		Assertions.assertTrue(e.getMessage().contains("RawRepository"), e.getMessage());
	}
}
