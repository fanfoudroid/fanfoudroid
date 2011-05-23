package com.ch_linghu.fanfoudroid.ui.module;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.ch_linghu.fanfoudroid.R;

/**
 * ActivityFlipper, 和 {@link ViewFactory} 类似, 只是设计用于切换activity.
 * 
 * 切换的前后顺序取决与注册时的先后顺序
 * 
 * USAGE: <code>
 *  ActivityFlipper mFlipper = new ActivityFlipper(this);
 *  mFlipper.addActivity(TwitterActivity.class);
 *  mFlipper.addActivity(MentionActivity.class);
 *  mFlipper.addActivity(DmActivity.class);
 *       
 *  // switch activity 
 *  mFlipper.setCurrentActivity(TwitterActivity.class);
 *  mFlipper.showNext();
 *  mFlipper.showPrevious();
 *       
 *  // or without set current activity
 *  mFlipper.showNextOf(TwitterActivity.class);
 *  mFlipper.showPreviousOf(TwitterActivity.class);
 * 
 *  // or auto mode, use the context as current activity
 *  mFlipper.autoShowNext();
 *  mFlipper.autoShowPrevious();
 * </code>
 * 
 */
public class ActivityFlipper implements Widget, IFlipper {
    private static final String TAG = "ActivityFlipper";

    private Context mContext;
    private List<Class<?>> mActivities = new ArrayList<Class<?>>();;
    private int mWhichActivity = 0;

    public ActivityFlipper() {
    }

    public ActivityFlipper(Context context) {
        mContext = context;
    }

    /**
     * Launch Activity
     * 
     * @param cls
     *            class of activity
     */
    public void launchActivity(Class<?> cls) {
        Log.v(TAG, "launch activity :" + cls.getName());
        Intent intent = new Intent();
        intent.setClass(getContext(), cls);
        getContext().startActivity(intent);

    }

    private static final int[] mResourceMap = new int[] {
            R.drawable.point_left, R.drawable.point_center,
            R.drawable.point_right };

    private void showToast(int whichActicity) {
        final Toast myToast = new Toast(getContext());
        final ImageView myView = new ImageView(getContext());
        myView.setImageResource(mResourceMap[whichActicity]);
        myToast.setView(myView);
        myToast.setDuration(Toast.LENGTH_SHORT);
        myToast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
        myToast.show();
    }

    /**
     * Launch Activity by index
     * 
     * @param whichActivity
     *            the index of Activity
     */
    private void launchActivity(int whichActivity) {
        launchActivity(mActivities.get(whichActivity));
        showToast(whichActivity);
    }

    /**
     * Add Activity NOTE: 添加的顺序很重要
     * 
     * @param cls
     *            class of activity
     */
    public void addActivity(Class<?> cls) {
        mActivities.add(cls);
    }

    /**
     * Get index of the Activity
     * 
     * @param cls
     *            class of activity
     * @return
     */
    private int getIndexOf(Class<?> cls) {
        int index = mActivities.indexOf(cls);
        if (-1 == index) {
            Log.e(TAG, "No such activity: " + cls.getName());
        }
        return index;
    }

    /**
     * Show next activity(already setCurrentActivity)
     */
    @Override
    public void showNext() {
        setDisplayedActivity(mWhichActivity + 1, true);
    }

    /**
     * Show next activity of
     * 
     * @param cls
     *            class of activity
     */
    public void showNextOf(Class<?> cls) {
        setCurrentActivity(cls);
        showNext();
    }

    /**
     * Show next activity(use current context as a activity)
     */
    public void autoShowNext() {
        showNextOf(getContext().getClass());
    }

    /**
     * Show previous activity(already setCurrentActivity)
     */
    @Override
    public void showPrevious() {
        setDisplayedActivity(mWhichActivity - 1, true);
    }

    /**
     * Show previous activity of
     * 
     * @param cls
     *            class of activity
     */
    public void showPreviousOf(Class<?> cls) {
        setCurrentActivity(cls);
        showPrevious();
    }

    /**
     * Show previous activity(use current context as a activity)
     */
    public void autoShowPrevious() {
        showPreviousOf(getContext().getClass());
    }

    /**
     * Sets which child view will be displayed
     * 
     * @param whichActivity
     *            the index of the child view to display
     * @param display
     *            display immediately
     */
    public void setDisplayedActivity(int whichActivity, boolean display) {
        mWhichActivity = whichActivity;
        if (whichActivity >= getCount()) {
            mWhichActivity = 0;
        } else if (whichActivity < 0) {
            mWhichActivity = getCount() - 1;
        }

        if (display) {
            launchActivity(mWhichActivity);
        }
    }

    /**
     * Set current Activity
     * 
     * @param cls
     *            class of activity
     */
    public void setCurrentActivity(Class<?> cls) {
        setDisplayedActivity(getIndexOf(cls), false);
    }

    /**
     * Count activities
     * 
     * @return the number of activities
     */
    public int getCount() {
        return mActivities.size();
    }

    @Override
    public Context getContext() {
        return mContext;
    }

}
