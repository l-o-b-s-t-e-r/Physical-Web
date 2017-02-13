package com.firebase.csm.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Application;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.firebase.csm.R;

/**
 * Created by Lobster on 13.02.17.
 */

public class AnimationHelper {

    private final int SHOW_HIDE_ANIM_DURATION = 200;

    private Animation animationPlay, animationPause;
    private boolean isAnimate;

    public AnimationHelper(Application application) {
        animationPlay = AnimationUtils.loadAnimation(application, R.anim.fab_play);
        animationPause = AnimationUtils.loadAnimation(application, R.anim.fab_pause);
    }

    public void onClickStartAnimation(View view, Animation.AnimationListener listener) {
        animationPlay.setAnimationListener(listener);
        view.startAnimation(animationPlay);
    }

    public void onClickEndAnimation(View view) {
        view.startAnimation(animationPause);
    }

    public void hide(View view) {
        if (isAnimate) return;

        view.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(SHOW_HIDE_ANIM_DURATION)
                .setInterpolator(new FastOutLinearInInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isAnimate = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Log.e("anim", "end");
                        isAnimate = false;
                        view.setVisibility(View.INVISIBLE);
                    }
                });
    }
}
