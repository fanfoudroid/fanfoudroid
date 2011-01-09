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

package com.ch_linghu.android.fanfoudroid.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ch_linghu.android.fanfoudroid.R;
import com.ch_linghu.android.fanfoudroid.TwitterApi.ApiException;
import com.ch_linghu.android.fanfoudroid.TwitterApi.AuthException;
import com.ch_linghu.android.fanfoudroid.data.Tweet;
import com.ch_linghu.android.fanfoudroid.task.Followable;
import com.ch_linghu.android.fanfoudroid.task.HasFavorite;
import com.ch_linghu.android.fanfoudroid.task.Retrievable;
import com.ch_linghu.android.fanfoudroid.ui.base.TwitterCursorBaseActivity;

public class MentionActivity extends TwitterCursorBaseActivity 
		implements Followable, Retrievable, HasFavorite {
	private static final String TAG = "MentionActivity";

	private static final String LAUNCH_ACTION = "com.ch_linghu.android.fanfoudroid.REPLIES";

	static final int DIALOG_WRITE_ID = 0;

	public static Intent createIntent(Context context) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		return intent;
	}

	public static Intent createNewTaskIntent(Context context) {
		Intent intent = createIntent(context);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHeaderTitle("@提到我的");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(0, OPTIONS_MENU_ID_REFRESH, 0,
				R.string.refresh);
		item.setIcon(R.drawable.refresh);

		item = menu.add(0, OPTIONS_MENU_ID_TWEETS, 0, R.string.tweets);
		item.setIcon(android.R.drawable.ic_menu_view);

		item = menu.add(0, OPTIONS_MENU_ID_DM, 0, R.string.dm);
		item.setIcon(android.R.drawable.ic_menu_send);

		/*
		 * item = menu.add(0, OPTIONS_MENU_ID_TOGGLE_REPLIES, 0,
		 * R.string.show_at_replies);
		 * item.setIcon(android.R.drawable.ic_menu_zoom);
		 */

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected Cursor fetchMessages() {
		// TODO Auto-generated method stub
		return getDb().fetchMentions();
	}
	
	@Override
	protected void markAllRead() {
		// TODO Auto-generated method stub
		getDb().markAllMentionsRead();
	}
	
	@Override
	protected String getActivityTitle() {
		// TODO Auto-generated method stub
		return getResources().getString(R.string.show_at_replies);
	}
	
	
	// for Retrievable interface

	@Override
	public String fetchMaxId() {
		// TODO Auto-generated method stub
		return getDb().fetchMaxMentionId();
	}

	@Override
	public JSONArray getMessageSinceId(String maxId) throws IOException,
			AuthException, ApiException {
		// TODO Auto-generated method stub
		return getApi().getMentionSinceId(maxId);
	}

	@Override
	public void addMessages(ArrayList<Tweet> tweets, boolean isUnread) {
		// TODO Auto-generated method stub
		getDb().addMentions(tweets, isUnread);
	}



}