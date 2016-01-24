package ua.com.sportevent.sportevent;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ua.com.sportevent.sportevent.helpers.Utility;

/**
 * Handle some common functionality:
 * - setContentView;
 * - instantiate empty view (need R.id.empty_card defined in the layout!);
 * - implements empty view updating.
 */
public abstract class ErrorActivity extends BaseMenuActivity {
    private View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        mEmptyView = findViewById(R.id.empty_card);
    }

    /**
     * This function is called in the onCreate. Child class need to setContentView here.
     */
    protected abstract void setContentView();

    /**
     * Child provide personal way to check if the data to show in the view in unavailable.
     * @return true if not available.
     */
    protected abstract boolean dataNotAvailable();

    /**
     Updates the empty view with contextually relevant information that the user can
     use to determine why they aren't seeing events. Hide emptyView is data is available.
     @param startLoad If this is true and dataNotAvailable - empty view shows the progress bar
                      with text about data loading.
     */
    protected void updateEmptyView(boolean startLoad) {
        if (mEmptyView != null) {
            // If we haven't event data.
            if (dataNotAvailable()) {
                mEmptyView.setVisibility(View.VISIBLE);
                TextView emptyTextView = (TextView) mEmptyView.findViewById(R.id.empty_text);
                ProgressBar loadBar = (ProgressBar) mEmptyView.findViewById(R.id.empty_load);
                ImageView emptyImageView = (ImageView) mEmptyView.findViewById(R.id.empty_image);
                int message;
                if (startLoad) {
                    emptyImageView.setVisibility(View.GONE);
                    loadBar.setVisibility(View.VISIBLE);
                    message = R.string.empty_events_download;
                } else {
                    emptyImageView.setVisibility(View.VISIBLE);
                    loadBar.setVisibility(View.GONE);
                    message = Utility.getErrorMessage(this);
                    emptyImageView.setImageResource(Utility.getErrorImage(this));
                }
                emptyTextView.setText(message);
            } else {
                mEmptyView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
