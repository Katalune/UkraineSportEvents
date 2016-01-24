package ua.com.sportevent.sportevent;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.facebook.FacebookSdk;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Map;

import ua.com.sportevent.sportevent.helpers.DeepLinksHelper;
import ua.com.sportevent.sportevent.helpers.Utility;
import ua.com.sportevent.sportevent.serverData.ServerContract;

/**
 * Handle some common functionality:
 * - Set ActionBar (need R.id.toolbar defined in the layout!);
 * - initialize the events loader with LOADER_ID;
 * - updateEmptyView in onLoadFinished;
 * - Add App Indexing Api;
 * - start app indexing in the onLoadFinished;
 * - stop app indexing in the onStop;
 * - child class should define mTitle, mPath in class abstract methods.
 * - implements inner class EventsUpdateReceiver to trigger connection change
 * - set Glide memory category.
 */
public abstract class BaseLoadingActivity extends ErrorActivity implements LoaderManager.LoaderCallbacks<Map> {
    public static final int EVENTS_LOADER = 0;
    public static final int PROFILE_LOADER = 1;
    public static final int AUTHORIZE_LOADER = 2;
    private static final Uri BASE_APP_URI = Uri.parse("android-app://ua.com.sportevent.sportevent/http/sportevent.com.ua/");
    protected int LOADER_ID;
    protected String mTitle;
    protected String mPath;
    private GoogleApiClient mClient;
    private EventsUpdateReceiver mEventsUpdateObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the SDK before start tracking fb token state.
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);

        mClient = DeepLinksHelper.addApi(this);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        // We typically initialize a Loader within the activity's onCreate() method,
        setLoaderId();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Classes that are going to load events data - set LOADER_ID = EVENTS_LOADER.
     * For profile data - set = PROFILE_LOADER.
     */
    protected abstract void setLoaderId();

    @Override
    protected void onResume() {
        super.onResume();
        // This loader should have been initialized in the onCreate.
        Loader<Object> loader = getLoaderManager().getLoader(LOADER_ID);
        // Create and register the observer - start watching for changes.
        mEventsUpdateObserver = new EventsUpdateReceiver(loader);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mEventsUpdateObserver);
        mEventsUpdateObserver = null;
    }

    @Override
    protected void onStop() {
        if (mTitle != null && mPath != null) {
            DeepLinksHelper.endAppIndexing(mClient, mTitle, getEventUri(mPath));
        }
        super.onStop();
    }

    private static Uri getEventUri(String path) {
        return BASE_APP_URI.buildUpon().appendEncodedPath(path).build();
    }

    /**
     * @return path as sportevent.com.ua/path
     */
    protected static Uri getEventUrl(String path) {
        return Uri.parse(ServerContract.BASE_URL).buildUpon().appendEncodedPath(path).build();
    }

    /**
     * Called when a previously created loader has finished its load. Perform any UI updates
     * we’d want to make. Isn't called if the data has the same reference.
     * Current implementation update empty view, adapter and starts app indexing. Need Non Null
     * mPath. Child class should call super().
     */
    @Override
    public void onLoadFinished(Loader<Map> loader, Map data) {
        if (data != null && data.size() > 0) {
            updateDataAndSetIndexingAttributes(data);
            DeepLinksHelper.startAppIndexing(mClient, mTitle, getEventUri(mPath));
        }
        updateEmptyView(false);
    }

    /**
     * This method called onLoadFinished. At this point you should remove all use of the old data,
     * but should not do your own release since its loader owns it. For example, child class
     * can update adapters data. (updateEmptyView called after this method).
     * Also Child class should set the title and the path.
     * Define mTitle as a title for your current page, shown in autocompletion UI.
     * It may be used in the Google app query autocompletion. Use an accurate and descriptive title.
     * Define mPath as a deep link that is triggered when the user clicks on the autocomplete result.
     * There should be an existing deep link in the app for each API call.
     * Provide the part of the path that comes after sportevent.com.ua/your_path.
     * @param data New data to update adapter.
     *             ((Utility.UpdatableWithServerEvent) mAdapter).updateData(serverEvent);
     */
    protected abstract void updateDataAndSetIndexingAttributes(@NonNull Map data);

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level > ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            Glide.get(this).setMemoryCategory(MemoryCategory.LOW);
        } else  {
            Glide.get(this).setMemoryCategory(MemoryCategory.NORMAL);
        }
    }

    /**
     * Helper class to look for interesting changes so that the loader can be updated. This is
     * an inner class, so the instance of this class should not exist longer than parent activity.
     */
    private class EventsUpdateReceiver extends BroadcastReceiver {
        final Loader<Object> mLoader;

        public EventsUpdateReceiver(Loader<Object> loader) {
            mLoader = loader;
            // We are registering an observer (this) to receive Intents
            // with certain actions name.
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(this, filter);
        }

        @Override public void onReceive(Context context, Intent intent) {
            // Tell the loader to load new data if we have not data and need to either update
            // the reason of its absence or download it. OR if user explicitly tell us
            // to update.
            if (dataNotAvailable()) {
                if (Utility.isNetworkConnected(getApplicationContext())) {
                    mLoader.onContentChanged();
                    updateEmptyView(true);
                } else {
                    updateEmptyView(false);
                }
            }
        }
    }
}
