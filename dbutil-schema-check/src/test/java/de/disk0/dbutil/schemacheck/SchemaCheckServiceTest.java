package de.disk0.dbutil.schemacheck;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/** The schema JSON (and so the logged hash) must not change with the JSON library. schema.json was recorded with Jackson 2 (0.3.x). */
class SchemaCheckServiceTest {

	static List<Table> tables() {
		Column c = new Column();
		c.setField("id");
		c.setType("varchar(64)");
		c.setNullable("NO");
		c.setKey("PRI");
		c.setDefaultValue(null);
		Key k = new Key();
		k.setUnique(true);
		k.setSeqInIndex(1);
		k.setTable("account");
		k.setKeyname("PRIMARY");
		k.setColumn("id");
		Table t = new Table();
		t.setName("account");
		t.setColumns(List.of(c));
		t.setKeys(List.of(k));
		// what the service actually produces today: its row mappers return null
		Table nulls = new Table();
		nulls.setName("range");
		nulls.setColumns(Arrays.asList(null, null));
		nulls.setKeys(Arrays.asList((Key) null));
		return List.of(t, nulls);
	}

	@Test
	void jsonIsUnchanged() throws Exception {
		String expected;
		try (InputStream in = SchemaCheckServiceTest.class.getResourceAsStream("/schema.json")) {
			expected = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		assertEquals(expected, SchemaCheckService.toJson(tables()));
	}

	/** Records schema.json; run once with the Jackson 2 implementation. */
	public static void main(String[] args) throws Exception {
		java.nio.file.Files.writeString(java.nio.file.Path.of(args[0]), SchemaCheckService.toJson(tables()));
	}
}
