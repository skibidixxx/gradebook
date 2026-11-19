package gradebook.model;

import java.util.List;

/**
 * Закрытая иерархия модулей учебного курса.
 * Каждый модуль определяет, какие контрольные точки в него входят.
 */
public sealed interface CourseModule
        permits LectureModule, PracticeModule, LabModule {

    /**
     * Возвращает название модуля.
     *
     * @return название
     */
    String title();

    /**
     * Возвращает контрольные точки модуля — полиморфный метод.
     *
     * @return список точек
     */
    List<ControlPoint> controlPoints();
}
