package gradebook.model;

import java.time.LocalDate;

/**
 * Оценка (балл) студента за контрольную точку дисциплины на дату.
 * Неизменяемое значение, реализовано как record.
 *
 * @param student студент
 * @param subject дисциплина
 * @param point   контрольная точка
 * @param points  выставленные баллы
 * @param date    дата выставления
 */
public record Grade(Student student, Subject subject, ControlPoint point,
                    int points, LocalDate date) {

    /**
     * Создаёт оценку с проверкой диапазона баллов.
     *
     * @throws IllegalArgumentException если баллов вне диапазона 0..100
     */
    public Grade {
        if (points < 0 || points > 100) {
            throw new IllegalArgumentException("Баллы вне диапазона 0..100: " + points);
        }
    }

    /**
     * Возвращает оценку (буквенный эквивалент) по набранным баллам.
     *
     * @return оценка из перечисления {@link Mark}
     */
    public Mark mark() {
        return Mark.byPoints(points);
    }
}
