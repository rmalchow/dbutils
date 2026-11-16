## ADDED Requirements

### Requirement: Parameter values are not logged by default
Log output about statement execution, slowness, failure or row mapping SHALL NOT contain parameter values, entity column values or values read from a result set, except on the logger `de.disk0.dbutil.sql.params` at TRACE level. Such log output SHALL name parameters or columns instead.

#### Scenario: Slow query with default logging
- **WHEN** a query exceeds a slow-query threshold and logging is at its default configuration
- **THEN** one log line contains the SQL, the duration, the row count and the parameter names, and no parameter values

#### Scenario: Failed statement with default logging
- **WHEN** a query, update, delete or save fails and logging is at its default configuration
- **THEN** the log contains the SQL and the parameter or column names, and no values

#### Scenario: Debug logging enabled
- **WHEN** the `de.disk0.dbutil.impl` loggers are at DEBUG and a query is run and its rows are mapped
- **THEN** the log contains no parameter values and no values read from the result set

#### Scenario: Values explicitly enabled
- **WHEN** the logger `de.disk0.dbutil.sql.params` is set to TRACE
- **THEN** the parameter values of those statements are logged on that logger

### Requirement: Statement builders bind values as parameters
SQL produced by `MysqlStatementBuilder` and the statements it creates SHALL carry every comparison value as a named parameter. Only identifiers and the numeric bounds of `LIMIT` SHALL appear literally in the SQL text.

#### Scenario: Values in conditions
- **WHEN** a select is built with string, date and number conditions, an `IN` list, a join and a limit
- **THEN** no condition value appears in the SQL text and every condition value appears in the parameter map
