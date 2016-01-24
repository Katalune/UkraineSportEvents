package ua.com.sportevent.sportevent.jsonModels;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;

import ua.com.sportevent.sportevent.helpers.Utility;

/**
 * Not used in the json parsing. But created to transform parent class to more convenient form
 * without data duplicates.
 */
public class ExpandedPackage extends DisciplinePackage {
    public ArrayList<Date> endDates;
    public ArrayList<Date> startDates;
    public ArrayList<Float> prices;

    public ExpandedPackage(DisciplinePackage disciplinePackage) {
        this.id = disciplinePackage.id;
        this.tariff = disciplinePackage.tariff;
        prices = new ArrayList<>();
        prices.add(Float.parseFloat(disciplinePackage.price));
        this.content = disciplinePackage.content;
        startDates = new ArrayList<>();
        startDates.add(Utility.parseServerDate(disciplinePackage.date_start));
        endDates = new ArrayList<>();
        endDates.add(Utility.parseServerDate(disciplinePackage.date_end));
        this.max_participants = disciplinePackage.max_participants;
        this.participants = disciplinePackage.participants;
    }

    /**
     * Check if two objects are equals. If so - merged them (leave tariff and content the same,
     * but added endDate (parsed to Date), startDate (parsed to Date) and price (parsed to Float)
     * to the lists.
     * @param another Other object of the same or the parent class.
     * @return if objects were equal and merged.
     */
    public boolean merge(@NonNull DisciplinePackage another) {
        if (another.equals(this)) {
            // Add startDate, endDate and price to the lists.
            prices.add(Float.parseFloat(another.price));
            startDates.add(Utility.parseServerDate(another.date_start));
            endDates.add(Utility.parseServerDate(another.date_end));
            return true;
        }
        return false;
    }
}
