# Quiz Contest Application - Java Spring Boot Setup Guide

## Project Overview

A complete Java Spring Boot 3+ application for hosting multi-user quizzes with:
- **Java 17** - Latest LTS Java version
- **Spring Boot 3.2.2** - Latest Spring Boot 3.x
- **Maven** - Build tool
- **H2 Database** - In-memory database (development)
- **Liquibase** - Database migrations and versioning
- **JPA/Hibernate** - ORM framework
- **Lombok** - Reduce boilerplate code (getters, setters, constructors)

---

## Project Structure

```
quiz-contest/
├── pom.xml                                    # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/quizcontest/
│   │   │       ├── QuizContestApplication.java
│   │   │       ├── entity/
│   │   │       │   ├── User.java
│   │   │       │   ├── Quiz.java
│   │   │       │   ├── Question.java
│   │   │       │   ├── QuestionOption.java
│   │   │       │   ├── QuizImage.java
│   │   │       │   ├── CorrectAnswer.java
│   │   │       │   ├── QuizParticipant.java
│   │   │       │   ├── PlayerAnswer.java
│   │   │       │   └── QuizLeaderboard.java
│   │   │       └── repository/
│   │   │           ├── UserRepository.java
│   │   │           ├── QuizRepository.java
│   │   │           ├── QuestionRepository.java
│   │   │           ├── QuestionOptionRepository.java
│   │   │           ├── QuizImageRepository.java
│   │   │           ├── CorrectAnswerRepository.java
│   │   │           ├── QuizParticipantRepository.java
│   │   │           ├── PlayerAnswerRepository.java
│   │   │           └── QuizLeaderboardRepository.java
│   │   └── resources/
│   │       ├── application.yml                # Spring Boot configuration
│   │       └── db/changelog/
│   │           ├── db.changelog-master.yaml   # Master changelog
│   │           ├── 001_create_enums.yaml
│   │           ├── 002_create_users_table.yaml
│   │           ├── 003_create_quizzes_table.yaml
│   │           ├── 004_create_questions_table.yaml
│   │           ├── 005_create_question_options_table.yaml
│   │           ├── 006_create_quiz_images_table.yaml
│   │           ├── 007_create_correct_answers_table.yaml
│   │           ├── 008_create_quiz_participants_table.yaml
│   │           ├── 009_create_player_answers_table.yaml
│   │           └── 010_create_quiz_leaderboard_table.yaml
│   └── test/
│       └── java/
│           └── com/quizcontest/
│               └── QuizContestApplicationTests.java
└── README.md                                  # Database design documentation
```

---

## Prerequisites

- **Java 17+** installed
- **Maven 3.6+** installed
- **Git** installed

---

## Installation & Setup

### 1. Clone or Download the Project

```bash
git clone <repository-url>
cd quiz-contest
```

### 2. Build the Project

```bash
mvn clean install
```

This will:
- Download all dependencies from Maven Central Repository
- Compile the Java code
- Run any tests
- Package the application

### 3. Run the Application

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/quiz-contest-app-1.0.0.jar
```

### 4. Access the Application

- **Application URL**: `http://localhost:8080/api`
- **H2 Console**: `http://localhost:8080/api/h2-console`
  - Username: `sa`
  - Password: (leave blank)

---

## Database Schema

### 9 Tables with Versioning

1. **users** - User accounts (creators, players, admins)
2. **quizzes** - Quiz metadata with banner image references
3. **questions** - Individual questions with types
4. **question_options** - Answer options with image references
5. **quiz_images** - Binary image storage (BYTEA)
6. **correct_answers** - Correct answer definitions
7. **quiz_participants** - Player participation tracking
8. **player_answers** - Individual answer submissions
9. **quiz_leaderboard** - Ranking and statistics

### Key Features

- **Versioning**: All tables include a `version` column for optimistic locking
- **Timestamps**: `created_at` (immutable) and `updated_at` (auto-updated)
- **UUIDs**: All primary keys use UUID for distributed system support
- **Indexes**: Comprehensive indexing for query performance
- **Foreign Keys**: Proper relationships with cascading deletes
- **Image Storage**: Binary BYTEA storage in database (no external CDN)

---

## Dependencies

### Core Dependencies

```xml
<!-- Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- H2 Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Liquibase -->
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
    <version>4.25.1</version>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

---

## Configuration (application.yml)

### Key Settings

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate          # Validate schema, don't modify
    show-sql: false               # Don't log SQL (set to true for debugging)
  
  datasource:
    url: jdbc:h2:mem:quizdb       # In-memory H2 database
    driver-class-name: org.h2.Driver
    username: sa
    password:                      # Empty password
  
  h2:
    console:
      enabled: true               # Enable H2 console at /h2-console
  
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml

server:
  port: 8080
  servlet:
    context-path: /api
```

---

## Entity Classes (with Lombok)

All entity classes use **Lombok annotations** to eliminate boilerplate:

```java
@Data                    // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor       // Generates no-arg constructor
@AllArgsConstructor      // Generates all-arg constructor
@Builder                 // Generates builder pattern
@Entity                  // JPA entity
public class User {
    // Fields only - no getters/setters needed!
}
```

### Benefits of Lombok

- ✅ **Less Code**: No manual getters/setters
- ✅ **Less Errors**: Auto-generated methods are correct
- ✅ **Cleaner Classes**: Focus on business logic
- ✅ **Easy Refactoring**: Change a field, annotations update methods automatically

---

## Repository Pattern

All repositories extend `JpaRepository`:

```java
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

### Spring Data JPA Benefits

- ✅ **No SQL**: Use method names instead of SQL
- ✅ **CRUD Operations**: Built-in save, delete, findById, etc.
- ✅ **Pagination**: Automatic pagination support
- ✅ **Type Safety**: Compile-time checking

---

## Liquibase Database Migrations

### How It Works

1. **Master Changelog** (`db.changelog-master.yaml`) includes all migration files
2. **Individual Changesets** (`001_create_enums.yaml`, `002_create_users_table.yaml`, etc.)
3. **Automatic Execution**: Liquibase runs on application startup
4. **Tracking**: Liquibase tracks executed migrations in `DATABASECHANGELOG` table

### Key Features

- ✅ **Version Control**: Database schema in Git
- ✅ **Rollback Support**: Can revert changes
- ✅ **Multi-Database**: Works with PostgreSQL, MySQL, H2, etc.
- ✅ **YAML Format**: Easy to read and maintain

---

## Running Database Migrations

### Automatic (Application Startup)

Migrations run automatically when the application starts:

```bash
mvn spring-boot:run
```

### Manual (Maven Plugin)

```bash
# Generate SQL
mvn liquibase:changelogSync

# Status
mvn liquibase:status

# Rollback
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

---

## Using the H2 Console

### Access

Navigate to: `http://localhost:8080/api/h2-console`

### Connection Details

- **JDBC URL**: `jdbc:h2:mem:quizdb`
- **User Name**: `sa`
- **Password**: (leave blank)

### Features

- ✅ **SQL Editor**: Write and execute SQL
- ✅ **Table Browser**: View all tables
- ✅ **Data Viewer**: Browse table contents
- ✅ **Query History**: View past queries

---

## Entity Relationships

```
User (1) ──────────── (Many) Quiz
User (1) ──────────── (Many) QuizParticipant
User (1) ──────────── (Many) QuizLeaderboard

Quiz (1) ──────────── (Many) Question
Quiz (1) ──────────── (Many) QuizParticipant
Quiz (1) ──────────── (Many) QuizImage

Question (1) ──────────── (Many) QuestionOption
Question (1) ──────────── (Many) CorrectAnswer
Question (1) ──────────── (Many) PlayerAnswer
Question (1) ──────────── (Many) QuizImage (linked_question_id)

QuestionOption (1) ──────────── (Many) QuizImage (linked_option_id)

QuizParticipant (1) ──────────── (Many) PlayerAnswer
```

---

## Next Steps

### 1. Create Service Layer

```java
@Service
public class QuizService {
    @Autowired
    private QuizRepository quizRepository;
    
    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }
}
```

### 2. Create REST Controllers

```java
@RestController
@RequestMapping("/quizzes")
public class QuizController {
    @Autowired
    private QuizService quizService;
    
    @PostMapping
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
        return ResponseEntity.ok(quizService.createQuiz(quiz));
    }
}
```

### 3. Add DTOs (Data Transfer Objects)

```java
@Data
public class QuizDTO {
    private UUID id;
    private String title;
    private String description;
    // ... other fields
}
```

### 4. Add Validation

```java
@Entity
public class Quiz {
    @NotNull
    @NotBlank
    private String title;
    
    @Future
    private LocalDateTime startDatetime;
}
```

### 5. Add Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.notFound().build();
    }
}
```

---

## Troubleshooting

### Issue: "Cannot find symbol" errors

**Solution**: Run `mvn clean install` to download dependencies

### Issue: H2 Console not accessible

**Solution**: Check `application.yml` has `h2.console.enabled: true`

### Issue: Liquibase migrations not running

**Solution**: 
1. Check `liquibase.change-log` path in `application.yml`
2. Verify changelog files exist in `src/main/resources/db/changelog/`
3. Check application logs for Liquibase errors

### Issue: Port 8080 already in use

**Solution**: Change port in `application.yml`:
```yaml
server:
  port: 8081
```

---

## Development Tips

### Enable SQL Logging

In `application.yml`:

```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Use Lombok Annotation Processor

In IDE (IntelliJ/Eclipse):
1. Install Lombok plugin
2. Enable annotation processing
3. Restart IDE

### Create Sample Data

Use `CommandLineRunner`:

```java
@Component
public class DataLoader implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) {
        User user = User.builder()
            .username("john_doe")
            .email("john@example.com")
            .role("creator")
            .build();
        userRepository.save(user);
    }
}
```

---

## Maven Commands Reference

```bash
# Clean and build
mvn clean install

# Run application
mvn spring-boot:run

# Run tests
mvn test

# Build JAR
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Check dependencies
mvn dependency:tree

# Update dependencies
mvn versions:display-dependency-updates
```

---

## IDE Setup

### IntelliJ IDEA

1. Open project
2. Configure JDK 17: File → Project Structure → SDK
3. Enable Lombok: Settings → Plugins → Install Lombok
4. Enable Annotation Processing: Settings → Compiler → Annotation Processors → Enable

### Eclipse

1. Install Lombok: `java -jar lombok.jar` (download from projectlombok.org)
2. Restart Eclipse
3. Enable Annotation Processing: Project → Properties → Java Compiler → Annotation Processing

### VS Code

1. Install "Extension Pack for Java"
2. Install "Lombok Annotations Support"
3. Restart VS Code

---

## Performance Optimization

### Hibernate Configuration

```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
          fetch_size: 50
        order_inserts: true
        order_updates: true
```

### Connection Pooling (HikariCP)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      connection-timeout: 20000
```

### Caching

```yaml
spring:
  jpa:
    properties:
      hibernate:
        cache:
          use_second_level_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
```

---

## Security Considerations

1. **Password Hashing**: Use BCrypt for password storage
2. **Input Validation**: Validate all user inputs
3. **SQL Injection**: Use JPA (automatic protection)
4. **CORS**: Configure CORS for API access
5. **Authentication**: Implement JWT or OAuth2
6. **Authorization**: Use Spring Security for role-based access

---

**Version**: 1.0.0
**Java**: 17+
**Spring Boot**: 3.2.2+
**Maven**: 3.6+
**Last Updated**: 2024-02-24