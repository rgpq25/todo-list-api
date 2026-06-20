# Todo REST API

## Project Overview

Build a REST API that allows a client to manage todo tasks.

The application should expose HTTP endpoints for creating, viewing, updating, completing, filtering, and deleting tasks.
The project does not require a frontend. The API should be usable from tools such as Postman, Insomnia, curl, or any
HTTP client.

This version focuses only on task management. User accounts, authentication, and authorization are not required.

---

## Application Requirements

The application must allow a client to:

- Create todo tasks
- View all todo tasks
- View a specific todo task by ID
- Update an existing todo task
- Partially update an existing todo task
- Mark a task as completed
- Mark a completed task as incomplete
- Delete a task
- Filter tasks by selected fields
- Receive clear responses for successful and failed requests

The application must store tasks so they can be retrieved after they are created.

---

## Required Resource

### Task

A task represents one todo item.

A task must contain the following information:

| Field         | Required | Description                             |
|---------------|---------:|-----------------------------------------|
| `id`          |      Yes | Unique identifier for the task          |
| `title`       |      Yes | Short name or summary of the task       |
| `description` |       No | Additional details about the task       |
| `completed`   |      Yes | Indicates whether the task is completed |
| `priority`    |       No | Importance level of the task            |
| `dueDate`     |       No | Date the task should be completed by    |
| `createdAt`   |      Yes | Date and time the task was created      |
| `updatedAt`   |      Yes | Date and time the task was last changed |

### Task Priority Values

The application must support the following priority values:

```text
LOW
MEDIUM
HIGH
```

When a task is created, it should start as incomplete unless the API request explicitly allows otherwise.

---

## API Base Path

All task endpoints should be grouped under:

```http
/api/tasks
```

---

## Required Endpoints

### Create a Task

```http
POST /api/tasks
```

Creates a new task.

#### Request Body Example

```json
{
  "title": "Finish project README",
  "description": "Write the requirements for the Todo REST API",
  "priority": "HIGH",
  "dueDate": "2026-07-01"
}
```

#### Successful Response

```http
201 Created
```

The response should include the created task, including its generated ID, completion status, creation timestamp, and
update timestamp.

---

### Get All Tasks

```http
GET /api/tasks
```

Returns all tasks.

#### Optional Query Parameters

| Parameter   | Description                            |
|-------------|----------------------------------------|
| `completed` | Filter tasks by completion status      |
| `priority`  | Filter tasks by priority               |
| `dueBefore` | Return tasks due before the given date |
| `dueAfter`  | Return tasks due after the given date  |

#### Example Request

```http
GET /api/tasks?completed=false&priority=HIGH
```

#### Successful Response

```http
200 OK
```

The response should be a list of tasks. If no tasks match the request, the response should be an empty list.

---

### Get a Task by ID

```http
GET /api/tasks/{id}
```

Returns one task by its ID.

#### Successful Response

```http
200 OK
```

The response should include the matching task.

#### Not Found Response

```http
404 Not Found
```

Returned when no task exists with the provided ID.

---

### Update a Task

```http
PUT /api/tasks/{id}
```

Replaces the editable information for an existing task.

#### Request Body Example

```json
{
  "title": "Finish updated README",
  "description": "Revise the assignment requirements",
  "completed": false,
  "priority": "MEDIUM",
  "dueDate": "2026-07-05"
}
```

#### Successful Response

```http
200 OK
```

The response should include the updated task.

#### Not Found Response

```http
404 Not Found
```

Returned when no task exists with the provided ID.

---

### Partially Update a Task

```http
PATCH /api/tasks/{id}
```

Updates only the fields included in the request body.

#### Request Body Example

```json
{
  "priority": "LOW"
}
```

#### Successful Response

```http
200 OK
```

The response should include the updated task.

#### Not Found Response

```http
404 Not Found
```

Returned when no task exists with the provided ID.

---

### Mark a Task as Completed

```http
PATCH /api/tasks/{id}/complete
```

Marks an existing task as completed.

#### Successful Response

```http
200 OK
```

The response should include the updated task with `completed` set to `true`.

#### Not Found Response

```http
404 Not Found
```

Returned when no task exists with the provided ID.

---

### Mark a Task as Incomplete

```http
PATCH /api/tasks/{id}/incomplete
```

Marks an existing task as incomplete.

#### Successful Response

```http
200 OK
```

The response should include the updated task with `completed` set to `false`.

#### Not Found Response

```http
404 Not Found
```

Returned when no task exists with the provided ID.

---

### Delete a Task

```http
DELETE /api/tasks/{id}
```

Deletes an existing task.

#### Successful Response

```http
204 No Content
```

#### Not Found Response

```http
404 Not Found
```

Returned when no task exists with the provided ID.

---

## Request Requirements

### Creating a Task

When creating a task:

- `title` is required
- `description` is optional
- `priority` is optional
- `dueDate` is optional
- `completed` should not be required from the client

### Updating a Task

When fully updating a task:

- `title` is required
- `completed` is required
- `description` may be included or omitted
- `priority` may be included or omitted
- `dueDate` may be included or omitted

### Partially Updating a Task

When partially updating a task:

- All fields are optional
- Only fields included in the request should be changed
- Fields not included in the request should keep their existing values

---

## Validation Requirements

The API must reject invalid request data.

The following validation rules are required:

- `title` is required when creating a task
- `title` cannot be blank
- `title` should have a reasonable maximum length
- `description` should have a reasonable maximum length
- `priority` must be one of: `LOW`, `MEDIUM`, or `HIGH`
- `dueDate` must be a valid date
- Invalid request data should return `400 Bad Request`

---

## Error Handling Requirements

The API must return clear error responses for failed requests.

The application should handle at least the following cases:

- A task with the requested ID does not exist
- The request body is missing required data
- The request body contains invalid data
- A priority value is not supported
- A query parameter is invalid
- The requested endpoint does not exist

Suggested status codes:

| Situation                      |       Status Code |
|--------------------------------|------------------:|
| Invalid request data           | `400 Bad Request` |
| Task not found                 |   `404 Not Found` |
| Successful creation            |     `201 Created` |
| Successful retrieval or update |          `200 OK` |
| Successful deletion            |  `204 No Content` |

---

## Filtering Requirements

The `GET /api/tasks` endpoint must support filtering tasks.

The API should allow clients to filter by:

- Completion status
- Priority
- Due date before a provided date
- Due date after a provided date

Filters may be used individually or together.

Example:

```http
GET /api/tasks?completed=false&priority=HIGH&dueBefore=2026-07-10
```

If no tasks match the filters, the API should return an empty list.

---

## Business Rules

The application must follow these rules:

- A task cannot be created without a title
- A new task should be incomplete by default
- Completing a task should change its completion status to `true`
- Marking a task as incomplete should change its completion status to `false`
- Updating a task should update its last modified timestamp
- Deleting a task should make it unavailable from future read requests
- Looking up a missing task should return `404 Not Found`
- Filtering should not cause an error when no matching tasks exist

---

## Expected Result

The final application should be a working Todo REST API that allows clients to manage tasks through HTTP requests.

A client should be able to create, retrieve, update, complete, filter, and delete todo tasks while receiving appropriate
success and error responses.