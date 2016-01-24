package ua.com.sportevent.sportevent;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import ua.com.sportevent.sportevent.gcm.RegistrationIntentService;
import ua.com.sportevent.sportevent.serverData.EventsLoader;

/**
 * This class ensure that the user is logged in with fb and has email read permission granted.
 * After that it redirects to the destination activity getIntent().getIntExtra(DESTINATION_KEY, DESTINATION_PROFILE).
 * Any error handling (internet absence or unauthorized user) should provide destination activity.
 */
public class LoginActivity extends LangActivity implements FacebookCallback<LoginResult>, LoaderManager.LoaderCallbacks<Map> {
    public static final int DESTINATION_PROFILE = 0;
    public static final int DESTINATION_WEBVIEW = 1;
    public static final String DESTINATION_KEY = "destination";
    private CallbackManager callbackManager;
    protected AccessTokenTracker mAccessTokenTracker;
    private View mLoginCard;
    private View mValidateCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the SDK before executing any other operations.
        // Your app can only have one person at a time logged in and LoginManager sets
        // the current AccessToken and Profile for that person.
        // The FacebookSDK saves this data in shared preferences and sets it during
        // SDK initialization.
        FacebookSdk.sdkInitialize(getApplicationContext());
        // Register tracker in case of async FB initialization.
        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Load user data from the server using
                // currentAccessToken when it's loaded from FB.
                getPermissionsOrRedirect(currentAccessToken);
            }
        };

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        mLoginCard = findViewById(R.id.login_cardview);
        mValidateCard = findViewById(R.id.login_validate_cardview);

        // Create a callback manager to handle login responses.
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Collections.singletonList("public_profile, email, user_birthday"));
        // Callback registration
        loginButton.registerCallback(callbackManager, this);

        // Need user log-in via facebook
        AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        if (currentAccessToken == null) {
            showLogin();
        } else {
            if (currentAccessToken.isExpired()) {
                AccessToken.refreshCurrentAccessTokenAsync();
            }
            getPermissionsOrRedirect(currentAccessToken);
        }
    }

    private void showLogin() {
        mLoginCard.setVisibility(View.VISIBLE);
        mValidateCard.setVisibility(View.GONE);
    }

    private void showValidate() {
        mLoginCard.setVisibility(View.GONE);
        mValidateCard.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAccessTokenTracker.stopTracking();
    }

    boolean getPermissionsOrRedirect(@NonNull AccessToken currentAccessToken) {
        Set<String> declined = currentAccessToken.getDeclinedPermissions();
        if (declined.contains("email")) {
            // Re-ask
            LoginManager.getInstance().logInWithReadPermissions(this, Collections.singletonList("email"));
            return false;
        } else {
            loadAndRedirect();
            return true;
        }
    }

    private void loadAndRedirect() {
        showValidate();
        int destination = getIntent().getIntExtra(DESTINATION_KEY, DESTINATION_PROFILE);
        switch (destination) {
            case DESTINATION_WEBVIEW:
                Loader<Object> loader = getLoaderManager().getLoader(BaseLoadingActivity.AUTHORIZE_LOADER);
                if (loader != null) {
                    // If we already have this loader - indicate to start new loading on next start.
                    loader.onContentChanged();
                }
                // Prepare the loader.  Either re-connect with an existing one, or start a new one.
                // Usually we do this in the onCreate.
                getLoaderManager().initLoader(BaseLoadingActivity.AUTHORIZE_LOADER, null, this);
                break;
            case DESTINATION_PROFILE:
            default:
                startActivity(new Intent(this, ProfileActivity.class));
                finish(); // finish the current activity as we don't need it anymore
                break;
        }
    }

    /**
     * Forward the login results to the callbackManager created in onCreate():
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // We already have mAccessTokenTracker for this case.
    @Override
    public void onSuccess(LoginResult loginResult) {
        Log.e("TAG", "on success");
    }

    @Override
    public void onCancel() {
        Log.e("TAG", "on cancel");
        finish();
    }

    @Override
    public void onError(FacebookException error) {
        Log.e("TAG", "on error");
    }

    @Override
    public Loader<Map> onCreateLoader(int id, Bundle bundle) {
        return new EventsLoader(this, id);
    }

    @Override
    public void onLoadFinished(Loader<Map> loader, Map values) {
        // CAUTION: any error handling should do destination activity.
        Intent destinationIntent = new Intent(this, WebViewActivity.class);
        // Add details about package and discipline.
        destinationIntent.putExtra(DetailActivity.EXTRA_BUYING, getIntent().getParcelableExtra(DetailActivity.EXTRA_BUYING));
        startActivity(destinationIntent);
        finish(); // finish the current activity as we don't need it anymore

        // TODO: 13.12.2015 place this somewhere
        // Register this application with GCM and send (if was not sent) gcm token with the user
        // identification information (if available).
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) ==
                ConnectionResult.SUCCESS) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Map> loader) {    }

    public void finish(View view) {
        finish();
    }
}
