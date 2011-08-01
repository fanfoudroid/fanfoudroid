package com.ch_linghu.fanfoudroid.ui.module;

import com.ch_linghu.fanfoudroid.R;

import android.app.Activity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * FlingGestureLIstener, 封装 {@link SimpleOnGestureListener} .
 * 主要用于识别类似向上下或向左右滑动等基本手势.
 * 
 * 该类主要解决了与ListView自带的上下滑动冲突问题. 解决方法为将listView的onTouchListener进行覆盖:<code>
 * FlingGestureListener gListener = new FlingGestureListener(this,
 *                   MyActivityFlipper.create(this));
 * myListView.setOnTouchListener(gListener);
 * </code>
 * 
 * 该类一般和实现了 {@link Widget.OnGestureListener} 接口的类共同协作. 在识别到手势后会自动调用其相关的回调方法,
 * 以实现手势触发事件效果.
 * 
 * @see Widget.OnGestureListener
 * 
 */
public class FlingGestureListener extends SimpleOnGestureListener implements
		OnTouchListener {
	private static final String TAG = "FlipperGestureListener";

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_DISTANCE = 400;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	private Widget.OnGestureListener mListener;
	private GestureDetector gDetector;
	private Activity activity;

	public FlingGestureListener(Activity activity,
			Widget.OnGestureListener listener) {
		this(activity, listener, null);
	}

	public FlingGestureListener(Activity activity,
			Widget.OnGestureListener listener, GestureDetector gDetector) {
		if (gDetector == null) {
			gDetector = new GestureDetector(activity, this);
		}
		this.gDetector = gDetector;
		mListener = listener;
		this.activity = activity;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.d(TAG, "On fling");
		boolean result = super.onFling(e1, e2, velocityX, velocityY);

		float xDistance = Math.abs(e1.getX() - e2.getX());
		float yDistance = Math.abs(e1.getY() - e2.getY());
		velocityX = Math.abs(velocityX);
		velocityY = Math.abs(velocityY);

		try {
			if (xDistance > SWIPE_MAX_DISTANCE
					|| yDistance > SWIPE_MAX_DISTANCE) {
				Log.d(TAG, "OFF_PATH");
				return result;
			}

			if (velocityX > SWIPE_THRESHOLD_VELOCITY
					&& xDistance > SWIPE_MIN_DISTANCE) {
				if (e1.getX() > e2.getX()) {
					Log.d(TAG, "<------");
					result = mListener
							.onFlingLeft(e1, e1, velocityX, velocityY);
					activity.overridePendingTransition(R.anim.push_left_in,
							R.anim.push_left_out);
				} else {
					Log.d(TAG, "------>");
					result = mListener.onFlingRight(e1, e1, velocityX,
							velocityY);
					activity.overridePendingTransition(R.anim.push_right_in,
							R.anim.push_right_out);
				}
			} else if (velocityY > SWIPE_THRESHOLD_VELOCITY
					&& yDistance > SWIPE_MIN_DISTANCE) {
				if (e1.getY() > e2.getY()) {
					Log.d(TAG, "up");
					result = mListener.onFlingUp(e1, e1, velocityX, velocityY);
				} else {
					Log.d(TAG, "down");
					result = mListener
							.onFlingDown(e1, e1, velocityX, velocityY);
				}
			} else {
				Log.d(TAG, "not hint");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "onFling error " + e.getMessage());
		}

		return result;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		super.onLongPress(e);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, "On Touch");

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