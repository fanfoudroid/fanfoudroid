package com.temp.afan.widget;

import android.content.Context;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public interface Widget {
    Context getContext();

    // TEMP
    public static interface OnGestureListener {
        /**
         * @param e1
         *            The first down motion event that started the fling.
         * @param e2
         *            The move motion event that triggered the current onFling.
         * @param velocityX
         *            The velocity of this fling measured in pixels per second
         *            along the x axis.
         * @param velocityY
         *            The velocity of this fling measured in pixels per second
         *            along the y axis.
         * @return true if the event is consumed, else false
         * 
         * @see SimpleOnGestureListener#onFling
         */
        boolean onFlingDown(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY);

        boolean onFlingUp(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY);

        boolean onFlingLeft(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY);

        boolean onFlingRight(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY);
    }

    public static interface OnRefreshListener {
        void onRefresh();
    }
}
