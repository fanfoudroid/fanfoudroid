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
package com.ch_linghu.fanfoudroid.ui.base;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.ListView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.task.TaskFactory;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.module.TweetAdapter;
import com.ch_linghu.fanfoudroid.ui.module.TweetCursorAdapter;
import com.ch_linghu.fanfoudroid.weibo.Status;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

/**
 * TwitterCursorBaseLine用于带有静态数据来源（对应数据库的，与twitter表同构的特定表）的展现
 */
public abstract class TwitterCursorBaseActivity extends TwitterListBaseActivity 
		{
	static final String TAG = "TwitterListBaseActivity";

	// Views.
	protected ListView mTweetList;
	protected TweetCursorAdapter mTweetAdapter;

	// Tasks.
	private AsyncTask<Void, Void, TaskResult> mRetrieveTask;
	private AsyncTask<Void, Void, TaskResult> mFollowersRetrieveTask;

	// Refresh data at startup if last refresh was this long ago or greater.
	private static final long REFRESH_THRESHOLD = 5 * 60 * 1000;

	// Refresh followers if last refresh was this long ago or greater.
	private static final long FOLLOWERS_REFRESH_THRESHOLD = 12 * 60 * 60 * 1000;

	abstract protected void markAllRead();

	abstract protected Cursor fetchMessages();

	abstract protected String fetchMaxId();

	abstract protected void addMessages(ArrayList<Tweet> tweets,
			boolean isUnread);

	abstract protected List<Status> getMessageSinceId(String maxId)
			throws WeiboException;

	public static final int CONTEXT_REPLY_ID = Menu.FIRST + 1;
	// public static final int CONTEXT_AT_ID = Menu.FIRST + 2;
	public static final int CONTEXT_RETWEET_ID = Menu.FIRST + 3;
	public static final int CONTEXT_DM_ID = Menu.FIRST + 4;
	public static final int CONTEXT_MORE_ID = Menu.FIRST + 5;
	public static final int CONTEXT_ADD_FAV_ID = Menu.FIRST + 6;
	public static final int CONTEXT_DEL_FAV_ID = Menu.FIRST + 7;

	@Override
	protected void setupState() {
		Cursor cursor;

		cursor = fetchMessages(); // getDb().fetchMentions();
		setTitle(getActivityTitle());

		startManagingCursor(cursor);

		mTweetList = (ListView) findViewById(R.id.tweet_list);
		mTweetAdapter = new TweetCursorAdapter(this, cursor);
		mTweetList.setAdapter(mTweetAdapter);
		registerOnClickListener(mTweetList);
	}

	@Override
	protected ListView getTweetList(){
		return mTweetList;
	}

	@Override
	protected TweetAdapter getTweetAdapter(){
		return mTweetAdapter;
	}

	@Override
	protected boolean useBasicMenu(){
		return true;
	}

	@Override
	protected Tweet getContextItemTweet(int position){
		Cursor cursor = (Cursor) mTweetAdapter.getItem(position);
		if (cursor == null){
			return null;
		}else{
			Tweet tweet = new Tweet();
			tweet.id = cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_ID));
			tweet.createdAt = Utils.parseDateTimeFromSqlite(cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_CREATED_AT)));
			tweet.favorited = cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_FAVORITED));
			tweet.screenName = cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_USER));
			tweet.userId = cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_USER_ID));
			tweet.text = cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_TEXT));
			tweet.source = cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_SOURCE));
			tweet.profileImageUrl = cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_PROFILE_IMAGE_URL));
			tweet.inReplyToScreenName = cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_IN_REPLY_TO_SCREEN_NAME));
			tweet.inReplyToStatusId = cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_IN_REPLY_TO_STATUS_ID));
			tweet.inReplyToUserId = cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_IN_REPLY_TO_USER_ID));
			return tweet;
		}
	}

	@Override
	protected void updateTweet(Tweet tweet){
		//对所有相关表的对应消息都进行刷新（如果存在的话）
		getDb().updateTweet(TwitterDbAdapter.TABLE_FAVORITE, tweet);
		getDb().updateTweet(TwitterDbAdapter.TABLE_MENTION, tweet);
		getDb().updateTweet(TwitterDbAdapter.TABLE_TWEET, tweet);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!checkIsLogedIn()) return;

		// Mark all as read.
		// getDb().markAllMentionsRead();
		markAllRead();

		boolean shouldRetrieve = false;

		long lastRefreshTime = mPreferences.getLong(
				Preferences.LAST_TWEET_REFRESH_KEY, 0);
		long nowTime = Utils.getNowTime();

		long diff = nowTime - lastRefreshTime;
		Log.i(TAG, "Last refresh was " + diff + " ms ago.");

		if (diff > REFRESH_THRESHOLD) {
			shouldRetrieve = true;
		} else if (Utils.isTrue(savedInstanceState, SIS_RUNNING_KEY)) {
			// Check to see if it was running a send or retrieve task.
			// It makes no sense to resend the send request (don't want dupes)
			// so we instead retrieve (refresh) to see if the message has
			// posted.
			Log.i(TAG,
					"Was last running a retrieve or send task. Let's refresh.");
			shouldRetrieve = true;
		}

		if (shouldRetrieve) {
			doRetrieve();
		}

		long lastFollowersRefreshTime = mPreferences.getLong(
				Preferences.LAST_FOLLOWERS_REFRESH_KEY, 0);

		diff = nowTime - lastFollowersRefreshTime;
		Log.i(TAG, "Last followers refresh was " + diff + " ms ago.");

		// Should Refresh Followers
		if (diff > FOLLOWERS_REFRESH_THRESHOLD && 
				(mRetrieveTask == null || mRetrieveTask.getStatus() != AsyncTask.Status.RUNNING)) {
			Log.i(TAG, "Refresh followers.");
			doRetrieveFollowers();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!getApi().isLoggedIn()) {
			Log.i(TAG, "Not logged in.");
			handleLoggedOut();
			return;
		}
	}

	private void doRetrieveFollowers() {
		Log.i(TAG, "Attempting followers retrieve.");

		if (mFollowersRetrieveTask != null
				&& mFollowersRetrieveTask.getStatus() == AsyncTask.Status.RUNNING) {
			Log.w(TAG, "Already retrieving.");
		} else {
//			mFollowersRetrieveTask = new FollowersTask().execute();
			AsyncTask<Void,Void,TaskResult> task = TaskFactory.create(TaskFactory.FOLLOWERS_TASK_TYPE, this);
			if (null != task) {
				mFollowersRetrieveTask = task.execute();
			}
		}
	}
	
	

	private static final String SIS_RUNNING_KEY = "running";

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == AsyncTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		} else if (mFavTask != null
				&& mFavTask.getStatus() == AsyncTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);

		// mTweetEdit.updateCharsRemain();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy.");

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == AsyncTask.Status.RUNNING) {
			mRetrieveTask.cancel(true);
		}

		if (mFavTask != null && mFavTask.getStatus() == AsyncTask.Status.RUNNING) {
			mFavTask.cancel(true);
		}

		// Don't need to cancel FollowersTask (assuming it ends properly).

		super.onDestroy();
	}

	// UI helpers.
	
	@Override
	protected void adapterRefresh() {
		mTweetAdapter.notifyDataSetChanged();
		mTweetAdapter.refresh();
	}

	//  Retrieve interface
	public void updateProgress(String progress) {
		mProgressText.setText(progress);
	}
	public void draw() {
		mTweetAdapter.refresh();
	}
	public void goTop() {
		mTweetList.setSelection(0);
	}
	
	public void onRetrieveBegin() {
		updateProgress(getString(R.string.page_status_refreshing));
	}
	
	public SharedPreferences getPreferences() {
		  return mPreferences;
	}

	public void doRetrieve() {
		Log.i(TAG, "Attempting retrieve.");

		// 旋转刷新按钮
		animRotate(refreshButton);

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == AsyncTask.Status.RUNNING) {
			Log.w(TAG, "Already retrieving.");
		} else {
//			mRetrieveTask = new RetrieveTask().execute();
			AsyncTask<Void,Void,TaskResult> task = TaskFactory.create(TaskFactory.RETRIEVE_LIST_TASK_TYPE, this);
			if (null != task) {
				mRetrieveTask = task.execute();
			}
		}
	}
	// for Retrievable interface
	public ImageButton getRefreshButton() {
		return refreshButton;
	}
	
}