package gradebook.model;

/**
 * Перечисление оценок с диапазоном баллов и фабричным методом.
 */
public enum Mark {

    /** Неудовлетворительно: 0..60 баллов. */
    FAIL("Неуд", 0, 60),
    /** Удовлетворительно: 61..73 балла. */
    SATISFACTORY("Удовл", 61, 73),
    /** Хорошо: 74..86 баллов. */
    GOOD("Хор", 74, 86),
    /** Отлично: 87..100 баллов. */
    EXCELLENT("Отл", 87, 100);

    private final String title;
    private final int minPoints;
    private final int maxPoints;

    Mark(String title, int minPoints, int maxPoints) {
        this.title = title;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
    }

    /**
     * Фабричный метод: определяет оценку по количеству баллов.
     *
     * @param points количество баллов
     * @return оценка, соответствующая диапазону баллов
     * @throws IllegalArgumentException если баллов вне диапазона 0..100
     */
    public static Mark byPoints(int points) {
        if (points < 0 || points > 100) {
            throw new IllegalArgumentException("Баллы вне диапазона 0..100: " + points);
        }
        for (Mark m : values()) {
            if (points >= m.minPoints && points <= m.maxPoints) {
                return m;
            }
        }
        throw new IllegalStateException("Нет диапазона для баллов " + points);
    }

    /**
     * Возвращает краткое название оценки.
     *
     * @return название оценки
     */
    public String getTitle() {
        return title;
    }

    /**
     * Возвращает минимальный балл диапазона оценки.
     *
     * @return минимальный балл
     */
    public int getMinPoints() {
        return minPoints;
    }

    /**
     * Возвращает максимальный балл диапазона оценки.
     *
     * @return максимальный балл
     */
    public int getMaxPoints() {
        return maxPoints;
    }

    /**
     * Проверяет, является ли оценка положительной (не двойка).
     *
     * @return true, если оценка выше неудовлетворительной
     */
    public boolean isPassing() {
        return this != FAIL;
    }
}
