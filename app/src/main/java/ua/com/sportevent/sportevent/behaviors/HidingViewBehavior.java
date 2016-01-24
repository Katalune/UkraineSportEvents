package ua.com.sportevent.sportevent.behaviors;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created to hide base event info in a detail view as appbar scrolls up.
 */
public class HidingViewBehavior extends CoordinatorLayout.Behavior<View>  {

    public static final int ANIM_DURATION = 200;
    static final LinearOutSlowInInterpolator sInInterpolator = new LinearOutSlowInInterpolator();
    static final FastOutLinearInInterpolator sOutInterpolator = new FastOutLinearInInterpolator();

    public HidingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return (dependency instanceof AppBarLayout);
    }


    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, final View child, View dependency) {
        if (dependency instanceof AppBarLayout) {
            if (dependency.getY() < -1) {
                if (child.getScaleX() == 1) {
                    ViewCompat.animate(child).setDuration(ANIM_DURATION).scaleX(0).scaleY(0).alpha(0f)
                            .setInterpolator(sOutInterpolator)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    child.setVisibility(View.GONE);
                                }
                            });
                }
            } else {
                child.setVisibility(View.VISIBLE);
                child.animate().setDuration(ANIM_DURATION).scaleX(1).scaleY(1).alpha(1f)
                        .setInterpolator(sInInterpolator);
            }
        }
        return true;
    }
}
