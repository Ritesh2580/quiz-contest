# Quiz Contest API Documentation

## Overview

This is a comprehensive REST API for the Quiz Contest application, built with Spring Boot 3.2.2 and integrated with Swagger UI for interactive API documentation.

**Base URL:** `http://localhost:8080/api/v1`

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

**OpenAPI Specification:** `http://localhost:8080/v3/api-docs`

## Authentication

The API uses JWT (JSON Web Token) for authentication.

1.  Register a user using `POST /api/v1/auth/register`.
2.  Login using `POST /api/v1/auth/login` to obtain a token.
3.  Include the token in the `Authorization` header for all protected endpoints:

```
Authorization: Bearer <your_token>
```

---

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- Spring Boot 3.2.2

### Running the Application

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## API Endpoints

### 1. Authentication

#### Register User
- **Endpoint:** `POST /api/v1/auth/register`
- **Description:** Register a new user in the system
- **Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "name": "John Doe",
  "role": "CREATOR"
}
```
- **Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "CREATOR",
  "createdAt": "2024-02-23T10:00:00",
  "updatedAt": "2024-02-23T10:00:00",
  "version": 1
}
```

#### Login User
- **Endpoint:** `POST /api/v1/auth/login`
- **Description:** Authenticate user and return token
- **Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```
- **Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "CREATOR",
  "token": "dummy-token-...",
  "loginTime": "2024-02-23T10:05:00"
}
```

### 2. User Management

#### Create User (Admin/Internal)
- **Endpoint:** `POST /api/v1/users`
- **Description:** Register a new user in the system
- **Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "name": "John Doe",
  "role": "CREATOR"
}
```
- **Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "name": "John Doe",
  "role": "CREATOR",
  "createdAt": "2024-02-23T10:00:00",
  "updatedAt": "2024-02-23T10:00:00",
  "version": 1
}
```

#### Get User by ID
- **Endpoint:** `GET /api/v1/users/{id}`
- **Response:** `200 OK`

#### Get All Users
- **Endpoint:** `GET /api/v1/users`
- **Response:** `200 OK` (Array of UserDTO)

#### Update User
- **Endpoint:** `PUT /api/v1/users/{id}`
- **Request Body:** Same as Create User
- **Response:** `200 OK`

#### Delete User
- **Endpoint:** `DELETE /api/v1/users/{id}`
- **Response:** `204 No Content`

---

### 2. Quiz Management

#### Create Quiz
- **Endpoint:** `POST /api/v1/quizzes?createdBy={userId}`
- **Description:** Create a new quiz
- **Request Body:**
```json
{
  "title": "General Knowledge Quiz",
  "description": "Test your knowledge on various topics",
  "startDateTime": "2024-02-23T10:00:00",
  "endDateTime": "2024-02-23T12:00:00",
  "durationMinutes": 120,
  "bannerImageId": "550e8400-e29b-41d4-a716-446655440001"
}
```
- **Response:** `201 Created`

#### Get Quiz by ID
- **Endpoint:** `GET /api/v1/quizzes/{id}`
- **Response:** `200 OK`

#### Get All Quizzes
- **Endpoint:** `GET /api/v1/quizzes`
- **Response:** `200 OK`

#### Get Quizzes by Creator
- **Endpoint:** `GET /api/v1/quizzes/creator/{creatorId}`
- **Response:** `200 OK`

#### Update Quiz
- **Endpoint:** `PUT /api/v1/quizzes/{id}`
- **Request Body:** Same as Create Quiz
- **Response:** `200 OK`

#### Delete Quiz
- **Endpoint:** `DELETE /api/v1/quizzes/{id}`
- **Response:** `204 No Content`

#### Check if Quiz is Active
- **Endpoint:** `GET /api/v1/quizzes/{id}/active`
- **Response:** `200 OK` (Boolean)

---

### 3. Question Management

#### Create Question
- **Endpoint:** `POST /api/v1/questions`
- **Request Body:**
```json
{
  "quizId": "550e8400-e29b-41d4-a716-446655440001",
  "questionText": "What is the capital of France?",
  "questionType": "multiple_choice",
  "timeLimitSeconds": 30,
  "displayOrder": 1
}
```
- **Response:** `201 Created`

#### Get Question by ID
- **Endpoint:** `GET /api/v1/questions/{id}`
- **Response:** `200 OK`

#### Get All Questions
- **Endpoint:** `GET /api/v1/questions`
- **Response:** `200 OK`

#### Get Questions by Quiz
- **Endpoint:** `GET /api/v1/questions/quiz/{quizId}`
- **Response:** `200 OK`

#### Update Question
- **Endpoint:** `PUT /api/v1/questions/{id}`
- **Request Body:** Same as Create Question
- **Response:** `200 OK`

#### Delete Question
- **Endpoint:** `DELETE /api/v1/questions/{id}`
- **Response:** `204 No Content`

---

### 4. Question Options

#### Create Question Option
- **Endpoint:** `POST /api/v1/question-options`
- **Request Body:**
```json
{
  "questionId": "550e8400-e29b-41d4-a716-446655440001",
  "optionText": "Paris",
  "isCorrect": true,
  "displayOrder": 1,
  "optionImageId": "550e8400-e29b-41d4-a716-446655440002"
}
```
- **Response:** `201 Created`

#### Get Option by ID
- **Endpoint:** `GET /api/v1/question-options/{id}`
- **Response:** `200 OK`

#### Get Options by Question
- **Endpoint:** `GET /api/v1/question-options/question/{questionId}`
- **Response:** `200 OK`

#### Update Question Option
- **Endpoint:** `PUT /api/v1/question-options/{id}`
- **Request Body:** Same as Create Option
- **Response:** `200 OK`

#### Delete Question Option
- **Endpoint:** `DELETE /api/v1/question-options/{id}`
- **Response:** `204 No Content`

---

### 5. Quiz Participation

#### Join Quiz
- **Endpoint:** `POST /api/v1/participants/join`
- **Request Body:**
```json
{
  "quizId": "550e8400-e29b-41d4-a716-446655440001",
  "userId": "550e8400-e29b-41d4-a716-446655440002"
}
```
- **Response:** `201 Created`

#### Get Participant by ID
- **Endpoint:** `GET /api/v1/participants/{id}`
- **Response:** `200 OK`

#### Get Participants by Quiz
- **Endpoint:** `GET /api/v1/participants/quiz/{quizId}`
- **Response:** `200 OK`

#### Get Participations by User
- **Endpoint:** `GET /api/v1/participants/user/{userId}`
- **Response:** `200 OK`

#### Start Quiz
- **Endpoint:** `POST /api/v1/participants/{id}/start`
- **Response:** `200 OK`

#### Complete Quiz
- **Endpoint:** `POST /api/v1/participants/{id}/complete?finalScore={score}`
- **Response:** `200 OK`

---

### 6. Player Answers

#### Submit Answer
- **Endpoint:** `POST /api/v1/answers/submit`
- **Request Body:**
```json
{
  "questionId": "550e8400-e29b-41d4-a716-446655440001",
  "participantId": "550e8400-e29b-41d4-a716-446655440002",
  "answerText": "Paris",
  "timeTakenSeconds": 15
}
```
- **Response:** `201 Created`

#### Get Answer by ID
- **Endpoint:** `GET /api/v1/answers/{id}`
- **Response:** `200 OK`

#### Get Answers by Participant
- **Endpoint:** `GET /api/v1/answers/participant/{participantId}`
- **Response:** `200 OK`

#### Get Answers by Question
- **Endpoint:** `GET /api/v1/answers/question/{questionId}`
- **Response:** `200 OK`

#### Calculate Total Score
- **Endpoint:** `GET /api/v1/answers/participant/{participantId}/score`
- **Response:** `200 OK` (Integer)

---

### 7. Leaderboard

#### Create/Update Leaderboard Entry
- **Endpoint:** `POST /api/v1/leaderboard?quizId={quizId}&userId={userId}&score={score}&timeTakenSeconds={time}`
- **Response:** `201 Created`

#### Get Leaderboard Entry by ID
- **Endpoint:** `GET /api/v1/leaderboard/{id}`
- **Response:** `200 OK`

#### Get Full Leaderboard by Quiz
- **Endpoint:** `GET /api/v1/leaderboard/quiz/{quizId}`
- **Response:** `200 OK` (Sorted by score descending, then time ascending)

#### Get User's Rank
- **Endpoint:** `GET /api/v1/leaderboard/quiz/{quizId}/rank?userId={userId}`
- **Response:** `200 OK` (Integer)

#### Get Top Leaderboard Entries
- **Endpoint:** `GET /api/v1/leaderboard/quiz/{quizId}/top?limit=10`
- **Response:** `200 OK`

---

## Error Handling

All errors are returned in a standardized format:

```json
{
  "status": 404,
  "message": "Resource Not Found",
  "details": "Quiz not found with ID: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-02-23T10:30:00",
  "path": "/api/v1/quizzes/550e8400-e29b-41d4-a716-446655440000"
}
```

### Common HTTP Status Codes

- **200 OK** - Successful GET, PUT, POST request
- **201 Created** - Resource successfully created
- **204 No Content** - Successful DELETE request
- **400 Bad Request** - Invalid input or validation error
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error

---

## Data Types and Enums

### Question Types
- `yes_no` - Yes/No questions
- `multiple_choice` - Multiple choice questions
- `number` - Numeric answer questions
- `text` - Text answer questions

### User Roles
- `CREATOR` - Quiz creator role
- `PLAYER` - Quiz player role

### Participant Status
- `pending` - User joined but hasn't started
- `in_progress` - User is currently taking the quiz
- `completed` - User completed the quiz
- `abandoned` - User abandoned the quiz

---

## Validation Rules

### User
- Email: Must be a valid email format
- Password: Minimum 8 characters, maximum 50 characters
- Name: Minimum 2 characters, maximum 100 characters
- Role: Required (CREATOR or PLAYER)

### Quiz
- Title: Required, non-empty
- Start DateTime: Must be in the future
- End DateTime: Must be in the future
- Duration Minutes: Minimum 1 minute

### Question
- Question Text: Required, non-empty
- Question Type: Required (yes_no, multiple_choice, number, text)
- Time Limit: Minimum 1 second
- Display Order: Minimum 1

### Question Option
- Option Text: Required, non-empty
- Is Correct: Required
- Display Order: Minimum 1

### Player Answer
- Question ID: Required
- Participant ID: Required
- Answer Text: Required, non-empty
- Time Taken: Non-negative integer

---

## Example Usage Flow

### 1. Create a User (Creator)
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "creator@example.com",
    "password": "password123",
    "name": "Quiz Creator",
    "role": "CREATOR"
  }'
```

### 2. Create a Quiz
```bash
curl -X POST "http://localhost:8080/api/v1/quizzes?createdBy=550e8400-e29b-41d4-a716-446655440000" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "General Knowledge",
    "description": "Test your knowledge",
    "startDateTime": "2024-03-01T10:00:00",
    "endDateTime": "2024-03-01T12:00:00",
    "durationMinutes": 120
  }'
```

### 3. Create Questions
```bash
curl -X POST http://localhost:8080/api/v1/questions \
  -H "Content-Type: application/json" \
  -d '{
    "quizId": "550e8400-e29b-41d4-a716-446655440001",
    "questionText": "What is the capital of France?",
    "questionType": "multiple_choice",
    "timeLimitSeconds": 30,
    "displayOrder": 1
  }'
```

### 4. Create Question Options
```bash
curl -X POST http://localhost:8080/api/v1/question-options \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": "550e8400-e29b-41d4-a716-446655440002",
    "optionText": "Paris",
    "isCorrect": true,
    "displayOrder": 1
  }'
```

### 5. Create a Player User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "player@example.com",
    "password": "password123",
    "name": "Quiz Player",
    "role": "PLAYER"
  }'
```

### 6. Join Quiz
```bash
curl -X POST http://localhost:8080/api/v1/participants/join \
  -H "Content-Type: application/json" \
  -d '{
    "quizId": "550e8400-e29b-41d4-a716-446655440001",
    "userId": "550e8400-e29b-41d4-a716-446655440003"
  }'
```

### 7. Start Quiz
```bash
curl -X POST http://localhost:8080/api/v1/participants/550e8400-e29b-41d4-a716-446655440004/start
```

### 8. Submit Answer
```bash
curl -X POST http://localhost:8080/api/v1/answers/submit \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": "550e8400-e29b-41d4-a716-446655440002",
    "participantId": "550e8400-e29b-41d4-a716-446655440004",
    "answerText": "Paris",
    "timeTakenSeconds": 15
  }'
```

### 9. Complete Quiz
```bash
curl -X POST "http://localhost:8080/api/v1/participants/550e8400-e29b-41d4-a716-446655440004/complete?finalScore=100"
```

### 10. View Leaderboard
```bash
curl http://localhost:8080/api/v1/leaderboard/quiz/550e8400-e29b-41d4-a716-446655440001
```

---

## Swagger UI Features

The Swagger UI provides:

1. **Interactive API Documentation** - View all endpoints with detailed descriptions
2. **Try It Out** - Test API endpoints directly from the browser
3. **Request/Response Examples** - See example payloads and responses
4. **Parameter Validation** - Understand required and optional parameters
5. **Error Codes** - See possible error responses for each endpoint
6. **Schema Documentation** - View data model definitions

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

---

## API Architecture

### Layer Structure

1. **Controller Layer** - REST endpoints with Swagger annotations
2. **Service Layer** - Business logic and validation
3. **Repository Layer** - Data access using Spring Data JPA
4. **Entity Layer** - JPA entities with Lombok annotations
5. **DTO Layer** - Data transfer objects for API requests/responses
6. **Exception Layer** - Global exception handling

### Key Components

- **SwaggerConfig** - OpenAPI 3.0 configuration
- **GlobalExceptionHandler** - Centralized error handling
- **Validation** - Jakarta validation annotations on DTOs
- **Transactional Management** - @Transactional on service methods
- **Optimistic Locking** - Version field for concurrency control

---

## Performance Considerations

1. **Pagination** - Can be added to list endpoints for large datasets
2. **Caching** - Can be implemented using Spring Cache abstraction
3. **Database Indexing** - Indexes on frequently queried columns
4. **Connection Pooling** - Configured via HikariCP
5. **Lazy Loading** - JPA relationship loading strategy

---

## Security Recommendations

1. **Authentication** - Implement JWT or OAuth2
2. **Authorization** - Add role-based access control (RBAC)
3. **Password Hashing** - Use BCrypt for password storage
4. **HTTPS** - Use SSL/TLS in production
5. **Rate Limiting** - Implement to prevent abuse
6. **Input Validation** - Already implemented with Jakarta validation
7. **CORS** - Configure for cross-origin requests

---

## Future Enhancements

1. Image upload/download endpoints
2. Quiz templates and duplication
3. Analytics and statistics
4. Email notifications
5. Real-time updates using WebSocket
6. Quiz scheduling and automation
7. Advanced reporting and insights
8. Mobile app support
9. Internationalization (i18n)
10. API versioning strategy

---

## Support

For issues or questions regarding the API, please refer to the Swagger UI documentation or check the application logs for detailed error information.
