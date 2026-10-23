# OrderFlow

OrderFlow is a Spring Boot application for managing orders, including order creation, retrieval, updates, deletion, and validation.

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway Database Migrations
- Maven
- JUnit and Mockito
- MockMvc
- GitHub Actions

## Prerequisites

Make sure the following tools are installed:

- Java 21
- PostgreSQL
- Git

The project includes Maven Wrapper scripts, so a separate Maven installation may not be required.

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/irshad255/OrderFlow.git
cd OrderFlow
```

### 2. Configure the database

Create a PostgreSQL database for local development.

Configure the datasource URL, username, and password in your local application configuration.

Do not commit database passwords, production credentials, or other secrets to Git.

### 3. Run the application

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

### 4. Run automated tests

Run the complete Maven verification lifecycle:

```powershell
.\mvnw.cmd clean verify
```

The project includes application-context, calculation, controller, service, and integration tests.

## Database Migrations

OrderFlow uses Flyway to manage database schema changes.

Migration files are located in:

```text
src/main/resources/db/migration/
```

Current migrations:

- `V1__create_orders_table.sql` — creates the orders table.
- `V2__add_order_data_constraints.sql` — adds database-level data constraints.

### Data Constraints

The database enforces the following rules:

- Quantity must be between 1 and 100000.
- Unit price must be at least 0.01.
- Order status must satisfy the configured status constraint.

Flyway migration history is stored in `flyway_schema_history`.

For existing databases, review the current schema and migration history before configuring baseline behavior. Do not apply migrations manually to production without verifying the database state and taking an appropriate backup.

## Testing

The automated test suite includes:

- Application context tests
- Order calculation tests
- Controller tests
- Service tests
- Integration tests

Run all tests with:

```powershell
.\mvnw.cmd clean verify
```

Test results are generated under:

```text
target/surefire-reports/
```

The test profile is configured in:

```text
src/main/resources/application-test.properties
```

Ensure tests use a dedicated local test database and never accidentally target the production database.

## Continuous Integration

The project uses GitHub Actions for continuous integration.

Repository workflow runs can be reviewed here:

https://github.com/irshad255/OrderFlow/actions

The latest reported OrderFlow CI workflow completed successfully after the Flyway migration changes.

## Production Deployment

The application has been deployed using Render.

Before deploying database changes:

1. Review the migration scripts.
2. Verify database compatibility and existing data.
3. Back up production data as appropriate.
4. Deploy and inspect application logs.
5. Verify migration history, constraints, and API behavior.

Keep production credentials in deployment environment variables rather than committing them to the repository.

## Security Notes

- Never commit passwords, tokens, or database credentials.
- Keep production and test database configurations separate.
- Validate API inputs and enforce critical data rules at the database level.
- Review logs to ensure sensitive values are not exposed.

## License

Add a license if and when one is selected for this project.
