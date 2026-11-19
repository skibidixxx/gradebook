package gradebook.app;

import gradebook.model.ControlPoint;
import gradebook.model.CourseModule;
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

import java.time.LocalDate;
import java.util.List;

/**
 * Консольная демонстрация журнала успеваемости (задача 12).
 */
public final class Main {

    private Main() {
    }

    /**
     * Точка входа: наполняет модель и показывает все операции базового уровня.
     *
     * @param args аргументы командной строки не используются
     */
    public static void main(String[] args) {
        var journal = new GradeJournal();

        // Дисциплины из модулей закрытой иерархии CourseModule (полиморфизм)
        Subject oop = new Subject("ООП",
                new LectureModule("Основы Java"),
                new PracticeModule("Классы и интерфейсы"),
                new LabModule("Итоговая лаборатория"));
        Subject math = new Subject("Математика",
                new PracticeModule("Анализ"),
                new LectureModule("Линейная алгебра"));
        journal.addSubject(oop);
        journal.addSubject(math);

        // Группы и студенты (30 студентов)
        String[][] names = {
                {"Иванов", "А"}, {"Петров", "Б"}, {"Сидоров", "В"}, {"Кузнецов", "Г"},
                {"Смирнова", "Е"}, {"Попов", "Ж"}, {"Соколова", "З"}, {"Лебедев", "И"},
                {"Козлов", "К"}, {"Новикова", "Л"}, {"Морозов", "М"}, {"Волкова", "Н"},
                {"Соловьёв", "О"}, {"Васильев", "П"}, {"Зайцева", "Р"}, {"Павлов", "С"},
                {"Семёнова", "Т"}, {"Голубев", "У"}, {"Виноградов", "Ф"}, {"Богданова", "Х"},
                {"Воробьёв", "Ц"}, {"Фёдоров", "Ч"}, {"Михайлова", "Ш"}, {"Беляев", "Э"},
                {"Тарасова", "Ю"}, {"Белов", "Я"}, {"Комаров", "А"}, {"Орлова", "Б"},
                {"Киселёв", "В"}, {"Макарова", "Г"}
        };
        var groupA = new Group("БПМИ-21");
        var groupB = new Group("БПМИ-22");
        var students = new java.util.ArrayList<Student>();
        for (int i = 0; i < names.length; i++) {
            var s = new Student("2025" + String.format("%03d", i + 1), names[i][0] + " " + names[i][1]);
            students.add(s);
            journal.addStudent(s);
            if (i % 2 == 0) {
                groupA.addStudent(s);
            } else {
                groupB.addStudent(s);
            }
        }
        journal.addGroup(groupA);
        journal.addGroup(groupB);

        System.out.println("=== ЖУРНАЛ УСПЕВАЕМОСТИ ===");
        System.out.println("Студентов: " + journal.getStudents().size()
                + ", групп: " + journal.getGroups().size()
                + ", дисциплин: " + journal.getSubjects().size());

        // 1. Выставление баллов с проверкой диапазона
        System.out.println("\n--- 1. Выставление баллов ---");
        int pointsBase = 45;
        for (var s : students) {
            pointsBase = (pointsBase + 7) % 60 + 40; // псевдослучайные 40..99
            journal.assignGrade(s, oop, ControlPoint.HOMEWORK, pointsBase, LocalDate.of(2025, 9, 10));
            journal.assignGrade(s, oop, ControlPoint.QUIZ, Math.min(100, pointsBase + 5), LocalDate.of(2025, 9, 24));
            journal.assignGrade(s, oop, ControlPoint.EXAM_TEST, Math.max(0, pointsBase - 10), LocalDate.of(2025, 10, 15));
            journal.assignGrade(s, math, ControlPoint.HOMEWORK, 100 - pointsBase % 40, LocalDate.of(2025, 9, 12));
            journal.assignGrade(s, math, ControlPoint.QUIZ, pointsBase, LocalDate.of(2025, 10, 3));
        }
        System.out.println("Выставлено оценок: " + students.size() * 5 + " за обе дисциплины.");
        try {
            journal.assignGrade(students.get(0), oop, ControlPoint.EXAM, 150, LocalDate.now());
        } catch (IllegalArgumentException e) {
            System.out.println("Проверка диапазона сработала: " + e.getMessage());
        }

        // 2. Средний балл по студенту, дисциплине и группе (полиморфно через Participant)
        System.out.println("\n--- 2. Средние баллы ---");
        var top = students.get(0);
        System.out.printf("Средний балл студента %s по ООП: %.2f%n", top.getName(),
                top.averageGrade(journal, oop));
        System.out.printf("Средний балл по дисциплине «%s»: %.2f%n", oop.getName(),
                journal.subjectAverage(oop));
        System.out.printf("Средний балл группы %s по ООП: %.2f%n", groupA.getName(),
                journal.groupAverage(groupA, oop));
        System.out.printf("Средний балл группы %s по Математике: %.2f%n", groupB.getName(),
                journal.groupAverage(groupB, math));

        // 3. Отличники и должники
        System.out.println("\n--- 3. Отличники и должники ---");
        var honor = journal.honorStudents(Mark.EXCELLENT.getMinPoints());
        System.out.println("Отличники (средний >= 87): " + honor);
        var failing = journal.failingStudents();
        System.out.println("Должники (не набран проходной балл): " + failing);

        // 4. Ведомость по дисциплине на дату
        System.out.println("\n--- 4. Ведомость по «ООП» на 2025-10-01 ---");
        List<Grade> sheet = journal.sheetByDate(oop, LocalDate.of(2025, 10, 1));
        for (Grade g : sheet) {
            System.out.printf("%-20s %-25s %3d балла (%s)%n",
                    g.student().getFullName(), g.point().getTitle(), g.points(), g.mark().getTitle());
        }

        // 5. Пересдачи: история попыток, лимит с исключением, итог по последней попытке
        System.out.println("\n--- 5. Пересдачи ---");
        var debtor = failing.isEmpty() ? students.get(1) : failing.get(0);
        journal.assignGrade(debtor, oop, ControlPoint.EXAM_TEST, 55, LocalDate.of(2025, 10, 20));
        journal.assignGrade(debtor, oop, ControlPoint.EXAM_TEST, 62, LocalDate.of(2025, 10, 27));
        System.out.println("История попыток точки «Контрольная работа» для " + debtor + ":");
        for (Grade g : journal.attemptHistory(debtor, ControlPoint.EXAM_TEST)) {
            System.out.printf("  %s -> %d балла (%s)%n", g.date(), g.points(), g.mark().getTitle());
        }
        System.out.println("Итог по последней попытке: " + journal.finalScore(debtor, ControlPoint.EXAM_TEST));
        try {
            journal.assignGrade(debtor, oop, ControlPoint.EXAM_TEST, 90, LocalDate.of(2025, 11, 5));
            journal.assignGrade(debtor, oop, ControlPoint.EXAM_TEST, 95, LocalDate.of(2025, 11, 10));
        } catch (RetakeLimitExceededException e) {
            System.out.println("Исключение лимита пересдач: " + e.getMessage()
                    + " (допустимо попыток: " + e.getAttemptsAllowed() + ")");
        }

        // 6. Рейтинг как взвешенная сумма контрольных точек
        System.out.println("\n--- 6. Рейтинг по «ООП» (взвешенная сумма последних попыток) ---");
        students.stream()
                .map(s -> new Object[]{s, journal.rating(s, oop)})
                .sorted((x, y) -> Double.compare((Double) y[1], (Double) x[1]))
                .limit(5)
                .forEach(e -> System.out.printf("%-20s рейтинг = %.1f%n",
                        ((Student) e[0]).getFullName(), (Double) e[1]));

        // Демонстрация sealed-иерархии: полиморфный метод controlPoints()
        System.out.println("\n--- Модули дисциплины «ООП» ---");
        for (CourseModule m : oop.getModules()) {
            System.out.println(m.title() + " -> точки: " + m.controlPoints());
        }
        System.out.println("Экзамен итоговая точка? " + ControlPoint.EXAM.isFinal()
                + ", домашка итоговая? " + ControlPoint.HOMEWORK.isFinal());

        // Сопоставление с образцом (sealed-иерархия)
        for (CourseModule m : oop.getModules()) {
            String kind = "Неизвестный модуль";
            if (m instanceof LectureModule lm) {
                kind = "Лекции: " + lm.title();
            } else if (m instanceof PracticeModule pm) {
                kind = "Практика: " + pm.title();
            } else if (m instanceof LabModule lb) {
                kind = "Лаборатории: " + lb.title();
            }
            System.out.println(kind + " (" + m.controlPoints().size() + " точек)");
        }
    }
}
