package com.ch_linghu.fanfoudroid.ui.module;

import android.content.Context;
import android.view.MotionEvent;

import com.ch_linghu.fanfoudroid.BrowseActivity;
import com.ch_linghu.fanfoudroid.MentionActivity;
import com.ch_linghu.fanfoudroid.TwitterActivity;

public class MyActivityFlipper extends ActivityFlipper implements
        Widget.OnGestureListener {

    public MyActivityFlipper() {
        super();
    }

    public MyActivityFlipper(Context context) {
        super(context);
    }

    // factory
    public static MyActivityFlipper create(Context context) {
        MyActivityFlipper mFlipper = new MyActivityFlipper(context);
        mFlipper.addActivity(BrowseActivity.class);
        mFlipper.addActivity(TwitterActivity.class);
        mFlipper.addActivity(MentionActivity.class);
        return mFlipper;
    }

    @Override
    public boolean onFlingDown(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        return false; // do nothing
    }

    @Override
    public boolean onFlingUp(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        return false; // do nothing
    }

    @Override
    public boolean onFlingLeft(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        autoShowPrevious();
        return true;
    }

    @Override
    public boolean onFlingRight(MotionEvent e1, MotionEvent e2,
            float velocityX, float velocityY) {
        autoShowNext();
        return true;
    }

}
