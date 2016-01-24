package ua.com.sportevent.sportevent.behaviors;

import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

import ua.com.sportevent.sportevent.R;

/**
 * Fab respond to the app-bar movement.
 * If dependency is AppBar - there is assumption that fab is a child of a CoordinatorLayout. And
 * AppBar contains a child with id R.id.toolbar.
 */
public class ScrollingFABBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
    private final int statusbarShift;
    private float toolbarHeight;
    private int distanceToScroll;

    public ScrollingFABBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.toolbarHeight = context.getResources().getDimension(R.dimen.toolbar_height);
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0 && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            statusbarShift = res.getDimensionPixelSize(resourceId);
        } else {
            statusbarShift = 0;
        }
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
        return (dependency instanceof AppBarLayout)
                || (dependency instanceof Snackbar.SnackbarLayout);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {

        if (dependency instanceof Snackbar.SnackbarLayout) {
            float fabTranslationY = fab.getTranslationY();
            if (fabTranslationY < fab.getHeight()) {
                float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
                fab.setTranslationY(translationY);
            }
        } else if (dependency instanceof AppBarLayout) {
            if (distanceToScroll == 0) {
                CoordinatorLayout.LayoutParams fabLayoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                distanceToScroll = fab.getHeight() + fabLayoutParams.bottomMargin;
            }
            float visiblePart = dependency.getHeight() + dependency.getY() - statusbarShift;
            float Y = Math.min(visiblePart - toolbarHeight, 0);
            float ratio = Y / toolbarHeight;
            fab.setTranslationY(-distanceToScroll * ratio);
        }
        return true;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        super.onDependentViewRemoved(parent, child, dependency);

        float fabTranslationY = child.getTranslationY();
        if (fabTranslationY < child.getHeight()) {
            child.setTranslationY(0);
        }
    }
}
