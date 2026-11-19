package gradebook.model;

import java.util.List;

/**
 * Лабораторный модуль: самостоятельные работы и экзамен.
 *
 * @param name название модуля
 */
public record LabModule(String name) implements CourseModule {

    @Override
    public String title() {
        return name;
    }

    @Override
    public List<ControlPoint> controlPoints() {
        return List.of(ControlPoint.QUIZ, ControlPoint.EXAM);
    }
}
