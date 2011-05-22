package com.ch_linghu.fanfoudroid.ui.module;

import android.content.Context;

public interface Widget {
    Context getContext();
    
    
    // TEMP
    public static interface OnGestureListener {
        void onSwipeDown();
        void onSwipeUp();
        void onSwipeLeft();
        void onSwipeRight();
    }
    
    public static interface OnRefreshListener {
        void onRefresh(); 
    }
}
