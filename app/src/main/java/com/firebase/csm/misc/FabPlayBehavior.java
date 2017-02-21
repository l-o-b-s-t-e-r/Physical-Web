package com.firebase.csm.misc;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Lobster on 08.02.17.
 */

public class FabPlayBehavior extends FloatingActionButton.Behavior {

    AnimationHelper mAnimationHelper;

    public FabPlayBehavior() {
        super();
        this.mAnimationHelper = new AnimationHelper();
    }

    public FabPlayBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mAnimationHelper = new AnimationHelper();
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (child.getVisibility() == View.VISIBLE && dyConsumed > 0) {
            mAnimationHelper.hide(child);
        } else if (child.getVisibility() == View.INVISIBLE && dyConsumed < 0) {
            child.show();
        }
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

}