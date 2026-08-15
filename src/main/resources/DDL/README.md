# DDL source of truth

Database schema changes are managed by Flyway migration files under
`src/main/resources/db/migration`.

- MySQL: `db/migration/mysql`
- Oracle: `db/migration/oracle`
- Database-neutral data migrations: `db/migration/common`

Do not add ad-hoc DDL files in this directory. Add the next versioned Flyway
migration for each supported database instead.
