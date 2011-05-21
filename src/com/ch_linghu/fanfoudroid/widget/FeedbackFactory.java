package com.ch_linghu.fanfoudroid.widget;

import android.content.Context;
import android.util.Log;

public class FeedbackFactory {
    private static final String TAG = "FeedbackFactory";
    
    public static final int DIALOG_MODE = 0x01;
    public static final int REFRESH_MODE = 0x02;
    public static final int PROGRESS_MODE = 0x03;

    public static Feedback getFeedback(Context context, int type) {
        Feedback feedback = null;
        switch (type) {
        case PROGRESS_MODE:
            feedback = new SimpleFeedback(context);
            break;
        }
        
        if (null == feedback || !feedback.isAvailable()) {
            feedback = new FeedbackAdapter(context);
            Log.e(TAG, type + " feedback is not available.");
        }
        return feedback;
    }
}
