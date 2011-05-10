package com.ch_linghu.fanfoudroid.helper;

import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

public class TestMovementMethod extends LinkMovementMethod {
  
  private double mY;
  private boolean mIsMoving = false;
  
  @Override
  public boolean onTouchEvent(TextView widget, Spannable buffer,
      MotionEvent event) {
    /*
    int action = event.getAction();    
    
    if (action == MotionEvent.ACTION_MOVE) {
      double deltaY = mY - event.getY();
      mY = event.getY();

      Log.d("foo", deltaY + "");
      
      if (Math.abs(deltaY) > 1) {
        mIsMoving = true;
      }      
    } else if (action == MotionEvent.ACTION_DOWN) {
      mIsMoving = false;
      mY = event.getY();
    } else if (action == MotionEvent.ACTION_UP) {      
      boolean wasMoving = mIsMoving;
      mIsMoving = false;

      if (wasMoving) {
        return true;
      }
    }
    */
    
    return super.onTouchEvent(widget, buffer, event);
  }  

  public static MovementMethod getInstance() {
    if (sInstance == null)
      sInstance = new TestMovementMethod();

    return sInstance;
  }

  private static TestMovementMethod sInstance;
  
}