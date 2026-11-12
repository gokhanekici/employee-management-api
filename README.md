**English** | [Türkçe](README.tr.md)

# Employee Management API

A RESTful web service for managing a company's employee list, built with Java and Spring Boot.

The project started as my work for the Hewlett Packard Enterprise (HPE) Software Engineering job simulation on Forage. The simulation covers a design proposal, a `GET` and a `POST` endpoint, and unit tests. I then kept going and implemented the rest of my own proposal: full CRUD, input validation, soft delete, and database persistence. The section [Beyond the simulation](#beyond-the-simulation) lists exactly what was added.

## Features

- List, fetch, create, update, and delete employees over HTTP
- Input validation with meaningful status codes (`400`, `404`, `409`)
- Soft delete: deleted employees are hidden from the API but kept in the database
- Persistence with Spring Data JPA; data survives restarts
- Unit tests and HTTP-level tests

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4 (Spring Web MVC) |
| Persistence | Spring Data JPA, H2 (file-based) |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5, Mockito, MockMvc |
| Build | Gradle (wrapper included) |

## Getting started

You need JDK 17 or newer. Nothing else has to be installed; the Gradle wrapper downloads Gradle and H2 runs inside the application.

```bash
git clone https://github.com/gokhanekici/employee-management-api.git
cd employee-management-api
./gradlew bootRun
```

The service starts on `http://localhost:8080`. On the first run it creates a local database under `./data/` and loads three sample employees.

To run the tests:

```bash
./gradlew test
```

## API

| Method | Path | Description | Success | Errors |
|---|---|---|---|---|
| `GET` | `/employees` | List all active employees | `200` | – |
| `GET` | `/employees/{employee_id}` | Get one employee | `200` | `404` |
| `POST` | `/employees` | Create an employee | `201` + `Location` header | `400`, `409` |
| `PUT` | `/employees/{employee_id}` | Replace an employee | `200` | `400`, `404` |
| `DELETE` | `/employees/{employee_id}` | Soft-delete an employee | `204` | `404` |

Error codes:

- `400 Bad Request`: a field is missing or blank, the email is not valid, the JSON is malformed, or (for `PUT`) the ID in the URL differs from the ID in the body.
- `404 Not Found`: no active employee has this ID.
- `409 Conflict`: the `employee_id` is already in use, including by a soft-deleted employee.

### Employee format

All five fields are required.

```json
{
  "employee_id": "E1001",
  "first_name": "Ada",
  "last_name": "Lovelace",
  "email": "ada.lovelace@example.com",
  "title": "Software Engineer"
}
```

`GET /employees` wraps the list in an object:

```json
{
  "Employees": [
    {
      "employee_id": "E1001",
      "first_name": "Ada",
      "last_name": "Lovelace",
      "email": "ada.lovelace@example.com",
      "title": "Software Engineer"
    }
  ]
}
```

### Examples

List all employees:

```bash
curl http://localhost:8080/employees
```

Create an employee:

```bash
curl -i -X POST http://localhost:8080/employees \
  -H "Content-Type: application/json" \
  -d '{"employee_id":"E2001","first_name":"Linus","last_name":"Torvalds","email":"linus.torvalds@example.com","title":"Staff Engineer"}'
```

Update an employee:

```bash
curl -i -X PUT http://localhost:8080/employees/E2001 \
  -H "Content-Type: application/json" \
  -d '{"employee_id":"E2001","first_name":"Linus","last_name":"Torvalds","email":"linus.torvalds@example.com","title":"Principal Engineer"}'
```

Delete an employee:

```bash
curl -i -X DELETE http://localhost:8080/employees/E2001
```

## Architecture

```
HTTP request
     │
EmployeeController     HTTP layer: routing, status codes, validation trigger
     │
EmployeeManager        Business rules: unique IDs, soft delete
     │
EmployeeRepository     Data access (Spring Data JPA)
     │
H2 database            Table "employees"
```

| Class | Role |
|---|---|
| `Employee` | JPA entity and JSON model, with validation rules |
| `Employees` | Wrapper that produces the `{"Employees": [...]}` response |
| `EmployeeController` | REST endpoints |
| `EmployeeManager` | Business logic |
| `EmployeeRepository` | Database queries |
| `DataSeeder` | Loads sample data when the table is empty |

## Design decisions

**Soft delete.** `DELETE` marks a record as inactive instead of removing it. Accidental deletions can be reversed in the database and the history is preserved. Inactive employees behave as "not found" for every endpoint.

**IDs are never reused.** Creating an employee with the ID of a soft-deleted one returns `409`. Reusing the ID would mix the old record's history with a new person.

**`PUT` rejects mismatched IDs.** If the URL says `E1001` and the body says `E2002`, the request is ambiguous, so the service returns `400` rather than guessing which record to change.

**Validation lives on the model.** The rules are annotations on `Employee`, so `POST` and `PUT` share them without duplication.

**The internal flag is not exposed.** The `active` field is excluded from JSON. Clients cannot read or set it, and the response format stays exactly as specified.

**Layered design.** Moving from an in-memory list to a database changed the data layer only; the controller and its unit tests were not modified.

**H2 for easy setup.** The proposal targets PostgreSQL on a private cloud (HPE GreenLake). This repository uses file-based H2 so anyone can run it without installing a database. Because data access goes through JPA, switching to PostgreSQL is a configuration change, not a code change.

## Beyond the simulation

| Topic | Simulation scope | This project |
|---|---|---|
| Endpoints | `GET` list, `POST` | Adds `GET` by ID, `PUT`, `DELETE` |
| Create response | Add the employee | `201` with a `Location` header pointing to the new resource |
| Duplicate IDs | Not covered | Rejected with `409` |
| Input validation | Not covered | Required fields and email format, `400` on failure |
| Delete | Proposed only | Implemented as soft delete |
| Storage | Hard-coded in-memory list | Spring Data JPA with a file-based database |
| JSON format | `snake_case` fields under `"Employees"` | Enforced with Jackson annotations and verified by tests |
| Tests | Unit tests | Unit tests with Mockito plus HTTP-level tests with MockMvc |
| Encapsulation | Getters and setters | The employee list is only handed out as an immutable copy |

## Testing

The test suite has three layers:

- `EmployeeControllerTest`: unit tests for the controller with a mocked `EmployeeManager` (Mockito).
- `EmployeeManagerTest`: business rules against an in-memory database.
- `EmployeeApiTest`: full HTTP requests through MockMvc, covering routing, status codes, JSON field names, and validation.

Tests run against a separate in-memory H2 database, and each test is rolled back so tests do not affect each other.

## Possible next steps

- Authentication and authorization, so only authorized users can modify records
- Endpoints to restore soft-deleted employees and to purge them after a retention period
- Error responses with a body that names the invalid fields
- A PostgreSQL profile and deployment to a private cloud
- Pagination for large employee lists

## Acknowledgements

- The project skeleton comes from the Spring guide [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service) ([source](https://github.com/spring-guides/gs-rest-service), Apache License 2.0).
- The original task comes from the Hewlett Packard Enterprise Software Engineering job simulation on [Forage](https://www.theforage.com).