package ua.com.sportevent.sportevent;

import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Date;
import java.util.Map;

import ua.com.sportevent.sportevent.helpers.PreferenceHelper;
import ua.com.sportevent.sportevent.helpers.Utility;
import ua.com.sportevent.sportevent.jsonModels.ServerEvent;
import ua.com.sportevent.sportevent.serverData.EventsLoader;
import ua.com.sportevent.sportevent.serverData.ServerContract;

public class EventsActivity extends BaseLoadingActivity
        implements EventAdapter.OnClickHandler, AppBarLayout.OnOffsetChangedListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AppBarLayout mAppBar;
    private EventAdapter mAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.events_menu, menu);
        menu.findItem(R.id.action_grid).setChecked(PreferenceHelper.getEventsLayout(this) == R.layout.list_item_event_large);
        final boolean isEn = PreferenceHelper.getLang(this).equals(PreferenceHelper.LANG_EN);
        if (isEn) menu.findItem(R.id.action_lang_en).setChecked(true);
        else  menu.findItem(R.id.action_lang_ru).setChecked(true);
//        item.setIcon(getGridMenuIcon(PreferenceHelper.getEventsLayout(this)));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_grid:
                // Go to the login (for check and redirect to profile)
                // Change layout in prefs and notify adapter
                PreferenceHelper.setEventsLayout(this);
                int layout = PreferenceHelper.getEventsLayout(this);
                mAdapter.setLayoutType(layout);
                item.setChecked(layout == R.layout.list_item_event_large);
//                item.setIcon(getGridMenuIcon(layout));
                return true;
            case R.id.action_lang_en:
                return setEventsLanguage(item, PreferenceHelper.LANG_EN);
            case R.id.action_lang_ru:
                return setEventsLanguage(item, PreferenceHelper.LANG_RU);
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean setEventsLanguage(MenuItem item, @PreferenceHelper.EventLang String newLang) {
        String oldLang = PreferenceHelper.getLang(this);
        if (oldLang.equals(newLang)) return false;
        item.setChecked(true);
        PreferenceHelper.setLang(this, newLang);
        refreshContent();
        finish();
        startActivity(getIntent());
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Make sure this is before calling super.onCreate
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        mAppBar = (AppBarLayout) findViewById(R.id.app_bar);
        ((CollapsingToolbarLayout) mAppBar.findViewById(R.id.collapsing_toolbar)).setTitle(getString(R.string.title_activity_events));

        // Switch event item layout based on sharedPrefs.
        mAdapter = new EventAdapter(this, PreferenceHelper.getEventsLayout(this), Utility.getLocale(this));
        // Attach an adapter for the data to be displayed.
        RecyclerView recyclerviewEventlist = (RecyclerView) findViewById(R.id.recyclerview_eventlist);
        recyclerviewEventlist.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccentMedium, R.color.colorAccent2);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });
    }

    @Override
    protected void setLoaderId() {
        LOADER_ID = EVENTS_LOADER;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Need for correct swipelayout work.
        mAppBar.addOnOffsetChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAppBar.removeOnOffsetChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        //The Refresh must be only active when the offset is zero :
        mSwipeRefreshLayout.setEnabled(verticalOffset == 0);
    }

    void refreshContent() {
        if (Utility.isNetworkConnected(this) || dataNotAvailable()) {
            getLoaderManager().getLoader(LOADER_ID).onContentChanged();
            if (dataNotAvailable()) {
                // We show empty view, no need to show swipe refresher
                mSwipeRefreshLayout.setRefreshing(false);
            }
        } else {
            // There's no Internet connection, so we would not try to refresh.
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onEventItemClick() {
        startActivity(new Intent(this, DetailActivity.class));
    }

    /**
     * Instantiate and return a new Loader for the given ID. Will be executed in a background thread.
     * @return a new Loader.
     */
    @Override // Loader callback method
    public Loader<Map> onCreateLoader(int id, Bundle args) {
        return new EventsLoader(this, id);
    }

    @Override // Loader callback method. Isn't called if the data has the same reference.
    public void onLoadFinished(Loader<Map> loader, Map data) {
        super.onLoadFinished(loader, data);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private String getFriendlyDate(Map map) {
        Date lastUpdateDate = Utility.parseServerDate((String) map.get(ServerContract.EventsEntry.LAST_UPDATE));
        long lastUpdatedElapsed = (long) map.get(ServerContract.EventsEntry.ELAPSED_FOR_LAST_UPDATE);
        long timeSpent = SystemClock.elapsedRealtime() - lastUpdatedElapsed;
        return String.valueOf(DateUtils.getRelativeTimeSpanString(lastUpdateDate.getTime(), lastUpdateDate.getTime() + timeSpent, DateUtils.DAY_IN_MILLIS)).toLowerCase(Utility.getLocale(this));
    }

    /**
     * Called when a previously created loader is being reset, thus making its data unavailable.
     * Loader callback method.
     */
    @Override
    public void onLoaderReset(Loader<Map> loader) {
        mAdapter.updateData(null);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void updateDataAndSetIndexingAttributes(@NonNull Map data) {
        mAdapter.updateData(null);
        ServerEvent[] serverEvents = (ServerEvent[]) data.get(ServerContract.EventsEntry.EVENTS);
        // Add without delay - to prevent bad experience on screen rotation.
        for (ServerEvent serverEvent : serverEvents) {
            mAdapter.updateData(serverEvent);
        }
        mTitle =  getString(R.string.indexed_title_activity_events);
        mPath = ServerContract.ServerEventsEntry.EVENTS_PATH;
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_events);
    }

    @Override
    protected boolean dataNotAvailable() {
        return mAdapter.getItemCount() == 0;
    }
}
