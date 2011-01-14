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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.fanfoudroid.task.Followable;
import com.ch_linghu.fanfoudroid.task.HasFavorite;
import com.ch_linghu.fanfoudroid.task.Retrievable;
import com.ch_linghu.fanfoudroid.ui.base.TwitterCursorBaseActivity;
import com.ch_linghu.fanfoudroid.weibo.Paging;
import com.ch_linghu.fanfoudroid.weibo.Status;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

public class MentionActivity extends TwitterCursorBaseActivity 
		implements Followable, Retrievable, HasFavorite {
	private static final String TAG = "MentionActivity";

	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.REPLIES";

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
				R.string.omenu_refresh);
		item.setIcon(R.drawable.refresh);

		item = menu.add(0, OPTIONS_MENU_ID_TWEETS, 0, R.string.page_title_home);
		item.setIcon(android.R.drawable.ic_menu_view);

		item = menu.add(0, OPTIONS_MENU_ID_DM, 0, R.string.page_title_direct_messages);
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
		return getDb().fetchAllTweets(TwitterDbAdapter.TABLE_MENTION);
	}
	
	@Override
	protected void markAllRead() {
		// TODO Auto-generated method stub
		getDb().markAllTweetsRead(TwitterDbAdapter.TABLE_MENTION);
	}
	
	@Override
	protected String getActivityTitle() {
		// TODO Auto-generated method stub
		return getResources().getString(R.string.page_title_mentions);
	}
	
	
	// for Retrievable interface

	@Override
	public String fetchMaxId() {
		// TODO Auto-generated method stub
		return getDb().fetchMaxId(TwitterDbAdapter.TABLE_MENTION);
	}

	@Override
	public List<Status> getMessageSinceId(String maxId) throws WeiboException {
		if (maxId != null){
			return getApi().getMentions(new Paging(maxId));
		}else{
			return getApi().getMentions();
		}
	}

	@Override
	public void addMessages(ArrayList<Tweet> tweets, boolean isUnread) {
		// TODO Auto-generated method stub
		getDb().addTweets(TwitterDbAdapter.TABLE_MENTION, tweets, isUnread);
	}
}