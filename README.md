# small-business-loan-application

A full-stack loan application system for small and medium-sized businesses built with Java, Spring Boot, and a modern frontend.

## Local database

The backend uses Flyway migrations to create or upgrade the `companies` table. Hibernate validates the migrated schema at startup and does not make untracked schema changes.

For the repository-managed MySQL instance:

1. Copy `backend/.env.example` to `backend/.env` and change the development passwords. This ignored local file is read by both Docker Compose and Spring Boot.
2. From `backend`, run `docker compose up -d`.
3. Run the backend with `mvnw.cmd spring-boot:run`.

If MySQL is already installed on the machine, set `DB_USERNAME` and `DB_PASSWORD` to that server's real credentials before starting the backend. Set `DB_URL` as well when its host, port, or database name differs from `jdbc:mysql://localhost:3306/small_business_loan_db`.

Port `3306` can only be owned by one MySQL server. Stop the installed service or change both `DB_PORT` and the port inside `DB_URL` in `backend/.env` before starting the Compose database.
