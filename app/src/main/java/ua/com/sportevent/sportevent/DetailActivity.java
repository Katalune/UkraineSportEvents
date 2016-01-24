package ua.com.sportevent.sportevent;

import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import ua.com.sportevent.sportevent.helpers.PreferenceHelper;
import ua.com.sportevent.sportevent.helpers.Utility;
import ua.com.sportevent.sportevent.jsonModels.ServerEvent;
import ua.com.sportevent.sportevent.serverData.EventsLoader;
import ua.com.sportevent.sportevent.serverData.ServerContract;

/**
 * Show detail information about event.
 */
public class DetailActivity extends BaseLoadingActivity implements RequestListener<String, GlideDrawable> {
    public static final String EXTRA_BUYING = "extra_buying";

    private CoordinatorLayout.LayoutParams mLayoutParams;
    private RecyclerView mRecyclerView;
    private View mAppbar;
    private ServerEvent mServerEvent;
    private DetailDisciplineAdapter mAdapter;
    private View mBottomScrim;
    private View mTopScrim;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private ImageView mToolbarCover;
    private ShareActionProvider mShareActionProvider;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.detail_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem itemShare = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider. You cannot use android.widget.ShareActionProvider
        // with the appcompat-v7 action bar backport
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(itemShare);
        setShareIntent(createShareEventIntent());

//        return super.onCreateOptionsMenu(menu);
        // Don't add contextually irrelevant menu items.
        return true;
    }

    /**
     * @return intent with text information about current event. Null if event is null.
     */
    @Nullable
    private Intent createShareEventIntent() {
        if (mServerEvent == null) return null;
        Date date = Utility.parseServerDate(mServerEvent.fields.event_time.value);
        DateFormat dateFormat =  DateFormat.getDateInstance(DateFormat.LONG, Utility.getLocale(this));
        String strDate;
        try {
            strDate = dateFormat.format(date);
        } catch (Exception e) {
            strDate = "";
        }
        String shareText = getString(R.string.format_detail_share, strDate, mServerEvent.name,
                getEventUrl(mServerEvent.link));

        Intent sendIntent = new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, shareText)
                .setType("text/plain");
        // it prevents the activity we’re sharing to from being placed onto the activity stack.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }
        return sendIntent;
    }

    // Call to update the share intent
    private void setShareIntent(@Nullable Intent shareIntent) {
        if (mShareActionProvider != null && shareIntent != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = new DetailDisciplineAdapter(Utility.getLocale(this));
        mRecyclerView = (RecyclerView) findViewById(R.id.detail_recyclerview);
        mRecyclerView.setAdapter(mAdapter);

        mAppbar = findViewById(R.id.appbar_layout);
        mLayoutParams = (CoordinatorLayout.LayoutParams) mAppbar.getLayoutParams();
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mToolbarCover = (ImageView) mCollapsingToolbar.findViewById(R.id.toolbar_cover);
        mBottomScrim = mCollapsingToolbar.findViewById(R.id.toolbar_scrim_bottom);
        mTopScrim = mCollapsingToolbar.findViewById(R.id.toolbar_scrim_top);
        // Increase the height of the appbar.
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        int height = Math.min(displaymetrics.widthPixels, displaymetrics.heightPixels);
        // Height returned larger than actual.
        height = (int) (height * 0.9);
        mLayoutParams.height = height;
        mLayoutParams.width = displaymetrics.widthPixels;
    }

    @Override
    protected void setLoaderId() {
        LOADER_ID = EVENTS_LOADER;
    }

    // Don't recreate parent activity - use old instance.
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        finish();
        startActivity(intent);
    }

    @Override
    protected void updateDataAndSetIndexingAttributes(@NonNull Map data) {
        Intent intent = getIntent();
        ServerEvent[] events = (ServerEvent[]) data.get(ServerContract.EventsEntry.EVENTS);
        /* adb shell am start -a android.intent.action.VIEW \
            -d "http://sportevent.com.ua/events/bukovel-sprint/" ua.com.sportevent.sportevent */

        // Verify that the Intent is a deep link intent.
        String intentData = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intentData != null) {
            // Find index of the event.
            mServerEvent = findEventByLink(intentData, events);
            if (mServerEvent == null) {
                // xz Unknown event id. Try to reload events.
                Loader<Object> loader = getLoaderManager().getLoader(EVENTS_LOADER);
                if (loader != null) loader.onContentChanged();

                Intent launchIntent = new Intent(this, EventsActivity.class);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
                mServerEvent = events[0];
                finish(); // finish this activity (but all current methods continue their execution)
            }
        } else {
            // Show last chosen event.
            String eventId = PreferenceHelper.getDetaileEventId(this);
            mServerEvent = findEventById(eventId, events);
        }
        // Update share intent as we change the event
        setShareIntent(createShareEventIntent());

        mAdapter.updateData(mServerEvent);
        mTitle = mServerEvent.name;
        mPath = mServerEvent.link;
    }

    /**
     * Looking for the event with id equals eventId in the array of events.
     * @return event or null if the event not found.
     */
    private static ServerEvent findEventById(String eventId, @NonNull ServerEvent[] events) {
        for (ServerEvent event : events) {
            if (event.id.equals(eventId)) return event;
        }
        return null;
    }

    /**
     * Looking for the event with link equals targetEventLink in the array of events.
     * @return event or null if the event not found.
     */
    private static ServerEvent findEventByLink(@NonNull String targetEventLink, @NonNull ServerEvent[] events) {
        // Trim last slash
        targetEventLink = findLastUrlSegment(trimSlashes(targetEventLink));

        for (ServerEvent event : events) {
            // Trim first and last slashes
            event.link = trimSlashes(event.link);
            String eventLink = findLastUrlSegment(event.link);
            if (eventLink.equals(targetEventLink)) {
                return event;
            }
        }
        return null;
    }

    @NonNull
    private static String findLastUrlSegment(@NonNull String eventId) {
        eventId = eventId.substring(eventId.lastIndexOf("/") + 1);
        return eventId;
    }

    private static String trimSlashes(@NonNull String string) {
        if (string.length() > 0 && string.charAt(0) == '/') {
            string = string.substring(1);
        }
        if (string.length() > 0 && string.charAt(string.length() - 1) == '/') {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_detail);
    }

    @Override
    protected boolean dataNotAvailable() {
        return mAdapter.getItemCount() == 0;
    }

    @Override
    public Loader<Map> onCreateLoader(int id, Bundle args) {
        return new EventsLoader(this, id);
    }

    @Override
    public void onLoadFinished(Loader<Map> loader, Map data) {
        super.onLoadFinished(loader, data);
        // Set title to the app bar.
        if (mServerEvent != null) {
            mCollapsingToolbar.setTitle(mServerEvent.name);

            // Load image into the appbar.
            if (mLayoutParams.width > 0 && mLayoutParams.height > 0 && mServerEvent.img_title != null) {
                Glide.with(this)
                        .load(ServerContract.getAbsoluteImgUrl(mServerEvent.img_title, mLayoutParams.width, mLayoutParams.height))
                        .placeholder(R.drawable.logo)
                        .centerCrop()
                        .dontAnimate()
                        .listener(this)
                        .into(mToolbarCover);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Map> loader) {
        mAdapter.updateData(null);
    }

    public void scrollDown(View view) {
        int columns = getResources().getInteger(R.integer.events_columns);
        CoordinatorLayout.Behavior behavior = mLayoutParams.getBehavior();
        if(behavior != null) {
            behavior.onNestedFling((CoordinatorLayout) findViewById(R.id.detail_coordinator), mAppbar, mRecyclerView, 0, mAppbar.getHeight(), true);
        }
//        mRecyclerView.smoothScrollBy(0, mRecyclerView.getChildAt(0).getHeight());
        switch (columns) {
            case 1:
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                break;
            case 2:
            default:
                mRecyclerView.smoothScrollToPosition(1);
        }
    }

    public void buyPackage(View view) {
        // Identify what package to buy
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.DESTINATION_KEY, LoginActivity.DESTINATION_WEBVIEW);
        intent.putExtra(EXTRA_BUYING, (Parcelable) view.getTag(R.id.buying_information));
        startActivity(intent);
    }

    public void addCalendarEvent(MenuItem item) {
        if (mServerEvent != null) {
            Date date = Utility.parseServerDate(mServerEvent.fields.event_time.value);
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date != null ? date.getTime() : 0)
                    .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                    .putExtra(CalendarContract.Events.TITLE, mServerEvent.name)
                    .putExtra(CalendarContract.Events.DESCRIPTION, mServerEvent.about)
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, mServerEvent.fields.city.value)
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Snackbar.make(mRecyclerView, R.string.detail_no_calendar, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
        return false;
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
        mCollapsingToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite));
        mBottomScrim.setVisibility(View.VISIBLE);
        mTopScrim.setVisibility(View.VISIBLE);
        return false;
    }
}
