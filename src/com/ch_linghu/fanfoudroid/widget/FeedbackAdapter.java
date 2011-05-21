package com.ch_linghu.fanfoudroid.widget;

import android.content.Context;

public class FeedbackAdapter implements Feedback {
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