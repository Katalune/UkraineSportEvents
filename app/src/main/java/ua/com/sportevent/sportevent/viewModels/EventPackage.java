package ua.com.sportevent.sportevent.viewModels;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import ua.com.sportevent.sportevent.R;
import ua.com.sportevent.sportevent.helpers.Utility;
import ua.com.sportevent.sportevent.jsonModels.ExpandedPackage;

/**
 * Stores information about the event package.
 */
public class EventPackage {
    public final ObservableField<Boolean> buttonActive = new ObservableField<>();
    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> content = new ObservableField<>();
    public final ObservableField<Spanned> datePrice = new ObservableField<>();

    @BindingAdapter("app:viewEnabled")
    public static void setButtonState(Button button, boolean isActive) {
            button.setEnabled(isActive);
    }

    @BindingAdapter("app:viewVisibility")
    public static void setVisibility(TextView textView, String content) {
        if (content.length() > 0) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    /**
     * Assume that start date, end date and price is the lists of the same length in the
     * package. And this lists have at least one element.
     * @param expandedPackage package to load data to the model.
     * @param context context to get formatting string resources.
     */
    public EventPackage(@NonNull ExpandedPackage expandedPackage, Context context) {
        Date serverDate = Utility.getServerDate();
        // Check if button should be active
        boolean isActive = !expandedPackage.startDates.get(0).after(serverDate) &&
                !expandedPackage.endDates.get(0).before(serverDate);
        name.set(expandedPackage.tariff);
        content.set(expandedPackage.content);
        buttonActive.set(isActive);
        String activeFontColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(context, R.color.colorGrey800)));
        String inactiveFontColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(context, R.color.colorGrey500)));

        // Build all dates and prices in one block with text color formatting
        int len = expandedPackage.startDates.size();
        StringBuilder builder = new StringBuilder(len * 60); // approx len of one line
        if (isActive) builder.append("<font color=\"").append(activeFontColor).append("\">");
        else builder.append("<font color=\"").append(inactiveFontColor).append("\">");
        builder.append(buildString(expandedPackage.startDates.get(0), expandedPackage.endDates.get(0),
                expandedPackage.prices.get(0), context));
        if (isActive) builder.append("</font><font color=\"").append(inactiveFontColor).append("\">");
        for (int i = 1; i < len; i++) {
            builder.append("<br/>").append(buildString(expandedPackage.startDates.get(i), expandedPackage.endDates.get(i),
                    expandedPackage.prices.get(i), context));
        }
        builder.append("</font>");
        datePrice.set(Html.fromHtml(builder.toString()));
    }

    /**
     * Build the string with start - end dates and price
     * @param context Context used temporarily to get a format strings.
     */
    private String buildString(Date startDate, Date endDate, Float price, Context context) {
        DateFormat dateFormat =  DateFormat.getDateInstance(DateFormat.SHORT, Utility.getLocale(context));
        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);
        return String.format( context.getResources().getString(R.string.format_detail_price), startDateStr, endDateStr, price);
    }
}
