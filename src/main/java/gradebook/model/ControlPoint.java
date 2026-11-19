package gradebook.model;

/**
 * Контрольная точка: вид работы, вес и минимальный проходной балл.
 * Перечисление с полями и переопределённым методом константы (EXAM).
 */
public enum ControlPoint {

    /** Домашнее задание: вес 1, проходной балл 40. */
    HOMEWORK("Домашнее задание", 1.0, 40),
    /** Самостоятельная работа: вес 2, проходной балл 50. */
    QUIZ("Самостоятельная работа", 2.0, 50),
    /** Контрольная работа: вес 3, проходной балл 60. */
    EXAM_TEST("Контрольная работа", 3.0, 60),
    /** Экзамен: вес 5, проходной балл 61. Итоговая контрольная точка. */
    EXAM("Экзамен", 5.0, 61) {
        @Override
        public boolean isFinal() {
            return true;
        }
    };

    private final String title;
    private final double weight;
    private final int minPassingPoints;

    ControlPoint(String title, double weight, int minPassingPoints) {
        this.title = title;
        this.weight = weight;
        this.minPassingPoints = minPassingPoints;
    }

    /**
     * Возвращает название контрольной точки.
     *
     * @return название
     */
    public String getTitle() {
        return title;
    }

    /**
     * Возвращает вес контрольной точки при расчёте рейтинга.
     *
     * @return вес
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Возвращает минимальный проходной балл для этой точки.
     *
     * @return проходной балл
     */
    public int getMinPassingPoints() {
        return minPassingPoints;
    }

    /**
     * Проверяет, набран ли проходной балл за эту точку.
     *
     * @param points выставленные баллы
     * @return true, если баллов достаточно
     */
    public boolean isPassed(int points) {
        return points >= minPassingPoints;
    }

    /**
     * Является ли точка итоговой (экзамен). Переопределяется константой EXAM.
     *
     * @return true, если точка итоговая
     */
    public boolean isFinal() {
        return false;
    }
}
