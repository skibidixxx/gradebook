package gradebook.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Учебная дисциплина: название и входящие в неё контрольные точки.
 */
public final class Subject {

    private final String name;
    private final List<CourseModule> modules = new ArrayList<>();

    /**
     * Создаёт дисциплину с перечнем модулей, задающих её контрольные точки.
     *
     * @param name    название дисциплины
     * @param modules модули курса
     * @throws IllegalArgumentException если название пустое или модулей нет
     */
    public Subject(String name, CourseModule... modules) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название дисциплины не может быть пустым");
        }
        if (modules.length == 0) {
            throw new IllegalArgumentException("У дисциплины должен быть хотя бы один модуль");
        }
        this.name = name;
        for (CourseModule m : modules) {
            this.modules.add(m);
        }
    }

    /**
     * Возвращает название дисциплины.
     *
     * @return название
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает неизменяемую копию списка модулей дисциплины.
     *
     * @return список модулей
     */
    public List<CourseModule> getModules() {
        return Collections.unmodifiableList(new ArrayList<>(modules));
    }

    /**
     * Возвращает уникальные контрольные точки дисциплины (полиморфный вызов модулей).
     *
     * @return список контрольных точек
     */
    public List<ControlPoint> controlPoints() {
        List<ControlPoint> result = new ArrayList<>();
        for (CourseModule m : modules) {
            for (ControlPoint p : m.controlPoints()) {
                if (!result.contains(p)) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    /**
     * Равенство дисциплин по названию.
     *
     * @param o объект сравнения
     * @return true, если это дисциплина с тем же названием
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Subject other)) {
            return false;
        }
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
