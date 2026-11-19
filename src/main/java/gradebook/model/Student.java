package gradebook.model;

import java.util.Objects;

/**
 * Студент. Ключ равенства — номер зачётной книжки.
 */
public final class Student implements Participant {

    private final String bookNumber;
    private final String fullName;

    /**
     * Создаёт студента.
     *
     * @param bookNumber номер зачётной книжки (ключ равенства)
     * @param fullName   фамилия и имя студента
     * @throws IllegalArgumentException если параметры пустые
     */
    public Student(String bookNumber, String fullName) {
        if (bookNumber == null || bookNumber.isBlank()) {
            throw new IllegalArgumentException("Номер зачётной книжки не может быть пустым");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("ФИО студента не может быть пустым");
        }
        this.bookNumber = bookNumber;
        this.fullName = fullName;
    }

    /**
     * Возвращает номер зачётной книжки.
     *
     * @return номер зачётной книжки
     */
    public String getBookNumber() {
        return bookNumber;
    }

    /**
     * Возвращает ФИО студента.
     *
     * @return ФИО
     */
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getName() {
        return fullName;
    }

    /**
     * Средний балл студента по дисциплине (по всем выставленным оценкам).
     *
     * @param journal журнал успеваемости
     * @param subject дисциплина
     * @return средний балл; 0, если оценок нет
     */
    @Override
    public double averageGrade(GradeJournal journal, Subject subject) {
        return journal.studentAverage(this, subject);
    }

    /**
     * Равенство по номеру зачётной книжки.
     *
     * @param o объект сравнения
     * @return true, если это студент с тем же номером книжки
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Student other)) {
            return false;
        }
        return bookNumber.equals(other.bookNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookNumber);
    }

    @Override
    public String toString() {
        return fullName + " (" + bookNumber + ")";
    }
}
