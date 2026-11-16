package de.disk0.dbutil.impl.regression;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.disk0.dbutil.api.entities.BaseGuidEntityV2;
import de.disk0.dbutil.impl.AbstractGuidRepositoryV2;
import de.disk0.dbutil.impl.mysql.TestBase;
import de.disk0.dbutil.impl.SimpleQuery;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

public class LifecycleTest extends TestBase {

	@Table(name = "reg_thing")
	public static class Thing extends BaseGuidEntityV2 {
		@Column(name = "name") public String name;
	}

	public static class ThingRepository extends AbstractGuidRepositoryV2<Thing> { }

	private ThingRepository repo;

	@Override
	public void setup(Connection c) throws Exception {
		c.createStatement().execute("CREATE TABLE reg_thing (id VARCHAR(36) PRIMARY KEY, name VARCHAR(50))");
	}

	@BeforeEach
	public void repo() {
		repo = new ThingRepository();
		repo.setDataSource(dataSource);
	}

	private Thing thing(String n) {
		Thing t = new Thing();
		t.name = n;
		return t;
	}

	@Test
	public void saveUpdateGetDeleteById() throws Exception {
		Thing a = repo.save(thing("one"));
		assertNotNull(a.getId());
		assertFalse(a.getId().isEmpty());

		a.name = "two";
		Thing b = repo.save(a);
		assertEquals(a.getId(), b.getId());
		assertEquals("two", repo.get(a.getId()).name);

		assertEquals(1, repo.delete(a.getId()));
		assertNull(repo.get(a.getId()));
	}

	@Test
	public void deleteByObject() throws Exception {
		Thing a = repo.save(thing("x"));
		assertEquals(1, repo.delete(a));
		assertNull(repo.get(a.getId()));
	}

	@Test
	public void deleteMissingOrNullIsNoOp() throws Exception {
		assertEquals(0, repo.delete("does-not-exist"));
		assertEquals(0, repo.delete((Thing) null));
	}

	@Test
	public void getUnknownIdIsNull() throws Exception {
		assertNull(repo.get("nope"));
	}

	@Test
	public void findOneNonUnique() throws Exception {
		repo.save(thing("dup"));
		repo.save(thing("dup"));
		SimpleQuery q = new SimpleQuery("SELECT * FROM reg_thing WHERE name = :n");
		q.put("n", "dup");
		assertThrows(jakarta.persistence.NonUniqueResultException.class, () -> repo.findOne(q));

		q.put("n", "none");
		assertNull(repo.findOne(q));
		repo.save(thing("solo"));
		q.put("n", "solo");
		assertEquals("solo", repo.findOne(q).name);
	}
}
