package ua.com.sportevent.sportevent.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import java.io.IOException;

import cz.msebera.android.httpclient.Header;
import ua.com.sportevent.sportevent.helpers.PreferenceHelper;
import ua.com.sportevent.sportevent.R;

/**
 * Handle getting GCM token and sending it to the server.
 */
public class RegistrationIntentService extends IntentService {
    private static final String TAG = RegistrationIntentService.class.getSimpleName();

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String token;

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // Initially this call goes out to the network to retrieve the token, subsequent
                // calls are local.
                // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
                // See https://developers.google.com/cloud-messaging/android/start for details on this file.
                token = InstanceID.getInstance(this)
                        .getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                // Check if we had'nt send it yet?
                if (!PreferenceHelper.getSentGcmToken(this)) {
                    // You should send the registration ID (token) to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationToServer(token);

                    // You should store a boolean that indicates whether the generated token has been
                    // sent to your server. If the boolean is false, send the token to your server,
                    // otherwise your server should have already received the token.
                    PreferenceHelper.setSentGcmToken(this, true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // If an exception happens while fetching the new token or updating our registration
            // data on a third-party server, this ensures that we'll attempt the update at a later
            // time.
            PreferenceHelper.setSentGcmToken(this, false);
        }
    }

    /**
     * Register a GCM registration token with the app server
     * @param token Registration token to be registered
     * @throws IOException
     */
    private void sendRegistrationToServer(String token) throws IOException {
        RequestParams requestParams = new RequestParams();
        // TODO: 27.11.2015 add user identifier (mb fb token or session id) - check if not null
        requestParams.put("name", "testname");
        requestParams.put("regid", token);
        SyncHttpClient client = new SyncHttpClient();
        client.post("http://my.site.com/register.php", requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

        // OR
        // TODO: 27.11.2015 choose 
        Bundle registration = createRegistrationBundle(token);

        GoogleCloudMessaging.getInstance(this).send(
                getServerUrl(getString(R.string.gcm_defaultSenderId)),
                String.valueOf(System.currentTimeMillis()), registration);
    }

    private String getServerUrl(String senderId) {
        return senderId + "@gcm.googleapis.com";
    }

    /**
     * Creates the registration bundle and fills it with user information
     * @param token Registration token to be registered
     * @return A bundle with registration data.
     */
    private Bundle createRegistrationBundle(String token) {
        Bundle registration = new Bundle();

        // Create the bundle for registration with the server.
        registration.putString("action", "new_client");
        registration.putString("token", token);
        registration.putString("identifier", "user identifier here");
        return registration;
    }
}
