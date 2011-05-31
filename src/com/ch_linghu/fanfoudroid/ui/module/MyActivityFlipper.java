package com.ch_linghu.fanfoudroid.ui.module;

import android.app.Activity;
import android.view.MotionEvent;

import com.ch_linghu.fanfoudroid.BrowseActivity;
import com.ch_linghu.fanfoudroid.MentionActivity;
import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterActivity;

/**
 * MyActivityFlipper 利用左右滑动手势切换Activity
 * 
 * 1. 切换Activity, 继承与 {@link ActivityFlipper} 2. 手势识别, 实现接口
 * {@link Widget.OnGestureListener}
 * 
 */
public class MyActivityFlipper extends ActivityFlipper implements
        Widget.OnGestureListener {

    public MyActivityFlipper() {
        super();
    }

    public MyActivityFlipper(Activity activity) {
        super(activity);
        // TODO Auto-generated constructor stub
    }

    // factory
    public static MyActivityFlipper create(Activity activity) {
        MyActivityFlipper flipper = new MyActivityFlipper(activity);
        flipper.addActivity(BrowseActivity.class);
        flipper.addActivity(TwitterActivity.class);
        flipper.addActivity(MentionActivity.class);

        flipper.setToastResource(new int[] { R.drawable.point_left,
                R.drawable.point_center, R.drawable.point_right });

        flipper.setInAnimation(R.anim.push_left_in);
        flipper.setOutAnimation(R.anim.push_left_out);
        flipper.setPreviousInAnimation(R.anim.push_right_in);
        flipper.setPreviousOutAnimation(R.anim.push_right_out);
        return flipper;
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
        autoShowNext();
        return true;
    }

    @Override
    public boolean onFlingRight(MotionEvent e1, MotionEvent e2,
            float velocityX, float velocityY) {
        autoShowPrevious();
        return true;
    }

}
