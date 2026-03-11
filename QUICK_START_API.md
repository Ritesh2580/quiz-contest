# Quiz Contest API - Quick Start Guide

## Start the Application

```bash
cd /root/.grok/worktrees/quiz-contest/ab-019c8e99-7fe9-7142-852f-453207de3e58-b
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## Access Swagger UI

Open your browser and navigate to:
```
http://localhost:8080/swagger-ui.html
```

You'll see an interactive API documentation page where you can:
- View all available endpoints
- See request/response schemas
- Test endpoints directly from the browser

---

## Quick Test Flow

### 1. Create a Creator User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "creator@test.com",
    "password": "password123",
    "name": "Quiz Creator",
    "role": "CREATOR"
  }'
```

**Response:** Save the returned `id` as `CREATOR_ID`

### 2. Create a Quiz
```bash
curl -X POST "http://localhost:8080/api/v1/quizzes?createdBy=CREATOR_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Science Quiz",
    "description": "Test your science knowledge",
    "startDateTime": "2025-03-01T10:00:00",
    "endDateTime": "2025-03-01T12:00:00",
    "durationMinutes": 120
  }'
```

**Response:** Save the returned `id` as `QUIZ_ID`

### 3. Create a Question
```bash
curl -X POST http://localhost:8080/api/v1/questions \
  -H "Content-Type: application/json" \
  -d '{
    "quizId": "QUIZ_ID",
    "questionText": "What is H2O?",
    "questionType": "multiple_choice",
    "timeLimitSeconds": 30,
    "displayOrder": 1
  }'
```

**Response:** Save the returned `id` as `QUESTION_ID`

### 4. Create Question Options
```bash
# Correct answer
curl -X POST http://localhost:8080/api/v1/question-options \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": "QUESTION_ID",
    "optionText": "Water",
    "isCorrect": true,
    "displayOrder": 1
  }'

# Wrong answers
curl -X POST http://localhost:8080/api/v1/question-options \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": "QUESTION_ID",
    "optionText": "Hydrogen",
    "isCorrect": false,
    "displayOrder": 2
  }'

curl -X POST http://localhost:8080/api/v1/question-options \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": "QUESTION_ID",
    "optionText": "Oxygen",
    "isCorrect": false,
    "displayOrder": 3
  }'
```

### 5. Create a Player User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "player@test.com",
    "password": "password123",
    "name": "Quiz Player",
    "role": "PLAYER"
  }'
```

**Response:** Save the returned `id` as `PLAYER_ID`

### 6. Player Joins Quiz
```bash
curl -X POST http://localhost:8080/api/v1/participants/join \
  -H "Content-Type: application/json" \
  -d '{
    "quizId": "QUIZ_ID",
    "userId": "PLAYER_ID"
  }'
```

**Response:** Save the returned `id` as `PARTICIPANT_ID`

### 7. Start Quiz
```bash
curl -X POST http://localhost:8080/api/v1/participants/PARTICIPANT_ID/start
```

### 8. Submit Answer
```bash
curl -X POST http://localhost:8080/api/v1/answers/submit \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": "QUESTION_ID",
    "participantId": "PARTICIPANT_ID",
    "answerText": "Water",
    "timeTakenSeconds": 15
  }'
```

### 9. Complete Quiz
```bash
curl -X POST "http://localhost:8080/api/v1/participants/PARTICIPANT_ID/complete?finalScore=10"
```

### 10. View Leaderboard
```bash
curl http://localhost:8080/api/v1/leaderboard/quiz/QUIZ_ID
```

---

## Key Endpoints Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/users` | Create user |
| GET | `/api/v1/users/{id}` | Get user |
| POST | `/api/v1/quizzes` | Create quiz |
| GET | `/api/v1/quizzes/{id}` | Get quiz |
| POST | `/api/v1/questions` | Create question |
| GET | `/api/v1/questions/quiz/{quizId}` | Get quiz questions |
| POST | `/api/v1/question-options` | Create option |
| POST | `/api/v1/participants/join` | Join quiz |
| POST | `/api/v1/participants/{id}/start` | Start quiz |
| POST | `/api/v1/answers/submit` | Submit answer |
| POST | `/api/v1/participants/{id}/complete` | Complete quiz |
| GET | `/api/v1/leaderboard/quiz/{quizId}` | View leaderboard |

---

## Using Swagger UI (Recommended)

1. Open `http://localhost:8080/swagger-ui.html`
2. Expand any endpoint by clicking on it
3. Click "Try it out" button
4. Fill in the required parameters
5. Click "Execute"
6. See the response immediately

This is the easiest way to test the API!

---

## Common Issues

### Port Already in Use
If port 8080 is already in use:
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8081
```

### Database Issues
The application uses H2 in-memory database which is automatically created. No setup needed.

### Validation Errors
Check that:
- Email format is valid (user@example.com)
- Password is 8+ characters
- DateTime format is correct (2025-03-01T10:00:00)
- All required fields are provided

---

## API Documentation Files

1. **API_DOCUMENTATION.md** - Complete reference with all endpoints
2. **API_LAYER_SUMMARY.md** - Implementation details
3. **QUICK_START_API.md** - This file

---

## Next Steps

- Explore Swagger UI to test all endpoints
- Read API_DOCUMENTATION.md for detailed endpoint information
- Check API_LAYER_SUMMARY.md for architecture details
- Implement authentication for production use
- Add image upload/download functionality

---

## Support

For detailed API information, always refer to:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

The Swagger UI provides the most up-to-date and interactive documentation!
