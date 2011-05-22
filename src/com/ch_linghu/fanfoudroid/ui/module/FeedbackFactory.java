package com.ch_linghu.fanfoudroid.ui.module;

import android.content.Context;
import android.util.Log;

public class FeedbackFactory {
    private static final String TAG = "FeedbackFactory";
    
    public static enum FeedbackType {
        DIALOG, PROGRESS, REFRESH
    };
    
    public static Feedback create(Context context, FeedbackType type) {
        Feedback feedback = null;
        switch (type) {
        case PROGRESS:
            feedback = new SimpleFeedback(context);
            break;
        }
        
        if (null == feedback || !feedback.isAvailable()) {
            feedback = new FeedbackAdapter(context);
            Log.e(TAG, type + " feedback is not available.");
        }
        return feedback;
    }
    
    public static class FeedbackAdapter implements Feedback {
        public FeedbackAdapter(Context context) {}
        @Override
        public void start(CharSequence text) {}
        @Override
        public void cancel(CharSequence text) {}
        @Override
        public void success(CharSequence text) {}
        @Override
        public void failed(CharSequence text) {}
        @Override
        public void update(Object arg0) {}
        @Override
        public boolean isAvailable() { return true; }
        @Override
        public void setIndeterminate(boolean indeterminate) {}
    }
}
