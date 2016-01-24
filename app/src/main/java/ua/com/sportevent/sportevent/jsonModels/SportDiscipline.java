package ua.com.sportevent.sportevent.jsonModels;

/**
 * Discipline in the {@link ServerEvent}. Has id, name and array of {@link DisciplinePackage}.
 */
public class SportDiscipline {
    public String id;
    public String name;
    // array of tariffs
    public DisciplinePackage[] tariffs;
}
