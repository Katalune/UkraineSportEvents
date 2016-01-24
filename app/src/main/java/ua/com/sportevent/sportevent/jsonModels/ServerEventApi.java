package ua.com.sportevent.sportevent.jsonModels;

/**
 * Root object in the http://sportevent.com.ua/api/
 */
public class ServerEventApi {
    // Array of events
    public ServerEvent[] appoints;
    public String current_time;
}
