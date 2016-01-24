package ua.com.sportevent.sportevent.helpers;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import ua.com.sportevent.sportevent.R;
import ua.com.sportevent.sportevent.serverData.RetrofitHelper;
import ua.com.sportevent.sportevent.serverData.ServerContract;

/**
 * Contains helper functions.
 */
public class Utility {

    /**
     * @return if the device is connected to some network.
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }

    /**
     * @return the events status integer type.
     */
    public static @RetrofitHelper.EventsStatus
    int getErrorStatus() {
        return RetrofitHelper.ERROR_STATUS;
    }

    public static void clearUserData(Activity context) {
        StorageHelper.setProfileValues(null);
        RetrofitHelper.resetCookie();
        PreferenceHelper.deleteUserFullName(context);
    }

    /**
     * @return resource id of the error string.
     */
    public static int getErrorMessage(Context context) {
        int message;
        // If we haven't internet connection.
        if (!Utility.isNetworkConnected(context)) {
            message = R.string.empty_events_list_no_network;
        } else {
            @RetrofitHelper.EventsStatus
            int status = Utility.getErrorStatus();
            switch (status) {
                case RetrofitHelper.EVENTS_STATUS_SERVER_EMPTY:
                    message = R.string.empty_events_list_server_down;
                    break;
                case RetrofitHelper.EVENTS_STATUS_SERVER_INVALID:
                    message = R.string.empty_events_list_server_error;
                    break;
                case RetrofitHelper.EVENTS_STATUS_BAD_CONNECTION:
                    message = R.string.empty_events_list_bad_network;
                    break;
                case RetrofitHelper.PROFILE_STATUS_NOT_ACTIVATED:
                    message = R.string.empty_profile_not_activated;
                    break;
                case RetrofitHelper.PROFILE_STATUS_LOGOUT:
                    message = R.string.empty_profile_login;
                    break;
                case RetrofitHelper.PROFILE_STATUS_ERROR:
                    message = R.string.empty_profile_error;
                    break;
                case RetrofitHelper.BUYING_STATUS_EMPTY_PROFILE:
                    message = R.string.empty_profile_field;
                    break;
                case RetrofitHelper.EVENTS_STATUS_DEFAULT_OK:
                    message = R.string.load_ok;
                    break;
                case RetrofitHelper.EVENTS_STATUS_UNKNOWN:
                default:
                    message = R.string.empty_events_list;
            }
        }
        return message;
    }

    /**
     * @return resource id of the error image.
     */
    public static int getErrorImage(Context context) {
        int image;
        // If we haven't internet connection.
        if (!Utility.isNetworkConnected(context)) {
            image = R.drawable.ic_signal_wifi_off_black_48dp;
        } else {
            @RetrofitHelper.EventsStatus
            int status = Utility.getErrorStatus();
            switch (status) {
                case RetrofitHelper.EVENTS_STATUS_SERVER_EMPTY:
                case RetrofitHelper.EVENTS_STATUS_SERVER_INVALID:
                    image = R.drawable.ic_cloud_off_black_48dp;
                    break;
                case RetrofitHelper.EVENTS_STATUS_BAD_CONNECTION:
                    image = R.drawable.ic_signal_wifi_off_black_48dp;
                    break;
                case RetrofitHelper.PROFILE_STATUS_NOT_ACTIVATED:
                    image = R.drawable.ic_email_black_48dp;
                    break;
                case RetrofitHelper.PROFILE_STATUS_LOGOUT:
                case RetrofitHelper.PROFILE_STATUS_ERROR:
                case RetrofitHelper.BUYING_STATUS_EMPTY_PROFILE:
                    image = R.drawable.ic_account_circle_black_48dp;
                    break;
                case RetrofitHelper.EVENTS_STATUS_UNKNOWN:
                default:
                    image = R.drawable.ic_error_outline_black_48dp;
            }
        }
        return image;
    }

    /**
     * If var a can differ from the boundA1 to boundA2, and var b can differ accordingly from
     * boundB1 to boundB2.
     * @param outBound if the value of a and b can go outside of ranges.
     * @return value of b, from the equation deltaA/lenA = deltaB/lenB
     */
    public static float mapValue(int boundA1, int boundA2, int boundB1, int boundB2, float a, boolean outBound) {
        int lenA = Math.abs(boundA2 - boundA1);
        float deltaA = a - Math.min(boundA1, boundA2); // must be from 0 to lenA
        if (!outBound) {
            if (deltaA < 0) deltaA = 0;
            if (deltaA > lenA) deltaA = lenA;
        }
        float lenB = Math.abs(boundB2 - boundB1);
        return boundB1 + lenB * deltaA / lenA;
    }

    public static Date getServerDate() {
        Map eventValues = StorageHelper.getEventValues();
        Date lastUpdateDate = parseServerDate((String) eventValues.get(ServerContract.EventsEntry.LAST_UPDATE));
        // It assumes that we load data from the server AFTER last phone reboot!!
        long lastUpdatedElapsed = (long) eventValues.get(ServerContract.EventsEntry.ELAPSED_FOR_LAST_UPDATE);
        long timeSpent = SystemClock.elapsedRealtime() - lastUpdatedElapsed;
        return new Date(lastUpdateDate.getTime() + timeSpent);
    }

    @Nullable
    public static Date parseServerDate(String dateStr) {
        Date date = null;
        try {
            date = ServerContract.ServerEventsEntry.TIME_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    @NonNull
    public static Locale getLocale(Context context) {
        return PreferenceHelper.getLocale(context);
    }
}
