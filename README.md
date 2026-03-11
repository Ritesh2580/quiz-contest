# Quiz Contest Application

A multi-user quiz hosting platform where creators can design and host quizzes with various question types, and players can participate in timed quiz competitions.

## Database Design

### Overview
The database is designed to support two main user types:
- **Quiz Creators**: Can create and manage quizzes and questions
- **Quiz Players**: Can participate in active quizzes and submit answers

### Core Tables

#### 1. **Users Table**
Stores user information with role-based access control and versioning for concurrency control.

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('creator', 'player', 'admin') NOT NULL DEFAULT 'player',
    full_name VARCHAR(255),
    version INTEGER DEFAULT 1,
    -- Optimistic locking version for concurrency control
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

**Fields:**
- `id`: Unique identifier (UUID)
- `username`: Unique username for login
- `email`: User email address
- `password_hash`: Hashed password
- `role`: User type (creator, player, or admin)
- `full_name`: User's display name
- `version`: Optimistic locking version (incremented on each update)
- `created_at`: Account creation timestamp
- `updated_at`: Last update timestamp
- `is_active`: Account status flag

---

#### 2. **Quizzes Table**
Stores quiz metadata and scheduling information with banner image and versioning.

```sql
CREATE TABLE quizzes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creator_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    banner_image_id UUID,
    -- Reference to quiz_images table for banner image
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP NOT NULL,
    duration_minutes INTEGER,
    is_published BOOLEAN DEFAULT FALSE,
    total_points INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    -- Optimistic locking version for concurrency control
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_quizzes_creator_id ON quizzes(creator_id);
CREATE INDEX idx_quizzes_start_datetime ON quizzes(start_datetime);
CREATE INDEX idx_quizzes_end_datetime ON quizzes(end_datetime);
CREATE INDEX idx_quizzes_banner_image_id ON quizzes(banner_image_id);
```

**Fields:**
- `id`: Unique quiz identifier
- `creator_id`: Reference to the creator user
- `title`: Quiz name
- `description`: Quiz description
- `banner_image_id`: Reference to quiz_images table for banner image
- `start_datetime`: When the quiz becomes available
- `end_datetime`: When the quiz closes
- `duration_minutes`: Optional time limit for completing the quiz
- `is_published`: Whether the quiz is active/visible
- `total_points`: Sum of all question points
- `version`: Optimistic locking version (incremented on each update)
- `created_at`: Quiz creation timestamp
- `updated_at`: Last modification timestamp

---

#### 3. **Question Types Enumeration**
Defines the types of questions supported.

```sql
CREATE TYPE question_type_enum AS ENUM (
    'yes_no',
    'multiple_choice',
    'number',
    'text'
);

-- Question Type Reference:
-- yes_no: Yes/No (Boolean) - Can have images for each option
-- multiple_choice: Multiple Choice - Can have images for each option
-- number: Numeric answer
-- text: Free form text answer
```

---

#### 4. **Questions Table**
Stores individual questions with their configurations and versioning.

```sql
CREATE TABLE questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_type question_type_enum NOT NULL,
    -- question_type: yes_no, multiple_choice, number, text
    points INTEGER DEFAULT 1,
    order_index INTEGER NOT NULL,
    is_trivia BOOLEAN DEFAULT FALSE,
    trivia_time_seconds INTEGER,
    -- trivia_time_seconds: If is_trivia=true, time limit for answering
    version INTEGER DEFAULT 1,
    -- Optimistic locking version for concurrency control
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_questions_quiz_id ON questions(quiz_id);
CREATE INDEX idx_questions_order_index ON questions(quiz_id, order_index);
```

**Fields:**
- `id`: Unique question identifier
- `quiz_id`: Reference to parent quiz
- `question_text`: The actual question content
- `question_type`: Type of question (yes_no, multiple_choice, number, text)
- `points`: Points awarded for correct answer
- `order_index`: Position in quiz sequence
- `is_trivia`: Whether this is a timed trivia question
- `trivia_time_seconds`: Time limit if trivia question
- `version`: Optimistic locking version (incremented on each update)
- `created_at`: Question creation timestamp
- `updated_at`: Last modification timestamp

---

#### 5. **Question Options Table**
Stores answer options for multiple choice and yes/no questions with images and versioning.

```sql
CREATE TABLE question_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_text VARCHAR(500) NOT NULL,
    option_image_id UUID,
    -- Reference to quiz_images table for option image
    option_index SMALLINT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 1,
    -- Optimistic locking version for concurrency control
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_question_options_question_id ON question_options(question_id);
CREATE INDEX idx_question_options_image_id ON question_options(option_image_id);
CREATE UNIQUE INDEX idx_question_options_unique ON question_options(question_id, option_index);
```

**Fields:**
- `id`: Unique option identifier
- `question_id`: Reference to parent question
- `option_text`: The option text/label
- `option_image_id`: Reference to quiz_images table for option image
- `option_index`: Position in options list
- `is_correct`: Whether this is the correct answer
- `version`: Optimistic locking version (incremented on each update)
- `created_at`: Option creation timestamp
- `updated_at`: Last modification timestamp

**Example Use Case:**
For "Who will win today's football match?" (Yes/No question):
- Option 1: "Team A" with `option_image_id` referencing Team A's logo image
- Option 2: "Team B" with `option_image_id` referencing Team B's logo image

---

#### 5.5 **Quiz Images Table** (Unified Image Storage)
Stores all images (quiz banners, question context images, option images) in the database with binary data.

```sql
CREATE TABLE quiz_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    image_data BYTEA NOT NULL,
    -- Binary image data (JPEG, PNG, GIF, WebP, etc.)
    image_mime_type VARCHAR(50) NOT NULL,
    -- MIME type (e.g., 'image/jpeg', 'image/png', 'image/webp')
    image_name VARCHAR(255),
    -- Original image filename
    image_alt_text VARCHAR(500),
    -- Alternative text for accessibility
    image_size_bytes INTEGER,
    -- Size of the image in bytes
    image_width INTEGER,
    -- Image width in pixels
    image_height INTEGER,
    -- Image height in pixels
    image_index SMALLINT DEFAULT 0,
    -- Order/position of images (for multiple images per question)
    image_type VARCHAR(50) NOT NULL,
    -- Type: 'quiz_banner', 'question_image', 'option_image'
    linked_question_id UUID,
    -- Reference to question (for question and option images)
    linked_option_id UUID,
    -- Reference to option (for option images)
    version INTEGER DEFAULT 1,
    -- Optimistic locking version for concurrency control
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_quiz_images_quiz_id ON quiz_images(quiz_id);
CREATE INDEX idx_quiz_images_question_id ON quiz_images(linked_question_id);
CREATE INDEX idx_quiz_images_option_id ON quiz_images(linked_option_id);
CREATE INDEX idx_quiz_images_type ON quiz_images(image_type);
CREATE INDEX idx_quiz_images_index ON quiz_images(linked_question_id, image_index);
```

**Fields:**
- `id`: Unique image identifier
- `quiz_id`: Reference to parent quiz (for data organization)
- `image_data`: Binary image data (BYTEA type)
- `image_mime_type`: MIME type of the image (image/jpeg, image/png, etc.)
- `image_name`: Original filename
- `image_alt_text`: Alternative text for accessibility
- `image_size_bytes`: Size of the image in bytes
- `image_width`: Image width in pixels
- `image_height`: Image height in pixels
- `image_index`: Order/position of images
- `image_type`: Type of image (quiz_banner, question_image, option_image)
- `linked_question_id`: Reference to question (if question or option image)
- `linked_option_id`: Reference to option (if option image)
- `version`: Optimistic locking version (incremented on each update)
- `created_at`: Image creation timestamp
- `updated_at`: Last modification timestamp

**Use Cases:**
- Quiz banner images
- Reference images for context (diagrams, maps, scenario images)
- Multiple images showing different perspectives
- Option images (Team logos, product images, etc.)

---

#### 6. **Correct Answers Table**
Stores the correct answers for questions (supports multiple correct answers) with versioning.

```sql
CREATE TABLE correct_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    answer_type question_type_enum NOT NULL,
    -- answer_type: yes_no, multiple_choice, number, text
    answer_value VARCHAR(500),
    -- For yes_no: 'true' or 'false'
    -- For multiple_choice: option_id (UUID)
    -- For number: numeric value as string
    -- For text: the exact/partial text to match
    is_case_sensitive BOOLEAN DEFAULT FALSE,
    version INTEGER DEFAULT 1,
    -- Optimistic locking version for concurrency control
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_correct_answers_question_id ON correct_answers(question_id);
```

**Fields:**
- `id`: Unique answer identifier
- `question_id`: Reference to question
- `answer_type`: Type of answer (yes_no, multiple_choice, number, text)
- `answer_value`: The actual correct answer
- `is_case_sensitive`: For text answers, whether to match case
- `version`: Optimistic locking version (incremented on each update)
- `created_at`: Creation timestamp
- `updated_at`: Last modification timestamp

---

#### 7. **Quiz Participants Table**
Tracks which players are participating in which quizzes with versioning for concurrency control.

```sql
CREATE TABLE quiz_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    player_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    total_score INTEGER DEFAULT 0,
    status ENUM('joined', 'in_progress', 'completed', 'abandoned') DEFAULT 'joined',
    version INTEGER DEFAULT 1,
    -- Optimistic locking version for concurrency control
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_quiz_participants_quiz_id ON quiz_participants(quiz_id);
CREATE INDEX idx_quiz_participants_player_id ON quiz_participants(player_id);
CREATE UNIQUE INDEX idx_quiz_participants_unique ON quiz_participants(quiz_id, player_id);
```

**Fields:**
- `id`: Unique participant record identifier
- `quiz_id`: Reference to quiz
- `player_id`: Reference to player user
- `joined_at`: When player registered for the quiz
- `started_at`: When player started the quiz
- `completed_at`: When player finished the quiz
- `total_score`: Final score for this player
- `status`: Current status of participation
- `version`: Optimistic locking version (incremented on each update)
- `created_at`: Record creation timestamp
- `updated_at`: Last modification timestamp

---

#### 8. **Player Answers Table**
Stores individual answers submitted by players for each question with versioning.

```sql
CREATE TABLE player_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_participant_id UUID NOT NULL REFERENCES quiz_participants(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    answer_value VARCHAR(1000),
    -- The player's answer (format depends on question type)
    is_correct BOOLEAN,
    points_earned INTEGER DEFAULT 0,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    time_taken_seconds INTEGER,
    -- How long the player took to answer (for trivia tracking)
    version INTEGER DEFAULT 1,
    -- Optimistic locking version for concurrency control
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_player_answers_quiz_participant_id ON player_answers(quiz_participant_id);
CREATE INDEX idx_player_answers_question_id ON player_answers(question_id);
CREATE UNIQUE INDEX idx_player_answers_unique ON player_answers(quiz_participant_id, question_id);
```

**Fields:**
- `id`: Unique answer submission identifier
- `quiz_participant_id`: Reference to participant record
- `question_id`: Reference to question
- `answer_value`: The answer submitted by player
- `is_correct`: Whether the answer is correct
- `points_earned`: Points awarded for this answer
- `answered_at`: When the answer was submitted
- `time_taken_seconds`: Time spent on this question
- `version`: Optimistic locking version (incremented on each update)
- `created_at`: Record creation timestamp
- `updated_at`: Last modification timestamp

---

#### 9. **Quiz Results/Leaderboard Table**
Denormalized table for quick leaderboard queries with versioning for concurrency control.

```sql
CREATE TABLE quiz_leaderboard (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    player_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rank INTEGER,
    total_score INTEGER,
    total_questions_correct INTEGER,
    total_questions_attempted INTEGER,
    completion_time_minutes INTEGER,
    version INTEGER DEFAULT 1,
    -- Optimistic locking version for concurrency control
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_quiz_leaderboard_quiz_id ON quiz_leaderboard(quiz_id);
CREATE UNIQUE INDEX idx_quiz_leaderboard_unique ON quiz_leaderboard(quiz_id, player_id);
```

**Fields:**
- `id`: Unique leaderboard entry identifier
- `quiz_id`: Reference to quiz
- `player_id`: Reference to player
- `rank`: Player's rank in the quiz
- `total_score`: Final score
- `total_questions_correct`: Number of correct answers
- `total_questions_attempted`: Number of questions answered
- `completion_time_minutes`: Time taken to complete
- `version`: Optimistic locking version (incremented on each update)
- `created_at`: Entry creation timestamp
- `updated_at`: Last update timestamp

---

### Relationships Diagram

```
Users (1) ──────────── (Many) Quizzes
  │
  ├─── (1) ──────────── (Many) Quiz_Participants
  │
  └─── (1) ──────────── (Many) Quiz_Leaderboard

Quizzes (1) ──────────── (Many) Questions
Quizzes (1) ──────────── (Many) Quiz_Participants
Quizzes (1) ──────────── (Many) Quiz_Images

Questions (1) ──────────── (Many) Question_Options
Questions (1) ──────────── (Many) Correct_Answers
Questions (1) ──────────── (Many) Player_Answers
Questions (1) ──────────── (Many) Quiz_Images (linked_question_id)

Question_Options (1) ──────────── (Many) Quiz_Images (linked_option_id)

Quiz_Participants (1) ──────────── (Many) Player_Answers
```

---

### Data Flow

#### Quiz Creation Flow:
1. Creator creates a Quiz
2. Creator adds Questions to the Quiz
3. For each Question:
   - Define question type
   - Add Question_Options (if multiple choice/yes-no)
   - Add Correct_Answers
   - Configure trivia timing if needed
4. Creator publishes the Quiz

#### Quiz Participation Flow:
1. Player joins Quiz → Quiz_Participants record created
2. Player starts Quiz → started_at timestamp set
3. Player answers Question → Player_Answers record created
4. System evaluates answer against Correct_Answers
5. Points awarded
6. Player completes Quiz → completed_at timestamp set, status updated
7. Leaderboard entry created/updated

---

### Practical Examples

#### Example 1: Football Match Prediction Quiz with Images

**Quiz Setup:**
```
Title: "Predict Today's Matches"
Banner Image: https://example.com/images/banner-football.jpg
Start: 2024-02-23 14:00:00 UTC
End: 2024-02-23 16:00:00 UTC
```

**Question 1: Yes/No with Team Images**
```
Question Text: "Will Team A beat Team B today?"
Question Type: yes_no
Is Trivia: true
Trivia Time: 30 seconds

Options:
- Option 1: "Yes" 
  - option_image_url: https://example.com/images/team-a-logo.jpg
  - is_correct: true

- Option 2: "No"
  - option_image_url: https://example.com/images/team-b-logo.jpg
  - is_correct: false
```

#### Example 2: History Question with Multiple Reference Images

**Question 2: Multiple Choice with Context Images**
```
Question Text: "Which monument is this?"
Question Type: multiple_choice
Points: 5

Question Images (for context):
- Image 1: https://example.com/images/monument-front.jpg
- Image 2: https://example.com/images/monument-side.jpg

Options:
- Option 1: "Eiffel Tower"
  - option_image_url: https://example.com/images/eiffel-thumbnail.jpg
  - is_correct: true

- Option 2: "Statue of Liberty"
  - option_image_url: https://example.com/images/liberty-thumbnail.jpg
  - is_correct: false

- Option 3: "Big Ben"
  - option_image_url: https://example.com/images/bigben-thumbnail.jpg
  - is_correct: false
```

#### Example 3: Sports Quiz with Number and Text Questions

**Question 3: Number Type**
```
Question Text: "How many goals did Player X score this season?"
Question Type: number
Points: 3
Is Trivia: true
Trivia Time: 15 seconds

Correct Answer: 25
```

**Question 4: Text Type**
```
Question Text: "What is the name of the stadium?"
Question Type: text
Points: 2

Correct Answer: "Old Trafford"
is_case_sensitive: false
```

---

### Key Features Supported

✅ Multiple quiz types and scheduling (start/end datetime)
✅ Quiz banner images stored in database (BYTEA format)
✅ Multiple question types (Yes/No, Multiple Choice, Number, Text) using enums
✅ Multiple images per question stored in database
✅ Image support for question options stored in database
✅ Configurable trivia timing per question
✅ Multiple correct answers support
✅ Role-based user management (Creator vs Player)
✅ Answer tracking with scoring
✅ Time tracking (question duration, quiz completion time)
✅ Leaderboard generation
✅ Case-sensitive text matching option
✅ Cascading deletes for data integrity
✅ **Versioning in all tables for optimistic locking and concurrency control**
✅ **Binary image storage in database (JPEG, PNG, GIF, WebP, etc.)**
✅ **Image metadata (MIME type, dimensions, size, alt text)**

---

### Indexing Strategy

All foreign keys and frequently queried columns are indexed for optimal performance:
- User lookups by ID
- Quiz lookups by creator, start/end dates
- Questions ordered by quiz
- Participant lookups by quiz and player
- Answer lookups by participant and question
- Leaderboard queries by quiz
- Image lookups by quiz, question, and option

---

## Versioning & Concurrency Control Strategy

### Overview
Every table includes a `version` column (INTEGER DEFAULT 1) for **optimistic locking**. This prevents concurrent update conflicts when multiple users modify the same record simultaneously.

### How Optimistic Locking Works

1. **Read**: Application fetches record with current `version` value
2. **Modify**: Application makes changes to the record
3. **Update**: Application sends UPDATE with WHERE clause checking version:
   ```sql
   UPDATE table_name 
   SET field1 = $1, version = version + 1, updated_at = CURRENT_TIMESTAMP
   WHERE id = $2 AND version = $3
   ```
4. **Verify**: If no rows updated (version mismatch), handle conflict:
   - Notify user of concurrent modification
   - Fetch latest version
   - Retry or merge changes

### Tables with Versioning

| Table | Purpose | Conflict Risk |
|-------|---------|---------------|
| users | User profile updates | Medium |
| quizzes | Quiz metadata changes | High (during creation) |
| questions | Question content edits | High (during quiz setup) |
| question_options | Option edits | High (during quiz setup) |
| correct_answers | Answer definition changes | High (during quiz setup) |
| quiz_images | Image updates | Medium |
| quiz_participants | Score/status updates | High (during quiz play) |
| player_answers | Answer submission | High (during quiz play) |
| quiz_leaderboard | Ranking updates | High (after quiz completion) |

### Benefits

✅ **No Pessimistic Locking**: No row locks, better concurrent performance
✅ **Conflict Detection**: Automatically detects concurrent modifications
✅ **Data Integrity**: Prevents lost updates and race conditions
✅ **Scalability**: Works well with distributed systems
✅ **Audit Trail**: Track how many times a record was modified

### Implementation Pattern (Pseudo-code)

```javascript
// Read
const quiz = await db.quizzes.findById(quizId);
const currentVersion = quiz.version;

// Modify
quiz.title = "Updated Title";

// Update with version check
const result = await db.quizzes.update(
  { id: quizId, version: currentVersion },
  { title: quiz.title, version: currentVersion + 1 }
);

if (result.affectedRows === 0) {
  // Version mismatch - concurrent modification detected
  throw new ConcurrentModificationError("Quiz was modified by another user");
}
```

---

## Image Storage Strategy

### Overview
All images (quiz banners, question context images, option images) are stored directly in the database as **binary data (BYTEA)** in the unified `quiz_images` table.

### Benefits of In-Database Storage

✅ **Self-Contained**: No external dependencies or CDN required
✅ **Atomic Transactions**: Images and metadata update together
✅ **Access Control**: Database-level security for images
✅ **Backup Simplicity**: Single database backup includes all images
✅ **GDPR Compliance**: All data in one location for deletion
✅ **No Network Calls**: Faster image retrieval
✅ **Versioning**: Image changes tracked with version field

### Supported Image Formats

The `quiz_images` table supports any image format via MIME type:
- `image/jpeg` - JPEG/JPG images
- `image/png` - PNG images
- `image/gif` - GIF images
- `image/webp` - WebP images
- `image/svg+xml` - SVG images
- Any other standard MIME type

### Quiz Images Table Structure

```
id                  - Unique identifier
quiz_id             - Parent quiz (for data organization)
image_data          - Binary image data (BYTEA)
image_mime_type     - MIME type (e.g., 'image/jpeg')
image_name          - Original filename
image_alt_text      - Accessibility text
image_size_bytes    - Image size in bytes
image_width         - Width in pixels
image_height        - Height in pixels
image_index         - Order/position
image_type          - Type: 'quiz_banner', 'question_image', 'option_image'
linked_question_id  - Reference to question (if applicable)
linked_option_id    - Reference to option (if applicable)
version             - Optimistic locking version
created_at          - Creation timestamp
updated_at          - Last modification timestamp
```

### Image Type Reference

| Type | Usage | Example |
|------|-------|---------|
| `quiz_banner` | Quiz header/cover image | Quiz logo or theme image |
| `question_image` | Context image for question | Diagram, map, scenario photo |
| `option_image` | Visual representation of option | Team logo, product image |

### Performance Considerations

**Advantages:**
- Single database query retrieves image + metadata
- No separate CDN latency
- Transactional consistency

**Considerations:**
- BYTEA storage increases database size
- Consider archiving old quizzes to separate storage
- Use pagination for image-heavy queries
- Consider image compression before storage

### Recommended Implementation

1. **Compression**: Compress images before storing in BYTEA
2. **Validation**: Validate MIME type and file size before storage
3. **Indexing**: Index by image_type and linked IDs for quick retrieval
4. **Archiving**: Archive images from completed quizzes after retention period
5. **Caching**: Cache frequently accessed images in application memory

---

### Notes for Implementation

1. **Timestamps**: All timestamps use UTC for consistency
2. **UUIDs**: All primary keys use UUID v4 for distributed systems support
3. **Enums**: Database enums used for `question_type_enum`, `role`, and `status`
4. **Versioning**: All tables include `version` column for optimistic locking
   - Always check version in WHERE clause during updates
   - Increment version on successful update
   - Handle version mismatch errors in application
5. **Image Storage**: Use `quiz_images` table for all images
   - Store binary data in `image_data` (BYTEA)
   - Include MIME type for proper rendering
   - Compress images before storage if needed
   - Index by image_type and linked IDs
6. **Validation**: Application should validate:
   - end_datetime > start_datetime
   - order_index uniqueness per quiz
   - At least one correct answer per question
   - Image file size and MIME type before storage
   - Version matches before update operations
7. **Soft Deletes**: Consider adding `deleted_at` columns if audit trail is needed
8. **Constraints**: Add CHECK constraints for positive integers (points, durations)
9. **Transactions**: Wrap quiz creation and answer submission in transactions
10. **Image Handling**: 
    - Validate image dimensions and size
    - Store image metadata (width, height, size_bytes)
    - Support image compression before storage

---

## Summary of Design Updates

### New Features Added:

1. **Binary Image Storage in Database** ✨ UPDATED
   - Unified `quiz_images` table with BYTEA binary storage
   - Stores quiz banners, question images, and option images
   - Supports JPEG, PNG, GIF, WebP, SVG, and other formats
   - Includes image metadata (MIME type, dimensions, size, alt text)
   - No external storage required - self-contained application

2. **Versioning for Concurrency Control** ✨ NEW
   - Added `version` column to all 9 tables
   - Implements optimistic locking pattern
   - Prevents concurrent modification conflicts
   - Tracks modification history via version increments
   - Works seamlessly with distributed systems

3. **Question Type Enum** 🔄 REFACTORED
   - Converted `question_type` from SMALLINT to `question_type_enum`
   - Values: `yes_no`, `multiple_choice`, `number`, `text`
   - Better type safety and readability
   - Also applied to `correct_answers.answer_type`

4. **Enhanced Question Options**
   - Updated to reference `quiz_images` table for images
   - Perfect for visual-based questions (e.g., "Which team will win?" with team logos)
   - Supports both yes/no and multiple choice questions

### Total Database Tables: 10

| # | Table Name | Purpose | Versioning |
|---|---|---|---|
| 1 | `users` | User management (creators, players, admins) | ✅ |
| 2 | `quizzes` | Quiz metadata with banner image references | ✅ |
| 3 | `questions` | Individual questions with enum types | ✅ |
| 4 | `question_options` | Answer options with image references | ✅ |
| 5 | `quiz_images` | Binary image storage (BYTEA) with metadata | ✅ |
| 6 | `correct_answers` | Correct answer definitions | ✅ |
| 7 | `quiz_participants` | Player participation tracking | ✅ |
| 8 | `player_answers` | Individual answer submissions | ✅ |
| 9 | `quiz_leaderboard` | Denormalized ranking data | ✅ |

### Image Storage Strategy:

**Binary In-Database Storage**: All images stored as BYTEA in `quiz_images` table. This approach:
- ✅ Self-contained application (no external dependencies)
- ✅ Atomic transactions (images and metadata update together)
- ✅ Database-level access control and security
- ✅ Single backup includes all images
- ✅ GDPR compliant (all data in one location)
- ✅ Transactional consistency
- ✅ Versioning support for image changes

**Supported Formats:**
- JPEG/JPG images (`image/jpeg`)
- PNG images (`image/png`)
- GIF images (`image/gif`)
- WebP images (`image/webp`)
- SVG images (`image/svg+xml`)
- Any standard MIME type

### Use Case Support:

✅ **Football Match Prediction**: Team logos as yes/no options
✅ **History Quiz**: Monument images with multiple reference photos
✅ **Sports Trivia**: Team/player images with timed questions
✅ **General Knowledge**: Diagrams and maps as context
✅ **Product Quiz**: Product images as options
✅ **Brand Recognition**: Logo identification questions

---

## Quick Reference: Enum Values

### Question Type Enum (`question_type_enum`)
```
yes_no          → Boolean questions with 2 options
multiple_choice → Multiple choice questions with 3+ options
number          → Numeric answer questions
text            → Free-form text answer questions
```

### User Role Enum
```
creator → Can create and manage quizzes
player  → Can participate in quizzes
admin   → Full system access
```

### Participant Status Enum
```
joined      → User has joined but not started
in_progress → User is actively answering questions
completed   → User has finished the quiz
abandoned   → User started but didn't complete
```

---

## Database Creation Order

When implementing this schema, create tables in this order to respect foreign key dependencies:

1. `users` (no dependencies)
2. `quizzes` (depends on users)
3. `questions` (depends on quizzes)
4. `question_options` (depends on questions)
5. `question_images` (depends on questions)
6. `correct_answers` (depends on questions)
7. `quiz_participants` (depends on quizzes, users)
8. `player_answers` (depends on quiz_participants, questions)
9. `quiz_leaderboard` (depends on quizzes, users)

---

## SQL Enum Creation (PostgreSQL)

```sql
-- Create enums first (before tables)
CREATE TYPE question_type_enum AS ENUM (
    'yes_no',
    'multiple_choice',
    'number',
    'text'
);

CREATE TYPE user_role_enum AS ENUM (
    'creator',
    'player',
    'admin'
);

CREATE TYPE participant_status_enum AS ENUM (
    'joined',
    'in_progress',
    'completed',
    'abandoned'
);
```

---

## Performance Considerations

### Indexing
- All foreign keys are indexed
- Composite indexes on frequently joined columns
- Indexes on datetime fields for range queries
- Indexes on order fields for sorting

### Query Optimization Tips
1. Use `quiz_leaderboard` table for leaderboard queries (denormalized)
2. Cache quiz data during active quiz period
3. Use pagination for large result sets
4. Consider materialized views for complex reports

### Scaling Recommendations
- Partition `player_answers` table by `quiz_id` or date range
- Archive old quiz data to separate storage
- Use read replicas for leaderboard queries
- Cache frequently accessed quizzes in Redis

---

## Design Changes Summary

### What Changed from Initial Design:

#### 1. **Quiz Banner Images** ✨ NEW
- Added `banner_image_url` field to `quizzes` table
- Allows visual branding and quiz identification
- Supports any image URL (S3, CDN, etc.)

#### 2. **Question Images** ✨ NEW TABLE
- Created new `question_images` table
- Supports multiple images per question
- Includes `image_alt_text` for accessibility
- Perfect for context, diagrams, maps, scenarios

#### 3. **Question Option Images** ✨ NEW FIELD
- Added `option_image_url` to `question_options` table
- Enables visual-based multiple choice and yes/no questions
- Example: Football teams as images for "Who will win?" question

#### 4. **Question Type Enum** 🔄 REFACTORED
- Changed from SMALLINT (0, 1, 2, 3) to `question_type_enum`
- New values: `yes_no`, `multiple_choice`, `number`, `text`
- Better type safety and code readability
- Also applied to `correct_answers.answer_type`

### Benefits of These Changes:

| Feature | Benefit |
|---------|---------|
| Banner Images | Visual appeal, quiz branding, better UX |
| Question Images | Context support, better question clarity |
| Option Images | Visual-based questions, improved engagement |
| Question Type Enum | Type safety, readability, maintainability |

### Database Statistics:

- **Total Tables**: 9 (10 with enums)
- **Total Indexes**: 15+
- **Foreign Keys**: 11
- **Enums**: 3 (`question_type_enum`, `user_role_enum`, `participant_status_enum`)
- **Image Fields**: 3 (`banner_image_url`, `option_image_url`, `image_url`)

### Migration Path (if upgrading from old schema):

```sql
-- Step 1: Create enums
CREATE TYPE question_type_enum AS ENUM ('yes_no', 'multiple_choice', 'number', 'text');

-- Step 2: Add banner_image_url to quizzes
ALTER TABLE quizzes ADD COLUMN banner_image_url VARCHAR(1000);

-- Step 3: Create question_images table
CREATE TABLE question_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL,
    image_alt_text VARCHAR(500),
    image_index SMALLINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Step 4: Add option_image_url to question_options
ALTER TABLE question_options ADD COLUMN option_image_url VARCHAR(1000);

-- Step 5: Migrate question_type from SMALLINT to enum
-- (Requires careful migration with data mapping)
```

---

## Next Steps for Implementation

1. **Review this design** - Ensure it meets all requirements
2. **Create migration scripts** - For your target database (PostgreSQL, MySQL, etc.)
3. **Implement API endpoints** - For quiz creation and participation
4. **Build frontend** - Quiz creator interface and player interface
5. **Setup image storage** - Configure S3/CDN for image hosting
6. **Add validation** - Image URL validation, enum constraints
7. **Setup caching** - Redis for quiz data and leaderboards
8. **Add monitoring** - Database performance tracking

---

## File Structure for Implementation

```
quiz-contest/
├── README.md (this file - database design)
├── database/
│   ├── migrations/
│   │   ├── 001_create_enums.sql
│   │   ├── 002_create_users.sql
│   │   ├── 003_create_quizzes.sql
│   │   ├── 004_create_questions.sql
│   │   ├── 005_create_question_options.sql
│   │   ├── 006_create_question_images.sql
│   │   ├── 007_create_correct_answers.sql
│   │   ├── 008_create_quiz_participants.sql
│   │   ├── 009_create_player_answers.sql
│   │   └── 010_create_quiz_leaderboard.sql
│   └── seed/ (optional test data)
├── src/
│   ├── api/
│   │   ├── quiz-creator/ (Part-1: Creator endpoints)
│   │   └── quiz-player/ (Part-2: Player endpoints)
│   ├── models/ (Database models/ORM)
│   ├── services/ (Business logic)
│   └── utils/ (Helpers, validators)
└── docs/
    └── API.md (API documentation)
```

---

## Visual Schema Diagram (Text Format)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         QUIZ CONTEST DATABASE                           │
└─────────────────────────────────────────────────────────────────────────┘

┌──────────────────┐
│     USERS        │
├──────────────────┤
│ id (PK)          │
│ username         │
│ email            │
│ password_hash    │
│ role (enum)      │◄─────────────────┐
│ full_name        │                  │
│ created_at       │                  │
│ updated_at       │                  │
│ is_active        │                  │
└──────────────────┘                  │
         │                             │
         │ creator_id                  │ player_id
         │                             │
         ▼                             │
┌──────────────────────────────┐      │
│      QUIZZES                 │      │
├──────────────────────────────┤      │
│ id (PK)                      │      │
│ creator_id (FK) ─────────────┤      │
│ title                        │      │
│ description                  │      │
│ banner_image_url ✨ NEW      │      │
│ start_datetime               │      │
│ end_datetime                 │      │
│ duration_minutes             │      │
│ is_published                 │      │
│ total_points                 │      │
│ created_at                   │      │
│ updated_at                   │      │
└──────────────────────────────┘      │
         │                             │
         │ quiz_id                     │
         │                             │
         ▼                             │
┌──────────────────────────────┐      │
│     QUESTIONS                │      │
├──────────────────────────────┤      │
│ id (PK)                      │      │
│ quiz_id (FK)                 │      │
│ question_text                │      │
│ question_type (enum) 🔄 NEW  │      │
│ points                       │      │
│ order_index                  │      │
│ is_trivia                    │      │
│ trivia_time_seconds          │      │
│ created_at                   │      │
│ updated_at                   │      │
└──────────────────────────────┘      │
    │              │                   │
    │ question_id  │ question_id       │
    │              │                   │
    ▼              ▼                   │
┌─────────────────────┐   ┌──────────────────────┐
│ QUESTION_OPTIONS    │   │ QUESTION_IMAGES      │
├─────────────────────┤   ├──────────────────────┤
│ id (PK)             │   │ id (PK)              │
│ question_id (FK)    │   │ question_id (FK)     │
│ option_text         │   │ image_url ✨ NEW     │
│ option_image_url ✨ │   │ image_alt_text       │
│ option_index        │   │ image_index          │
│ is_correct          │   │ created_at           │
│ created_at          │   └──────────────────────┘
└─────────────────────┘

         │ question_id
         │
         ▼
┌──────────────────────────────┐
│   CORRECT_ANSWERS            │
├──────────────────────────────┤
│ id (PK)                      │
│ question_id (FK)             │
│ answer_type (enum) 🔄 NEW    │
│ answer_value                 │
│ is_case_sensitive            │
│ created_at                   │
└──────────────────────────────┘


┌──────────────────────────────┐
│   QUIZ_PARTICIPANTS          │
├──────────────────────────────┤
│ id (PK)                      │
│ quiz_id (FK) ──────────────┐ │
│ player_id (FK) ────────────┤─┤──────────┐
│ joined_at                  │ │          │
│ started_at                 │ │          │
│ completed_at               │ │          │
│ total_score                │ │          │
│ status (enum)              │ │          │
│ created_at                 │ │          │
└──────────────────────────────┘ │          │
         │                        │          │
         │ quiz_participant_id    │          │
         │                        │          │
         ▼                        │          │
┌──────────────────────────────┐ │          │
│   PLAYER_ANSWERS             │ │          │
├──────────────────────────────┤ │          │
│ id (PK)                      │ │          │
│ quiz_participant_id (FK)     │ │          │
│ question_id (FK)             │ │          │
│ answer_value                 │ │          │
│ is_correct                   │ │          │
│ points_earned                │ │          │
│ answered_at                  │ │          │
│ time_taken_seconds           │ │          │
│ created_at                   │ │          │
└──────────────────────────────┘ │          │
                                  │          │
                                  │          │
┌──────────────────────────────┐  │          │
│   QUIZ_LEADERBOARD           │  │          │
├──────────────────────────────┤  │          │
│ id (PK)                      │  │          │
│ quiz_id (FK) ────────────────┼──┘          │
│ player_id (FK) ──────────────┼─────────────┘
│ rank                         │
│ total_score                  │
│ total_questions_correct      │
│ total_questions_attempted    │
│ completion_time_minutes      │
│ created_at                   │
│ updated_at                   │
└──────────────────────────────┘

Legend:
  ✨ NEW   = Added in this update
  🔄 NEW  = Changed from initial design
  (PK)    = Primary Key
  (FK)    = Foreign Key
```

---

**Database Design Status**: ✅ Complete and Ready for Review

---

## Quick Reference Card

### Image Fields Summary

| Table | Field | Type | Purpose | Example |
|-------|-------|------|---------|---------|
| `quizzes` | `banner_image_url` | VARCHAR(1000) | Quiz branding | `https://cdn.example.com/quiz-banner.jpg` |
| `question_options` | `option_image_url` | VARCHAR(1000) | Option visual | `https://cdn.example.com/team-a-logo.jpg` |
| `question_images` | `image_url` | VARCHAR(1000) | Question context | `https://cdn.example.com/monument.jpg` |

### Question Type Enum Values

```
'yes_no'          - Boolean choice (2 options)
'multiple_choice' - Multiple options (3+ options)
'number'          - Numeric input
'text'            - Text input
```

### Common Queries

```sql
-- Get all quizzes for a creator with images
SELECT id, title, banner_image_url, start_datetime 
FROM quizzes 
WHERE creator_id = $1 
ORDER BY start_datetime DESC;

-- Get a question with all its images
SELECT q.id, q.question_text, q.question_type,
       qi.image_url, qi.image_alt_text
FROM questions q
LEFT JOIN question_images qi ON q.id = qi.question_id
WHERE q.id = $1
ORDER BY qi.image_index;

-- Get question options with images
SELECT id, option_text, option_image_url, is_correct
FROM question_options
WHERE question_id = $1
ORDER BY option_index;

-- Get quiz leaderboard
SELECT qp.player_id, u.username, ql.rank, ql.total_score
FROM quiz_leaderboard ql
JOIN quiz_participants qp ON ql.quiz_id = qp.quiz_id
JOIN users u ON ql.player_id = u.id
WHERE ql.quiz_id = $1
ORDER BY ql.rank;
```

### API Endpoint Examples (Suggested)

**Part 1: Quiz Creator Endpoints**
```
POST   /api/quizzes                 - Create quiz
PUT    /api/quizzes/:id             - Update quiz (with banner_image_url)
POST   /api/quizzes/:id/questions   - Add question
PUT    /api/questions/:id           - Update question
POST   /api/questions/:id/images    - Add question images
POST   /api/questions/:id/options   - Add options (with option_image_url)
```

**Part 2: Quiz Player Endpoints**
```
GET    /api/quizzes/active          - List active quizzes
GET    /api/quizzes/:id             - Get quiz details (with banner_image_url)
POST   /api/quizzes/:id/join        - Join quiz
POST   /api/quiz-sessions/:id/start - Start quiz
POST   /api/quiz-sessions/:id/answer - Submit answer
GET    /api/quizzes/:id/leaderboard - Get leaderboard
```

---

## Checklist for Review

- [ ] All 9 tables are necessary for the requirements
- [ ] Image support meets the use cases (quiz banner, question context, option images)
- [ ] Question type enum provides proper type safety
- [ ] Relationships and foreign keys are correct
- [ ] Indexing strategy is comprehensive
- [ ] Enum values are appropriate
- [ ] Example queries are helpful
- [ ] Migration path is clear
- [ ] Design supports both Part-1 (Creator) and Part-2 (Player) requirements

---

## Contact & Support

For questions about this database design, please refer to:
1. The detailed table descriptions in this document
2. The practical examples section
3. The SQL enum creation section
4. The quick reference queries

---

*Last Updated: 2024-02-23*
*Design Version: 2.0 (with images and enums)*