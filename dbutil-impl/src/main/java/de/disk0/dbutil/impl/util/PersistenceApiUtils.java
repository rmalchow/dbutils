package de.disk0.dbutil.impl.util;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

@UtilityClass
public class PersistenceApiUtils {

	private static final Function<Field, Column> JAKARTA_COLUMN = columnAccessor("jakarta.persistence.Column");
	private static final Function<Field, Column> JAVAX_COLUMN = columnAccessor("javax.persistence.Column");

	private static final Function<Class<?>, Table> JAKARTA_TABLE = tableAccessor("jakarta.persistence.Table");
	private static final Function<Class<?>, Table> JAVAX_TABLE = tableAccessor("javax.persistence.Table");

	private static final Class<? extends RuntimeException> NON_UNIQUE_RESULT_EXCEPTION_CLASS;

	static {
		Class exceptionClass = initPersistenceClass("jakarta.persistence.NonUniqueResultException");
		if (exceptionClass == null) exceptionClass = initPersistenceClass("javax.persistence.NonUniqueResultException");
		if (exceptionClass == null) throw new RuntimeException("Missing JAVAX or JAKARTA persistence API");

		NON_UNIQUE_RESULT_EXCEPTION_CLASS = exceptionClass;
	}

	@SneakyThrows
	public static RuntimeException nonUniqueResultException() {
		return NON_UNIQUE_RESULT_EXCEPTION_CLASS.getConstructor().newInstance();
	}

	@Nullable
	public static Column getColumn(Field field) {
		Column column = JAKARTA_COLUMN.apply(field);
		if (column != null) return column;

		return JAVAX_COLUMN.apply(field);
	}

	@Nullable
	public static Table getTable(Class<?> entity) {
		Table column = JAKARTA_TABLE.apply(entity);
		if (column != null) return column;

		return JAVAX_TABLE.apply(entity);
	}

	@Nullable
	public static String getTableName(Class<?> entity) {
		Table table = getTable(entity);
		return table != null ? table.getName() : null;
	}

	@Nullable
	private static <T> Class<T> initPersistenceClass(String className) {
		try {
			return (Class<T>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	@SneakyThrows
	private static <T> T propertyAccessor(Annotation field, String annotationProperty) {
		return (T) field.getClass().getMethod(annotationProperty).invoke(field);
	}

	private static Function<Field, Column> columnAccessor(String columnClassName) {
		Class<? extends Annotation> columnClass = initPersistenceClass(columnClassName);
		if (columnClass == null) return field -> null;

		Function<Annotation, String> nameAccessor = field -> propertyAccessor(field, "name");
		Predicate<Annotation> uniqueAccessor = field -> propertyAccessor(field, "unique");
		Predicate<Annotation> nullableAccessor = field -> propertyAccessor(field, "nullable");
		Predicate<Annotation> insertableAccessor = field -> propertyAccessor(field, "insertable");
		Predicate<Annotation> updatableAccessor = field -> propertyAccessor(field, "updatable");
		Function<Annotation, String> columnDefinitionAccessor = field -> propertyAccessor(field, "columnDefinition");
		Function<Annotation, String> tableAccessor = field -> propertyAccessor(field, "table");
		ToIntFunction<Annotation> lengthAccessor = field -> propertyAccessor(field, "length");
		ToIntFunction<Annotation> precisionAccessor = field -> propertyAccessor(field, "precision");
		ToIntFunction<Annotation> scaleAccessor = field -> propertyAccessor(field, "scale");

		return field -> {
			Annotation annotation = field.getAnnotation(columnClass);
			if (annotation == null) return null;

			return new Column(
					nameAccessor.apply(annotation),
					uniqueAccessor.test(annotation),
					nullableAccessor.test(annotation),
					insertableAccessor.test(annotation),
					updatableAccessor.test(annotation),
					columnDefinitionAccessor.apply(annotation),
					tableAccessor.apply(annotation),
					lengthAccessor.applyAsInt(annotation),
					precisionAccessor.applyAsInt(annotation),
					scaleAccessor.applyAsInt(annotation)
			);
		};
	}

	private static Function<Class<?>, Table> tableAccessor(String tableClassName) {
		Class<? extends Annotation> tableClass = initPersistenceClass(tableClassName);
		if (tableClass == null) return field -> null;

		Function<Annotation, String> nameAccessor = field -> propertyAccessor(field, "name");
		Function<Annotation, String> catalogAccessor = field -> propertyAccessor(field, "catalog");
		Function<Annotation, String> schemaAccessor = field -> propertyAccessor(field, "schema");

		return entityClass -> {
			Annotation annotation = entityClass.getAnnotation(tableClass);
			if (annotation == null) return null;

			return new Table(
					nameAccessor.apply(annotation),
					catalogAccessor.apply(annotation),
					schemaAccessor.apply(annotation)
			);
		};
	}

	@Value
	public static class Table {
		String name;
		String catalog;
		String schema;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getCatalog() {
			return catalog;
		}
		public void setCatalog(String catalog) {
			this.catalog = catalog;
		}
		public String getSchema() {
			return schema;
		}
		public void setSchema(String schema) {
			this.schema = schema;
		}
	}

	@Value
	public static class Column {
		String name;
		boolean unique;
		boolean nullable;
		boolean insertable;
		boolean updatable;
		String columnDefinition;
		String table;
		int length;
		int precision;
		int scale;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public boolean isUnique() {
			return unique;
		}
		public void setUnique(boolean unique) {
			this.unique = unique;
		}
		public boolean isNullable() {
			return nullable;
		}
		public void setNullable(boolean nullable) {
			this.nullable = nullable;
		}
		public boolean isInsertable() {
			return insertable;
		}
		public void setInsertable(boolean insertable) {
			this.insertable = insertable;
		}
		public boolean isUpdatable() {
			return updatable;
		}
		public void setUpdatable(boolean updatable) {
			this.updatable = updatable;
		}
		public String getColumnDefinition() {
			return columnDefinition;
		}
		public void setColumnDefinition(String columnDefinition) {
			this.columnDefinition = columnDefinition;
		}
		public String getTable() {
			return table;
		}
		public void setTable(String table) {
			this.table = table;
		}
		public int getLength() {
			return length;
		}
		public void setLength(int length) {
			this.length = length;
		}
		public int getPrecision() {
			return precision;
		}
		public void setPrecision(int precision) {
			this.precision = precision;
		}
		public int getScale() {
			return scale;
		}
		public void setScale(int scale) {
			this.scale = scale;
		}
	}

}
