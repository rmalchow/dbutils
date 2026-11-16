package de.disk0.dbutil.impl.regression;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.disk0.dbutil.api.entities.BaseGuidEntityV2;
import de.disk0.dbutil.impl.AbstractGuidRepositoryV2;
import de.disk0.dbutil.impl.mysql.TestBase;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

/** One field per type handled by ParsedEntity.ParsedColumn.set/get. */
public class FieldTypeRoundTripTest extends TestBase {

	public enum Color { RED, GREEN }

	@Table(name = "all_types")
	public static class AllTypes extends BaseGuidEntityV2 {
		@Column(name = "f_string") public String string;
		@Column(name = "f_bigdec") public BigDecimal bigDecimal;
		@Column(name = "f_bigint") public BigInteger bigInteger;
		@Column(name = "f_boolean_w") public Boolean booleanW;
		@Column(name = "f_boolean_p") public boolean booleanP;
		@Column(name = "f_int_w") public Integer intW;
		@Column(name = "f_int_p") public int intP;
		@Column(name = "f_long_w") public Long longW;
		@Column(name = "f_long_p") public long longP;
		@Column(name = "f_double_w") public Double doubleW;
		@Column(name = "f_double_p") public double doubleP;
		@Column(name = "f_enum") public Color color;
		@Column(name = "f_date") public Date date;
	}

	public static class AllTypesRepository extends AbstractGuidRepositoryV2<AllTypes> { }

	private AllTypesRepository repo;

	@Override
	public void setup(Connection c) throws Exception {
		c.createStatement().execute("CREATE TABLE all_types (id VARCHAR(36) PRIMARY KEY, f_string VARCHAR(100), "
				+ "f_bigdec DECIMAL(20,4), f_bigint DECIMAL(40,0), f_boolean_w BOOLEAN, f_boolean_p BOOLEAN NOT NULL, "
				+ "f_int_w INT, f_int_p INT NOT NULL, f_long_w BIGINT, f_long_p BIGINT NOT NULL, "
				+ "f_double_w DOUBLE PRECISION, f_double_p DOUBLE PRECISION NOT NULL, f_enum VARCHAR(20), f_date TIMESTAMP)");
	}

	@BeforeEach
	public void repo() {
		repo = new AllTypesRepository();
		repo.setDataSource(dataSource);
	}

	@Test
	public void allValuesRoundTrip() throws Exception {
		AllTypes in = new AllTypes();
		in.string = "hällo €";
		in.bigDecimal = new BigDecimal("12345.6700");
		in.bigInteger = new BigInteger("123456789012345678901234567890");
		in.booleanW = Boolean.TRUE;
		in.booleanP = true;
		in.intW = 42;
		in.intP = -7;
		in.longW = Long.MAX_VALUE;
		in.longP = Long.MIN_VALUE + 1;
		in.doubleW = 3.25;
		in.doubleP = -0.5;
		in.color = Color.GREEN;
		in.date = new Date(1_700_000_000_123L);

		AllTypes saved = repo.save(in);
		AllTypes out = repo.get(saved.getId());

		for (AllTypes o : new AllTypes[] { saved, out }) {
			assertFalse(o.getId().isEmpty());
			assertEquals(in.string, o.string);
			assertEquals(in.bigDecimal, o.bigDecimal);
			assertEquals(in.bigInteger, o.bigInteger);
			assertEquals(Boolean.TRUE, o.booleanW);
			assertTrue(o.booleanP);
			assertEquals(42, o.intW);
			assertEquals(-7, o.intP);
			assertEquals(Long.MAX_VALUE, o.longW);
			assertEquals(Long.MIN_VALUE + 1, o.longP);
			assertEquals(3.25, o.doubleW);
			assertEquals(-0.5, o.doubleP);
			assertEquals(Color.GREEN, o.color);
			// the driver hands back a java.sql.Timestamp; compare instants, not Date.equals
			assertEquals(in.date.getTime(), o.date.getTime());
		}
	}

	@Test
	public void falseAndZeroRoundTrip() throws Exception {
		AllTypes in = new AllTypes();
		in.booleanW = Boolean.FALSE;
		in.intW = 0;
		in.longW = 0L;
		in.doubleW = 0.0;
		AllTypes out = repo.get(repo.save(in).getId());
		assertEquals(Boolean.FALSE, out.booleanW);
		assertFalse(out.booleanP);
		assertEquals(0, out.intW);
		assertEquals(0L, out.longW);
		assertEquals(0.0, out.doubleW);
	}

	@Test
	public void nullableFieldsNull() throws Exception {
		AllTypes out = repo.get(repo.save(new AllTypes()).getId());
		assertNotNull(out);
		assertNull(out.string);
		assertNull(out.bigDecimal);
		assertNull(out.bigInteger);
		assertNull(out.booleanW);
		assertNull(out.intW);
		assertNull(out.longW);
		assertNull(out.doubleW);
		assertNull(out.color);
		assertNull(out.date);
		// primitives were stored as their defaults
		assertFalse(out.booleanP);
		assertEquals(0, out.intP);
		assertEquals(0L, out.longP);
		assertEquals(0.0, out.doubleP);
	}
}
