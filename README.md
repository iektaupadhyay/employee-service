# Salary Management System

## 1. Project Overview

The **Salary Management System** is a Spring Boot backend application intended to manage employee information, departments, and employee leave allocations.

The application follows a layered architecture, separating REST controllers, service logic, repositories, DTOs, entities, and exception handling.

### Main modules

- **Employee Management** – Create, update, retrieve, and manage employee information.
- **Department Management** – Manage department records and their association with employees.
- **Employee Leave Management** – Allocate, update, retrieve, and delete employee leave allocations.

---

## 2. Technology Stack

| Technology | Purpose |
|---|---|
| Java 25 | Backend programming language |
| Spring Boot 4 | Application framework |
| Spring Web | REST API development |
| Spring Data JPA | Database access and persistence |
| Hibernate | ORM |
| Jakarta Bean Validation | Request and entity validation |
| PostgreSQL | Relational database |
| Neon | PostgreSQL hosting |
| Maven | Dependency and build management |
| JUnit 5 | Unit and integration testing |
| Mockito | Mocking dependencies in unit tests |
| MockMvc | Controller/API testing |
| Lombok | Boilerplate code reduction |

*Confirm the exact dependency versions against `pom.xml` before publishing this table as a versioned technology inventory.*

---

## 3. Application Architecture

The application uses a layered architecture.

```text
Client / API Consumer
        |
        v
REST Controllers
        |
        v
Request DTOs
(Validation)
        |
        v
Service Layer
(Business Logic)
        |
        v
Repository Layer
(Spring Data JPA)
        |
        v
PostgreSQL Database
```

### Layer responsibilities

**Controller Layer**
- Exposes REST endpoints.
- Accepts incoming HTTP requests.
- Performs request validation through Jakarta Bean Validation.
- Delegates business operations to services.
- Returns appropriate HTTP responses.

**DTO Layer**
- Defines the structure of API requests and responses.
- Keeps API contracts separate from persistence entities.
- Uses validation annotations to reject invalid input.

**Service Layer**
- Contains application business logic.
- Performs business validations.
- Coordinates repository operations.
- Throws appropriate exceptions when operations cannot be completed.

**Repository Layer**
- Handles database access through Spring Data JPA.
- Provides entity retrieval, persistence, and deletion operations.

**Model Layer**
- Contains JPA entities and embedded value objects representing application data.

**Exception Handling**
- Centralizes exception handling through `GlobalExceptionHandler`.
- Produces consistent error responses for validation failures and invalid request bodies.

---

## 4. Project Package Structure

The existing package root is:

`com.employee_managemet.employee_service`

The current package structure follows this organization:

```text
com.employee_managemet.employee_service
│
├── controller
│   ├── EmployeeController
│   ├── DepartmentController
│   └── EmployeeLeaveController
│
├── dto
│   ├── EmployeeRequest
│   ├── EmployeeResponse
│   ├── EmployeeLeaveRequest
│   └── EmployeeLeaveResponse
│
├── enums
│   ├── EmploymentStatus
│   ├── EmploymentType
│   ├── Gender
│   └── LeaveType
│
├── exception
│   └── GlobalExceptionHandler
│
├── model
│   ├── Employee
│   ├── Department
│   ├── EmployeeLeave
│   └── EmployeeAddress
│
├── repository
│   ├── EmployeeRepository
│   ├── DepartmentRepository
│   └── EmployeeLeaveRepository
│
├── service
│   ├── EmployeeService
│   ├── DepartmentService
│   ├── EmployeeLeaveService
│   │
│   └── impl
│       ├── EmployeeServiceImpl
│       ├── DepartmentServiceImpl
│       └── EmployeeLeaveServiceImpl
│
└── EmployeeServiceApplication
```

This is a representative structure based on the classes discussed. Verify the actual filenames and package locations against the source tree.

---

## 5. Core Functional Modules

### 5.1 Employee Management

Employee management handles employee information and department association.

The employee request currently contains:

| Field | Type | Validation |
|---|---|---|
| employeeCode | String | Required, maximum 30 characters |
| name | String | Required, maximum 100 characters |
| gender | Gender | Required |
| employmentType | EmploymentType | Required |
| employmentStatus | EmploymentStatus | Required |
| joiningDate | LocalDate | Required |
| mobileNumber | String | Required, 10–15 digits, optional `+` prefix |
| email | String | Required, valid email, maximum 150 characters |
| position | String | Required, maximum 100 characters |
| departmentId | Long | Required |
| address | EmployeeAddress | Required, nested validation |

#### Employee request validation

The `EmployeeRequest` record uses Jakarta validation annotations:

- `@NotBlank` for mandatory text fields.
- `@NotNull` for mandatory non-text fields.
- `@Size` for maximum lengths.
- `@Email` for email validation.
- `@Pattern` for mobile number validation.
- `@Valid` for nested address validation.

Mobile number validation:

```java
@Pattern(
    regexp = "^\\+?[0-9]{10,15}$",
    message = "Mobile number must contain 10 to 15 digits, optionally prefixed with +"
)
```

#### Employment enums

The employee request uses:
- `Gender`
- `EmploymentType`
- `EmploymentStatus`

The `PERMANENT` employment type is confirmed as a valid enum value from the service tests.

Always use the exact enum constants defined in the source code when constructing JSON requests.

### 5.2 Employee Address

`EmployeeAddress` is an embeddable JPA value object.

It is annotated with `@Embeddable` and uses Lombok `@Getter` and `@Setter`.

Current fields:

| Field | Type | Required |
|---|---|---|
| addressLine1 | String | Yes |
| addressLine2 | String | No |
| city | String | Yes |
| state | String | Yes |
| country | String | Yes |
| postalCode | String | Yes |

Validation rules:

- Address line 1: required, maximum 150 characters.
- Address line 2: optional, maximum 150 characters.
- City: required, maximum 80 characters.
- State: required, maximum 80 characters.
- Country: required, maximum 80 characters.
- Postal code: required, maximum 15 characters.

Example employee request:

```json
{
  "employeeCode": "EMP001",
  "name": "John Doe",
  "gender": "MALE",
  "employmentType": "PERMANENT",
  "employmentStatus": "ACTIVE",
  "joiningDate": "2025-01-10",
  "mobileNumber": "9876543210",
  "email": "john@example.com",
  "position": "Software Engineer",
  "departmentId": 1,
  "address": {
    "addressLine1": "123 Main Street",
    "addressLine2": "Near City Centre",
    "city": "Bengaluru",
    "state": "Karnataka",
    "country": "India",
    "postalCode": "560001"
  }
}
```

**Important:** The address object cannot be `null` because the employee request declares it with `@NotNull`.

### 5.3 Department Management

Department management handles department records and their business validations.

The department service tests cover:
- Updating a department.
- Rejecting duplicate department names.
- Deactivating a department.
- Handling a department that does not exist.
- Successful department updates.

A department ID is important in service test fixtures. When creating test entities manually, assign an ID if the service logic compares entity IDs.

For nullable IDs, prefer null-safe comparisons such as:

```java
Objects.equals(existingDepartment.getId(), departmentId)
```

The actual department entity fields and complete API contract should be documented from the implementation.

### 5.4 Employee Leave Management

The employee leave module manages leave allocations associated with employees.

The current request structure is:

```java
new EmployeeLeaveRequest(
    1L,
    LeaveType.CASUAL,
    12,
    LocalDate.of(2026, 1, 1)
);
```

The request includes:

- Employee ID.
- Leave type.
- Total leave allocation.
- Applied date.

The leave module supports the following service operations:

| Operation | Description |
|---|---|
| Create allocation | Create a leave allocation for an employee |
| Get allocation | Retrieve an allocation by ID |
| Get employee leaves | Retrieve leave allocations for an employee |
| Update allocation | Update an existing allocation |
| Delete allocation | Delete an allocation |

#### Leave business validations

The service tests cover these scenarios:

- Employee must exist before an allocation is created.
- Duplicate employee and leave-type allocations are rejected.
- Null requests are rejected.
- Negative leave totals are rejected.
- Non-existent allocations return a not-found error.
- An allocation cannot be reassigned to another employee during an update.
- Duplicate leave types are rejected during an update.
- Successful create, update, retrieve, and delete operations are tested.

The repository methods used in the discussed tests include:

```java
employeeRepository.findById(employeeId);

leaveRepository.findById(allocationId);

leaveRepository.findByEmployeeId(employeeId);

leaveRepository.findByEmployeeIdAndLeaveType(
    employeeId,
    leaveType
);

leaveRepository.existsByEmployeeIdAndLeaveType(
    employeeId,
    leaveType
);
```

---

## 6. REST API

The following employee endpoints have been confirmed from the controller tests:

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/employees/createemployee` | Create an employee |
| PUT | `/api/v1/employees/updateemployee/{id}` | Update an employee |

The following are the known module capabilities, but their exact HTTP paths and methods should be verified from the controllers before being added to a published API reference:

- Employee retrieval.
- Department creation, retrieval, update, and deactivation.
- Leave allocation creation, retrieval, update, and deletion.

### Example: Create employee

**Request**

```http
POST /api/v1/employees/createemployee
Content-Type: application/json
```

Use the employee request example in Section 5.2.

The controller test expects:

```http
201 Created
```

### Example: Update employee

**Request**

```http
PUT /api/v1/employees/updateemployee/1
Content-Type: application/json
```

The controller test expects:

```http
200 OK
```

---

## 7. Exception Handling and Validation

The application uses a centralized `GlobalExceptionHandler`.

The discussed behavior includes:

### Validation failure

When a request violates Bean Validation constraints, the API returns HTTP 400.

Example response:

```json
{
  "timestamp": "2026-11-02T16:45:49.992135600Z",
  "status": 400,
  "error": "Bad Request",
  "message": "One or more request fields are invalid",
  "path": "/api/v1/employees/createemployee",
  "correlationId": null,
  "validationErrors": {
    "address": "Address is required"
  }
}
```

### Invalid request body

Invalid JSON or unsupported enum values are handled as bad requests.

### Common HTTP statuses

| Status | Meaning |
|---|---|
| 200 | Successful operation |
| 201 | Resource created |
| 400 | Invalid request or validation failure |
| 404 | Requested resource not found |
| 409 | Conflict, such as a duplicate allocation |

The exact mapping for every exception should be verified in `GlobalExceptionHandler`.

---

## 8. Testing Strategy

The project uses JUnit 5, Mockito, and MockMvc.

### Unit tests

Unit tests focus on service business logic.

Examples:

- `EmployeeLeaveServiceImplTest`
- `EmployeeServiceImplTest`
- Department service tests

Mockito is used to mock repositories and isolate service behavior.

Example:

```java
when(employeeRepository.findById(1L))
    .thenReturn(Optional.of(employee));
```

For save operations:

```java
when(leaveRepository.save(any(EmployeeLeave.class)))
    .thenAnswer(invocation -> invocation.getArgument(0));
```

This returns the entity passed to the repository instead of returning Mockito's default `null`.

### Controller tests

MockMvc is used to test HTTP endpoints and response statuses.

Examples:

- `createEmployee_shouldReturn201()`
- `updateEmployee_shouldReturn200()`

These tests validate request deserialization, Bean Validation, controller behavior, and HTTP response codes.

### Application context test

`EmployeeServiceApplicationTests` is intended to verify that the Spring application context loads successfully.

A separate test database configuration may be needed to avoid depending on an externally hosted database during tests.

---

## 9. Important Testing Findings and Fixes

The following issues were identified while running the test suite.

### 9.1 Employee controller tests returned HTTP 400

**Observed behavior**

The create and update controller tests expected HTTP 201 and HTTP 200 respectively, but received HTTP 400.

**Root cause**

The request JSON contained:

```json
"address": null
```

However, `EmployeeRequest` declares:

```java
@NotNull(message = "Address is required")
@Valid
EmployeeAddress address
```

The request therefore failed validation before reaching the service layer.

**Fix**

Provide a valid nested address object containing:

- `addressLine1`
- `city`
- `state`
- `country`
- `postalCode`

The `addressLine2` field is optional.

The request also needs valid enum constants. `PERMANENT` was confirmed as the valid employment type used in the service tests; `FULL_TIME` was not accepted by the enum deserializer in the earlier test run.

### 9.2 Leave update duplicate-allocation test

**Test**

```java
updateLeaveAllocation_whenDuplicateAllocationExists_shouldThrowConflict()
```

**Observed behavior**

The service logs:

```text
Duplicate leave allocation detected.
employeeId=1, leaveType=SICK
```

This confirms that the duplicate-detection branch is reached.

The latest failure was:

```text
UnnecessaryStubbingException
```

The test contained a stub for:

```java
when(employeeRepository.findById(1L))
    .thenReturn(Optional.of(employee));
```

That stub was unnecessary because duplicate detection occurs before the employee lookup.

**Fix**

Remove the unused employee repository stub from this test.

Keep the required stubs for:

```java
leaveRepository.findById(1L)
```

and:

```java
leaveRepository.findByEmployeeIdAndLeaveType(
    1L,
    LeaveType.SICK
)
```

Verify that the repository save operation is not called when a duplicate is detected.

Mockito strict stubbing should remain enabled. Avoid using `lenient()` just to suppress unnecessary-stubbing failures.

### 9.3 Duplicate allocation test fixture IDs

The shared `createLeaveAllocation()` helper assigns ID `1L` to the created leave allocation.

When a test needs two different allocation records, assign distinct IDs to the fixtures. Otherwise, the service may interpret the records as the same allocation.

Example:

```java
EmployeeLeave existingLeave = createLeaveAllocation();

EmployeeLeave anotherLeave = createLeaveAllocation();

ReflectionTestUtils.setField(anotherLeave, "id", 2L);
```

This is important when the implementation excludes the current allocation by comparing IDs.

### 9.4 Repository save mocks

When a service calls `save()` and immediately maps the returned entity into a response, a mock returning `null` can cause a `NullPointerException`.

For successful update tests, use:

```java
when(leaveRepository.save(any(EmployeeLeave.class)))
    .thenAnswer(invocation -> invocation.getArgument(0));
```

This simulates the repository returning the saved entity.

---

## 10. Database Configuration

The application uses PostgreSQL, with Neon as the hosted database provider.

The database connection should be configured through environment variables rather than committing credentials to source control.

Example configuration structure:

```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
```

The actual URL format and SSL parameters must match the Neon connection string.

### Test database isolation

For tests that load the complete Spring context, consider using H2 as an in-memory test database.

Maven dependency:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

Example `src/test/resources/application-test.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:salary_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
```

Use the test profile:

```java
@SpringBootTest
@ActiveProfiles("test")
class EmployeeServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

This avoids requiring a live Neon connection for application-context tests.

**Note:** H2 PostgreSQL compatibility mode does not reproduce every PostgreSQL feature. Database-specific queries and migrations should also be tested against PostgreSQL.

---

## 11. Running the Application

### Prerequisites

- Java 25
- Maven
- PostgreSQL database or Neon connection
- Environment variables configured for database access

### Run using Maven

```bash
mvn clean install
```

Start the application:

```bash
mvn spring-boot:run
```

### Run tests

Run the complete test suite:

```bash
mvn test
```

Run a specific test class:

```bash
mvn -Dtest=EmployeeControllerTest test
```

```bash
mvn -Dtest=EmployeeLeaveServiceImplTest test
```

Run an individual test method:

```bash
mvn -Dtest=EmployeeControllerTest#createEmployee_shouldReturn201 test
```

---

## 12. Development Guidelines

The following conventions are useful for maintaining consistency across the application.

1. Keep business logic in service implementations, not controllers.
2. Use request and response DTOs instead of exposing persistence entities directly.
3. Validate incoming requests using Jakarta Bean Validation.
4. Use `@Valid` for nested request objects.
5. Keep database operations in repository interfaces.
6. Handle exceptions centrally through `GlobalExceptionHandler`.
7. Use meaningful HTTP status codes.
8. Write unit tests for successful and failure scenarios.
9. Mock repository save operations to return the expected entity when the service maps the result.
10. Keep Mockito strict stubbing enabled and remove unused stubs.
11. Do not hardcode database credentials in configuration files.
12. Keep test configuration separate from runtime configuration.

---

## 13. Current Development Status

The following items reflect the work discussed so far. They should not be interpreted as a claim that the complete test suite has passed.

| Area | Current status |
|---|---|
| Employee request DTO validation | Defined |
| Employee address model | Defined as an embeddable object |
| Employment type enum | `PERMANENT` confirmed in tests |
| Employee create/update controller tests | Request payload correction identified; rerun required |
| Department service tests | Existing test scenarios discussed |
| Leave allocation service tests | Existing test scenarios discussed |
| Leave duplicate update test | Unnecessary stub identified for removal |
| Mockito strict-stubbing issues | Identified and being corrected |
| PostgreSQL / Neon configuration | Used for application database |
| Isolated H2 test configuration | Suggested; verify implementation |
| Full test suite | Final passing status not yet confirmed |

---

## 14. Project Goal

The objective is to build a maintainable employee and salary management backend with clearly separated modules, validated API contracts, consistent exception handling, reliable database persistence, and meaningful automated tests.

The system is being developed incrementally, with emphasis on correctness of business logic, request validation, database behavior, and test coverage.