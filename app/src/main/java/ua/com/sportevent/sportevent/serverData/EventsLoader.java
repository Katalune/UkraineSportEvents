package ua.com.sportevent.sportevent.serverData;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import ua.com.sportevent.sportevent.BaseLoadingActivity;
import ua.com.sportevent.sportevent.helpers.PreferenceHelper;
import ua.com.sportevent.sportevent.helpers.StorageHelper;

/**
 * Load data from the server or return cached values.
 */
public class EventsLoader extends AsyncTaskLoader<Map> {
    private final int ID;
    // The observer could be anything so long as it is able to detect content changes
    // and report them to the loader with a call to onContentChanged().

    public EventsLoader(Context context, int id) {
        // Loaders may be used across multiple Activities (assuming they aren't
        // bound to the LoaderManager), so NEVER hold a reference to the context
        // directly. Doing so will cause you to leak an entire Activity's context.
        // The superclass constructor will store a reference to the Application
        // Context instead, and can be retrieved with a call to getContext().
        super(context);
        ID = id;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public Map loadInBackground() {
        Map<String, Object> data = null;
        switch (ID) {
            case BaseLoadingActivity.EVENTS_LOADER:
                data = ServerData.getEventsValues();
                break;
            case BaseLoadingActivity.PROFILE_LOADER:
            case BaseLoadingActivity.AUTHORIZE_LOADER:
                data = ServerData.getProfileValues();
                break;
        }
        // We care about caching the data only if it contains some information.
        if (data != null) {
            switch (ID) {
                case BaseLoadingActivity.EVENTS_LOADER:
                    StorageHelper.setEventValues(data);
                    break;
                case BaseLoadingActivity.PROFILE_LOADER:
                case BaseLoadingActivity.AUTHORIZE_LOADER:
                    StorageHelper.setProfileValues(data);
                    String name = (String) data.get(ServerContract.ProfileEntry.FIRST_NAME);
                    String lastName = (String) data.get(ServerContract.ProfileEntry.LAST_NAME);
                    StringBuilder builder = (new StringBuilder(name)).append(" ").append(lastName);
                    PreferenceHelper.setUserFullName(this.getContext(), builder.toString());
                    break;
            }
        } else {
            // During loading something can goes wrong, so in this case we return our cache.
            Map<String, Object> temp = getCache();
            if (temp != null) {
                // The cleanest way is to build a new data each time by passing in the existing List
                // and copy the contents.
                data = new HashMap<>(temp);
            }
        }

        // Build new data to force the loader to return the result.
        if (data == null) data = new HashMap<>(0);
        return data;
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        // Get data from the cache - as the most recent updated source.
        Map data = getCache();

        // Give whatever true or error value we have.
        if (data != null) {
            deliverResult(data);
        }

        // Load new data when we have no data, OR have notification about content change
        // from the observer.
        if (takeContentChanged() || data == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
            forceLoad();
        }
    }

    @Nullable
    private Map<String, Object> getCache() {
        Map<String, Object> data = null;
        switch (ID) {
            case BaseLoadingActivity.EVENTS_LOADER:
                data = StorageHelper.getEventValues();
                break;
            case BaseLoadingActivity.PROFILE_LOADER:
                // If ProfileActivity will receive data with less than 1 element, it will show
                // an error message.
                data = StorageHelper.getProfileValues();
                break;
        }
        return data;
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // The Loader is in a stopped state, so we should attempt to cancel the
        // current load (if there is one).
        cancelLoad();

        // Note that we leave the observer as is. Loaders in a stopped state
        // should still monitor the data source for changes so that the Loader
        // will know to force a new load if it is ever started again.
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();
    }
}


