# Tasks: dbutils 0.3.0 (JDK 25 / Spring Boot 4)

> **For the implementing agents.** Read `proposal.md` and `design.md` first; decision numbers
> (D1…D8) refer to `design.md`. Each task names the unit it belongs to, e.g. `[A1]`.
>
> - **Do not commit at all**, unless a task's text explicitly says to. The orchestrator / the user
>   decides on commits. Leave your changes in the working tree and report the files you touched.
> - The working tree has an **uncommitted** slow-query change in
>   `dbutil-impl/src/main/java/de/disk0/dbutil/impl/AbstractMappingRepository.java` that logs
>   parameter values. Never commit it. Unit D discards it with `git checkout` as its first step
>   (task 3.0); no other unit touches that file.
> - Do not run `release.sh` and do not push. Unit A2 stops after group 9 and reports back.
> - `.claude/` and `openspec/` in the working tree are not yours to commit or edit (apart from
>   ticking boxes in this file, if the orchestrator asks).
> - JDKs on this machine: 25 (default) and 21 under `/usr/lib/jvm`. Maven 3.9.
> - MariaDB (only needed by unit C and A2, task 3.4 / 7.3): start a throwaway container as shown in
>   design D4 (`docker run … mariadb:11` on port 33306). If Docker is unavailable, use the
>   platform's dev stack at `localhost:3306`; credentials are in
>   `…/automation/all/development/automation.env`. Use your own scratch schema; never touch the
>   `mc_*` / `cvm_*` schemas.
> - Every unit ends with `mvn verify` (JDK 25, default BOM) green from the repo root, unless the
>   task says otherwise.

## Work units

Tasks touching the same file are in the same unit. **All pom edits belong to unit A** (A1 at the
start, A2 at the end; see design D1 "Order of the pom edits"). **All edits to
`AbstractMappingRepository.java`** (and to `AbstractRepository`, `AbstractGuidRepositoryV2`,
`SimpleRowMapper`) **belong to unit D**, in the order its tasks are listed.

| Unit | Tasks | Files | Depends on | Model |
|---|---|---|---|---|
| A1 Build foundation | 1.1–1.5 | `pom.xml`, `dbutil-api/pom.xml`, `dbutil-impl/pom.xml`, `dbutils-examples/pom.xml`; `dbutil-impl/src/test/java/de/disk0/dbutil/impl/mysql/*Test.java`, `…/mysql/TestBase.java`, `…/uuids/UuidTests.java` | – | sonnet |
| B jakarta only | 2.1–2.4, 4.1 | test entities/repos in `dbutil-impl/src/test/java/de/disk0/dbutil/impl/{pets,hund,katze,uuids,mysql/entities}/` (non-`*Test` files only); `dbutils-examples/src/main/java/de/disk0/dbutil/impl/Example{Entity,Repository}.java`; `dbutil-api/…/entities/Base{Entity,GuidEntity}V1.java` (delete); `dbutil-impl/…/impl/AbstractGuidRepository.java` (delete); `…/impl/util/PersistenceApiUtils.java`; `…/impl/config/ReaderConfig.java` (delete) | A1 | sonnet |
| C BLOB + builder test | 3.3, 3.4, 5.3 | `dbutil-impl/…/impl/util/ParsedEntity.java`; new `dbutil-impl/src/test/java/de/disk0/dbutil/impl/blob/*`; new `dbutil-impl/src/test/java/de/disk0/dbutil/impl/mysql/ValuesAreBoundTest.java` | A1 | sonnet |
| D Repository core | 3.0, 3.1, 3.2, 3.5, 3.6, 6.1, 6.2, 5.1, 5.2 (in this order) | `dbutil-impl/…/impl/AbstractMappingRepository.java`, `AbstractRepository.java`, `AbstractGuidRepositoryV2.java`, `SimpleRowMapper.java`; new `…/impl/util/SqlLog.java`; `…/impl/micrometer/*` (delete); new `dbutil-impl/src/test/java/de/disk0/dbutil/impl/core/*` | A1 | opus |
| E README | 8.1, 8.2 | `README.md` | – (content comes from design.md) | sonnet |
| A2 Finalize | 7.1–7.3, 9.1, 9.2 | all `pom.xml` files (incl. `dbutil-schema-check/pom.xml` if needed) | B, C, D (and E for the report) | sonnet |

`…` stands for `src/main/java/de/disk0/dbutil` inside the module named before it.

**Running B, C, D, E in parallel.** Their file sets are disjoint, so their results merge without
conflicts. They must not share one working tree while running, though: Maven compiles the whole
module, so one unit's half-finished edit breaks another unit's build. To run them concurrently, the
orchestrator gives each unit its own copy of the tree *with A1 applied* (for example a git worktree
on a local WIP commit or a stash the orchestrator makes, or a plain `cp -r`), and copies the touched
files back afterwards. The snapshot must not carry the uncommitted `AbstractMappingRepository.java`
change into a commit: run task 3.0 (discard it) in the main tree before snapshotting, or exclude
that file. In a single shared tree, run the units one after another. E only touches
`README.md` and can always run alongside.

## 1. Build baseline (D1) — unit A1

- [x] 1.1 [A1] Before changing anything: `mvn verify` on JDK 25 from the repo root with the current poms. Record the surefire test count per module (`Tests run: …`) for the report.
- [x] 1.2 [A1] Root `pom.xml`:
  - remove the `spring-boot-starter-parent` 3.0.1 `<parent>`;
  - properties: `springboot.version` = latest 3.5.x (3.5.7 is in the local Maven repository; use a newer 3.5.x if one resolves), `maven.compiler.release` 25, `project.build.sourceEncoding` and `project.reporting.outputEncoding` UTF-8; remove `java.version`;
  - `dependencyManagement` imports `org.springframework.boot:spring-boot-dependencies:${springboot.version}` (`type` pom, `scope` import), exactly as in design D1;
  - `build/pluginManagement` pins `maven-compiler-plugin` (with `<parameters>true</parameters>`), `maven-surefire-plugin` (≥ 3.5.4) and `maven-source-plugin` to current releases; the existing `attach-sources` execution stays;
  - move `jakarta.persistence-api` out of the root `<dependencies>` (see 1.3); leave `javax.persistence-api` and `lombok` there for now (removed in 7.1).
- [x] 1.3 [A1] Module poms — additions only, plus the two swaps that 1.4 needs:
  - `dbutil-api/pom.xml` and `dbutil-impl/pom.xml`: add `jakarta.persistence:jakarta.persistence-api` (compile scope, no version, not optional);
  - `dbutil-impl/pom.xml` and `dbutils-examples/pom.xml`: the `de.disk0.dbutil` inter-module dependency versions become `${project.version}`;
  - `dbutil-impl/pom.xml`: add `io.micrometer:micrometer-observation` (compile); test scope: `io.micrometer:micrometer-observation-test`, `org.junit.jupiter:junit-jupiter`, and `org.mariadb.jdbc:mariadb-java-client` with `<version>${mariadb.test.version}</version>` and the property `mariadb.test.version` = the `mariadb.version` of the chosen 3.5 BOM (3.5.6 for 3.5.7; read it from `~/.m2/repository/org/springframework/boot/spring-boot-dependencies/<v>/*.pom`);
  - `dbutil-impl/pom.xml`: remove `junit:junit` and `commons-dbcp2` (replaced in 1.4). Leave `lombok`, `commons-io`, `micrometer-core`, `spring-boot-autoconfigure` in place (removed in 7.1).
  - Run `mvn -pl dbutil-impl dependency:tree -Dscope=test` and look for `slf4j`, `log4j` or `logback`. If any appears, add `org.slf4j:slf4j-jdk14` (test scope) — see design, Risks.
- [x] 1.4 [A1] Move the tests from JUnit 4 to JUnit 5 (Jupiter), design D1 "Test setup": the ten `*Test` classes in `dbutil-impl/src/test/java/de/disk0/dbutil/impl/mysql/`, `mysql/TestBase.java` and `uuids/UuidTests.java` (`expected =` → `assertThrows`). In `mysql/TestBase.java`, `@Before`/`@After` → `@BeforeEach`/`@AfterEach`, and `BasicDataSource` → `new SimpleDriverDataSource(new org.h2.Driver(), "jdbc:h2:" + db.getAbsolutePath())`. Do not change what the tests assert. `MapNullTest.testNonNullableFields` walks a fixed depth of `getCause()`; leave it as it is.
- [x] 1.5 [A1] `mvn verify` green on JDK 25 with the 3.5 BOM; the test counts equal those of 1.1. Report: the chosen `springboot.version`, `mariadb.test.version`, plugin versions, whether `slf4j-jdk14` was needed.

## 2. jakarta only (D2) — unit B

- [x] 2.1 [B] Move every V1 user in the tests to V2: `Pet`, `Hund`, `Katze`, `BigDecimalEntity` extend `BaseGuidEntityV2`; `PetRepository`, `HundRepository`, `KatzeRepository`, `BigDecimalEntityRepository`, `uuids/PetRepository1`, `PetRepository2`, `BrokenPetRepositoryA`, `BrokenPetRepositoryB` extend `AbstractGuidRepositoryV2` (all under `dbutil-impl/src/test/java/de/disk0/dbutil/impl/`). Do not edit the `*Test`/`*Tests` classes (unit A1 owns them; they need no change for this).
- [x] 2.2 [B] `dbutils-examples/src/main/java/de/disk0/dbutil/impl/ExampleEntity.java` → `BaseGuidEntityV2`, `ExampleRepository.java` → `AbstractGuidRepositoryV2`.
- [x] 2.3 [B] Delete `dbutil-api/src/main/java/de/disk0/dbutil/api/entities/BaseEntityV1.java`, `BaseGuidEntityV1.java` and `dbutil-impl/src/main/java/de/disk0/dbutil/impl/AbstractGuidRepository.java`.
- [x] 2.4 [B] Rewrite `dbutil-impl/src/main/java/de/disk0/dbutil/impl/util/PersistenceApiUtils.java` as in design D2: `jakarta.persistence.Column` / `Table` / `NonUniqueResultException` used directly; no `Class.forName`, no reflective attribute reads, no `lombok`, no `@Nullable` (say "returns null when …" in a comment instead, design D8), no delombok header. Public methods and the nested `Column` / `Table` classes keep their signatures.

## 3. Mapping fixes (D3, D4, D8) — units C and D

- [x] 3.0 [D] Discard the uncommitted change: `git checkout -- dbutil-impl/src/main/java/de/disk0/dbutil/impl/AbstractMappingRepository.java`. (It is replaced by 5.1; it must never be committed.)
- [x] 3.1 [D] D3 in `AbstractMappingRepository.mapRow`: delete the `applicableColumns` field and its `synchronized` block; read the column labels (upper-cased) from `rs.getMetaData()` into a local `Set<String>` on every call. The missing-column WARN stays as it is.
- [x] 3.2 [D] Test `dbutil-impl/src/test/java/de/disk0/dbutil/impl/core/ColumnSetTest.java` (extends `mysql.TestBase`, H2). Unit D uses its own test entity and repository in package `core` (e.g. `Item extends BaseGuidEntityV2` with table `item`, columns `name`, `size`; `ItemRepository extends AbstractGuidRepositoryV2<Item>`), not the shared `Pet`/`Hund` classes. On one repository instance: `SELECT id, name` then `SELECT *` → the second maps `size`; and in the reverse order → the projection maps without exception, `size` unset.
- [x] 3.3 [C] D4 in `dbutil-impl/src/main/java/de/disk0/dbutil/impl/util/ParsedEntity.java`: replace the `org.mariadb.jdbc.MariaDbBlob` reflection with the `java.sql.Blob` branch from design D4 (no `IOUtils`, no `Class.forName`, no `Method`); drop the value from the `mapping column:` DEBUG line (design D5). Remove the now unused imports. If a driver rejects `getBytes(1, 0)` on an empty BLOB (found by 3.4), return `new byte[0]` when `blob.length() == 0`.
- [x] 3.4 [C] `dbutil-impl/src/test/java/de/disk0/dbutil/impl/blob/MariaDbBlobTest.java`, with its own `BlobEntity` (`byte[] data` mapped to a `LONGBLOB` column) and `BlobEntityRepository` in the same package. Skip via `Assumptions.assumeTrue` when the system property `dbutil.it.mariadb.url` is unset; otherwise build a `SimpleDriverDataSource` from it, create the table, save and read back rows with `NULL`, empty and 1 KiB of random bytes, and expect `null`, `byte[0]` and equal content. Run it as shown in design D4, once with the default `mariadb.test.version` and once with `-Dmariadb.test.version=2.6.2`, and record both results. Without the property, `mvn verify` must stay green (test reported as skipped).
- [x] 3.5 [D] `AbstractMappingRepository.getClazz()`: `ResolvableType.forClass(getClass()).as(AbstractMappingRepository.class)`; if `hasUnresolvableGenerics()` or `resolveGeneric(0) == null` → `IllegalStateException("cannot resolve entity type of " + getClass())` (not `GenericTypeResolver`: for a raw subclass it returns the bound `BaseGuidEntityV2`, design D8). Test `core/EntityTypeTest.java`: a three-level hierarchy (`ItemRepoDeep extends MiddleRepository<Item>`, `MiddleRepository<T extends BaseGuidEntityV2> extends AbstractGuidRepositoryV2<T>`, test classes in `core`) saves and reads an `Item`; for a raw `class RawRepository extends AbstractGuidRepositoryV2`, `new RawRepository().getParsedEntity()` throws an `IllegalStateException` whose message names `RawRepository`.
- [x] 3.6 [D] Replace `Class.newInstance()` with `getDeclaredConstructor().newInstance()` in `AbstractMappingRepository.mapRow`, `SimpleRowMapper.mapRow` and `AbstractGuidRepositoryV2.generateId`. In `AbstractGuidRepositoryV2.save`, `StringUtils.isEmpty(t.getId())` → `!StringUtils.hasLength(t.getId())`.

## 4. Remove Boot internals (D8) — unit B

- [x] 4.1 [B] Delete `dbutil-impl/src/main/java/de/disk0/dbutil/impl/config/ReaderConfig.java` (and the then-empty `config` directory). The `spring-boot-autoconfigure` dependency is removed later, in 7.1.

## 5. Logging (D5, D6) — units D and C

- [x] 5.1 [D] Create `dbutil-impl/src/main/java/de/disk0/dbutil/impl/util/SqlLog.java` as in design D5. Apply the D5 table to every listed statement in `AbstractMappingRepository` (`find`/`doFind`, `findOne`, `update`, `delete`), `AbstractRepository` (`get`, `delete`) and `AbstractGuidRepositoryV2` (`save`): class loggers get SQL, duration, row count, entity and `SqlLog.names(…)`; each such statement also calls `SqlLog.values(<entity simple name>, params)`. Slow/very-slow become one line each in the format of the table. The failed-update message says "update failed". Do not change exception types, messages passed to `SqlException`, or wrapping depth.
- [x] 5.2 [D] Test `core/QueryLoggingTest.java` (H2): attach a `java.util.logging.Handler` to `de.disk0.dbutil.impl` and `de.disk0.dbutil.sql.params` (design D5 "How it is tested"); restore levels/handlers afterwards. Cases, each with a distinctive value such as `jane@example.org`: (a) a very slow `find` via the H2 `SLEEP` alias → one WARN record containing the SQL, `ms`, the row count and `[name]`, and no record anywhere containing the value; (b) a failing `find` (bad SQL) and (c) a failing `save` (duplicate id, or `NOT NULL` violation) → WARN with SQL and names, no value in any record's message (exception causes from the driver are out of scope, design D5 "Known limit"); (d) with `de.disk0.dbutil.impl` at FINE (DEBUG), a normal `find` → no value in any record; (e) with `de.disk0.dbutil.sql.params` at FINEST (TRACE) → the value appears on that logger.
- [x] 5.3 [C] D6 test `dbutil-impl/src/test/java/de/disk0/dbutil/impl/mysql/ValuesAreBoundTest.java` (no database): build a select with `MysqlStatementBuilder` with a string, a `java.util.Date`, a number and an `IN` condition (look at `InTest` for the `IN` API), a join with a value in its `ON`, and `limit(…)`. Assert that no value's string form appears in `getSql()` and that every value is among `getParams().values()`. No product code change; if a value does appear in the SQL, stop and report it.

## 6. Metrics (D7) — unit D

- [x] 6.1 [D] In `AbstractMappingRepository`: replace the `MicrometerAdapter` field and the timer code in `find` with the `observationRegistry` field, the `@Autowired(required = false) setObservationRegistry` setter and the `Observation…observeChecked(() -> doFind(sql, params))` call from design D7; today's body of `find(String, Map)` becomes `private List<T> doFind(…)`. Delete `dbutil-impl/src/main/java/de/disk0/dbutil/impl/micrometer/MicrometerAdapter.java` and `MicrometerAdapterImpl.java`.
- [x] 6.2 [D] Test `core/ObservationTest.java` (H2): with a `TestObservationRegistry` set via `setObservationRegistry`, one `find` → exactly one observation `dbutil.sql.find` with low-cardinality `object=Item` (`TestObservationRegistryAssert`); without calling the setter, `find` returns the rows; a failing `find` with the registry set still throws the same `SqlException` as without.

## 7. Finalize the poms and verify both lines — unit A2

- [x] 7.1 [A2] Remove what is no longer used: `javax.persistence-api` and `lombok` from the root `pom.xml`; `lombok` from `dbutil-api/pom.xml` and `dbutil-impl/pom.xml`; `commons-io`, `micrometer-core` and `spring-boot-autoconfigure` from `dbutil-impl/pom.xml`.
- [x] 7.2 [A2] Checks, each must print nothing:
  - `grep -rnE 'javax\.persistence|lombok' --include=*.java --include=pom.xml dbutil-api dbutil-impl dbutils-examples dbutil-schema-check pom.xml`
  - `grep -rn 'org.springframework.boot' --include=*.java dbutil-api dbutil-impl/src/main`
  - `grep -rn 'org\.mariadb' --include=*.java dbutil-impl/src/main`
  - `grep -rn '\.newInstance()' --include=*.java dbutil-impl/src/main | grep -v getDeclaredConstructor`
  - `mvn -pl dbutil-api,dbutil-impl dependency:tree -Dscope=runtime | grep 'org.springframework.boot:'`
- [x] 7.3 [A2] On JDK 25: `mvn clean verify` green with the default BOM **and** with `-Dspringboot.version=<latest 4.0.x>`, same test counts on both. That includes `dbutil-schema-check`: if the Boot 4 BOM no longer manages Jackson 2, import `com.fasterxml.jackson:jackson-bom` 2.x in `dbutil-schema-check/pom.xml` only (design D8). If the Boot 4 run reports zero tests, raise the surefire pin (design, Risks). Then run `MariaDbBlobTest` once more on the final tree with the default driver and with 2.6.2 (design D4).

## 8. Docs — unit E

- [x] 8.1 [E] `README.md`:
  - "compatibility": Spring Boot 3.5 and 4.0, Java 25 or later, `jakarta.persistence` only; 0.2.x for older setups;
  - "dependencies": `jakarta.persistence-api` now comes with dbutils; delete the "pick your poison" paragraph and the wrong `2.2` snippet; the dbutil-api / dbutil-impl snippets show `<version>0.3.0</version>`;
  - every example uses `BaseGuidEntityV2` / `AbstractGuidRepositoryV2` (today: `BaseGuidEntityV1`, `AbstractGuidRepository`); the intro sentence no longer mentions `javax.persistence`;
  - new "Logging" section: what is logged at which level (design D5 table, condensed), the `de.disk0.dbutil.sql.params` TRACE switch with a warning that it logs personal data, the driver-exception limit, and that hand-written SQL (`SimpleQuery`, `find(sql, params)`) must use `:named` parameters rather than concatenated values;
  - new "Metrics" section: the `dbutil.sql.find` observation and its `object` key, that it appears whenever an `ObservationRegistry` exists, `setObservationRegistry` for hand-wired repositories, and how to switch it off with an `ObservationPredicate` (design D7);
  - new "Migrating from 0.2.x": V1 classes removed, Java 25, `dbutil.micrometer.enabled` removed, `ReaderConfig` removed, log format changed (old two-line `VERY SLOW` output).
- [x] 8.2 [E] A short "Build" section in the README: `mvn verify` builds against Boot 3.5; `mvn -Dspringboot.version=<4.0.x> verify` against Boot 4; how to run the MariaDB BLOB test (design D4).

## 9. Prepare the release (do not run it) — unit A2

- [x] 9.1 [A2] `mvn versions:set -DnewVersion=0.3.0-SNAPSHOT -DgenerateBackups=false -DprocessAllModules=true` so that `release.sh` (which strips `-SNAPSHOT`) produces **0.3.0**. Check that every module pom's `<parent><version>` reads `0.3.0-SNAPSHOT` and no `0.2.15-SNAPSHOT` is left (`grep -rn 0.2.15 --include=pom.xml .`).
- [x] 9.2 [A2] Report back (no commit):
  - test counts on both BOMs (and the 1.1 baseline);
  - the BLOB results on both drivers;
  - the versions chosen (Boot 3.5.x, Boot 4.0.x, plugins, `mariadb.test.version`);
  - any deviation from design.md, and anything left open.

## 10. After release (platform repo, not these agents)

- [ ] 10.1 Platform `boot-3-5-baseline`: `dbutil.version` 0.2.14 → 0.3.0, after the three checks in design.md "Migration" (driver pin, `dbutil.micrometer.enabled`, log alerts).
