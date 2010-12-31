package com.ch_linghu.android.fanfoudroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

public class WithHeaderActivity extends BaseActivity implements Refreshable {
	
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
	
	protected void addLogoButton() {
		
		// Find View
		floatDiv   = findViewById(R.id.float_div);
		logoButton = (ImageButton) findViewById(R.id.logo);
		logoButton.setVisibility(View.VISIBLE);
		
		// LOGO按钮
		logoButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// 动画
				Animation anim = AnimationUtils.loadAnimation(v.getContext(),
						R.anim.scale_lite);
				v.startAnimation(anim);

				// toggle float div
				if (floatDiv.getVisibility() == View.VISIBLE) {
					floatDiv.setVisibility(View.INVISIBLE);
				} else {
					floatDiv.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	protected void addRefreshButton(final Refreshable activity) {
		refreshButton = (ImageButton) findViewById(R.id.top_refresh);
		refreshButton.setVisibility(View.VISIBLE);
		
		// 刷新
		refreshButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// 旋转动画
				animRotate(v);
				
				// 刷新
				activity.doRetrieve();
			}
		});
	}
	
	protected void animRotate(View v) {
		Animation anim = AnimationUtils.loadAnimation(v.getContext(),
				R.anim.rotate360);
		v.startAnimation(anim);
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
		switch (Style) {
		case HEADER_STYLE_HOME:
			addLogoButton();
			addWriteButton();
			addSearchButton();
			break;
		case HEADER_STYLE_WRITE:
			addBackButton();
			addSearchButton();
			addHomeButton();
		}
	}
	
	protected void initHeader(int Style, final Refreshable activity) {
		switch (Style) {
		case HEADER_STYLE_HOME:
			addLogoButton();
			addWriteButton();
			addSearchButton();
			addRefreshButton(activity);
			break;
		}
	}


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		View header = (View) View.inflate(WithHeaderActivity.this, R.layout.header, null);

	}
	
	public void doRetrieve() {}
}
