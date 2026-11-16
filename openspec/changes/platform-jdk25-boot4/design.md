# Design: dbutils 0.3.0 for JDK 25 and Spring Boot 4

## Context: how a row becomes an object

Take one of the platform's repositories, `class UserRepository extends AbstractGuidRepositoryV2<UserEntity>`,
and a call `userRepository.find("SELECT * FROM users WHERE email = :email", Map.of("email", "jane@example.org"))`.

1. **Finding the entity type.** `AbstractMappingRepository.java:49` works out that the repository is
   about `UserEntity`. It casts `getClass().getGenericSuperclass()` to `ParameterizedType` and
   takes type argument 0.
   - This works for all 70 platform repositories, because each is directly below the generic base
     class.
   - It throws `ClassCastException` for a deeper hierarchy, for example
     `UserRepository extends AuditedRepository<UserEntity>`, where
     `AuditedRepository<T> extends AbstractGuidRepositoryV2<T>`. There the direct superclass is
     `AuditedRepository<UserEntity>`, and its argument is a `TypeVariable` named `T`, not a class.
   - It also throws for a *CGLIB proxy*. Spring sometimes creates a generated subclass of a bean
     at runtime to add behaviour such as `@Transactional`; that subclass's superclass is
     `UserRepository` itself, which has no type argument.
2. **Reading the mapping.** `ParsedEntity` walks the entity's fields. For each one,
   `PersistenceApiUtils.getColumn(field)` looks for `jakarta.persistence.Column` and then for
   `javax.persistence.Column`. Both are looked up by *string name* (`Class.forName`), because either
   jar might be missing. Their attributes are read reflectively:
   `field.getClass().getMethod("name").invoke(field)` at `PersistenceApiUtils.java:65`, where
   `field` is the annotation instance, despite the name.
3. **Running the query.** `AbstractMappingRepository.find` hands SQL and parameters to Spring's
   `NamedParameterJdbcTemplate`, which replaces `:email` with a `?` and binds the value separately,
   so the value never becomes part of the SQL text.
4. **Creating the instance.** `Class.newInstance()` (`AbstractMappingRepository.java:106`). This has
   been deprecated since Java 9 because it rethrows the constructor's checked exceptions without
   declaring them.
5. **Filling the fields.** `mapRow` (line 95) checks each mapped column against the result set's
   column list and calls `ParsedColumn.set`, which picks a JDBC getter by field type. Anything not
   matched falls through to `rs.getObject(column)`. If that object's class is called
   `org.mariadb.jdbc.MariaDbBlob`, its `getBinaryStream()` is called reflectively and copied into a
   `byte[]` with commons-io (`ParsedEntity.java:140–152`).

## Decisions

### D1. Build: imported BOM, release 25, explicit encoding

Today's root `pom.xml` has `spring-boot-starter-parent` 3.0.1 as its parent. That parent supplies
three things:

- dependency versions;
- plugin versions and configuration, including `-parameters` for `maven-compiler-plugin`, which
  keeps method parameter names in the bytecode;
- `project.build.sourceEncoding` = UTF-8 and resource filtering.

New:

```xml
<properties>
  <springboot.version>3.5.7</springboot.version>   <!-- latest 3.5.x at implementation time -->
  <maven.compiler.release>25</maven.compiler.release>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>${springboot.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

- The plugins the build uses (`maven-compiler-plugin` with `<parameters>true</parameters>`,
  `maven-surefire-plugin`, `maven-source-plugin`) get explicit versions in `pluginManagement`,
  the latest release of each at implementation time (for example compiler 3.14.x, surefire 3.5.4,
  source 3.3.x). Without the parent nobody else pins them, and Maven would otherwise fall back to
  the old defaults built into Maven itself.
- The `java.version` property goes; only the Boot parent read it.
- The modules refer to each other with a literal version today, for example
  `dbutil-impl/pom.xml` depends on `dbutil-api` `0.2.15-SNAPSHOT`. These become
  `${project.version}`, so that setting the release version later (task 9.1) is one command and
  cannot leave a stale reference behind.
- `mvn -Dspringboot.version=<latest 4.0.x> verify` must pass as well. This is the same approach
  remoting uses.

**Test setup.** Two test-only changes ride along, because both are about not depending on what a
particular BOM happens to manage:

- **JUnit 4 → JUnit 5 (Jupiter).** JUnit 4 is maintenance-only. Boot 3.5's BOM still manages
  `junit:junit` 4.13.2; Boot 4's manages JUnit 6, whose API is the Jupiter one. The twelve test
  files in `dbutil-impl/src/test/java` (eleven test classes plus `TestBase`) change their imports (`org.junit.Test` →
  `org.junit.jupiter.api.Test`, `Assert` → `Assertions`, `@Before`/`@After` →
  `@BeforeEach`/`@AfterEach`). The two `@Test(expected = SqlException.class)` in `UuidTests`
  become `assertThrows(SqlException.class, () -> …)`.
- **`commons-dbcp2` → Spring's `SimpleDriverDataSource`.** `TestBase` builds an H2 database per test
  with dbcp2's `BasicDataSource`, a connection pool. A pool is pointless for a test that opens a
  handful of connections, and `SimpleDriverDataSource` from `spring-jdbc` (already a dependency)
  does the same in two lines: `new SimpleDriverDataSource(new org.h2.Driver(), "jdbc:h2:" + path)`.
  One test-scoped dependency fewer to keep managed on two BOMs.

**Order of the pom edits.** Some dependencies can only be removed once the code that uses them is
gone. Removing `lombok` before `PersistenceApiUtils` stops calling `lombok.Lombok.sneakyThrow`
breaks the build; the same holds for `commons-io` (`ParsedEntity`), `spring-boot-autoconfigure`
(`ReaderConfig`, `MicrometerAdapterImpl`) and `micrometer-core` (`MicrometerAdapterImpl`). The poms
therefore change in two passes: first everything is *added* that the new code needs
(`micrometer-observation`, the JUnit 5 and MariaDB test dependencies, `jakarta.persistence-api` as a
real dependency), and only at the end, after the code changes, is everything *removed* that is no
longer used. In between, the build stays green at every step.

**Release 25, not 17.** The platform is the consumer that matters, and it moves to 25. 0.3.0 breaks
the API anyway (D2), so raising the floor costs nothing extra. This was the user's decision
(2026-11-16): "this is a new version, old projects can keep using the old libraries."

What this means for a consumer: `release 25` writes class files with major version 69. A JVM older
than 25 refuses to load them (`UnsupportedClassVersionError`), and `javac` compiling for an older
release refuses to read them. So a project can take 0.3.0 only once **its own build and runtime are
on JDK 25**. For the platform, the `dbutil.version` bump therefore goes into the same step as its
JDK 25 switch (step 1, Boot 3.5 + JDK 25), and cannot come before it.

Rejected alternatives:

- **Keep 17.** It would keep the jar usable on older JDKs, but no such consumer is known, and
  0.2.x stays available for them.
- **Two artifacts or classifiers, one per Boot line.** This doubles the release work for a library
  whose API is identical on both lines.

### D2. jakarta only; V1 removed

The *V1* classes are `BaseEntityV1` and `BaseGuidEntityV1`, which are annotated with
`javax.persistence`, and `AbstractGuidRepository<T extends BaseGuidEntityV1>`. The *V2* classes are
the same thing on `jakarta.persistence`. The platform uses only V2 (116 imports, no V1).

Inside this repo, however, V1 is still in use:

- `AbstractGuidRepository` (main code);
- the test entities `Pet`, `Hund`, `Katze` and `BigDecimalEntity`, which extend
  `BaseGuidEntityV1` but carry `jakarta` annotations on their own fields;
- seven test repositories (`PetRepository`, `PetRepository1/2`, `BrokenPetRepositoryA/B`,
  `HundRepository`, `KatzeRepository`, `BigDecimalEntityRepository`);
- `dbutils-examples` (`ExampleEntity`, `ExampleRepository`).

All of these move to `BaseGuidEntityV2` / `AbstractGuidRepositoryV2`, and then the V1 classes are
deleted.

With `javax` gone, `PersistenceApiUtils` no longer has to cope with either jar being absent. Before
and after, for the column lookup:

```java
// before: two string-named lookups, reflective attribute reads, Lombok at runtime
private static final Function<Field, Column> JAKARTA_COLUMN = columnAccessor("jakarta.persistence.Column");
private static final Function<Field, Column> JAVAX_COLUMN   = columnAccessor("javax.persistence.Column");
...
return (T) field.getClass().getMethod(annotationProperty).invoke(field);   // throws via lombok.Lombok.sneakyThrow

// after (returns null when the field has no @Column)
public static Column getColumn(Field field) {
    jakarta.persistence.Column c = field.getAnnotation(jakarta.persistence.Column.class);
    return c == null ? null
        : new Column(c.name(), c.unique(), c.nullable(), c.insertable(), c.updatable(),
                     c.columnDefinition(), c.table(), c.length(), c.precision(), c.scale());
}
```

The same goes for `Table`, and `nonUniqueResultException()` becomes
`new jakarta.persistence.NonUniqueResultException()`. The public surface of the class stays as it
is (`getColumn`, `getTable`, `getTableName`, `nonUniqueResultException` and the nested `Column` and
`Table` value classes), because `ParsedEntity` and `AbstractMappingRepository` call it; only the
inside changes, and the `// Generated by delombok` header goes with it. This removes in one step:

- the reflective lookup of annotation attributes on the annotation's proxy class, which the audit
  called D4;
- the runtime `Lombok.sneakyThrow`, which the audit called D5.

Consequences:

- `jakarta.persistence-api` changes from an `optional` dependency in the root pom to a normal
  compile dependency of `dbutil-api` and `dbutil-impl`, with its version from the BOM (3.1.x on
  Boot 3.5, 3.2.x on Boot 4; the annotations are identical).
- Consumers no longer have to add it themselves.
- Nothing in the repo uses a Lombok annotation any more. The source files are the output of a past
  `delombok` run, so the `lombok` dependency is dropped from all poms.

Rejected alternative: **keep the `javax` fallback in `PersistenceApiUtils`** while removing only the
V1 base classes. That would leave the reflection, the string class names and the Lombok problem in
place, for an API whose last release was in 2018 and which nobody here uses.

### D3. Missing columns stay a warning; the column list is computed per result set

Today, if an entity has `@Column String nickname` and a query selects only `id, email`, `mapRow`
logs `column: nickname in object (UserEntity), but not in result set` at WARN and leaves the field
at its default. **That stays as it is.** Projections (queries that select only some columns) rely
on it.

What is wrong is *where the column list comes from*. `applicableColumns` is a field of the
repository, which is a singleton. It is filled from the first result set the repository ever sees
and never refreshed. Two queries on the same `UserRepository`, in this order:

| # | Query | Columns in this result set | `applicableColumns` used | Effect |
|---|---|---|---|---|
| 1 | `SELECT id, email FROM users` | id, email | id, email (cached now) | correct |
| 2 | `SELECT * FROM users` | id, email, nickname, … | **id, email** | `nickname` and every other column are silently skipped and stay `null` |

In the reverse order, query 2 fills the cache, and then query 1 tries `rs.getString("nickname")` on
a result set that lacks it, so the mapping fails with an SQL exception. Because the field is
shared, two threads running different queries also race on it.

Fix: `mapRow` reads the column labels from `rs.getMetaData()` on every call and keeps nothing on
the instance. The driver caches the metadata object for the result set, so this costs a short loop
over strings per row, which is small next to the reflective field writes that follow.

Rejected alternatives:

- **Throw on a missing column.** This breaks every projection.
- **Create a separate row mapper per query inside `find`.** That is cleaner, but `mapRow(ResultSet,
  int)` is documented in the README as the method subclasses override to post-process rows, and
  some may do so. Moving the logic elsewhere would silently bypass those overrides.
- **Cache keyed by SQL string.** This saves the per-row loop, but adds a cache that grows without
  limit, for no measurable gain.

### D4. BLOBs through `java.sql.Blob`

The audit claimed that `org.mariadb.jdbc.MariaDbBlob` exists only in driver 2.x and that this is
why the platform pins `mariadb-java-client` 2.6.2. **That is not correct.** The class is present
in 3.5.6 as well; this was checked in the local Maven repository. The change is still right,
because a library should not depend on driver internals. But it does not prove that the platform
can drop its pin (see "Migration").

In the fallback branch of `ParsedColumn.set`:

```java
Object o = rs.getObject(column);
if (o instanceof java.sql.Blob blob) {
    try {
        field.set(target, blob.getBytes(1, (int) blob.length()));   // JDBC positions start at 1
    } finally {
        blob.free();
    }
} else {
    field.set(target, o);
}
```

| Column value | Mapped field value (any driver) |
|---|---|
| SQL `NULL` | `null` |
| empty BLOB (0 bytes) | `byte[0]` |
| N bytes | `byte[N]`, equal content |

commons-io was used only for this copy, so the dependency is dropped (this was the audit's D11).

While `ParsedColumn.set` is open, its last line, `log.debug("mapping column: " + column + " - " +
rs.getObject(column))`, loses the value and keeps the column name (see D5).

**How it is tested.** H2, the in-memory database the other tests use, does not return
`java.sql.Blob` from `getObject`, so the test needs a real MariaDB. It is a normal JUnit test,
`dbutil-impl/src/test/java/de/disk0/dbutil/impl/blob/MariaDbBlobTest.java`, that reads the JDBC URL
from the system property `dbutil.it.mariadb.url` and is *skipped* (a JUnit assumption, not a
failure) when the property is not set. So `mvn verify` stays green on a machine without a database,
and a run against MariaDB looks like this:

```sh
docker run -d --rm --name dbutil-mariadb -p 33306:3306 \
  -e MARIADB_ROOT_PASSWORD=test -e MARIADB_DATABASE=dbutil mariadb:11
mvn -pl dbutil-impl -am test -Dtest=MariaDbBlobTest -Dsurefire.failIfNoSpecifiedTests=false \
  -Ddbutil.it.mariadb.url='jdbc:mariadb://localhost:33306/dbutil?user=root&password=test'
# and once more with the old driver the platform pins:
mvn … -Dmariadb.test.version=2.6.2
```

The MariaDB driver is a test-scoped dependency of `dbutil-impl` with the version
`${mariadb.test.version}`, a property in `dbutil-impl/pom.xml` whose default is the version the
Boot 3.5 BOM manages (3.5.6 for Boot 3.5.7). An explicit version is needed because a property set
in our pom cannot override a version that comes from an *imported* BOM.

Rejected alternatives:

- **`rs.getBytes(column)` whenever the field type is `byte[]`.** This works for binary columns, but
  changes which getter is used for every `byte[]` field, including `BINARY` / `VARBINARY` columns
  that work today through `getObject`. Checking for `Blob` changes only what was actually broken.
- **Testcontainers** (a library that starts a Docker container from inside the test). The Boot 3.5
  BOM manages Testcontainers 1.21 (`org.testcontainers:mariadb`); the Boot 4 BOM manages 2.x, whose
  artifacts are renamed (`org.testcontainers:testcontainers-mariadb`). Supporting both BOMs would
  mean pinning one of them by hand, for a single test. A system property and a `docker run` line
  are simpler and also work against an existing database.

### D5. Parameter values are never logged at default levels

Every log statement about a statement follows one rule:

- **Default loggers** (`de.disk0.dbutil.impl.*`, at their usual levels) get the SQL, the duration,
  the row count, the entity class, and the parameter **names**.
- **Values** go only to the logger `de.disk0.dbutil.sql.params`, at TRACE. Nobody switches that on
  by accident.

What changes, statement by statement:

| Where | Today | New |
|---|---|---|
| `find`, > 1000 ms | WARN `VERY SLOW: 12 / 1834ms`, INFO the SQL, uncommitted: INFO one line per value | WARN `VERY SLOW: 1834ms, 12 rows, UserEntity: SELECT … params=[email, unitId]` |
| `find`, > 500 ms | INFO `SLOW: 12 / 712ms`, INFO the SQL | INFO `SLOW: 712ms, 12 rows, UserEntity: SELECT … params=[email, unitId]` |
| `find` / `findOne` failed | WARN `query failed: <sql> / {email=jane@…}` | WARN `query failed: <sql> params=[email]` with the exception |
| `update` / `delete` | DEBUG `update: <sql> / {…}`; WARN `delete failed: <sql> / {…}` | names only; the message for a failed update says "update failed" |
| `AbstractRepository` query failed | WARN `<sql> / {…}` | names only |
| `AbstractGuidRepositoryV2.save` | DEBUG `INSERT: {…values}`; WARN `saving failed: {…values}` | DEBUG/WARN with column names only |
| `find`, every call | DEBUG `query: <sql> / {…}` | DEBUG with names only |
| `AbstractRepository.get` failed | WARN `query failed: <sql> / {id=…}` | names only |
| `AbstractGuidRepositoryV2.save`, after saving | DEBUG `retrieving object as saved: >>>4f1c…<<< (UserEntity)` | DEBUG without the id |
| `ParsedEntity.ParsedColumn.set`, every column of every row | DEBUG `mapping column: email - jane@example.org` | DEBUG `mapping column: email` |

In every one of these cases, if `de.disk0.dbutil.sql.params` is at TRACE, one more line with the
values is written there, for example `params UserEntity: {email=jane@example.org, unitId=42}`.
(The last two rows are the exception: the id and the per-column value are simply dropped; nobody
needs them on the params logger.)

**One helper.** The rule is applied through one small class, so that the next log line someone adds
has an obvious way to do the right thing:

```java
// dbutil-impl/src/main/java/de/disk0/dbutil/impl/util/SqlLog.java
public final class SqlLog {
    private static final Log PARAMS = LogFactory.getLog("de.disk0.dbutil.sql.params");

    /** Parameter names only, sorted: "[email, unitId]". Safe at any level. */
    public static String names(Map<String, ?> params) {
        return params == null ? "[]" : new TreeSet<>(params.keySet()).toString();
    }

    /** Values, only on de.disk0.dbutil.sql.params at TRACE. */
    public static void values(String context, Map<String, ?> params) {
        if (PARAMS.isTraceEnabled()) PARAMS.trace("params " + context + ": " + params);
    }
}
```

For `save`, the map is `MapSqlParameterSource.getValues()`, whose keys are the column names.

**How it is tested.** dbutils logs through `org.apache.commons.logging` (Spring's `spring-jcl` on
Spring 6, Apache commons-logging 1.3 on Spring 7). With neither Log4j 2 nor SLF4J on the test
classpath, both route to `java.util.logging`, so the test attaches a `java.util.logging.Handler` to
the loggers `de.disk0.dbutil.impl` and `de.disk0.dbutil.sql.params` and inspects the records. The
slow path is reached for real: H2 can call a Java method from SQL, so
`CREATE ALIAS SLEEP FOR "java.lang.Thread.sleep(long)"` (the signature must be spelled out: on
JDK 25 `Thread` has several `sleep` overloads, and H2 refuses an ambiguous alias) and `SELECT * FROM item WHERE SLEEP(1100) IS NULL
AND name = :name` take just over the 1000 ms threshold. The thresholds stay hard-coded; making them
configurable only for a test is not worth a new setting.

**Known limit.** The rule covers what dbutils itself writes. When a statement fails, the driver's
exception message travels on as the cause of the `SqlException`, and some drivers put values in it
(MariaDB: `Duplicate entry 'jane@example.org' for key 'email'`). Whoever logs that exception logs
the value. dbutils cannot rewrite driver messages without losing the information they carry, so
this stays as it is and is mentioned in the README.

The slow and very-slow lines now have the SQL and the timing on one line. Today they are on two
lines at different levels, so the SQL of a very slow query is lost wherever INFO is filtered out.

Rejected alternatives:

- **Commit the uncommitted change as is.** That puts personal data into production logs.
- **Drop the slow-query details entirely.** Names alone make most slow queries reproducible, and
  values are still available when deliberately enabled.
- **Mask values** (`jane@…`). Which values are sensitive depends on the column, and the library
  cannot know. Partial masking still leaks, and it gives a false sense of safety.

### D6. The query builder binds every value

The no-values-in-logs rule depends on values never being written into the SQL text itself.
`MysqlTableReferenceSimple.value(x)` already returns a named parameter (`:value_1`), and
`SimpleQuery` takes a parameter map. The only things written into the SQL text are:

- identifiers: table names, column names, aliases, `ORDER BY` field names;
- the two integers of `LIMIT offset,max` (`MysqlSelect.java:204–206`).

That stays the rule. A test builds a select with string, date and number conditions, an `IN` list,
a join and a limit, and asserts that none of the values appears in `getSql()`, and that all of them
appear in `getParams()`. No product code changes for D6; the test pins existing behaviour down.

The guarantee is for the builder only. `SimpleQuery` and the `find(String sql, Map params)` methods
take SQL that the caller writes, and dbutils cannot stop a caller from writing
`"… WHERE email = '" + email + "'"`. The README says so next to the logging section.

### D7. Query metrics through the Micrometer Observation API

Today:

- `AbstractMappingRepository.find` records a Micrometer timer named `dbutil.sql.find`, tagged
  `object=<entity simple name>`.
- It does so through a `MicrometerAdapter` interface that is `@Autowired(required = false)`.
- The only implementation, `MicrometerAdapterImpl`, is a `@Configuration` guarded by
  `@ConditionalOnClass(MeterRegistry.class)` and `@ConditionalOnProperty("dbutil.micrometer.enabled")`.
  These are Boot auto-configuration annotations. The class also only takes effect if the
  application happens to component-scan `de.disk0.dbutil.impl.micrometer`.

The *Observation API* (`io.micrometer.observation`) is what Spring Framework itself instruments with
since 6.0. Code describes *what* happens ("a query for UserEntity") once. Whatever handlers the
application has registered turn that into a timer, a tracing span, or nothing. Boot auto-configures
an `ObservationRegistry` with a metrics handler whenever Actuator is present, on both 3.5 and 4.

New:

```java
private ObservationRegistry observationRegistry = ObservationRegistry.NOOP;

@Autowired(required = false)   // Spring calls this only if a registry bean exists
public void setObservationRegistry(ObservationRegistry observationRegistry) {
    this.observationRegistry = observationRegistry;
}

public List<T> find(String sql, Map<String,Object> params) throws SqlException {
    return Observation.createNotStarted("dbutil.sql.find", observationRegistry)
        .lowCardinalityKeyValue("object", getClazz().getSimpleName())
        .observeChecked(() -> doFind(sql, params));   // doFind = today's find body
}
```

A setter rather than an autowired field, for the same reason `setDataSource` exists: the tests (and
anyone using dbutils without a Spring context) create repositories with `new` and wire them by
hand. `observeChecked` rethrows the `SqlException` unchanged, so callers and the cause chain that
`MapNullTest` inspects see exactly what they see today.

**How it is tested.** `TestObservationRegistry` from `io.micrometer:micrometer-observation-test`
(test scope, BOM-managed) is a registry that remembers every observation. The test passes it to
`setObservationRegistry`, runs one `find`, and asserts one observation named `dbutil.sql.find` with
`object=<entity simple name>`. A second test leaves the setter alone and checks that `find` works.

- The resulting timer keeps the name `dbutil.sql.find` and the tag `object`, so existing dashboards
  keep working.
- Micrometer adds an `error` tag, and a long-task timer `dbutil.sql.find.active` for queries still
  running.
- If tracing is configured, each query also becomes a span.
- The dependency is `io.micrometer:micrometer-observation`: small, BOM-managed, and not optional.
  It replaces the optional `micrometer-core`.
- The `micrometer` package (`MicrometerAdapter`, `MicrometerAdapterImpl`) is deleted.

**Behaviour change:** the observation runs whenever the application context has an
`ObservationRegistry`. The `dbutil.micrometer.enabled` property no longer exists. An application
that does not want it registers an `ObservationPredicate` that rejects names starting with
`dbutil.`.

Rejected alternatives:

- **Remove metrics entirely.** It is the only per-entity query timing the platform has, and
  keeping it now costs about ten lines.
- **Keep the `MeterRegistry` adapter, without the Boot conditions.** It would still be
  metrics-only, and it would still depend on the consumer scanning a package or importing a config
  class. The Observation API solves both and is the current Spring/Micrometer recommendation.

### D8. Smaller items

| Item | Today | New |
|---|---|---|
| `config/ReaderConfig` | Boot `DataSourceProperties` import; never loaded by the platform; its `static @Bean` methods call each other directly, so `spring.reader.*` never binds | deleted; `dbutils-examples` does not use it (checked) |
| Entity type resolution | `getGenericSuperclass()` cast (see Context, step 1) | Spring's `ResolvableType`, see below the table; unresolvable → `IllegalStateException("cannot resolve entity type of " + getClass())` |
| Instantiation | `Class.newInstance()` in `AbstractMappingRepository:106`, `SimpleRowMapper:37`, `AbstractGuidRepositoryV2:40` | `getDeclaredConstructor().newInstance()` (the fourth occurrence goes away with `AbstractGuidRepository`) |
| `@Nullable` | `org.springframework.lang.Nullable` on four methods of `PersistenceApiUtils`, deprecated in Spring 7; the only use in the repo | dropped; the "returns `null` when …" is stated in the method comment. Rejected: `org.jspecify:jspecify`, a new dependency for four annotations on an internal helper that no null-checker in this build reads |
| `StringUtils.isEmpty(t.getId())` in `AbstractGuidRepositoryV2.save` | deprecated since Spring 5.3 | `!StringUtils.hasLength(t.getId())`, same meaning for a `String` |
| Boot dependency | `dbutil-impl` depends on `spring-boot-autoconfigure` | removed. After D7 and `ReaderConfig`, library code imports nothing from `org.springframework.boot` |
| Tests | JUnit 4, dbcp2 pool | JUnit 5 and `SimpleDriverDataSource`, see D1 "Test setup" |
| `dbutil-schema-check` | Jackson 2 `ObjectMapper` | unchanged; it only has to keep building on both BOMs. If the Boot 4 BOM no longer manages Jackson 2, import `com.fasterxml.jackson:jackson-bom` 2.x in that module |

**Entity type resolution, in detail.** `ResolvableType` is Spring's description of a generic type
together with the concrete type arguments it was given somewhere up the class hierarchy. The new
`getClazz()`:

```java
ResolvableType rt = ResolvableType.forClass(getClass()).as(AbstractMappingRepository.class);
if (rt.hasUnresolvableGenerics() || rt.resolveGeneric(0) == null)
    throw new IllegalStateException("cannot resolve entity type of " + getClass());
clazz = (Class<T>) rt.resolveGeneric(0);
```

`as(AbstractMappingRepository.class)` walks up from the actual class, for example
`ItemRepoDeep → MiddleRepository<Item> → AbstractGuidRepositoryV2<T> → AbstractRepository<T,String>
→ AbstractMappingRepository<T>`, and carries `Item` along for `T`. The same walk works for a CGLIB
proxy, whose superclass is the real repository.

| Repository class | `resolveGeneric(0)` | Result |
|---|---|---|
| `UserRepository extends AbstractGuidRepositoryV2<UserEntity>` | `UserEntity` | `UserEntity` |
| `ItemRepoDeep extends MiddleRepository<Item>` | `Item` | `Item` |
| `RawRepository extends AbstractGuidRepositoryV2` (raw, no type argument) | `hasUnresolvableGenerics()` is true | `IllegalStateException` naming `RawRepository` |

Rejected alternative: **`GenericTypeResolver.resolveTypeArgument(getClass(),
AbstractMappingRepository.class)`**, which this design originally named. For the raw
`RawRepository` it does not return `null`: it falls back to the *bound* of the type variable,
`BaseGuidEntityV2` (from `AbstractGuidRepositoryV2<T extends BaseGuidEntityV2>`). The repository
then tries to map `BaseGuidEntityV2`, which has no `@Table`, and fails later and less clearly with
`InvalidParameterException("entity … has no table")` instead of the `IllegalStateException` the
spec requires. This was found while implementing (unit D).

### D9. An empty `IN` list stays an SQL error

Found while testing the snapshot in the platform: when an ACL-filtered select gets an empty list
(an API-key user with no principals), the query builder renders `… WHERE id IN () LIMIT 0,1`. MariaDB
rejects that as a syntax error, and the repository throws `SqlException`. 0.2.14 behaves the same.

**This stays as it is (user decision, 2026-11-16): an empty `IN` is a mistake in the calling code,
and failing loudly is the right outcome.** The caller has to decide what an empty set means, for
example "no access", before it builds the query.

Rejected alternative: **render an empty `IN` as `1=0`** (and an empty `NOT IN` as `1=1`). That makes
the query legal and returns no rows, but it silently turns a caller's bug into "nothing found", and
for `NOT IN` into "everything found", which for an ACL filter would be the dangerous direction.

## Risks

- **BLOB (D4).** The value of a binary column could come back different, for example `null` vs.
  `byte[0]`. Mitigation: an integration test on MariaDB with `NULL`, empty and binary values, run on
  driver 2.6.2 and on the 3.x version the Boot 3.5 BOM manages.
- **Metrics on by default (D7).** Services with Actuator start emitting `dbutil.sql.find` without
  having asked. The cardinality is bounded (one series per entity class, about 70), so the cost is
  small. It is still a visible change, and it is listed in the README.
- **Log format (D5).** Anything that greps for the old two-line `VERY SLOW` output, such as an
  alert, needs adjusting.
- **Java 25 floor (D1) and V1 removal (D2)** break any consumer outside the platform. Accepted: no
  such consumer is known, and 0.2.x remains available.
- **JUnit 6 on the Boot 4 BOM (D1).** Boot 4 manages JUnit 6. The test code is the same Jupiter
  API, but an old surefire may not launch it. Mitigation: surefire is pinned to a current release
  in `pluginManagement`; if the Boot 4 run reports zero tests, raise that pin first.
- **A logging backend sneaking onto the test classpath (D5).** If a test dependency brings
  `slf4j-api` without a binding, commons-logging sends everything to SLF4J's no-op logger and the
  logging test sees nothing. Mitigation: after adding the test dependencies, check
  `mvn -pl dbutil-impl dependency:tree -Dscope=test` for `slf4j`/`log4j`/`logback`; if one shows,
  add `org.slf4j:slf4j-jdk14` (test scope) so output still ends up in `java.util.logging`.

## Migration

1. Release 0.3.0.
2. In the platform's `boot-3-5-baseline` change, bump `dbutil.version` 0.2.14 → 0.3.0. This
   requires the platform build and runtime to be on JDK 25 already (D1), so the bump lands together
   with the JDK 25 switch, not before it. Check first:
   - **The `mariadb-java-client` 2.6.2 pin.** D4 removes dbutils' dependency on driver classes,
     but the pin may exist for another reason. A likely one: driver 3.x rejects `jdbc:mysql:`
     connection URLs unless `permitMysqlScheme` is set. Drop the pin only after the services start
     against 3.x.
   - **`dbutil.micrometer.enabled`.** If it is set anywhere, remove it. If metrics are unwanted,
     add the `ObservationPredicate` from D7.
   - **Alerts or log searches** on `VERY SLOW`.
3. Nothing further for Boot 4.
