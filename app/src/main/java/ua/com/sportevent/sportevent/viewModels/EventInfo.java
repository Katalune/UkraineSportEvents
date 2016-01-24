package ua.com.sportevent.sportevent.viewModels;

import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ua.com.sportevent.sportevent.R;
import ua.com.sportevent.sportevent.helpers.Utility;
import ua.com.sportevent.sportevent.jsonModels.EventField;
import ua.com.sportevent.sportevent.jsonModels.ServerEvent;
import ua.com.sportevent.sportevent.serverData.ServerContract;

/**
 * Stores information about the event.
 */
public class EventInfo {

    public static final int DAY_MILLIS = 24 * 60 * 60 * 1000;
    public static final int SOON_DAYS = 14;
    // Info about all events.
    public final ObservableField<String> lastUpdate = new ObservableField<>("");
    // Instance specific information.
    public final ObservableField<String> name = new ObservableField<>("");
    public final ObservableField<String> cover = new ObservableField<>("");
    public final ObservableField<String> description = new ObservableField<>("");
    public final ObservableField<String> text = new ObservableField<>();
    public final ObservableField<String> organizer = new ObservableField<>("");
    public final ObservableField<String> site = new ObservableField<>("");
    public final ObservableField<String> city = new ObservableField<>("");
    public final ObservableField<String> where = new ObservableField<>();
    public final ObservableField<String> startMonth = new ObservableField<>("");
    public final ObservableField<String> startDay = new ObservableField<>("");
    public final ObservableField<String> startTime = new ObservableField<>("");
    public final ObservableField<String> startFullDate = new ObservableField<>("");
    public final ObservableField<Integer> icoRun = new ObservableField<>();
    public final ObservableField<Integer> icoBike = new ObservableField<>();
    public final ObservableField<Integer> icoSwim = new ObservableField<>();
    public final ObservableField<String> participants = new ObservableField<>("");
    public final ObservableField<String> regEnd = new ObservableField<>("");
    public final ObservableField<String> signed = new ObservableField<>("");
    public String mEventId;

    @BindingAdapter("app:textColor")
    public static void setColor(TextView textView, String dateStr) {
        boolean isSoon;
        try {
            Date eventDate = DateFormat.getDateInstance(DateFormat.SHORT, Utility.getLocale(textView.getContext())).parse(dateStr);
            Date serverDate = Utility.getServerDate();
            long different = eventDate.getTime() - serverDate.getTime();
            long elapsedDays = different / DAY_MILLIS;
            isSoon = elapsedDays < SOON_DAYS;
        }  catch (Exception e) {
            isSoon = false;
        }
        int colorID = isSoon ? R.color.colorRed : R.color.colorGrey500;
        int color = ContextCompat.getColor(textView.getContext(), colorID);
        textView.setTextColor(color);
    }

    @BindingAdapter("app:loadHtml")
    public static void loadData(WebView web, String data) {
        web.setBackgroundColor(0x00000000);
        web.loadDataWithBaseURL(ServerContract.ServerEventsEntry.BASE_URL_API, data, "text/html", "utf-8", null);
        web.getSettings().setTextZoom(100);
//        DisplayMetrics metrics = web.getResources().getDisplayMetrics();
//        float k = metrics.scaledDensity / metrics.density;
//        web.getSettings().setTextZoom((int) (90f * k));
    }

    @BindingAdapter("app:coverImg")
    public static void loadImage(ImageView img, String relativeUrl) {
        // Load img with width and height.
        int width = img.getWidth();
        int height = img.getHeight();
        if (width == 0) {
            DisplayMetrics displaymetrics = img.getResources().getDisplayMetrics();
            width = displaymetrics.widthPixels / 2;
            height = width;
        }
        if (width > 0 && height > 0 && relativeUrl != null) {
            Glide.with(img.getContext())
                    .load(ServerContract.getAbsoluteImgUrl(relativeUrl, width, height))
                    .placeholder(R.drawable.logo_loader)
                    .centerCrop()
                    .dontAnimate()
                    .into(img);
        }
    }

    @BindingAdapter("app:ico")
    public static void setImgRes(ImageView view, Integer res) {
        Integer resInt;
        if (res == null) {
            resInt = android.R.color.transparent;
        } else {
            resInt = res;
        }
        view.setImageResource(resInt);
    }

    /**
     * Create event info with only one variable set - time of last update.
     */
    public EventInfo(String lastUpdate) {
        this.lastUpdate.set(lastUpdate);
    }

    /**
     * Set event info with notification of binded fields.
     */
    public EventInfo(ServerEvent eventInfo, final Locale locale) {
        mEventId = eventInfo.id;
        setIfNotNull(signed, eventInfo.signed);
        setIfNotNull(name, eventInfo.name);
        setIfNotNull(cover, eventInfo.img_title);
        setIfNotNull(description, eventInfo.about);
        setIfNotNull(participants, eventInfo.participants);
        setIfNotNull(organizer, eventInfo.fields.organizer);
        setIfNotNull(site, eventInfo.fields.site);
        setIfNotNull(city, eventInfo.fields.city);
        setIfNotNull(where, eventInfo.fields.where);

        // start time convert to date.
        String header = null;
        String footer = null;
        String time = null;
        String fullDate = null;
        try {
            Date date = Utility.parseServerDate(eventInfo.fields.event_time.value);
            DateFormat full_format =  DateFormat.getDateInstance(DateFormat.FULL, locale);
            DateFormat month_format = new SimpleDateFormat("LLL", locale);
            DateFormat day_format = new SimpleDateFormat("dd", locale);
            DateFormat time_format = new SimpleDateFormat("HH:mm", locale);
            header = month_format.format(date);
            footer = day_format.format(date);
            time = time_format.format(date);
            fullDate = full_format.format(date);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        setIfNotNull(startMonth, header);
        setIfNotNull(startDay, footer);
        setIfNotNull(startTime, time);
        setIfNotNull(startFullDate, fullDate);

        // run/bike/swim
        icoRun.set(eventInfo.fields.run.value.length() > 0 ? R.drawable.run : android.R.color.transparent);
        icoSwim.set(eventInfo.fields.swim.value.length() > 0 ? R.drawable.swim : android.R.color.transparent);
        icoBike.set(eventInfo.fields.bike.value.length() > 0 ? R.drawable.bike : android.R.color.transparent);
        // reg end
        String regEndStr = null;
        try {
            Date dateRegEnd = Utility.parseServerDate(eventInfo.fields.registration_end.value);
            DateFormat regFormat =  DateFormat.getDateInstance(DateFormat.SHORT, locale);
            regEndStr = regFormat.format(dateRegEnd);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        setIfNotNull(regEnd, regEndStr);
        setIfNotNull(text, eventInfo.text);
    }

    private void setIfNotNull(ObservableField<String> field, String value) {
        if (value != null) field.set(value);
    }

    private void setIfNotNull(ObservableField<String> field, EventField valueField) {
        if (valueField != null) field.set(valueField.value);
    }
}
