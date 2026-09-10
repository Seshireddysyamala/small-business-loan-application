# Database Files

The normal application schema is managed by the versioned Flyway scripts under `src/main/resources/db/migration`.

`workbench-schema-update.sql` is only a manual compatibility helper for an older MySQL schema that already existed in Workbench. Do not run it for a fresh database; start the backend and let Flyway create the current schema.
