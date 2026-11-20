# Task Management REST API

A JWT-secured REST API for managing personal tasks, built using **Java 25**, **Spring Boot 4**, and **PostgreSQL**.

The application supports user authentication, task management, role-based access control, keyword searching, database migrations, and request/error logging.

```markdown
## Repository

GitHub repository: https://github.com/DilkiSanjana/Task-management.git

## Features

- User registration and login with JWT authentication.
- BCrypt password hashing.
- Create, retrieve, update, and delete tasks.
- Users can access and manage only their own tasks.
- Pagination and task-status filtering.
- Case-insensitive keyword search across task titles and descriptions.
- Due-date sorting.
- Role-based access control with `ROLE_USER` and `ROLE_ADMIN`.
- Admin endpoint for viewing all tasks.
- Interactive Swagger / OpenAPI documentation.
- Liquibase database migrations.
- Centralized exception handling.
- HTTP request logging and application error logging.

## Technologies Used

| Technology | Version / Purpose |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.2 |
| Maven | 3.9+ / Maven Wrapper |
| Spring Data JPA | Database access |
| Spring Security | Authentication and authorization |
| JJWT | 0.12.6 |
| PostgreSQL | 14+ |
| Liquibase | Database migrations |
| Springdoc OpenAPI | 3.2.0 |
| SLF4J | Application logging |

## Requirements

Before running the application, install:

- Java JDK 25
- PostgreSQL 14 or later
- Git

Maven can be used through the included Maven Wrapper.

## Setup

### 1. Clone the repository

```bash
git clone <your-repository-url>
cd task-management
```

### 2. Create the PostgreSQL database

Open PostgreSQL or pgAdmin and execute:

```sql
CREATE DATABASE task_management_db; 
CREATE DATABASE task_management_test_db;
```

### 3. Configure environment variables

Create a `.env` file in the project root.

Example:

```env
DB_URL=jdbc:postgresql://localhost:5432/task_management_db
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# Generate a Base64-encoded 32-byte secret
# Example: openssl rand -base64 32
JWT_SECRET=your_base64_encoded_32_byte_secret

# Token expiry in milliseconds (24 hours)
JWT_EXPIRATION=86400000
```

The project includes the `springboot4-dotenv` dependency for loading environment variables from `.env`.

**Important:** Never commit `.env` or expose your database credentials or JWT secret.

### 4. Database configuration

The application uses Liquibase to manage database schema changes.

Migration files are located in:

```text
src/main/resources/db/changelog/
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This validates the database schema against the JPA entities without automatically modifying the schema.

## Running the Application

Start the application using the Maven Wrapper.

**Windows PowerShell:**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
./mvnw spring-boot:run
```

The application starts at:

http://localhost:8080

### Swagger UI

Interactive API documentation is available at:

http://localhost:8080/swagger-ui/index.html

Use the Authorize option in Swagger UI to provide your JWT for secured endpoints.

## Running the Tests

The project uses PostgreSQL for its database configuration, including automated test execution.

Ensure PostgreSQL is running and the database connection environment variables are configured before running the tests.

**Windows PowerShell:**

```powershell
.\mvnw.cmd test
```

**Linux / macOS:**

```bash
./mvnw test
```

To run the complete Maven build:

```powershell
.\mvnw.cmd clean install
```

**Recommendation:** Use a separate PostgreSQL test database when running tests that may create, update, or delete data. Configure `DB_URL` accordingly so test execution does not affect development data.

## API Endpoints

### Authentication

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |

### Task Management

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/tasks` | Authenticated |
| GET | `/api/tasks` | Authenticated – own tasks |
| GET | `/api/tasks/{id}` | Task owner |
| PUT | `/api/tasks/{id}` | Task owner |
| DELETE | `/api/tasks/{id}` | Task owner |
| GET | `/api/tasks/search` | Authenticated |

### Administration

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/admin/tasks` | ADMIN only |

## Example Requests

The following examples use curl and a Bash-compatible terminal.

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"secret123"}'
```

Example response:

```json
{
  "token": "<jwt>",
  "tokenType": "Bearer"
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"secret123"}'
```

Use the returned JWT for authenticated requests.

### 3. Create a Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Buy groceries",
    "description": "Milk, eggs",
    "dueDate": "2026-12-01"
  }'
```

### 4. List Tasks

Retrieve the authenticated user's tasks:

```bash
curl "http://localhost:8080/api/tasks?page=0&size=5" \
  -H "Authorization: Bearer <token>"
```

Filter by task status:

```bash
curl "http://localhost:8080/api/tasks?status=IN_PROGRESS" \
  -H "Authorization: Bearer <token>"
```

### 5. Search Tasks

Search by keyword:

```bash
curl "http://localhost:8080/api/tasks/search?keyword=groceries&sortDir=asc" \
  -H "Authorization: Bearer <token>"
```

Use `sortDir=asc` or `sortDir=desc` for due-date sorting.

### 6. Update a Task

```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Buy groceries",
    "status": "DONE",
    "dueDate": "2026-12-01"
  }'
```

### 7. Delete a Task

```bash
curl -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer <token>"
```

## HTTP Status Codes

| Status | Meaning |
|---|---|
| 200 | Request successful |
| 201 | Resource created |
| 204 | Resource deleted |
| 400 | Invalid request or validation failure |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Resource not found |
| 409 | Resource conflict |
| 500 | Unexpected server error |

## Design Decisions

### Layered Architecture

The project follows a layered architecture:

`Controller → Service → Repository`

- Controllers handle HTTP requests and responses.
- Services contain business logic.
- Repositories handle database operations.

### Security

- Passwords are hashed using BCrypt before storage.
- JWT tokens are signed using HMAC-SHA through JJWT 0.12.6.
- Tokens contain the user's email and role.
- The JWT authentication filter validates tokens and populates the Spring Security context.
- The application uses stateless authentication.
- Database credentials and JWT secrets are supplied through environment variables.

### Ownership Enforcement

Task retrieval and modification enforce task ownership.

A user attempting to access another user's task receives a `404 Not Found` response rather than a `403 Forbidden` response, avoiding disclosure of another user's task existence.

### Role-Based Access Control

The application supports:

- `ROLE_USER`: Can manage their own tasks.
- `ROLE_ADMIN`: Can access administrative functionality, including listing all tasks through `/api/admin/tasks`.

### Error Handling and Logging

A centralized `@RestControllerAdvice` (`GlobalExceptionHandler`) handles application exceptions and returns consistent JSON error responses.

The error response includes:

- Timestamp
- HTTP status
- Error description
- Error message
- Request path

SLF4J logging records expected application errors and unexpected exceptions.

`RequestLoggingFilter` logs HTTP methods, request URIs, response statuses, and request processing durations.

Sensitive information such as passwords and JWT tokens is not intentionally logged.

### Database Migrations

Liquibase manages database schema changes through versioned migration files.

Hibernate uses `ddl-auto=validate` to validate the schema without automatically changing database tables.

## Future Improvements

Potential improvements include:

- Refresh token support.
- Additional dynamic filtering options using Spring Data Specifications.
- Rate limiting on authentication endpoints.
- GitHub Actions CI/CD integration.
- More integration tests using Testcontainers and PostgreSQL.
- Additional API documentation and test coverage.

## Project Structure

```text
task-management/
├── src/
│   ├── main/
│   │   ├── java/com/example/task_management/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── enums/
│   │   │   ├── exception/
│   │   │   ├── filter/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── TaskManagementApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/changelog/
│   │           ├── changes/
│   │           └── db.changelog-master.yaml
│   └── test/
├── .gitignore
├── .env.example
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## License

This project was developed as part of a Backend Engineering assignment.
