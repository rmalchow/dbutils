## ADDED Requirements

### Requirement: One release spans the supported framework lines
Each release SHALL build, pass its tests and run unchanged against every Spring Boot line listed as supported in the README, on Java 25. The Spring Boot line SHALL be selectable at build time through the `springboot.version` property.

#### Scenario: Supported lines
- **WHEN** the build runs with `springboot.version` set to the latest release of any supported line
- **THEN** compilation and all tests succeed

#### Scenario: Java 25
- **WHEN** the build and tests run on JDK 25
- **THEN** they succeed without additional JVM flags such as `--add-opens`

### Requirement: No framework auto-configuration or driver internals
Library code SHALL NOT reference classes from Spring Boot (`org.springframework.boot..`) or classes of a specific JDBC driver, and the published `dbutil-api` and `dbutil-impl` artifacts SHALL NOT depend on Spring Boot. Result data SHALL be accessed only through the JDBC API.

#### Scenario: Plain Spring application
- **WHEN** dbutils is used without Spring Boot on the classpath
- **THEN** repositories load, query and save entities

### Requirement: Persistence annotations are a transitive dependency
`dbutil-api` and `dbutil-impl` SHALL declare `jakarta.persistence-api` as a compile-scope, non-optional dependency.

#### Scenario: Consumer without its own JPA dependency
- **WHEN** a project depends only on `dbutil-impl` and declares an entity with `jakarta.persistence` annotations
- **THEN** the project compiles
