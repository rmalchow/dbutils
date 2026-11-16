# Make dbutils ready for JDK 25 and Spring Boot 4 (release 0.3.0)

## Why

The selfservice platform (`mcg-automation`) is moving to **JDK 25 on Spring Boot 3.5**, and a few
weeks later to **Spring Boot 4** (which brings Spring Framework 7). dbutils is its database layer:

- 70 repositories in the platform extend `AbstractGuidRepositoryV2`, the base class that gives an
  entity `get`, `save`, `delete` and `find`.
- Queries are SQL strings, written by hand, through `SimpleQuery` (a SQL string plus a map of
  named parameters such as `:userId`), or through the query builder `MysqlStatementBuilder`, which
  produces such a string and map from method calls.
- Rows are mapped onto entity classes by reflection. `ParsedEntity` reads the `@Table` and
  `@Column` annotations of an entity class and writes the column values straight into its fields.

An audit (`docs/proposals/updating/report.html` §4 in the platform repo) found that dbutils 0.2.14
already compiles and runs on JDK 25 and Boot 3.5. It needs no `--add-opens` (the JVM flag that
lets reflection into JDK-internal classes): every `setAccessible(true)` targets the application's
own entity classes. What stands in the way is smaller and spread out:

- The build inherits from `spring-boot-starter-parent` **3.0.1**. A *parent* POM is a Maven project
  that this project inherits settings from; here it supplies dependency versions, plugin versions
  and compiler flags. A library pinned to Boot 3.0.1's parent cannot be verified against Boot 4.
- Two classes import Spring Boot *auto-configuration* classes, the part of Boot that creates beans
  automatically when certain classes or properties are present. Boot 4 splits that module up and
  moves some of these classes:
  - `config/ReaderConfig` uses `org.springframework.boot.autoconfigure.jdbc.DataSourceProperties`;
  - `micrometer/MicrometerAdapterImpl` uses `@ConditionalOnClass` and `@ConditionalOnProperty`.
- Annotation support exists twice, once for `javax.persistence` and once for `jakarta.persistence`.
  These are the same JPA annotations before and after the Java EE → Jakarta EE rename. Choosing
  between them at runtime takes string class names, reflection and a `lombok.Lombok.sneakyThrow`
  call. Lombok is only a `provided` dependency, so that class is missing at runtime and a real
  error turns into `NoClassDefFoundError: lombok/Lombok`.
- BLOB columns are read by looking up the MariaDB driver class `org.mariadb.jdbc.MariaDbBlob` by
  name and calling its method reflectively. A library should not know which JDBC driver is in use.
- Several log statements write SQL **parameter values** at WARN or INFO, which is on in production.
  Those values are user names, e-mail addresses and whatever else is being searched for:
  - every failed query (`-------- query failed: <sql> / {email=jane@example.org}`);
  - every failed save (the entity's complete column values);
  - and, in an uncommitted change in the working tree, every query slower than one second.

The full list, with what was decided for each item, is in `design.md`.

## What Changes

- **Build.**
  - The parent POM is replaced by importing Spring Boot's *BOM*. A BOM ("bill of materials") is a
    POM that contains only dependency versions. Importing it gives the same versions as the parent,
    without inheriting anything else.
  - A Maven property `springboot.version` selects which BOM is imported, defaulting to the latest
    3.5.x. The build is also verified with the Boot 4 BOM.
  - `maven.compiler.release` goes from 17 to **25**.
  - Source encoding stays UTF-8, now declared explicitly because the parent no longer provides it.
- **jakarta only.**
  - `BaseEntityV1`, `BaseGuidEntityV1` and `AbstractGuidRepository` (the V1 repository) are
    removed, along with all `javax.persistence` support.
  - `PersistenceApiUtils` reads `jakarta.persistence.Column` and `Table` directly, with no
    reflection and no Lombok.
  - The tests and `dbutils-examples` move to the V2 classes.
- **Missing columns stay a warning.** An entity field whose column is not in the result set is left
  unset and logged, as today. The list of result-set columns is currently cached from the *first*
  query a repository runs and reused for every later one, which makes that warning (and the
  mapping) wrong. It is now computed per result set.
- **BLOBs through plain JDBC** (`java.sql.Blob`), so any driver works.
- **Logging never contains parameter values by default.** Statements log the SQL, timing, row count
  and parameter **names**. Values appear only on a dedicated logger, `de.disk0.dbutil.sql.params`,
  at TRACE, which is off unless someone switches it on. The same goes for values read *back* from
  the database: today `ParsedEntity` writes every mapped column value at DEBUG
  (`mapping column: email - jane@example.org`); afterwards it writes only the column name. The
  formatting lives in one small helper class, `util/SqlLog`, so the rule is applied in one place.
- **The query builder keeps binding every value as a named parameter**, and a test now pins that
  down. Only identifiers (table and column names) and the `LIMIT` numbers are written into the
  SQL text.
- **Query metrics move to the Micrometer Observation API.** The timer is still called
  `dbutil.sql.find` and is still tagged with the entity class. It now appears whenever the
  application has an `ObservationRegistry` (Spring injects it through a new optional setter,
  `setObservationRegistry`, next to the existing `setDataSource`); the `dbutil.micrometer.enabled`
  switch is gone.
- Clean-ups:
  - `ReaderConfig` is deleted;
  - the entity type is resolved with Spring's `ResolvableType`, which follows type arguments through
    deeper hierarchies and proxies (design D8);
  - `Class.newInstance()` is replaced;
  - the four `@Nullable` annotations in `PersistenceApiUtils` (Spring's, deprecated in Spring 7)
    are dropped rather than replaced, see D8;
  - the deprecated `StringUtils.isEmpty` in `AbstractGuidRepositoryV2.save` becomes
    `StringUtils.hasLength`;
  - the commons-io and Lombok dependencies go;
  - tests move to JUnit 5, and the test connection pool `commons-dbcp2` is replaced by Spring's own
    `SimpleDriverDataSource`.
- **Docs.** The README is brought up to date: supported versions, jakarta only, V2 examples, the
  parameter-value logger, and metrics.
- Release **0.3.0**: one artifact that works unchanged on Boot 3.5 and Boot 4.

Not changing: the query builder API, the public methods of `AbstractGuidRepositoryV2` and
`AbstractMappingRepository` (including `mapRow` as an override point; the only addition is the
`setObservationRegistry` setter), and the SQL that is produced.

## Capabilities

### New Capabilities

- `platform-compatibility`: one release builds and runs on every Boot line the platform is moving
  between, on Java 25, without any JDBC-driver-specific or Boot-auto-configuration code.
- `entity-mapping`: how entity classes are declared (jakarta annotations only) and how result-set
  rows become objects, including missing columns, BLOBs and failure messages.
- `query-logging`: what is logged about statements, and the guarantee that parameter values are
  not logged unless explicitly enabled.
- `query-metrics`: the observation recorded for queries.

### Modified Capabilities

None. This repo has no capability specs yet.

## Impact

- Code:
  - `dbutil-api`: V1 entities removed;
  - `dbutil-impl`: `ParsedEntity`, `PersistenceApiUtils`, `AbstractMappingRepository`,
    `AbstractRepository`, `AbstractGuidRepositoryV2` and `SimpleRowMapper` change; `util/SqlLog`
    is new; `AbstractGuidRepository`, `config/ReaderConfig` and the `micrometer` package are
    deleted;
  - `dbutils-examples` moves to V2;
  - all poms;
  - the README.
- **Breaking for consumers**, which is why this is 0.3.0:
  - V1 classes and `javax.persistence` annotations are gone;
  - Java 25 is the minimum;
  - `jakarta.persistence-api` becomes a normal dependency of dbutils instead of one the consumer
    has to add.
- The platform bumps `dbutil.version` 0.2.14 → 0.3.0 while still on Boot 3.5. Before that it
  checks three things, listed in `design.md` under "Migration":
  - its `mariadb-java-client` pin;
  - the `dbutil.micrometer.enabled` property;
  - any log-based alerts on the old slow-query lines.
- `dbutil-schema-check` uses Jackson 2 (`new ObjectMapper()`). The platform does not use it, so it
  stays as it is. It only has to keep building on both BOMs.
