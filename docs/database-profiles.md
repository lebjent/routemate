# MySQL and Oracle development

RouteMate runs against one database per application process. Select `mysql` or
`oracle` with `SPRING_PROFILES_ACTIVE`; do not enable both profiles together.

## New database

Flyway creates the schema before JPA starts. JPA then uses `validate`, so an
entity/schema mismatch stops startup instead of changing production tables.

### macOS and Linux

```bash
# MySQL
SPRING_PROFILES_ACTIVE=mysql \
SPRING_DATASOURCE_USERNAME=routemate \
SPRING_DATASOURCE_PASSWORD=change-me \
./gradlew bootRun

# Oracle 12c or newer
SPRING_PROFILES_ACTIVE=oracle \
SPRING_DATASOURCE_URL='jdbc:oracle:thin:@localhost:1521/ORCL' \
SPRING_DATASOURCE_USERNAME=routemate \
SPRING_DATASOURCE_PASSWORD=change-me \
./gradlew bootRun
```

### Windows PowerShell

```powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'
$env:SPRING_DATASOURCE_USERNAME = 'routemate'
$env:SPRING_DATASOURCE_PASSWORD = 'change-me'
.\gradlew.bat bootRun
```

Use the same PowerShell commands with `oracle` and an Oracle JDBC URL when
switching databases.

## Existing database

`V1__initial_schema.sql` is only for a new, empty schema. Do not point it at a
database created by Hibernate `ddl-auto=update` without preparation.

1. Back up the database.
2. Compare its tables and constraints with the matching `V1` migration.
3. Run a staging copy with `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` and
   `SPRING_FLYWAY_BASELINE_VERSION=1`.
4. Confirm JPA `validate` starts successfully.
5. Repeat the same process for production during a maintenance window.

## Seed data

Sample countries, regions, destinations, and travel plans are disabled by
default. They are never inserted into a normal application startup.

```bash
APP_SEED_ENABLED=true SPRING_PROFILES_ACTIVE=mysql ./gradlew bootRun
```

Use seed data only for an empty local database. Production country/region data
should be added through versioned Flyway migrations or an administrative import
process.

## Schema change rules

- Add matching versions under `db/migration/mysql` and `db/migration/oracle`.
- Do not alter migrations already applied to a shared database.
- Keep application queries in JPQL/Spring Data. Put unavoidable vendor SQL in
  separately named database-specific repository implementations.
- Oracle support requires 12c or newer because numeric identifiers use identity
  columns.
