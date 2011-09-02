package com.ch_linghu.fanfoudroid.ui.module;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ch_linghu.fanfoudroid.R;

public class SimpleFeedback implements Feedback, Widget {
	private static final String TAG = "SimpleFeedback";

	public static final int MAX = 100;

	private ProgressBar mProgress = null;
	//private ProgressBar mLoadingProgress = null;

	public SimpleFeedback(Context context) {
		mProgress = (ProgressBar) ((Activity) context)
				.findViewById(R.id.progress_bar);
		//mLoadingProgress = (ProgressBar) ((Activity) context)
		//		.findViewById(R.id.top_refresh_progressBar);
	}

	@Override
	public void start(CharSequence text) {
		mProgress.setProgress(20);
		//mLoadingProgress.setVisibility(View.VISIBLE);
	}

	@Override
	public void success(CharSequence text) {
		mProgress.setProgress(100);
		//mLoadingProgress.setVisibility(View.GONE);
		resetProgressBar();
	}

	@Override
	public void failed(CharSequence text) {
		resetProgressBar();
		showMessage(text);
	}

	@Override
	public void cancel(CharSequence text) {

	}

	@Override
	public void update(Object arg0) {
		if (arg0 instanceof Integer) {
			mProgress.setProgress((Integer) arg0);
		} else if (arg0 instanceof CharSequence) {
			showMessage((String) arg0);
		}
	}

	@Override
	public void setIndeterminate(boolean indeterminate) {
		mProgress.setIndeterminate(indeterminate);
	}

	@Override
	public Context getContext() {
		if (mProgress != null) {
			return mProgress.getContext();
		}
		//if (mLoadingProgress != null) {
		//	return mLoadingProgress.getContext();
		//}
		return null;
	}

	@Override
	public boolean isAvailable() {
		if (null == mProgress) {
			Log.e(TAG, "R.id.progress_bar is missing");
			return false;
		}
		//if (null == mLoadingProgress) {
		//	Log.e(TAG, "R.id.top_refresh_progressBar is missing");
		//	return false;
		//}
		return true;
	}

	/**
	 * @param total
	 *            0~100
	 * @param maxSize
	 *            max size of list
	 * @param list
	 * @return
	 */
	public static int calProgressBySize(int total, int maxSize, List<?> list) {
		if (null != list) {
			return (MAX - (int) Math.floor(list.size() * (total / maxSize)));
		}
		return MAX;
	}

	private void resetProgressBar() {
		if (mProgress.isIndeterminate()) {
			// TODO: 第二次不会出现
			mProgress.setIndeterminate(false);
		}
		mProgress.setProgress(0);
	}

	private void showMessage(CharSequence text) {
		Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
	}

}
