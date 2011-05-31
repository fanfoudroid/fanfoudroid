package com.ch_linghu.fanfoudroid.ui.module;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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
 *  
 *  // set toast
 *  mFlipper.setToastResource(new int[] {
 *       R.drawable.point_left,
 *       R.drawable.point_center,
 *       R.drawable.point_right
 *  });
 *
 *  // set Animation
 *  mFlipper.setInAnimation(R.anim.push_left_in);
 *  mFlipper.setOutAnimation(R.anim.push_left_out);
 *  mFlipper.setPreviousInAnimation(R.anim.push_right_in);
 *  mFlipper.setPreviousOutAnimation(R.anim.push_right_out);
 * </code>
 * 
 */
public class ActivityFlipper implements IFlipper {
    private static final String TAG = "ActivityFlipper";

    private static final int SHOW_NEXT = 0;
    private static final int SHOW_PROVIOUS = 1;
    private int mDirection = SHOW_NEXT;

    private boolean mToastEnabled = false;
    private int[] mToastResourcesMap = new int[]{};
    
    private boolean mAnimationEnabled = false;
    private int mNextInAnimation = -1;
    private int mNextOutAnimation = -1;
    private int mPreviousInAnimation = -1;
    private int mPreviousOutAnimation = -1;

    private Activity mActivity;
    private List<Class<?>> mActivities = new ArrayList<Class<?>>();;
    private int mWhichActivity = 0; 

    public ActivityFlipper() {
    }

    public ActivityFlipper(Activity activity) {
        mActivity = activity;
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
        intent.setClass(mActivity, cls);
        mActivity.startActivity(intent);

    }

    public void setToastResource(int[] resourceIds) {
        mToastEnabled = true;
        mToastResourcesMap = resourceIds;
    }

    private void maybeShowToast(int whichActicity) {
        if (mToastEnabled && whichActicity < mToastResourcesMap.length) {
            final Toast myToast = new Toast(mActivity);
            final ImageView myView = new ImageView(mActivity);
            myView.setImageResource(mToastResourcesMap[whichActicity]);
            myToast.setView(myView);
            myToast.setDuration(Toast.LENGTH_SHORT);
            myToast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
            myToast.show();
        }
    }

    private void maybeShowAnimation(int whichActivity) {
        if (mAnimationEnabled) {
            boolean showPrevious = (mDirection == SHOW_PROVIOUS);
            if (showPrevious && mPreviousInAnimation != -1
                    && mPreviousOutAnimation != -1) {
                mActivity.overridePendingTransition(
                        mPreviousInAnimation, mPreviousOutAnimation);
                return; // use Previous Animation
            }
            
            if (mNextInAnimation != -1 && mNextOutAnimation != -1) {
                mActivity.overridePendingTransition(
                        mNextInAnimation, mNextOutAnimation);
            }
        }
    }

    /**
     * Launch Activity by index
     * 
     * @param whichActivity
     *            the index of Activity
     */
    private void launchActivity(int whichActivity) {
        launchActivity(mActivities.get(whichActivity));
        maybeShowToast(whichActivity);
        maybeShowAnimation(whichActivity);
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

    @SuppressWarnings("unused")
    private Class<?> getActivityAt(int index) {
        if (index > 0 && index < mActivities.size()) {
            return mActivities.get(index);
        }
        return null;
    }

    /**
     * Show next activity(already setCurrentActivity)
     */
    @Override
    public void showNext() {
        mDirection = SHOW_NEXT;
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
        showNextOf(mActivity.getClass());
    }

    /**
     * Show previous activity(already setCurrentActivity)
     */
    @Override
    public void showPrevious() {
        mDirection = SHOW_PROVIOUS;
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
        showPreviousOf(mActivity.getClass());
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
    
    public void setInAnimation(int resourceId) {
        setEnableAnimation(true);
        mNextInAnimation = resourceId;
    }

    public void setOutAnimation(int resourceId) {
        setEnableAnimation(true);
        mNextOutAnimation = resourceId;
    }

    public void setPreviousInAnimation(int resourceId) {
        mPreviousInAnimation = resourceId;
    }

    public void setPreviousOutAnimation(int resourceId) {
        mPreviousOutAnimation = resourceId;
    }
    
    public void setEnableAnimation(boolean enable) {
        mAnimationEnabled = enable;
    }

    /**
     * Count activities
     * 
     * @return the number of activities
     */
    public int getCount() {
        return mActivities.size();
    }
}
