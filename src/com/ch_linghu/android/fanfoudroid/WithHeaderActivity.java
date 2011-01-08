package com.ch_linghu.android.fanfoudroid;

import tk.sandin.android.fanfoudoird.task.Retrievable;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class WithHeaderActivity extends BaseActivity {
	
	private static final String TAG = "WithHeaderActivity";
	
	public static final int HEADER_STYLE_HOME  = 1;
	public static final int HEADER_STYLE_WRITE = 2;

	protected View floatDiv;
	protected ImageButton refreshButton;
	protected ImageButton searchButton;
	protected ImageButton writeButton;
	protected ImageButton logoButton;
	protected Button backButton;
	protected ImageButton homeButton;
	protected MenuDialog dialog;
	
	protected void addLogoButton() {
		
		// Find View
		floatDiv   = findViewById(R.id.float_div);
		logoButton = (ImageButton) findViewById(R.id.logo);
		logoButton.setVisibility(View.VISIBLE);
		
		// LOGO按钮
		logoButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int top = logoButton.getTop();
				int height = logoButton.getHeight();
				int x = top + height;

				if (null == dialog) {
					dialog = new MenuDialog(WithHeaderActivity.this);
					dialog.setPosition(-1, x);
				}
				
				// toggle dialog
				if (dialog.isShowing()) {
					dialog.hide();
				} else {
					dialog.show();
				}
			}
		});
	}
	
	protected void addRefreshButton() {
		final Activity that = this;
		refreshButton = (ImageButton) findViewById(R.id.top_refresh);
		refreshButton.setVisibility(View.VISIBLE);
		
		refreshButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// 旋转动画
				animRotate(v);
				
				// 刷新
				if (that instanceof Retrievable) {
					((Retrievable) that).doRetrieve();
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
	
	protected void addSearchButton() {
		searchButton  = (ImageButton) findViewById(R.id.search);
		searchButton.setVisibility(View.VISIBLE);
		// 搜索
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
	
	
	protected void addWriteButton() {
		writeButton = (ImageButton) findViewById(R.id.writeMessage);
		writeButton.setVisibility(View.VISIBLE);
		
		// 撰写
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
	
	protected void addHomeButton() {
		homeButton = (ImageButton) findViewById(R.id.home);
		homeButton.setVisibility(View.VISIBLE);
		
		// 回首页
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
	
	protected void addBackButton() {
		backButton = (Button) findViewById(R.id.top_back);
		backButton.setVisibility(View.VISIBLE);
		// 中文粗体
//		TextPaint tp = backButton.getPaint(); 
//	    tp.setFakeBoldText(true);
		
		// 返回
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Go back to previous activity
				finish();
			}
		});
	}
	
	protected void initHeader(int Style) {
//		Log.i("LDS", "initHeader call by " + this.getClass().getName());
		switch (Style) {
		case HEADER_STYLE_HOME:
			addLogoButton();
			addWriteButton();
			addSearchButton();
			addRefreshButton();
			break;
		case HEADER_STYLE_WRITE:
			addBackButton();
			addSearchButton();
			addHomeButton();
			break;
		}
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		View header = (View) View.inflate(WithHeaderActivity.this, R.layout.header, null);

	}
}
