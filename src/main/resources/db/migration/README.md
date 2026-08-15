# RouteMate database migrations

Flyway is the single source of truth for schema changes.

Each database has its own migration directory because identity columns,
timestamp defaults, CLOBs, and index syntax differ between MySQL and Oracle.

## Rules

1. Add matching migration versions to `mysql` and `oracle` when a schema
   change is database-specific.
2. Put only database-neutral data migrations in `common`.
3. Never edit a migration that has already been applied to a shared database.
4. Use a new migration version for every schema change.

## Existing databases

The initial `V1` migration is for a new, empty schema. Before enabling Flyway
on an existing database, back it up and compare it with the matching `V1`
migration. Baseline it only after confirming the schema is equivalent.

```bash
# Example: mark an already-equivalent schema as version 1 at application startup.
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true \
SPRING_FLYWAY_BASELINE_VERSION=1 \
SPRING_PROFILES_ACTIVE=mysql \
./gradlew bootRun
```

Oracle support requires Oracle Database 12c or newer because RouteMate uses
identity columns for its numeric primary keys.
