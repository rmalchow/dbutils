## ADDED Requirements

### Requirement: Entities are declared with Jakarta Persistence annotations
Table and column mapping SHALL be read from `jakarta.persistence` annotations only. `javax.persistence` annotations SHALL have no effect.

#### Scenario: Jakarta-annotated entity
- **WHEN** an entity class carries `jakarta.persistence.Table` and fields carry `jakarta.persistence.Column`
- **THEN** the table name and column names are taken from those annotations

### Requirement: Columns absent from a result set are skipped with a warning
When a mapped column is not present in the result set being mapped, the field SHALL be left unset and a warning naming the entity and column SHALL be logged. The set of present columns SHALL be determined from the result set being mapped, not from any earlier result set.

#### Scenario: Projection
- **WHEN** a query selects only some of an entity's mapped columns
- **THEN** the selected columns are mapped, the others are left unset, and no exception is raised

#### Scenario: Projection after a full select
- **WHEN** one repository instance runs a query selecting all columns and then a query selecting some columns
- **THEN** the second query maps its columns without error

#### Scenario: Full select after a projection
- **WHEN** one repository instance runs a query selecting some columns and then a query selecting all columns
- **THEN** the second query maps all columns

### Requirement: Binary columns map independently of the driver
A column whose value the driver returns as `java.sql.Blob` SHALL be mapped to its bytes using only the JDBC API.

#### Scenario: BLOB values
- **WHEN** an entity with a `byte[]` field mapped to a BLOB column is read with MariaDB driver 2.6.2 or with the version managed by the default Spring Boot BOM
- **THEN** SQL `NULL` maps to `null`, an empty value to an empty array, and other values to arrays with equal content

### Requirement: Entity type is resolved from the repository's type hierarchy
The entity class of a repository SHALL be resolved from the type arguments along the repository's whole class hierarchy. If it cannot be resolved, an `IllegalStateException` naming the repository class SHALL be thrown.

#### Scenario: Deep repository hierarchy
- **WHEN** a repository class is more than one level below the generic base repository
- **THEN** the entity type is resolved from the type arguments of the hierarchy

#### Scenario: Unresolvable entity type
- **WHEN** a repository class does not bind the entity type parameter to a class
- **THEN** an `IllegalStateException` naming the repository class is thrown

### Requirement: Mapping failures name their cause
A failure to read an entity's mapping or to convert a column value SHALL surface as an exception whose cause chain contains the original exception and an exception message naming the entity class and, for conversion failures, the column.

#### Scenario: Unconvertible value
- **WHEN** a column value cannot be assigned to its field
- **THEN** the cause chain of the exception thrown to the caller names the entity and the column and contains the original exception
