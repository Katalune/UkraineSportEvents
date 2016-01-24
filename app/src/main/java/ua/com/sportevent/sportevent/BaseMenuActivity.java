package ua.com.sportevent.sportevent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import ua.com.sportevent.sportevent.helpers.PreferenceHelper;

/**
 * Extend this class for each activity that should share the same Profile options menu item.
 */
public abstract class BaseMenuActivity extends LangActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener  {
    MenuItem mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // At this point we should already have this values
        // Set user name
        mName = menu.findItem(R.id.action_profile);
        mName.setTitle(PreferenceHelper.getUserFullName(this));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                // Go to the login (for check and redirect to profile)
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceHelper.isFullNameChanged(key)) {
            if (mName != null) {
                mName.setTitle(PreferenceHelper.getUserFullName(this, sharedPreferences));
            }
        }
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
}
