package com.mit.utils;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;

/**
 * Created by LSY on 15-5-22.
 */
public class GuideUtils {

    private static final String TAG = "GuideUtils";

    public static void setAnimation(View v) {
        AnimationSet animationSet = new AnimationSet(true);
        RotateAnimation rotateAnimation = new RotateAnimation(-10.0f, 10.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        Animation scaleAnimation = new ScaleAnimation(0.5f, 1.2f, 0.5f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(200);
        rotateAnimation.setRepeatCount(1);
        rotateAnimation.setInterpolator(new AccelerateInterpolator());
        animationSet.addAnimation(rotateAnimation);
//        animationSet.addAnimation(scaleAnimation);
        v.startAnimation(animationSet);
    }

}
