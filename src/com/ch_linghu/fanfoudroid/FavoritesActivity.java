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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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

//TODO: 数据来源换成 getFavorites()
public class FavoritesActivity extends TwitterCursorBaseActivity {
	private static final String TAG = "FavoritesActivity";

	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.FAVORITES";
	private static final String USER_ID = "userid";
	private static final String USER_NAME = "userName";
	private static final int DIALOG_WRITE_ID = 0;
	
	private String userId = null;
	private String userName = null;
	
	public static Intent createIntent(String userId, String userName) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(USER_ID, userId);
		intent.putExtra(USER_NAME, userName);

		return intent;
	}

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (super._onCreate(savedInstanceState)){
			mNavbar.setHeaderTitle(getActivityTitle());
			
			return true;
		}else{
			return false;
		}

	}

	public static Intent createNewTaskIntent(String userId, String userName) {
		Intent intent = createIntent(userId, userName);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		return intent;
	}

	// Menu.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected Cursor fetchMessages() {
		// TODO Auto-generated method stub
		return getDb().fetchAllTweets(getUserId(), StatusTable.TYPE_FAVORITE);
	}

	@Override
	protected String getActivityTitle() {
		// TODO Auto-generated method stub
		String template = getString(R.string.page_title_favorites);
		String who;
		if (getUserId().equals(TwitterApplication.getMyselfId())){
			who = "我";
		}else{
			who = getUserName();
		}
		return MessageFormat.format(template, who);
	}

	
	@Override
	protected void markAllRead() {
		// TODO Auto-generated method stub
		getDb().markAllTweetsRead(getUserId(), StatusTable.TYPE_FAVORITE);
	}
	
	
	// hasRetrieveListTask interface
	
	@Override
	public int addMessages(ArrayList<Tweet> tweets, boolean isUnread) {
		return getDb().putTweets(tweets, getUserId(), StatusTable.TYPE_FAVORITE, isUnread);
	}
	
	@Override
	public String fetchMaxId() {
		return getDb().fetchMaxTweetId(getUserId(), StatusTable.TYPE_FAVORITE);
	}

	@Override
	public List<Status> getMessageSinceId(String maxId) throws HttpException {
		if (maxId != null){
			return getApi().getFavorites(getUserId(), new Paging(maxId));
		}else{
			return getApi().getFavorites(getUserId());
		}
	}

	@Override
	public String fetchMinId() {
		return getDb().fetchMinTweetId(getUserId(), StatusTable.TYPE_FAVORITE);
	}

	@Override
	public List<Status> getMoreMessageFromId(String minId)
			throws HttpException {
		Paging paging = new Paging(1, 20);
		paging.setMaxId(minId);
		return getApi().getFavorites(getUserId(), paging);
	}

	@Override
	public int getDatabaseType() {
		return StatusTable.TYPE_FAVORITE;
	}

	@Override
	public String getUserId() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null){
			userId = extras.getString(USER_ID);
		} else {
			userId = TwitterApplication.getMyselfId();
		}

		return userId;
	}
	
	public String getUserName(){
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null){
			userName = extras.getString(USER_NAME);
		} else {
			userName = TwitterApplication.getMyselfName();
		}

		return userName;		
	}
}