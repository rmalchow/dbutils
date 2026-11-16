package de.disk0.dbutil.impl.core;

import java.sql.Connection;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.mysql.TestBase;
import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;

public class ObservationTest extends TestBase {

	@Override
	public void setup(Connection c) throws Exception {
		c.createStatement().execute("CREATE TABLE item (id VARCHAR(36) PRIMARY KEY, name VARCHAR(128), size INT)");
		c.createStatement().execute("INSERT INTO item (id, name, size) VALUES ('a', 'x', 7)");
	}

	private ItemRepository repo() {
		ItemRepository repo = new ItemRepository();
		repo.setDataSource(dataSource);
		return repo;
	}

	@Test
	public void findIsObserved() throws Exception {
		TestObservationRegistry registry = TestObservationRegistry.create();
		ItemRepository repo = repo();
		repo.setObservationRegistry(registry);
		repo.find("SELECT * FROM item", Map.of());
		TestObservationRegistryAssert.assertThat(registry)
			.hasNumberOfObservationsEqualTo(1)
			.hasSingleObservationThat()
			.hasNameEqualTo("dbutil.sql.find")
			.hasLowCardinalityKeyValue("object", "Item");
	}

	@Test
	public void findWithoutRegistry() throws Exception {
		Assertions.assertEquals(1, repo().find("SELECT * FROM item", Map.of()).size());
	}

	@Test
	public void failingFindThrowsSameException() {
		ItemRepository plain = repo();
		ItemRepository observed = repo();
		observed.setObservationRegistry(TestObservationRegistry.create());
		SqlException a = Assertions.assertThrows(SqlException.class, () -> plain.find("SELECT nope FROM item", Map.of()));
		SqlException b = Assertions.assertThrows(SqlException.class, () -> observed.find("SELECT nope FROM item", Map.of()));
		Assertions.assertEquals(a.getMessage(), b.getMessage());
		Assertions.assertEquals(a.getCause().getClass(), b.getCause().getClass());
	}
}
