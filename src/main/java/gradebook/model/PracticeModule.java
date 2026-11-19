package gradebook.model;

import java.util.List;

/**
 * Практический модуль: домашние и самостоятельные работы.
 *
 * @param name название модуля
 */
public record PracticeModule(String name) implements CourseModule {

    @Override
    public String title() {
        return name;
    }

    @Override
    public List<ControlPoint> controlPoints() {
        return List.of(ControlPoint.HOMEWORK, ControlPoint.QUIZ);
    }
}
