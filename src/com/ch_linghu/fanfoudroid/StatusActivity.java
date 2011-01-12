/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ch_linghu.fanfoudroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.ui.base.WithHeaderActivity;

public class StatusActivity extends WithHeaderActivity {

	private static final String TAG = "WriteActivity";
	private static final String SIS_RUNNING_KEY = "running";
	private static final String PREFS_NAME = "com.ch_linghu.fanfoudroid";
	
	private static final String EXTRA_TWEET = "tweet";
	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.STATUS";
	
	// View
	private TextView tweet_user_name; 
	private TextView status_content; 
	private TextView tweet_user_info;
	private ImageView profile_image;
	private TextView status_source;
	private TextView status_date;
	private ImageButton person_more;
	private ImageView status_photo = null; //if exists
	private TextView reply_status_text = null; //if exists
	private TextView reply_status_date = null; //if exists
	
	public static Intent createIntent(Tweet tweet) {
	    Intent intent = new Intent(LAUNCH_ACTION);
	    intent.putExtra(EXTRA_TWEET, tweet);
	    return intent;
	}


	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate.");
		super.onCreate(savedInstanceState);

		// init View
		setContentView(R.layout.status);
		initHeader(HEADER_STYLE_BACK);
		
		// Intent & Action & Extras
		Intent intent = getIntent();
		String action = intent.getAction();
		Bundle extras = intent.getExtras();

		// View
		tweet_user_name = (TextView)	findViewById(R.id.tweet_user_name);
		tweet_user_info = (TextView)	findViewById(R.id.tweet_user_info);
		status_content  = (TextView)	findViewById(R.id.status_content);
		status_source   = (TextView)	findViewById(R.id.status_source);
		profile_image   = (ImageView)	findViewById(R.id.profile_image);
		status_date 	= (TextView)	findViewById(R.id.status_date);
		person_more 	= (ImageButton)	findViewById(R.id.person_more);
		
		// Set view with intent data
		Tweet tweet = extras.getParcelable(EXTRA_TWEET);
		tweet_user_name.setText(tweet.screenName);
		status_content.setText(tweet.text);
		
		
		//TODO: 为单推界面绑定顶部按钮监听器
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause.");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG, "onRestart.");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume.");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "onStart.");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop.");
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy.");
		
		super.onDestroy();
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

}