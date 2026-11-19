package gradebook.model;

import java.util.List;

/**
 * Лекционный модуль: только контрольная работа.
 *
 * @param name название модуля
 */
public record LectureModule(String name) implements CourseModule {

    @Override
    public String title() {
        return name;
    }

    @Override
    public List<ControlPoint> controlPoints() {
        return List.of(ControlPoint.EXAM_TEST);
    }
}
