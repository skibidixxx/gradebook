# Gradebook

A class library modeling a "Gradebook" domain, accompanied by a console application that demonstrates all basic and advanced-level operations.

## Build and Run

```bash
mvn test                                    # Run JUnit 5 tests (19 scenarios)
mvn compile exec:java                       # Run the console demonstration
```

Requirements: JDK 17+ and Maven (code is compatible with Java 21; utilizes records, sealed hierarchies, and `var`).

## Model Description (`gradebook.model`)

| Class | Purpose |
|---|---|
| `Mark` | An enum representing grades, containing fields (name, score range) and a factory method `byPoints(int)` |
| `ControlPoint` | An enum for assessment points, including weight and a passing score; the `EXAM` constant overrides the `isFinal()` method |
| `CourseModule` | A **sealed** interface for course modules, featuring a polymorphic `controlPoints()` method; implemented via records: `LectureModule`, `PracticeModule`, and `LabModule` |
| `Subject` | A course subject: a collection of modules and unique assessment points; `equals/hashCode` are based on the subject name |
| `Student` | A student; equality is determined by their record book number (`equals/hashCode`); implements the `Participant` interface |
| `Group` | A group of students; implements the `Participant` interface; `equals/hashCode` are based on the group name |
| `Participant` | An interface with a polymorphic `averageGrade(journal, subject)` method (students and groups calculate their average grades differently) |
| `Grade` | A **record** representing a student's grade for a specific assessment point on a given date; includes validation for a score range of 0–100 |
| `GradeJournal` | System: assigning grades, retakes, averages, high-achievers/students with outstanding debts, grade reports, and rankings |
| `RetakeLimitExceededException` | custom exception: thrown when the maximum number of retakes (limit: 3) is exceeded |

## Implemented Operations

Basic Level:
- Assigning grades with validation (range: 0–100);
- Calculating average grades for a student, a subject, or a group (implemented polymorphically via `Participant`);
- Identifying high-achievers (based on an average grade threshold) and students with outstanding debts (those who failed to reach the passing score on their final attempt);
- Generating a grade report for a specific subject as of a given date (`sheetByDate`, using `java.time.LocalDate`).

Advanced Level:
- Retake management: tracking the history of attempts, including dates (`attemptHistory`);
- Enforcing a limit on the number of attempts, throwing a `RetakeLimitExceededException` when exceeded;
- Determining the final result based on the last attempt (`finalScore`);
- Calculating a student's ranking as a weighted sum of the most recent attempts for key assessment points (`rating`).

## Demonstration (`gradebook.app.Main`)

Populates the model with 30 students, 2 groups, 2 subjects, and over 150 grades,
demonstrating each operation sequentially with clear console output.

## Tests

`src/test/java/gradebook/GradeJournalTest.java` — 19 JUnit 5 test scenarios:
covers boundary values ​​for grades (0/60/61/86/87/100), exception handling (`assertThrows`),
`equals/hashCode` contracts, date-based grade reports, retake limits, and rankings.

## Encapsulation

All fields are private; lists are returned as immutable copies,
and mutable objects passed from outside are copied before being stored.
