package com.ch_linghu.android.fanfoudroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class MyTextView extends TextView {

  public MyTextView(Context context) {
    super(context);

    setLinksClickable(false);
    
    Resources res = getResources();
    int color = res.getColor(R.color.link_color);
    setLinkTextColor(color);
  }

  public MyTextView(Context context, AttributeSet attrs) {
    super(context, attrs);

    setLinksClickable(false);
    
    Resources res = getResources();
    int color = res.getColor(R.color.link_color);
    setLinkTextColor(color);
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

    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN
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
          buffer.setSpan(mLinkFocusStyle, buffer.getSpanStart(link[0]), buffer
              .getSpanEnd(link[0]), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return true;
      }
    }

    mCurrentLink = null;
    buffer.removeSpan(mLinkFocusStyle);

    return super.onTouchEvent(event);
  }

}
