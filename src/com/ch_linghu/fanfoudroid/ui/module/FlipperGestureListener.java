package com.ch_linghu.fanfoudroid.ui.module;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class FlipperGestureListener extends SimpleOnGestureListener implements
        OnTouchListener {
    private static final String TAG = "FlipperGestureListener";

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private Widget.OnGestureListener mListener;
    private GestureDetector gDetector;

    public FlipperGestureListener(Context context,
            Widget.OnGestureListener listener) {
        this(context, listener, null);
    }

    public FlipperGestureListener(Context context,
            Widget.OnGestureListener listener, GestureDetector gDetector) {
        if (gDetector == null) {
            gDetector = new GestureDetector(context, this);
        }
        this.gDetector = gDetector;
        mListener = listener;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        Log.d(TAG, "On fling");
        boolean orig = super.onFling(e1, e2, velocityX, velocityY);

        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                return orig;
            }
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d(TAG, "<------");
                mListener.onSwipeLeft();
                return true;
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d(TAG, "------>");
                mListener.onSwipeRight();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onFling error " + e.getMessage());
        }

        return orig;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
        super.onLongPress(e);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Log.d("FLING", "On Touch");

        // Within the MyGestureListener class you can now manage the
        // event.getAction() codes.

        // Note that we are now calling the gesture Detectors onTouchEvent.
        // And given we've set this class as the GestureDetectors listener
        // the onFling, onSingleTap etc methods will be executed.
        return gDetector.onTouchEvent(event);
    }

    public GestureDetector getDetector() {
        return gDetector;
    }
}