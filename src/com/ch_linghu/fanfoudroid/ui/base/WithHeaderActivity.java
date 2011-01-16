package com.ch_linghu.fanfoudroid.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterActivity;
import com.ch_linghu.fanfoudroid.WriteActivity;
import com.ch_linghu.fanfoudroid.task.Retrievable;
import com.ch_linghu.fanfoudroid.ui.module.MenuDialog;

public class WithHeaderActivity extends BaseActivity {
	
	private static final String TAG = "WithHeaderActivity";
	
	public static final int HEADER_STYLE_HOME  = 1;
	public static final int HEADER_STYLE_WRITE = 2;
	public static final int HEADER_STYLE_BACK  = 3;

	protected ImageButton refreshButton;
	protected ImageButton searchButton;
	protected ImageButton writeButton;
	protected TextView titleButton;
	protected Button backButton;
	protected ImageButton homeButton;
	protected MenuDialog dialog;
	
	// LOGO按钮
	protected void addTitleButton() {
		
		// Find View
		titleButton = (TextView) findViewById(R.id.title); 
		
		titleButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				int top = titleButton.getTop();
				int height = titleButton.getHeight();
				int x = top + height;

				if (null == dialog) {
					Log.i(TAG, "Create menu dialog.");
					dialog = new MenuDialog(WithHeaderActivity.this);
					dialog.bindEvent(WithHeaderActivity.this);
					dialog.setPosition(-1, x);
				}
				
				// toggle dialog
				if (dialog.isShowing()) {
					Log.i("LDS", "hiding dialog");
					dialog.dismiss(); //没机会触发
				} else {
					Log.i("LDS", "showing dialog");
					dialog.show();
				}
			}
		});
	}
	
	protected void setHeaderTitle(String title) {
		titleButton.setBackgroundDrawable( new BitmapDrawable());
		titleButton.setText(title);
		LayoutParams lp = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.setMargins(3, 12, 0, 0);
		titleButton.setLayoutParams(lp);
		// 中文粗体
		TextPaint tp = titleButton.getPaint(); 
	    tp.setFakeBoldText(true);
	}
	
	protected void setHeaderTitle(int resource) {
		titleButton.setBackgroundResource(resource);
	}
	
	// 刷新
	protected void addRefreshButton() {
		final Activity that = this;
		refreshButton = (ImageButton) findViewById(R.id.top_refresh);
		
		refreshButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// 旋转动画
				animRotate(v);
				
				if (that instanceof Retrievable) {
					((Retrievable) that).doRetrieve();
				} else if (that instanceof Refreshable) {
					((Refreshable) that).doRetrieve();
				} else {
					Log.e(TAG, "The current view " + that.getClass().getName() + " cann't be retrieved");
				}
			}
		});
	}
	
	protected void animRotate(View v) {
		if (null != v) {
			Animation anim = AnimationUtils.loadAnimation(v.getContext(),
					R.anim.rotate360);
			v.startAnimation(anim);
		}
	}
	
	// 搜索
	protected void addSearchButton() {
		searchButton  = (ImageButton) findViewById(R.id.search);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// 旋转动画
				Animation anim = AnimationUtils.loadAnimation(v.getContext(),
						R.anim.scale_lite);
				v.startAnimation(anim);
				onSearchRequested();
			}
		});
	}
	
	
	// 撰写
	protected void addWriteButton() {
		writeButton = (ImageButton) findViewById(R.id.writeMessage);
		
		writeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// 动画
				Animation anim = AnimationUtils.loadAnimation(v.getContext(),
						R.anim.scale_lite);
				v.startAnimation(anim);

				// forward to write activity
				Intent intent = new Intent();
				intent.setClass(v.getContext(), WriteActivity.class);
				v.getContext().startActivity(intent);
			}
		});
	}
	
	// 回首页
	protected void addHomeButton() {
		homeButton = (ImageButton) findViewById(R.id.home);
		
		homeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// 动画
				Animation anim = AnimationUtils.loadAnimation(v.getContext(),
						R.anim.scale_lite);
				v.startAnimation(anim);

				// forward to TwitterActivity
				Intent intent = new Intent();
				intent.setClass(v.getContext(), TwitterActivity.class);
				v.getContext().startActivity(intent);

			}
		});
	}
	
	// 返回
	protected void addBackButton() {
		backButton = (Button) findViewById(R.id.top_back);
		// 中文粗体
//		TextPaint tp = backButton.getPaint(); 
//	    tp.setFakeBoldText(true);
		
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Go back to previous activity
				finish();
			}
		});
	}
	
	
	
	protected void initHeader(int style) {
		
		switch (style) {
		case HEADER_STYLE_HOME:
			addHeaderView(R.layout.header);
			addTitleButton();
			addWriteButton();
			addSearchButton();
			addRefreshButton();
			break;
		case HEADER_STYLE_BACK:
			addHeaderView(R.layout.header_back);
			addBackButton();
			addWriteButton();
			addSearchButton();
			addRefreshButton();
			break;
		case HEADER_STYLE_WRITE:
			addHeaderView(R.layout.header_write);
			addBackButton();
			addSearchButton();
			addHomeButton();
			break;
		}
	}
	
	private void addHeaderView(int resource) {
		// find content root view
		ViewGroup root =  (ViewGroup) getWindow().getDecorView();
		ViewGroup content = (ViewGroup) root.getChildAt(0);
		View header = View.inflate(WithHeaderActivity.this, resource, null);
//		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		
		content.addView(header, 0);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		View header = (View) View.inflate(WithHeaderActivity.this, R.layout.header, null);

	}

	@Override
	protected void onDestroy() {
		// dismiss dialog before destroy 
		// to avoid android.view.WindowLeaked Exception
		if (dialog != null){
			dialog.dismiss();
		}
		super.onDestroy();
	}
}
