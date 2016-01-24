package ua.com.sportevent.sportevent.helpers;

import java.util.HashMap;
import java.util.Map;

import ua.com.sportevent.sportevent.viewModels.UserProfile;

/**
 * Save and read cached values.
 */
public class StorageHelper {
    // The most recent eventValues - updated by loader. We don't
    // initialize it, because while loading data from the server this variable can be set to null anyway.
    private static Map<String, Object> eventValues;

    // The most recent profileValues - updates either by loader or by user himself. We don't
    // initialize it, because while loading data from the server this variable can be set to null anyway.
    private static Map<String, Object> profileValues;

    /**
     * @return The most recent eventValues - updated by the loader.
     * Object with event info and field names from the ServerContract.ServerEventsEntry.
     */
    public static Map<String, Object> getEventValues() {
        return eventValues;
    }

    /**
     * Store the most recent values - recommended for use by the loader.
     * @param eventValues Object with event info and field names from the ServerContract.ServerEventsEntry.
     */
    public static void setEventValues(Map<String, Object> eventValues) {
        StorageHelper.eventValues = eventValues;
    }

    /**
     * @return Object with user info and field names from the ServerContract.ServerProfileEntry.
     * The most recent profileValues - updates either by loader or by user himself.
     * Can be null. Can store null value (so we made a check before writing
     * to the nullable profile fields).
     */
    public static Map<String, Object> getProfileValues() {
        return profileValues;
    }

    /**
     * Save only real values or null, if there's array of zero length we have nothing to do with it.
     * Should be called when we load new (not cached) data from the server - so it will overwrite
     * current fields and we don't care what was changed there anymore (we clear updatedFields).
     * @param profileValues User profile information with field names from the ServerContract.ServerProfileEntry.
     */
    public static void setProfileValues(Map<String, Object> profileValues) {
        StorageHelper.profileValues = profileValues;
        UserProfile.clearUpdatedFields();
    }

    /**
     * Update the most recent profileValues (NOT initially received from the server).
     * @return If the same key-value was already there return false and don't update the values. If
     * it is the new value - return true. If we have not values and new value is empty string -
     * return false and don't create new one.
     */
    public static boolean saveProfileValue(String key, String value) {
        if (profileValues == null) {
            if (value.length() > 0) {
                profileValues = new HashMap<>();
            } else {
                return false;
            }
        }
        if (profileValues.containsKey(key) && profileValues.get(key).equals(value)) {
            return false;
        } else {
            profileValues.put(key, value);
            return true;
        }
    }
}
