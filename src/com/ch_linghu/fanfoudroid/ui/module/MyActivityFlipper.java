package com.ch_linghu.fanfoudroid.ui.module;

import android.content.Context;

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
    public void onSwipeDown() {
        return; // do nothing;
    }

    @Override
    public void onSwipeUp() {
        return; // do nothing;
    }

    @Override
    public void onSwipeLeft() {
        autoShowPrevious();
    }

    @Override
    public void onSwipeRight() {
        autoShowNext();
    }

}
