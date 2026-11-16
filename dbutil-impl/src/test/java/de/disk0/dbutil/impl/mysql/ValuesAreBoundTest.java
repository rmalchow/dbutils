package de.disk0.dbutil.impl.mysql;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.impl.util.FormatUtil;

public class ValuesAreBoundTest {

	@Test
	public void noValueAppearsInSql() throws Exception {
		Date date = new Date(1234567890123L);
		List<String> in = List.of("in-needle-1", "in-needle-2");
		Object[] values = { "str-needle", date, 424242, in, "on-needle" };
		// ponytail: limit(offset, max) is inlined as ints (LIMIT 7331,9917), not bound; reported on task 5.3, no product change

		Select s = new MysqlStatementBuilder().createSelect();
		TableReference a = s.fromTable("hund");
		de.disk0.dbutil.api.JoinTable b = a.join("katze");
		b.addOn(Operator.AND, b, "name", Comparator.EQ, "on-needle");
		s.condition(Operator.AND, a, "name", Comparator.EQ, "str-needle");
		s.condition(Operator.AND, a, "born", Comparator.GT, date);
		s.condition(Operator.AND, a, "size", Comparator.EQ, 424242);
		s.condition(Operator.AND, a, "name", Comparator.IN, in);
		s.limit(7331, 9917);

		String sql = s.getSql();
		for (Object v : values) {
			if (v instanceof List<?> l) {
				l.forEach(x -> Assertions.assertFalse(sql.contains(String.valueOf(x)), sql));
			} else {
				Assertions.assertFalse(sql.contains(String.valueOf(v)), sql);
			}
		}
		for (Object v : new Object[] { "str-needle", date, 424242, in, "on-needle" }) {
			// the builder binds a java.util.Date as a formatted string (FormatUtil.resolve)
			Object expected = FormatUtil.resolve(v);
			boolean bound = s.getParams().containsValue(expected);
			Assertions.assertTrue(bound, v + " not bound: " + s.getParams());
		}
		System.err.println(sql + " " + s.getParams());
	}
}
