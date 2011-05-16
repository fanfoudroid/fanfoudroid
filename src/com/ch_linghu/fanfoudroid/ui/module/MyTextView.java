package com.ch_linghu.fanfoudroid.ui.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.helper.Preferences;

public class MyTextView extends TextView {
    private static float mFontSize = 16;
    private static boolean mFontSizeChanged = true;

    public MyTextView(Context context) {
        super(context, null);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLinksClickable(false);

        Resources res = getResources();
        int color = res.getColor(R.color.link_color);
        setLinkTextColor(color);
        
        initFontSize();
    }
    
    public void initFontSize() {
        if ( mFontSizeChanged ) {
            mFontSize = getFontSizeFromPreferences(mFontSize);
            setFontSizeChanged(false); // reset
        }
        setTextSize(mFontSize);
    }
    
    private float getFontSizeFromPreferences(float defaultValue) {
        SharedPreferences preferences = TwitterApplication.mPref;
        if (preferences.contains(Preferences.UI_FONT_SIZE)) {
            Log.v("DEBUG", preferences.getString(Preferences.UI_FONT_SIZE, "null") + " CHANGE FONT SIZE");
            return Float.parseFloat(preferences.getString(
                    Preferences.UI_FONT_SIZE, "14"));
        }
        return defaultValue;
    }

    private URLSpan mCurrentLink;
    private ForegroundColorSpan mLinkFocusStyle = new ForegroundColorSpan(
            Color.RED);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        CharSequence text = getText();
        int action = event.getAction();

        if (!(text instanceof Spannable)) {
            return super.onTouchEvent(event);
        }

        Spannable buffer = (Spannable) text;

        if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_MOVE) {
            TextView widget = this;

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    if (mCurrentLink == link[0]) {
                        link[0].onClick(widget);
                    }
                    mCurrentLink = null;
                    buffer.removeSpan(mLinkFocusStyle);
                } else if (action == MotionEvent.ACTION_DOWN) {
                    mCurrentLink = link[0];
                    buffer.setSpan(mLinkFocusStyle,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                return true;
            }
        }

        mCurrentLink = null;
        buffer.removeSpan(mLinkFocusStyle);

        return super.onTouchEvent(event);
    }
    
    public static void setFontSizeChanged(boolean isChanged) {
        mFontSizeChanged = isChanged;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }
}
