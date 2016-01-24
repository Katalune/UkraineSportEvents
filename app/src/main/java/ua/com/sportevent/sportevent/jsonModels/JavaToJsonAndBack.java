package ua.com.sportevent.sportevent.jsonModels;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tester class to create json model.
 */
public class JavaToJsonAndBack {

    public static void main(String[] args) {

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting().serializeNulls();
        Gson gson = builder.create();

        DisciplinePackage dPackage = new DisciplinePackage();
        dPackage.id = "168";
        dPackage.tariff = "Standard";
        dPackage.price = "0.00";
        dPackage.content = "";

        EventField org = new EventField();
        org.name = "Organizer";
        org.value = "Capital TRI";
        EventField site = new EventField();
        site.name = "Sajt";
        site.value = "http:capital-triatlon.com";

        EventFields eventFields = new EventFields();
        eventFields.organizer = org;
        eventFields.site = site;

        SportDiscipline discipline = new SportDiscipline();
        discipline.id = "225";
        discipline.name = "800 m + 5 km";
        discipline.tariffs = new DisciplinePackage[] {dPackage};

        ServerEvent appointsEvent = new ServerEvent();
        appointsEvent.id = "224";
        appointsEvent.name = "Winter championship";
        appointsEvent.img_title="img/224";
        appointsEvent.about="december 18 2015 in the Kyiv";
        appointsEvent.text="Participation cost only 150uah";
        appointsEvent.participants="6";
        appointsEvent.disciplines = new SportDiscipline[] {discipline};
        appointsEvent.fields = eventFields;

        ServerEventApi eventlist = new ServerEventApi();
        eventlist.current_time = "24.11.2015 15:05:17";
        eventlist.appoints = new ServerEvent[]{appointsEvent};
        String out = gson.toJson(eventlist);
        System.out.println(gson.toJson(eventlist));

        ServerEventApi newApi = gson.fromJson(out, ServerEventApi.class);
        eventlist.current_time = "1";
    }
}