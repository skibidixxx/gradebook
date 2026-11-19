package gradebook;

import gradebook.model.ControlPoint;
import gradebook.model.Grade;
import gradebook.model.GradeJournal;
import gradebook.model.Group;
import gradebook.model.LabModule;
import gradebook.model.LectureModule;
import gradebook.model.Mark;
import gradebook.model.PracticeModule;
import gradebook.model.RetakeLimitExceededException;
import gradebook.model.Student;
import gradebook.model.Subject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты модели журнала успеваемости (задача 12).
 */
class GradeJournalTest {

    private static final Subject OOP = new Subject("ООП",
            new LectureModule("Лекции"), new PracticeModule("Практика"),
            new LabModule("Лаборатории"));

    private GradeJournal journal;
    private Student ivanov;
    private Student petrov;

    @BeforeEach
    void setUp() {
        journal = new GradeJournal();
        ivanov = new Student("2025001", "Иванов И");
        petrov = new Student("2025002", "Петров П");
        journal.addStudent(ivanov);
        journal.addStudent(petrov);
    }

    // --- Перечисление Mark ---

    @Test
    void markByPointsBoundaries() {
        assertEquals(Mark.FAIL, Mark.byPoints(0));
        assertEquals(Mark.FAIL, Mark.byPoints(60));
        assertEquals(Mark.SATISFACTORY, Mark.byPoints(61));
        assertEquals(Mark.GOOD, Mark.byPoints(86));
        assertEquals(Mark.EXCELLENT, Mark.byPoints(87));
        assertEquals(Mark.EXCELLENT, Mark.byPoints(100));
    }

    @Test
    void markByPointsOutOfRangeThrows() {
        assertThrows(IllegalArgumentException.class, () -> Mark.byPoints(-1));
        assertThrows(IllegalArgumentException.class, () -> Mark.byPoints(101));
    }

    // --- Выставление баллов и диапазон ---

    @Test
    void assignGradeRejectsOutOfRangePoints() {
        assertThrows(IllegalArgumentException.class, () ->
                journal.assignGrade(ivanov, OOP, ControlPoint.HOMEWORK, 101, LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () ->
                journal.assignGrade(ivanov, OOP, ControlPoint.HOMEWORK, -5, LocalDate.now()));
    }

    @Test
    void assignGradeAcceptsBoundaryPoints() {
        Grade g0 = journal.assignGrade(ivanov, OOP, ControlPoint.HOMEWORK, 0, LocalDate.now());
        Grade g100 = journal.assignGrade(petrov, OOP, ControlPoint.QUIZ, 100, LocalDate.now());
        assertEquals(0, g0.points());
        assertEquals(100, g100.points());
        assertEquals(Mark.EXCELLENT, g100.mark());
    }

    @Test
    void assignGradeUnknownStudentThrows() {
        var stranger = new Student("9999", "Незнакомец Н");
        assertThrows(IllegalArgumentException.class, () ->
                journal.assignGrade(stranger, OOP, ControlPoint.HOMEWORK, 50, LocalDate.now()));
    }

    @Test
    void assignGradeForeignControlPointThrows() {
        Subject math = new Subject("Математика", new LectureModule("Лекции"));
        assertThrows(IllegalArgumentException.class, () ->
                journal.assignGrade(ivanov, math, ControlPoint.EXAM, 70, LocalDate.now()));
    }

    // --- Средние баллы ---

    @Test
    void studentAverageOverAllGrades() {
        var d = LocalDate.of(2025, 9, 1);
        journal.assignGrade(ivanov, OOP, ControlPoint.HOMEWORK, 80, d);
        journal.assignGrade(ivanov, OOP, ControlPoint.QUIZ, 90, d.plusDays(7));
        assertEquals(85.0, journal.studentAverage(ivanov, OOP), 1e-9);
    }

    @Test
    void averageOfEmptyDataIsZero() {
        assertEquals(0, journal.studentAverage(ivanov, OOP), 1e-9);
        assertEquals(0, journal.subjectAverage(OOP), 1e-9);
        var group = new Group("БПМИ-21");
        group.addStudent(ivanov);
        assertEquals(0, journal.groupAverage(group, OOP), 1e-9);
    }

    @Test
    void subjectAndGroupAverages() {
        var d = LocalDate.of(2025, 9, 1);
        journal.assignGrade(ivanov, OOP, ControlPoint.HOMEWORK, 70, d);
        journal.assignGrade(petrov, OOP, ControlPoint.HOMEWORK, 90, d);
        assertEquals(80.0, journal.subjectAverage(OOP), 1e-9);
        var group = new Group("БПМИ-21");
        group.addStudent(ivanov);
        assertEquals(70.0, journal.groupAverage(group, OOP), 1e-9);
    }

    // --- Отличники и должники ---

    @Test
    void honorStudentsByThreshold() {
        var d = LocalDate.of(2025, 9, 1);
        journal.assignGrade(ivanov, OOP, ControlPoint.HOMEWORK, 95, d);
        journal.assignGrade(ivanov, OOP, ControlPoint.QUIZ, 90, d);
        journal.assignGrade(petrov, OOP, ControlPoint.HOMEWORK, 60, d);
        assertTrue(journal.honorStudents(87).contains(ivanov));
        assertFalse(journal.honorStudents(87).contains(petrov));
    }

    @Test
    void failingStudentsByLastAttempt() {
        var d = LocalDate.of(2025, 9, 1);
        journal.assignGrade(petrov, OOP, ControlPoint.QUIZ, 30, d);
        assertTrue(journal.failingStudents().contains(petrov));
        // Пересдача выше проходного балла снимает долг
        journal.assignGrade(petrov, OOP, ControlPoint.QUIZ, 80, d.plusDays(5));
        assertFalse(journal.failingStudents().contains(petrov));
    }

    // --- Ведомость на дату ---

    @Test
    void sheetByDateOnlyUpToDateAndLatestAttempt() {
        journal.assignGrade(ivanov, OOP, ControlPoint.HOMEWORK, 50, LocalDate.of(2025, 9, 1));
        journal.assignGrade(ivanov, OOP, ControlPoint.HOMEWORK, 75, LocalDate.of(2025, 9, 20));
        journal.assignGrade(petrov, OOP, ControlPoint.QUIZ, 80, LocalDate.of(2025, 10, 5));

        var oct1 = journal.sheetByDate(OOP, LocalDate.of(2025, 10, 1));
        assertEquals(1, oct1.size());
        assertEquals(75, oct1.get(0).points()); // взята последняя попытка

        var oct15 = journal.sheetByDate(OOP, LocalDate.of(2025, 10, 15));
        assertEquals(2, oct15.size());
    }

    // --- Пересдачи (повышенный уровень) ---

    @Test
    void retakeHistoryAndFinalScore() {
        var d = LocalDate.of(2025, 9, 1);
        journal.assignGrade(ivanov, OOP, ControlPoint.EXAM_TEST, 40, d);
        journal.assignGrade(ivanov, OOP, ControlPoint.EXAM_TEST, 55, d.plusDays(7));
        journal.assignGrade(ivanov, OOP, ControlPoint.EXAM_TEST, 70, d.plusDays(14));
        var history = journal.attemptHistory(ivanov, ControlPoint.EXAM_TEST);
        assertEquals(3, history.size());
        assertEquals(70, journal.finalScore(ivanov, ControlPoint.EXAM_TEST));
    }

    @Test
    void fourthAttemptThrowsRetakeLimit() {
        var d = LocalDate.of(2025, 9, 1);
        for (int i = 0; i < GradeJournal.MAX_ATTEMPTS; i++) {
            journal.assignGrade(ivanov, OOP, ControlPoint.EXAM_TEST, 50, d.plusDays(i));
        }
        var ex = assertThrows(RetakeLimitExceededException.class, () ->
                journal.assignGrade(ivanov, OOP, ControlPoint.EXAM_TEST, 90, d.plusDays(10)));
        assertEquals(GradeJournal.MAX_ATTEMPTS, ex.getAttemptsAllowed());
    }

    @Test
    void finalScoreWithoutAttemptsThrows() {
        assertThrows(IllegalStateException.class, () ->
                journal.finalScore(ivanov, ControlPoint.EXAM));
    }

    // --- Рейтинг (взвешенная сумма) ---

    @Test
    void ratingIsWeightedSumOfLastAttempts() {
        var d = LocalDate.of(2025, 9, 1);
        journal.assignGrade(ivanov, OOP, ControlPoint.HOMEWORK, 80, d);       // вес 1
        journal.assignGrade(ivanov, OOP, ControlPoint.QUIZ, 50, d);           // вес 2
        journal.assignGrade(ivanov, OOP, ControlPoint.QUIZ, 60, d.plusDays(3));
        journal.assignGrade(ivanov, OOP, ControlPoint.EXAM_TEST, 70, d);      // вес 3
        double expected = 80 * 1.0 + 60 * 2.0 + 70 * 3.0;
        assertEquals(expected, journal.rating(ivanov, OOP), 1e-9);
    }

    // --- equals/hashCode ---

    @Test
    void studentEqualsByBookNumber() {
        var same = new Student("2025001", "Другое ФИО");
        assertEquals(ivanov, same);
        assertEquals(ivanov.hashCode(), same.hashCode());
        assertNotEquals(ivanov, petrov);
        assertNotEquals(ivanov, "не студент");
        assertEquals(ivanov, ivanov);
    }

    @Test
    void groupEqualsAndHashCode() {
        var g1 = new Group("БПМИ-21");
        var g2 = new Group("БПМИ-21");
        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());
        assertNotEquals(g1, new Group("БПМИ-22"));
    }

    @Test
    void duplicateStudentIgnoredInJournal() {
        journal.addStudent(new Student("2025001", "Иванов И.И."));
        assertEquals(2, journal.getStudents().size());
    }
}
