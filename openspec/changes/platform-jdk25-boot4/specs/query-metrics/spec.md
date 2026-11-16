## ADDED Requirements

### Requirement: Queries are observed
Each `find` call SHALL be recorded as a Micrometer observation named `dbutil.sql.find` with the low-cardinality key `object` set to the entity's simple class name, using the application's `ObservationRegistry`. Recording SHALL NOT depend on any dbutils configuration property.

#### Scenario: Registry present
- **WHEN** the application context provides an `ObservationRegistry` and a repository runs a query
- **THEN** one `dbutil.sql.find` observation with the entity's `object` value is recorded

#### Scenario: No registry
- **WHEN** no `ObservationRegistry` is available to the repository
- **THEN** queries run normally and nothing is recorded

#### Scenario: Failing query
- **WHEN** an observed query fails
- **THEN** the original exception reaches the caller unchanged
