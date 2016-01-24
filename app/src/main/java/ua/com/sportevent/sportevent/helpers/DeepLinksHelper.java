package ua.com.sportevent.sportevent.helpers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Helper class to add\remove GoogleApiClient
 */
public class DeepLinksHelper {
    static final String TAG = DeepLinksHelper.class.getName();

    /**
     * Before we can make calls to the App Indexing API,
     * we need to add an instance of GoogleApiClient to our Activity.
     * @param context context to create GoogleApiClient in.
     * @return an instance of GoogleApiClient.
     */
    public static GoogleApiClient addApi(@NonNull Context context) {
        return new GoogleApiClient.Builder(context).addApi(AppIndex.API).build();
    }

    /**
     * Only call this method once each time the user explicitly chooses to view some content.
     * Tell Google that the user has viewed a specific page within our app.
     * This will make that page available as an autocompletion the next time they search
     * for this topic in the Google app.
     * @param client Instantiated with the {@link DeepLinksHelper#addApi} GoogleApiClient.
     * @param TITLE Define a title for your current page, shown in autocompletion UI.
     *              It may be used in the Google app query autocompletion. Use an accurate
     *              and descriptive title
     * @param APP_URI This is the deep link that is triggered when the user clicks
     *                 on the autocomplete result. There should be an existing deep link
     *                 in the app for each API call. Provide full path.
     */
    public static void startAppIndexing(GoogleApiClient client, @NonNull final String TITLE,
                                        @NonNull final Uri APP_URI) {
        // Connect your client
        client.connect();

        Action viewAction = Action.newAction(Action.TYPE_VIEW, TITLE, APP_URI);

        // Call the App Indexing API view method
        PendingResult<Status> result = AppIndex.AppIndexApi.start(client, viewAction);

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "App Indexing API: Recorded view start successfully.");
                } else {
                    Log.e(TAG, "App Indexing API: There was an error recording the view."
                            + status.toString());
                }
            }
        });
    }

    /**
     * When a user has finished viewing a page, it's important to send an update to the API
     * to notify it that the pageview is completed.
     * Parameters the same as in the {@link DeepLinksHelper#startAppIndexing(GoogleApiClient, String, Uri)}.
     */
    public static void endAppIndexing(GoogleApiClient client, @NonNull final String TITLE,
                                      @NonNull final Uri APP_URI) {
        Action viewAction = Action.newAction(Action.TYPE_VIEW, TITLE, APP_URI);
        PendingResult<Status> result = AppIndex.AppIndexApi.end(client, viewAction);

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "App Indexing API: Recorded view end successfully.");
                } else {
                    Log.e(TAG, "App Indexing API: There was an error recording the view."
                            + status.toString());
                }
            }
        });

        client.disconnect();
    }
}
