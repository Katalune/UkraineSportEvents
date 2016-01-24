package ua.com.sportevent.sportevent.jsonModels;

import android.support.annotation.NonNull;

import java.util.Date;

import ua.com.sportevent.sportevent.helpers.Utility;

/**
 * Tariff in the {@link SportDiscipline}. Had id, price, content etc.
 */
public class DisciplinePackage implements Comparable<DisciplinePackage> {
    public String id;
    public String tariff;
    public String price;
    public String content;
    public String date_start;
    public String date_end;
    public String max_participants;
    public String participants;

    /**
     * Check if number of participants is less then max number. And end date is later
     * then current serverDate.
     * @param serverDate current serverDate.
     * @return false if one of mentioned conditions is false.
     */
    public boolean isValid(Date serverDate) {
        int maxNumber = Integer.valueOf(max_participants);
        int number = Integer.valueOf(participants);
        if (maxNumber > 0 && maxNumber <= number) return false;
        Date endDate = Utility.parseServerDate(date_end);
        return serverDate.before(endDate);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DisciplinePackage) {
            return this.tariff.equals(((DisciplinePackage) o).tariff);
        } else {
            return super.equals(o);
        }
    }

    /**
     * Compare this objects by their start date. The earliest is the smallest.
     */
    @Override
    public int compareTo(@NonNull DisciplinePackage another) {
        Date start = Utility.parseServerDate(date_start);
        Date otherStart = Utility.parseServerDate(another.date_start);
        if (start.before(otherStart)) return -1;
        if (start.after(otherStart)) return 1;
        return 0;
    }
}
