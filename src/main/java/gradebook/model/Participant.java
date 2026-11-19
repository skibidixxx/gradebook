package gradebook.model;

/**
 * Участник учебного процесса: студент или группа.
 * Общий интерфейс для полиморфного расчёта среднего балла.
 */
public interface Participant {

    /**
     * Возвращает имя участника (ФИО студента или номер группы).
     *
     * @return имя участника
     */
    String getName();

    /**
     * Вычисляет средний балл участника по заданной дисциплине — полиморфный метод.
     *
     * @param journal журнал успеваемости
     * @param subject дисциплина
     * @return средний балл; 0, если оценок нет
     */
    double averageGrade(GradeJournal journal, Subject subject);
}
