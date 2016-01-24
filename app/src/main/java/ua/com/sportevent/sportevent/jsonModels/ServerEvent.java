package ua.com.sportevent.sportevent.jsonModels;

/**
 * Model of the event in http://sportevent.com.ua/api/, part of {@link ServerEventApi}.
 */
public class ServerEvent {
    public String id;
    public String name;
    public String img_title;
    public String about;
    public String participants;
    // array of disciplines
    public SportDiscipline[] disciplines;
    // class of fields
    public EventFields fields;
    public String text;
    public String signed;
    // "events\/akvathlon-kyiv\/"
    public String link;
}
