package de.disk0.dbutil.impl.uuids;

import java.sql.Connection;

import org.junit.Assert;
import org.junit.Test;

import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.mysql.TestBase;
import de.disk0.dbutil.impl.pets.Pet;

public class UuidTests extends TestBase {
	
	@Test(expected = SqlException.class)
	public void checkBrokenUUIDGeneratorA() throws SqlException {
		BrokenPetRepositoryA fr = new BrokenPetRepositoryA();
		fr.setDataSource(dataSource);
		Pet p = new Pet();
		p = fr.save(p);
		System.err.println(p.getId());
	}

	@Test(expected = SqlException.class)
	public void checkBrokenUUIDGeneratorB() throws SqlException {
		BrokenPetRepositoryB fr = new BrokenPetRepositoryB();
		fr.setDataSource(dataSource);
		Pet p = new Pet();
		p = fr.save(p);
		System.err.println(p.getId());
	}

	@Test
	public void checkCorrectUUIDGenerator1() throws SqlException {
		PetRepository1 fr = new PetRepository1();
		fr.setDataSource(dataSource);
		Pet p = new Pet();
		p = fr.save(p);
		Assert.assertEquals(36,p.getId().length());
	}

	@Test
	public void checkCorrectUUIDGenerator2() throws SqlException {
		PetRepository2 fr = new PetRepository2();
		fr.setDataSource(dataSource);
		Pet p = new Pet();
		p = fr.save(p);
		Assert.assertEquals(16,p.getId().length());
	}

	@Override
	public void setup(Connection c) throws Exception {
		c.createStatement().execute("CREATE TABLE pet (id VARCHAR(36) PRIMARY KEY, age INT, size INT)");
	}

}
