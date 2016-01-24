package ua.com.sportevent.sportevent;

import android.app.LoaderManager;
import android.content.Loader;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.login.LoginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.sportevent.sportevent.databinding.ActivityProfileBinding;
import ua.com.sportevent.sportevent.helpers.StorageHelper;
import ua.com.sportevent.sportevent.helpers.Utility;
import ua.com.sportevent.sportevent.serverData.RetrofitHelper;
import ua.com.sportevent.sportevent.serverData.ServerContract;
import ua.com.sportevent.sportevent.viewModels.UserProfile;
import ua.com.sportevent.sportevent.serverData.EventsLoader;
import ua.com.sportevent.sportevent.serverData.ServerData;

/**
 * Show user profile screen.
 */
public class ProfileActivity extends BaseLoadingActivity implements LoaderManager.LoaderCallbacks<Map> {
    protected AccessTokenTracker mAccessTokenTracker;
    UserProfile mUser;
    private CoordinatorLayout mCoordinatorLayout;
    private MenuItem mRefreshMenuItem;
    private boolean isLoading = true;
    private View mProfileLayout;
    private View mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fresco.initialize(this);
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.profile_coordinator);
        ((CollapsingToolbarLayout) mCoordinatorLayout.findViewById(R.id.collapsing_toolbar)).setTitle(getString(R.string.title_activity_profile));
        mProfileLayout = findViewById(R.id.profile_nestedscroll);
        mFab = findViewById(R.id.fab);

        // You don't need a registerCallback for login to succeed, you can choose to follow
        // current access token changes with the AccessTokenTracker class
        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                if (currentAccessToken != null) {
                    fetchData();
                } else {
                    // User logged out
                    userLogOut();
                }
            }
        };
    }

    @Override
    protected void setLoaderId() {
        LOADER_ID = PROFILE_LOADER;
    }

    void userLogOut() {
//        Utility.checkUserAuthorized(this);
        Utility.clearUserData(this);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAccessTokenTracker != null) mAccessTokenTracker.stopTracking();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        mRefreshMenuItem = menu.findItem(R.id.action_refresh);
        // Loading not finished yet.
        if (isLoading) {
            mRefreshMenuItem.setActionView(R.layout.progress_bar);
        }
        // Don't call super, because we don't need profile menu (we already here)
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // Fetch data from the server and update userProfile...
                fetchData();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveInfo(View v) {
        final Map<String, Object> values =  StorageHelper.getProfileValues();
        final List<String> updatedValueFields = new ArrayList<>(UserProfile.getUpdatedFields());
        final boolean existEmptyRequiredFields = UserProfile.existEmptyRequiredFields();
        int messageBeforeLoading;
        if (existEmptyRequiredFields) {
            messageBeforeLoading = R.string.profile_empty_required_error_message;
        } else if (updatedValueFields.size() == 0) {
            messageBeforeLoading = R.string.profile_nothing_edited_error_message;
        } else {
            messageBeforeLoading = R.string.profile_start_upload_message;
            (new Thread() {
                @Override
                public void run() {
                    ServerData.postProfileValues(values, updatedValueFields);
                    int messageAfterLoading;
                    switch (Utility.getErrorStatus()) {
                        case RetrofitHelper.EVENTS_STATUS_DEFAULT_OK:
                            messageAfterLoading = R.string.profile_post_success;
                            mUser.clearUpdatedFieldsAndNotify();
                            break;
                        default:
                            messageAfterLoading = R.string.empty_profile_post_error;
                    }
                    showMessage(messageAfterLoading);
                }
            }).start();
        }
        showMessage(messageBeforeLoading);
    }

    void fetchData() {
        // This loader should have been initialized in the onCreate.
        Loader<Object> loader = getLoaderManager().getLoader(LOADER_ID);
        loader.onContentChanged();
        if (mRefreshMenuItem != null) {
            mRefreshMenuItem.setActionView(R.layout.progress_bar);
        }
        isLoading = true;
    }

    @Override
    public Loader<Map> onCreateLoader(int id, Bundle bundle) {
        // Don't try to load data if we are not logged in!
        if (AccessToken.getCurrentAccessToken() == null) userLogOut();
        isLoading = true;
        return new EventsLoader(this, id);
    }

    @Override
    public void onLoadFinished(Loader<Map> loader, Map contentValues) {
        super.onLoadFinished(loader, contentValues);
        // Check that Menu was created and stops show loading state.
        if (isLoading) {
            if (mRefreshMenuItem != null) {
                mRefreshMenuItem.setActionView(null);
            }
            isLoading = false;
        }
    }

    @Override
    public void onLoaderReset(Loader<Map> loader) {    }

    @Override
    protected void updateDataAndSetIndexingAttributes(@NonNull Map data) {
        // Not registered on the server BUT logged in with the fb.
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        int status = Utility.getErrorStatus();
        if (status == RetrofitHelper.PROFILE_STATUS_LOGOUT
                && accessToken != null) {
            // Revoke all permissions.
            GraphRequest delPermRequest = new GraphRequest(accessToken,
                    "/" + Profile.getCurrentProfile().getId() + "/permissions/",
                    null,
                    HttpMethod.DELETE,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {
                            if(graphResponse != null){
                                FacebookRequestError error = graphResponse.getError();
                                if (error == null) {
                                    // Clear cached access token and profile.
                                    LoginManager.getInstance().logOut();
                                }
                            }
                        }
                    });
            delPermRequest.executeAsync();
        } else {
            // Show layout with profile info
            mProfileLayout.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.VISIBLE);
            mUser.fillUserInfo(data, this);
        }
        mTitle = "Спортивный профиль";
        mPath = ServerContract.ServerProfileEntry.PROFILE_PATH;
    }

    @Override
    protected void setContentView() {
        ActivityProfileBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        mUser = new UserProfile(this);
        binding.setUser(mUser);
    }

    @Override
    protected boolean dataNotAvailable() {
        return !mUser.isFilled();
    }

    void showMessage(int message) {
        Snackbar.make(mCoordinatorLayout, getString(message), Snackbar.LENGTH_LONG).show();
    }
}
