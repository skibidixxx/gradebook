package gradebook.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Учебная группа: объединяет студентов под общим номером.
 */
public final class Group implements Participant {

    private final String name;
    private final List<Student> students = new ArrayList<>();

    /**
     * Создаёт группу с указанным номером.
     *
     * @param name номер группы, например "БПМИ-21"
     * @throws IllegalArgumentException если название пустое
     */
    public Group(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название группы не может быть пустым");
        }
        this.name = name;
    }

    /**
     * Добавляет студента в группу (без дублей по номеру книжки).
     *
     * @param student студент
     * @throws IllegalArgumentException если студент уже в группе
     */
    public void addStudent(Student student) {
        Objects.requireNonNull(student, "Студент не может быть null");
        if (students.contains(student)) {
            throw new IllegalArgumentException("Студент уже в группе: " + student);
        }
        students.add(student);
    }

    /**
     * Возвращает неизменяемую копию списка студентов группы.
     *
     * @return список студентов
     */
    public List<Student> getStudents() {
        return Collections.unmodifiableList(new ArrayList<>(students));
    }

    /**
     * Возвращает название группы.
     *
     * @return номер группы
     */
    public String getName() {
        return name;
    }

    /**
     * Средний балл группы по дисциплине: среднее от средних баллов студентов.
     *
     * @param journal журнал успеваемости
     * @param subject дисциплина
     * @return средний балл; 0, если оценок нет
     */
    @Override
    public double averageGrade(GradeJournal journal, Subject subject) {
        double total = 0;
        int count = 0;
        for (Student s : students) {
            if (!journal.gradesOf(s, subject).isEmpty()) {
                total += journal.studentAverage(s, subject);
                count++;
            }
        }
        return count == 0 ? 0 : total / count;
    }

    /**
     * Равенство по названию группы.
     *
     * @param o объект сравнения
     * @return true, если это группа с тем же названием
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Group other)) {
            return false;
        }
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
