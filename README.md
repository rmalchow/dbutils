# dbutils

this is an attempt at a object-relational mapper and query builder that uses JPA annotations without the need of any compile time shenanigans and with mostly reasonable defaults.

i have been using (and improving) it for quite a while now, and found it useful in many cases. if you use it, and find it useful too, that's even better.



### why bother?

i started this mostly out of lazyness. i didn't want the complexity of full blown JPA criteria, and i also wanted less runtim magic than what spring data repositories do. for me, this library is a good balance between simplicity and minimizing boilerplate code. basically, once the entity is defined, the only thing i have to deal with the code to find specific entries (more on the pattern i use below).

this uses `java.sql.DataSource` and `NamedParameterJdbcTemplate` from spring-jdbc, the annotations from `jakarta.persistence`, and plain reflection to do the actual mapping. it can currently map most standard field types (including enums).

it also offers pretty extensive and direct control over the SQL that is produced.



### thanks

big thanks to everyone who has contributed to this, especially @mdjimy



### compatibility

Spring Boot 3.5 and 4.0, Java 25 or later, `jakarta.persistence` only. [0.3.0 and later]

if you are on older setups (Java 8 - 17, Spring Boot 2.x, `javax.persistence`), stay on 0.2.x. see "migrating from 0.2.x" below.


### dependencies

this has very few dependencies. `jakarta.persistence-api` now comes with dbutils, so there is nothing to pick anymore.

in places where you want to do annotations or extend some of the base classes, but not actually pull in any implementations, you can do:

```xml
<dependency>
    <groupId>de.disk0.dbutil</groupId>
    <artifactId>dbutil-api</artifactId>
    <version>0.3.0</version>
</dependency>
```

and then, where you actually need the statement builder implementation:

```xml
<dependency>
    <groupId>de.disk0.dbutil</groupId>
    <artifactId>dbutil-impl</artifactId>
    <version>0.3.0</version>
</dependency>
```


---

don't forget to also pull in your JDBC and configure springboot to make a DataSource available

---



# Components



### basic mapping

for a basic entity, extend `BaseGuidEntityV2` and annotate your class with `@Table` and `@Column` annotations:

```java
@Table(name="foo")
public class Foo extends BaseGuidEntityV2 {

	/* no ID field, because the superclass has one already */
	
	/** no name in annotation => uses name of the field  **/
	@Column 
	private Date created;
	
	/** fieldname in db is different from fieldname in class  **/
	@Column(name="last_modified") 
	private Date lastModified;
	
	/** no annotation = no mapping **/
	private boolean bar;

	/**
	any getters and setters you want. dbutils modifies the fields directly 
	through reflection, so this can really be anything
	**/
	
}
```



### repository

With a basic entity like this, you can extend `AbstractGuidRepositoryV2` to have basic functionality:

```java

@Repository // add spring annotations if you want
public FooRepository extends AbstractGuidRepositoryV2<Foo> {

}
```

if you're using this from within springboot, this already has an `@Autowired` `DataSource` , otherwise you may have to instantiate a DataSource and wire it yourself.

this comes with basic functionality, such as:

```java
public T get(String id) 
public T save(T object)
public T save(T object, String id) // saves under the given ID instead of generating one
public void delete(String id)
public void delete(T object)
```

The <T> here is your class, in this case "Foo", and the save method automatically determines if it is a new object (id is null) or an update to an existing one (id != null). this also comes with a bunch of hooks you can override:

```java
public void beforeDelete(T object)
public void afterDelete(T object)
public void beforeSave(T object)
public void afterSave(T object)
```

as well as the actual mapping- and unmapping methods:

```
public MapSqlParameterSource unmap(T object)
public T mapRow(ResultSet rs,int count)
```

these you can override to modify the objects before saving or after loading, e.g. to load child objects or encrypt / decrypt fields.

since this repository has no real idea of the object it is dealing with, listing functions are not included, except this:

```java
public List<T> find(String query, Map<String,Object> params)
```

this builds a `NamedParameterJdbcTemplate` from the given String with the given params, e.g:

```java

Map<String,Object> params = new HashMap<>();
params.put("updated", new Date());

List<Foo> foos = r.find("SELECT FROM foo WHERE update < :updated", params);

```



### simplequery

if you find yourself in a situation where you just want a simple SQL statement with one or two params, an you're too lazy to use the full query builder (below), you can use the "SimpleQuery" class:

```java
SimpleQery sq = new SimpleQuery("SELECT * FROM x WHERE foo = :foo"); // placeholder 'foo'
sq.puf("foo", "bar"); // provider a value for the placeholder
return r.find(sq.getQuery(),sq.getParams());
```

how is that for an SQL-injection-proof load in three lines? 



### querybuilder

#### SELECT

if you want to build simple queries, you can write them by hand, or possible use the `SimpleQuery` class, which basically removes the need to instantiate a separate map. the example above would be:

```java
SimpleQuery sq = new SimpleQuery("SELECT FROM foo WHERE update < :updated");
sq.put("updated", new Date());

r.find(sq.getSql(), sq.getParams())

OR

r.find(sq)
```

for anything more complicated, you can use the query builder. this is currently only implemented for MySQL, but it is pretty much standard SQL, and as long as you don't go nuts with it and/or use features that are potentially unsupported, this should be fine.

Here's a basic example (this starts a simple SELECT statement):

```
Select s = new MysqlStatementBuilder().createSelect();
TableReference tr = s.fromTable(Foo.class);

```


at this point:
```
s.getSql() ---> "SELECT * FROM `foo` `foo_1`"
s.getParams() ----> {}
```

#### WHERE

you can now add additional things: 

```java
Select s = new MysqlStatementBuilder().createSelect();
TableReference tr = s.fromTable(Foo.class);
		
Date d = new Date();
if(d!=null) {
	s.condition(Operator.AND, tr.field("updated"), Comparator.EQ, tr.value(d));
}
```

which would yield this:

```
SQL: SELECT * FROM `foo` `foo_1` WHERE `foo_1`.`updated` = :value_1
PARAMS: {value_1=Thu Oct 03 13:14:44 CEST 2019}
```

#### JOIN

or you can do a JOIN, such as this:

```java

Select s = new MysqlStatementBuilder().createSelect();
TableReference tr = s.fromTable(Foo.class);
		
JoinTable jt = tr.join(Bar.class);
		
jt.addOn(Operator.AND, tr.field("id"), Comparator.EQ, jt.field("foo_id"));
```

which leads to this:

```
SQL: SELECT * FROM `foo` `foo_1` JOIN `bar` `bar_1` ON `foo_1`.`id` = `bar_1`.`foo_id`
PARAMS: {}
```

#### CONDITIONS

you can also easily group conditions:

```java
Select s = new MysqlStatementBuilder().createSelect();
TableReference tr = s.fromTable(Foo.class);
	
Condition c1 = s.condition(Operator.AND);
		
c1.condition(Operator.OR, tr.field("id"), Comparator.EQ, tr.value("00000"));
c1.condition(Operator.OR, tr.field("id"), Comparator.EQ, tr.value("00001"));
	
Date tomorrow = DateUtils.addDays(new Date(), 1);
Date yesterday = DateUtils.addDays(new Date(), -1);
		
Condition c2 = s.condition(Operator.AND);
c2.condition(Operator.OR, tr.field("updated"), Comparator.GTE, tr.value(tomorrow));
c2.condition(Operator.OR, tr.field("updated"), Comparator.LTE, tr.value(yesterday));
```

which yields  this result:

```sql
SELECT 
	* 
FROM 
	`foo` `foo_1` 
WHERE 
	(
		( // this block is condition "c1"
			`foo_1`.`id` = :value_1 
			OR 
			`foo_1`.`id` = :value_2
		) 
		AND 
		( // this block is condition "c2"
			`foo_1`.`updated` >= :value_3 
			OR 
			`foo_1`.`updated` <= :value_4
		)
	)
```


### logging

dbutils logs through `commons-logging` (what spring uses anyway), so it ends up wherever your other logs go. the rule is simple: the SQL, the duration, the row count, the entity class and the parameter **names** are logged on the normal loggers (`de.disk0.dbutil.impl.*`). parameter **values** are never logged there.

| what | level | example |
|---|---|---|
| `find` takes more than 1000 ms | WARN | `VERY SLOW: 1834ms, 12 rows, UserEntity: SELECT ... params=[email, unitId]` |
| `find` takes more than 500 ms | INFO | `SLOW: 712ms, 12 rows, UserEntity: SELECT ... params=[email, unitId]` |
| any query fails (`find`, `findOne`, `get`, `update`, `delete`, `save`) | WARN | `query failed: <sql> params=[email]`, with the exception |
| every `find`, `update`, `save`, column mapping | DEBUG | SQL and parameter / column names only |

the thresholds are hard-coded.

if you do want the values (to reproduce a slow query, say), set the logger `de.disk0.dbutil.sql.params` to TRACE. you then get one more line per statement, e.g. `params UserEntity: {email=jane@example.org, unitId=42}`.

**warning: this writes personal data (whatever is in your parameters: emails, names, ids, ...) to your logs. don't leave it on in production, and don't ship those logs anywhere you wouldn't ship the data itself.**

two limits you should know about:

- when a statement fails, the driver's exception message is passed on as the cause of the `SqlException`, and some drivers put values in there (mariadb: `Duplicate entry 'jane@example.org' for key 'email'`). if you log that exception, you log the value. dbutils can't rewrite that without losing information.
- the query builder (below) binds every value as a named parameter, so values never end up in the SQL text. but `SimpleQuery` and `find(sql, params)` take SQL that *you* write. use `:named` parameters there and never concatenate values (`"... WHERE email = '" + email + "'"`) - besides being an injection hole, that puts the value into every log line that contains the SQL.



### metrics

every `find` is wrapped in a micrometer observation named `dbutil.sql.find`, with the low-cardinality key `object` set to the entity's simple class name (e.g. `object=UserEntity`). what that turns into depends on your setup: with spring boot actuator you get a timer `dbutil.sql.find` tagged `object` (plus `error`, and a `dbutil.sql.find.active` long task timer), with tracing configured you get a span per query, without any handlers nothing happens.

the observation is on whenever an `ObservationRegistry` bean exists, there is no property for it. if you create repositories by hand (with `new`, no spring context), give them the registry yourself:

```java
FooRepository r = new FooRepository();
r.setDataSource(dataSource);
r.setObservationRegistry(observationRegistry);
```

without it, `find` works the same and simply doesn't record anything.

to switch it off, register an `ObservationPredicate` that rejects the name:

```java
@Bean
ObservationPredicate noDbutilObservations() {
    return (name, context) -> !name.startsWith("dbutil.");
}
```



### migrating from 0.2.x

- Java 25 or later is required.
- `BaseEntityV1`, `BaseGuidEntityV1` and `AbstractGuidRepository` (the `javax.persistence` ones) are removed. use `BaseGuidEntityV2` and `AbstractGuidRepositoryV2`.
- `javax.persistence` is no longer supported, `jakarta.persistence` only.
- the `dbutil.micrometer.enabled` property is gone; metrics now go through the observation api (see "metrics"). if you had it unset and don't want the metrics now, add the `ObservationPredicate`.
- `ReaderConfig` is removed.
- log format changed: the old two-line `VERY SLOW` output is now one line with timing, rows, entity and parameter names, and parameter values are no longer logged unless you enable `de.disk0.dbutil.sql.params`. check any alerts or log searches that match on the old text.



### build

the root pom imports the spring boot BOM through the property `springboot.version`.

```sh
mvn verify                                  # builds and tests against Spring Boot 3.5
mvn -Dspringboot.version=<4.0.x> verify     # same, against Spring Boot 4
```

there is one test that needs a real MariaDB (the H2 used elsewhere doesn't return `java.sql.Blob`): `MariaDbBlobTest`. it is skipped unless you give it a JDBC url in the system property `dbutil.it.mariadb.url`:

```sh
docker run -d --rm --name dbutil-mariadb -p 33306:3306 \
  -e MARIADB_ROOT_PASSWORD=test -e MARIADB_DATABASE=dbutil mariadb:11
mvn -pl dbutil-impl -am test -Dtest=MariaDbBlobTest -Dsurefire.failIfNoSpecifiedTests=false \
  -Ddbutil.it.mariadb.url='jdbc:mariadb://localhost:33306/dbutil?user=root&password=test'
```

to run it with another driver version (say the old 2.6.2), add `-Dmariadb.test.version=2.6.2`.



# useful patterns (maybe)

i am usually using this for things that involve REST APIs, and for this, i found the following pattern useful:



### Controller 

a spring `@RestController` only serves as HTTP -> Service translation. for listing, i usually have a few common fields (such as "filter" for searching, applied to all fields that make sense, along with "order", "asc", "offset" and "max"). POST / PUT / DELETE and single GET methods are more or less self-explanatory, a list get would usually look something like this:

```java
@RestController
public class FooController {
	
	@Autowired
	private FooService fooService;
	
	@GetMapping(value="/foo")
	public List<Foo> list(
		@RequestParam(required=false) String parentId, 
		@RequestParam(required=false) Date before, 
		@RequestParam(required=false) Date after, 
		@RequestParam(default="name") String order,
		@RequestParam(default="true") boolean asc,
		@RequestParam(default="0") int offset,
		@RequestParam(default="25") int max
        ) throws AuthException, SqlException {
    
    	/**
        1:1 passthrough to service, for more complicated things,
        you could wrap this query in a "FooQuery"
    	**/ 
    	return fooService.list(parentId,....,order,asc,offset,max);
	}
}
```



### Service

deals with any kind of business logic, and especially authorization (if applicable). again, the simple CRUD methods are trivial, listing could be:

```java
@Service
public class FooService {
	@Autowired
	private FooRepository fooRepository;

    @GetMapping(value="/foo")
    public List<Foo> list(
    	String parentId, Date before,Date after, 
    	String order,
        boolean asc,
        int offset,
        int max
        ) throws AuthException, SqlException {

		if(!checkAuthorization()) {
			throw NotAuthorizedException()
		}

		return fooRepository.list(parentId,....,order,asc,offset,max);
    }
}
```


### Repository

this deals with building the appropriate SQL and loading the object. The important distinctions here is that

the `createSelect()` method is separate, so it can very, very easily be unit tested. Also, note how the "updated" criteria changes based on the input:

```java
@Repository
public class FooRepository {
    
	public Select createSelect(String parentId, Date before, Date after, String filter, String order, boolean asc, int offset, int max) {

		Select s = new MysqlStatementBuilder().createSelect();
		TableReference tr = s.fromTable(Foo.class);

		if(parentId!=null) {
			s.condition(
                Operator.AND, 
                tr.field("parent_id"),
                Comparator.EQ,
                tr.value(parentId)
            );
		}
		
		if(before==null && after==null) {
			// nothing
		} else if (before == null) {
			s.condition(
                Operator.AND, 
                tr.field("updated"), 
                Comparator.GTE, tr.value(after)
            );
		} else if (after == null) {
			s.condition(
                Operator.AND, 
                tr.field("updated"), 
                Comparator.LTE, 
                tr.value(before)
            );
		} else if (after.before(before)) {
			// BETWEEN
			s.condition(
                Operator.AND, 
                tr.field("updated"),
                Comparator.GTE, tr.value(after)
            );
			s.condition(
                Operator.AND, 
                tr.field("updated"),
                Comparator.LTE, 
                tr.value(before)
            );
		} else {
			// before a OR after b
			Condition c = s.condition(Operator.AND);
			c.condition(
                Operator.OR, 
                tr.field("updated"),
                Comparator.GTE,
                tr.value(after)
            );
			c.condition(
                Operator.OR, 
                tr.field("updated"),
                Comparator.LTE, 
                tr.value(before)
            );
		}

		s.order(tr.field(order), asc);
		
		s.limit(offset, max);
		
		return s;

	}

	public List<Foo> list(
        String parentId, Date before, Date after, String filter, 
        String order, boolean asc, int offset, int max
    ) throws SqlException {
		Select s = createSelect(parentId,before,after,filter,order,asc,offset,max);
		return find(s.getSql(),s.getParams());
	}
    
}
```


