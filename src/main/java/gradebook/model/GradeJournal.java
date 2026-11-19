package gradebook.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Журнал успеваемости: хранит студентов, группы, дисциплины и оценки.
 * Поддерживает выставление баллов, пересдачи, средние баллы,
 * отличников/должников, ведомости и рейтинг.
 */
public final class GradeJournal {

    /** Максимальное число попыток (основная сдача + пересдачи) по одной точке. */
    public static final int MAX_ATTEMPTS = 3;

    private final Map<String, Student> students = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final List<Subject> subjects = new ArrayList<>();
    private final List<Grade> grades = new ArrayList<>();

    /**
     * Добавляет студента в журнал (дубли по номеру книжки игнорируются).
     *
     * @param student студент
     */
    public void addStudent(Student student) {
        students.putIfAbsent(student.getBookNumber(), student);
    }

    /**
     * Добавляет группу в журнал (дубли по названию игнорируются).
     *
     * @param group группа
     */
    public void addGroup(Group group) {
        groups.putIfAbsent(group.getName(), group);
    }

    /**
     * Добавляет дисциплину в журнал (дубли по названию игнорируются).
     *
     * @param subject дисциплина
     */
    public void addSubject(Subject subject) {
        if (!subjects.contains(subject)) {
            subjects.add(subject);
        }
    }

    /**
     * Выставляет студенту баллы за контрольную точку дисциплины с проверкой диапазона.
     * Первая оценка — обычная сдача, последующие — пересдачи.
     * Общее число попыток ограничено {@link #MAX_ATTEMPTS}.
     *
     * @param student студент
     * @param subject дисциплина
     * @param point   контрольная точка (должна входить в дисциплину)
     * @param points  баллы 0..100
     * @param date    дата выставления
     * @return созданная оценка
     * @throws IllegalArgumentException если баллов вне диапазона, студент не в журнале
     *                                  или точка не относится к дисциплине
     * @throws RetakeLimitExceededException если превышено допустимое число попыток
     */
    public Grade assignGrade(Student student, Subject subject, ControlPoint point,
                             int points, LocalDate date) {
        if (points < 0 || points > 100) {
            throw new IllegalArgumentException("Баллы вне диапазона 0..100: " + points);
        }
        if (!students.containsKey(student.getBookNumber())) {
            throw new IllegalArgumentException("Студент не добавлен в журнал: " + student);
        }
        if (!subject.controlPoints().contains(point)) {
            throw new IllegalArgumentException("Точка «" + point.getTitle()
                    + "» не входит в дисциплину «" + subject.getName() + "»");
        }
        addSubject(subject);
        int attempts = attemptsOf(student, subject, point);
        if (attempts >= MAX_ATTEMPTS) {
            throw new RetakeLimitExceededException(
                    "Превышено число попыток (" + MAX_ATTEMPTS + ") для точки «"
                            + point.getTitle() + "» по дисциплине «" + subject.getName()
                            + "»: студент " + student,
                    MAX_ATTEMPTS);
        }
        Grade grade = new Grade(student, subject, point, points, date);
        grades.add(grade);
        return grade;
    }

    /**
     * Возвращает список оценок студента по дисциплине (копию, в порядке выставления).
     *
     * @param student студент
     * @param subject дисциплина
     * @return список оценок
     */
    public List<Grade> gradesOf(Student student, Subject subject) {
        List<Grade> result = new ArrayList<>();
        for (Grade g : grades) {
            if (g.student().equals(student) && g.subject().equals(subject)) {
                result.add(g);
            }
        }
        return result;
    }

    /**
     * Возвращает историю попыток (основная сдача и пересдачи) по паре (студент, точка).
     *
     * @param student студент
     * @param point   контрольная точка
     * @return список оценок в хронологическом порядке
     */
    public List<Grade> attemptHistory(Student student, ControlPoint point) {
        List<Grade> result = new ArrayList<>();
        for (Grade g : grades) {
            if (g.student().equals(student) && g.point().equals(point)) {
                result.add(g);
            }
        }
        return result;
    }

    /**
     * Итоговый балл по последней попытке контрольной точки.
     *
     * @param student студент
     * @param point   контрольная точка
     * @return баллы последней попытки
     * @throws IllegalStateException если попыток не было
     */
    public int finalScore(Student student, ControlPoint point) {
        List<Grade> history = attemptHistory(student, point);
        if (history.isEmpty()) {
            throw new IllegalStateException(
                    "Нет попыток для " + student + " / " + point.getTitle());
        }
        return history.get(history.size() - 1).points();
    }

    /**
     * Средний балл студента по дисциплине по всем выставленным оценкам.
     *
     * @param student студент
     * @param subject дисциплина
     * @return средний балл; 0, если оценок нет
     */
    public double studentAverage(Student student, Subject subject) {
        List<Grade> list = gradesOf(student, subject);
        if (list.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Grade g : list) {
            total += g.points();
        }
        return (double) total / list.size();
    }

    /**
     * Средний балл по дисциплине среди всех оценок журнала.
     *
     * @param subject дисциплина
     * @return средний балл; 0, если оценок нет
     */
    public double subjectAverage(Subject subject) {
        int total = 0;
        int count = 0;
        for (Grade g : grades) {
            if (g.subject().equals(subject)) {
                total += g.points();
                count++;
            }
        }
        return count == 0 ? 0 : (double) total / count;
    }

    /**
     * Средний балл группы по дисциплине (среднее от средних баллов её студентов).
     *
     * @param group   группа
     * @param subject дисциплина
     * @return средний балл; 0, если оценок нет
     */
    public double groupAverage(Group group, Subject subject) {
        return group.averageGrade(this, subject);
    }

    /**
     * Отличники: студенты, чей общий средний балл не ниже заданного порога.
     *
     * @param threshold порог среднего балла, например 87
     * @return список отличников
     */
    public List<Student> honorStudents(double threshold) {
        List<Student> result = new ArrayList<>();
        for (Student s : students.values()) {
            if (overallAverage(s) >= threshold) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Должники: студенты, у которых по последней попытке какой-либо контрольной
     * точки не набран проходной балл этой точки.
     *
     * @return список должников
     */
    public List<Student> failingStudents() {
        List<Student> result = new ArrayList<>();
        for (Student s : students.values()) {
            for (ControlPoint p : ControlPoint.values()) {
                List<Grade> history = attemptHistory(s, p);
                if (!history.isEmpty()) {
                    Grade last = history.get(history.size() - 1);
                    if (!p.isPassed(last.points())) {
                        result.add(s);
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Ведомость по дисциплине на заданную дату: последние попытки точек,
     * выставленные не позже даты.
     *
     * @param subject дисциплина
     * @param date    дата ведомости
     * @return список оценок ведомости, отсортированный по ФИО и порядку точек
     */
    public List<Grade> sheetByDate(Subject subject, LocalDate date) {
        Map<String, Grade> latest = new LinkedHashMap<>();
        for (Grade g : grades) {
            if (g.subject().equals(subject) && !g.date().isAfter(date)) {
                String key = g.student().getBookNumber() + "#" + g.point().name();
                Grade old = latest.get(key);
                if (old == null || !g.date().isBefore(old.date())) {
                    latest.put(key, g);
                }
            }
        }
        List<Grade> result = new ArrayList<>(latest.values());
        result.sort((a, b) -> {
            int cmp = a.student().getFullName().compareTo(b.student().getFullName());
            return cmp != 0 ? cmp : a.point().ordinal() - b.point().ordinal();
        });
        return result;
    }

    /**
     * Рейтинг студента по дисциплине: взвешенная сумма баллов последних попыток
     * всех контрольных точек дисциплины (вес — {@link ControlPoint#getWeight()}).
     *
     * @param student студент
     * @param subject дисциплина
     * @return значение рейтинга
     */
    public double rating(Student student, Subject subject) {
        double sum = 0;
        for (ControlPoint p : subject.controlPoints()) {
            List<Grade> history = attemptHistoryFor(student, subject, p);
            if (!history.isEmpty()) {
                sum += history.get(history.size() - 1).points() * p.getWeight();
            }
        }
        return sum;
    }

    /**
     * Возвращает неизменяемую копию списка всех студентов журнала.
     *
     * @return список студентов
     */
    public List<Student> getStudents() {
        return Collections.unmodifiableList(new ArrayList<>(students.values()));
    }

    /**
     * Возвращает неизменяемую копию списка всех групп журнала.
     *
     * @return список групп
     */
    public List<Group> getGroups() {
        return Collections.unmodifiableList(new ArrayList<>(groups.values()));
    }

    /**
     * Возвращает неизменяемую копию списка всех дисциплин журнала.
     *
     * @return список дисциплин
     */
    public List<Subject> getSubjects() {
        return Collections.unmodifiableList(new ArrayList<>(subjects));
    }

    private List<Grade> attemptHistoryFor(Student student, Subject subject, ControlPoint point) {
        List<Grade> result = new ArrayList<>();
        for (Grade g : grades) {
            if (g.student().equals(student) && g.subject().equals(subject)
                    && g.point().equals(point)) {
                result.add(g);
            }
        }
        return result;
    }

    private int attemptsOf(Student student, Subject subject, ControlPoint point) {
        return attemptHistoryFor(student, subject, point).size();
    }

    private double overallAverage(Student student) {
        int total = 0;
        int count = 0;
        for (Grade g : grades) {
            if (g.student().equals(student)) {
                total += g.points();
                count++;
            }
        }
        return count == 0 ? 0 : (double) total / count;
    }
}
