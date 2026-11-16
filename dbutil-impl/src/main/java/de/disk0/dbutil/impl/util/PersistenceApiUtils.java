package de.disk0.dbutil.impl.util;

import java.lang.reflect.Field;

public final class PersistenceApiUtils {

	public static RuntimeException nonUniqueResultException() {
		return new jakarta.persistence.NonUniqueResultException();
	}

	// returns null when the field has no @Column
	public static Column getColumn(Field field) {
		jakarta.persistence.Column c = field.getAnnotation(jakarta.persistence.Column.class);
		return c == null ? null
			: new Column(c.name(), c.unique(), c.nullable(), c.insertable(), c.updatable(), c.columnDefinition(), c.table(), c.length(), c.precision(), c.scale());
	}

	// returns null when the class has no @Table
	public static Table getTable(Class<?> entity) {
		jakarta.persistence.Table t = entity.getAnnotation(jakarta.persistence.Table.class);
		return t == null ? null : new Table(t.name(), t.catalog(), t.schema());
	}

	// returns null when the class has no @Table
	public static String getTableName(Class<?> entity) {
		Table table = getTable(entity);
		return table != null ? table.getName() : null;
	}

	public static final class Table {
		
		private String name;
		private String catalog;
		private String schema;

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

		@java.lang.SuppressWarnings("all")
		public Table(final String name, final String catalog, final String schema) {
			this.name = name;
			this.catalog = catalog;
			this.schema = schema;
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof PersistenceApiUtils.Table)) return false;
			final PersistenceApiUtils.Table other = (PersistenceApiUtils.Table) o;
			final java.lang.Object this$name = this.getName();
			final java.lang.Object other$name = other.getName();
			if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
			final java.lang.Object this$catalog = this.getCatalog();
			final java.lang.Object other$catalog = other.getCatalog();
			if (this$catalog == null ? other$catalog != null : !this$catalog.equals(other$catalog)) return false;
			final java.lang.Object this$schema = this.getSchema();
			final java.lang.Object other$schema = other.getSchema();
			if (this$schema == null ? other$schema != null : !this$schema.equals(other$schema)) return false;
			return true;
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			final java.lang.Object $name = this.getName();
			result = result * PRIME + ($name == null ? 43 : $name.hashCode());
			final java.lang.Object $catalog = this.getCatalog();
			result = result * PRIME + ($catalog == null ? 43 : $catalog.hashCode());
			final java.lang.Object $schema = this.getSchema();
			result = result * PRIME + ($schema == null ? 43 : $schema.hashCode());
			return result;
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "PersistenceApiUtils.Table(name=" + this.getName() + ", catalog=" + this.getCatalog() + ", schema=" + this.getSchema() + ")";
		}
	}


	public static final class Column {
		
		private String name;
		private boolean unique;
		private boolean nullable;
		private boolean insertable;
		private boolean updatable;
		private String columnDefinition;
		private String table;
		private int length;
		private int precision;
		private int scale;

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

		@java.lang.SuppressWarnings("all")
		public Column(final String name, final boolean unique, final boolean nullable, final boolean insertable, final boolean updatable, final String columnDefinition, final String table, final int length, final int precision, final int scale) {
			this.name = name;
			this.unique = unique;
			this.nullable = nullable;
			this.insertable = insertable;
			this.updatable = updatable;
			this.columnDefinition = columnDefinition;
			this.table = table;
			this.length = length;
			this.precision = precision;
			this.scale = scale;
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public boolean equals(final java.lang.Object o) {
			if (o == this) return true;
			if (!(o instanceof PersistenceApiUtils.Column)) return false;
			final PersistenceApiUtils.Column other = (PersistenceApiUtils.Column) o;
			if (this.isUnique() != other.isUnique()) return false;
			if (this.isNullable() != other.isNullable()) return false;
			if (this.isInsertable() != other.isInsertable()) return false;
			if (this.isUpdatable() != other.isUpdatable()) return false;
			if (this.getLength() != other.getLength()) return false;
			if (this.getPrecision() != other.getPrecision()) return false;
			if (this.getScale() != other.getScale()) return false;
			final java.lang.Object this$name = this.getName();
			final java.lang.Object other$name = other.getName();
			if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
			final java.lang.Object this$columnDefinition = this.getColumnDefinition();
			final java.lang.Object other$columnDefinition = other.getColumnDefinition();
			if (this$columnDefinition == null ? other$columnDefinition != null : !this$columnDefinition.equals(other$columnDefinition)) return false;
			final java.lang.Object this$table = this.getTable();
			final java.lang.Object other$table = other.getTable();
			if (this$table == null ? other$table != null : !this$table.equals(other$table)) return false;
			return true;
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			result = result * PRIME + (this.isUnique() ? 79 : 97);
			result = result * PRIME + (this.isNullable() ? 79 : 97);
			result = result * PRIME + (this.isInsertable() ? 79 : 97);
			result = result * PRIME + (this.isUpdatable() ? 79 : 97);
			result = result * PRIME + this.getLength();
			result = result * PRIME + this.getPrecision();
			result = result * PRIME + this.getScale();
			final java.lang.Object $name = this.getName();
			result = result * PRIME + ($name == null ? 43 : $name.hashCode());
			final java.lang.Object $columnDefinition = this.getColumnDefinition();
			result = result * PRIME + ($columnDefinition == null ? 43 : $columnDefinition.hashCode());
			final java.lang.Object $table = this.getTable();
			result = result * PRIME + ($table == null ? 43 : $table.hashCode());
			return result;
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "PersistenceApiUtils.Column(name=" + this.getName() + ", unique=" + this.isUnique() + ", nullable=" + this.isNullable() + ", insertable=" + this.isInsertable() + ", updatable=" + this.isUpdatable() + ", columnDefinition=" + this.getColumnDefinition() + ", table=" + this.getTable() + ", length=" + this.getLength() + ", precision=" + this.getPrecision() + ", scale=" + this.getScale() + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	private PersistenceApiUtils() {
		throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
