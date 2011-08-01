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

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.fanfou.Paging;
import com.ch_linghu.fanfoudroid.fanfou.Status;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.ui.base.TwitterCursorBaseActivity;
import com.ch_linghu.fanfoudroid.R;

public class MentionActivity extends TwitterCursorBaseActivity {
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
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (super._onCreate(savedInstanceState)) {
			mNavbar.setHeaderTitle("@提到我的");
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected Cursor fetchMessages() {
		return getDb().fetchAllTweets(getUserId(), StatusTable.TYPE_MENTION);
	}

	@Override
	protected void markAllRead() {
		getDb().markAllTweetsRead(getUserId(), StatusTable.TYPE_MENTION);
	}

	@Override
	protected String getActivityTitle() {
		return getResources().getString(R.string.page_title_mentions);
	}

	// for Retrievable interface

	@Override
	public String fetchMaxId() {
		return getDb().fetchMaxTweetId(getUserId(), StatusTable.TYPE_MENTION);
	}

	@Override
	public List<Status> getMessageSinceId(String maxId) throws HttpException {
		if (maxId != null) {
			return getApi().getMentions(new Paging(maxId));
		} else {
			return getApi().getMentions();
		}
	}

	@Override
	public int addMessages(ArrayList<Tweet> tweets, boolean isUnread) {
		return getDb().putTweets(tweets, getUserId(), StatusTable.TYPE_MENTION,
				isUnread);
	}

	@Override
	public String fetchMinId() {
		return getDb().fetchMinTweetId(getUserId(), StatusTable.TYPE_MENTION);
	}

	@Override
	public List<Status> getMoreMessageFromId(String minId) throws HttpException {
		Paging paging = new Paging(1, 20);
		paging.setMaxId(minId);
		return getApi().getMentions(paging);
	}

	@Override
	public int getDatabaseType() {
		return StatusTable.TYPE_MENTION;
	}

	@Override
	public String getUserId() {
		return TwitterApplication.getMyselfId();
	}
}