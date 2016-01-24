package ua.com.sportevent.sportevent.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import ua.com.sportevent.sportevent.R;

/**
 * Handle access to the SharedPreferences.
 */
public class PreferenceHelper {
    // Keys
    private static final String SENT_GCM_TOKEN = "sent_token_to_server";
    private static final String USER_FULL_NAME = "user_full_name";
    private static final String EVENTS_LAYOUT_TYPE = "event_layout_type";
    private static final String DETAIL_EVENT_ID = "detail_event_id";
    private static final String EVENTS_LANG = "events_language";
    // Values
    private static final int LAYOUTS_NUMBER = 2;
    private static final int LAYOUT_BIG = 0;
    private static final int LAYOUT_MEDIUM = 1;
    private static final int LAYOUT_SMALL = 2;
    // The strings to create locale from
    public static final String LANG_RU = "ru";
    public static final String LANG_EN = "en";
    private static final String COUNTRY_RU = "RU";
    private static final String COUNTRY_EN = "US";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({LANG_EN, LANG_RU})
    public @interface EventLang {}

    public static boolean getSentGcmToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SENT_GCM_TOKEN, false);
    }

    public static void setSentGcmToken(Context context, Boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(SENT_GCM_TOKEN, value).apply();
    }

    /**
     * @param context Context to access system resources.
     * @return Name and last name from the user profile. In case of any error - return menu
     * username text.
     */
    public static String getUserFullName(Context context) {
        return getUserFullName(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static String getUserFullName(Context context, SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(USER_FULL_NAME, context.getString(R.string.action_username_text));
    }

    public static void setUserFullName(Context context, String fullName) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(USER_FULL_NAME, fullName).apply();
    }

    public static void deleteUserFullName(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(USER_FULL_NAME).apply();
    }

    public static boolean isFullNameChanged(String key) {
        return key.equals(USER_FULL_NAME);
    }

    /**
     * @return id of the events layout.
     */
    public static int getEventsLayout(Context context) {
        int layout = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(EVENTS_LAYOUT_TYPE, LAYOUT_BIG);
        switch (layout) {
            case LAYOUT_MEDIUM:
                return R.layout.list_item_event_medium;
            case LAYOUT_SMALL:
                return R.layout.list_item_event_small;
            case LAYOUT_BIG:
            default:
                return R.layout.list_item_event_large;
        }
    }

    /**
     * Switch layout type to the next.
     */
    public static void setEventsLayout(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int currentType = sharedPreferences.getInt(EVENTS_LAYOUT_TYPE, R.layout.list_item_event_large);
        currentType++;
        sharedPreferences.edit().putInt(EVENTS_LAYOUT_TYPE, currentType % LAYOUTS_NUMBER).apply();
    }

    /**
     * Save the latest id of the event chosen by the user.
     * @param id The latest id of the event. {@link ua.com.sportevent.sportevent.jsonModels.ServerEvent#id}.
     */
    public static void setDetailsEventId(Context context, String id) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(DETAIL_EVENT_ID, id).apply();
    }

    /**
     * @return The latest id of the event to show to the user in the Detail Screen.
     */
    public static String getDetaileEventId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(DETAIL_EVENT_ID, "");
    }

    public static void setLang(Context context, @EventLang String lang) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(EVENTS_LANG, lang).apply();
    }

    /**
     * @return the string to create Locale from: new Locale(lang);
     */
    @EventLang
    public static String getLang(Context context) {
        @EventLang
        String lang = PreferenceManager.getDefaultSharedPreferences(context).getString(EVENTS_LANG, LANG_RU);
        return lang;
    }

    public static Locale getLocale(Context context) {
        @EventLang
        String lang = PreferenceManager.getDefaultSharedPreferences(context).getString(EVENTS_LANG, LANG_RU);
        String country;
        switch (lang) {
            case LANG_EN:
                country = COUNTRY_EN;
                break;
            default:
                country = COUNTRY_RU;
        }
        return new Locale(lang, country);
    }
}
