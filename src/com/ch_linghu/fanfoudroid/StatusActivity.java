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

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.R.layout;
import com.ch_linghu.fanfoudroid.ui.base.WithHeaderActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class StatusActivity extends WithHeaderActivity {

	private static final String TAG = "WriteActivity";
	private static final String SIS_RUNNING_KEY = "running";
	private static final String PREFS_NAME = "com.ch_linghu.fanfoudroid";
	
	private static final String EXTRA_USER = "user";
	private static final String EXTRA_NAME_SCREEN = "name";
	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.STATUS";
	
	public static Intent createIntent(String user, String name) {
	    Intent intent = new Intent(LAUNCH_ACTION);
	    intent.putExtra(EXTRA_USER, user);
	    intent.putExtra(EXTRA_NAME_SCREEN, name);

	    return intent;
	}


	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate.");
		super.onCreate(savedInstanceState);

		// init View
		setContentView(R.layout.status);
		initHeader(HEADER_STYLE_HOME);
		
		// Intent & Action & Extras
		Intent intent = getIntent();
		String action = intent.getAction();
		Bundle extras = intent.getExtras();

		// View
		
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