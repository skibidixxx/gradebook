package gradebook.model;

/**
 * Собственное исключение журнала: превышено допустимое число попыток пересдачи.
 */
public class RetakeLimitExceededException extends RuntimeException {

    private final int attemptsAllowed;

    /**
     * Создаёт исключение.
     *
     * @param message         текст сообщения
     * @param attemptsAllowed допустимое число попыток
     */
    public RetakeLimitExceededException(String message, int attemptsAllowed) {
        super(message);
        this.attemptsAllowed = attemptsAllowed;
    }

    /**
     * Возвращает допустимое число попыток пересдачи.
     *
     * @return лимит попыток
     */
    public int getAttemptsAllowed() {
        return attemptsAllowed;
    }
}
