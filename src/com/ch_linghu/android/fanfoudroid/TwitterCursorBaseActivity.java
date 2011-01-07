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
package com.ch_linghu.android.fanfoudroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.ch_linghu.android.fanfoudroid.TwitterApi.ApiException;
import com.ch_linghu.android.fanfoudroid.TwitterApi.AuthException;
import com.google.android.photostream.UserTask;

/**
 * TwitterCursorBaseLine用于带有静态数据来源（对应数据库的，与twitter表同构的特定表）的展现
 */
public abstract class TwitterCursorBaseActivity extends TwitterListBaseActivity {
	static final String TAG = "TwitterListBaseActivity";

	// Views.
	protected ListView mTweetList;
	protected TweetCursorAdapter mTweetAdapter;

	// Tasks.
	private UserTask<Void, Void, RetrieveResult> mRetrieveTask;
	private UserTask<Void, Void, RetrieveResult> mFollowersRetrieveTask;

	// Refresh data at startup if last refresh was this long ago or greater.
	private static final long REFRESH_THRESHOLD = 5 * 60 * 1000;

	// Refresh followers if last refresh was this long ago or greater.
	private static final long FOLLOWERS_REFRESH_THRESHOLD = 12 * 60 * 60 * 1000;

	abstract protected void markAllRead();

	abstract protected Cursor fetchMessages();

	abstract protected String fetchMaxId();

	abstract protected void addMessages(ArrayList<Tweet> tweets,
			boolean isUnread);

	abstract protected JSONArray getMessageSinceId(String maxId)
			throws IOException, AuthException, ApiException;

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
			tweet.createdAt = Utils.parseDateTime(cursor.getString(cursor.getColumnIndex(TwitterDbAdapter.KEY_CREATED_AT)));
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
		getDb().updateTweet(tweet);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkIsLogedIn();

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

		if (diff > FOLLOWERS_REFRESH_THRESHOLD) {
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
				&& mFollowersRetrieveTask.getStatus() == UserTask.Status.RUNNING) {
			Log.w(TAG, "Already retrieving.");
		} else {
			mFollowersRetrieveTask = new FollowersTask().execute();
		}
	}

	private static final String SIS_RUNNING_KEY = "running";

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == UserTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		} else if (mFavTask != null
				&& mFavTask.getStatus() == UserTask.Status.RUNNING) {
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
				&& mRetrieveTask.getStatus() == UserTask.Status.RUNNING) {
			mRetrieveTask.cancel(true);
		}

		if (mFavTask != null && mFavTask.getStatus() == UserTask.Status.RUNNING) {
			mFavTask.cancel(true);
		}

		// Don't need to cancel FollowersTask (assuming it ends properly).

		super.onDestroy();
	}

	// UI helpers.

	private void updateProgress(String progress) {
		mProgressText.setText(progress);
	}

	private void draw() {
		mTweetAdapter.refresh();
	}

	private void goTop() {
		mTweetList.setSelection(0);
	}

	protected void adapterRefresh() {
		mTweetAdapter.notifyDataSetChanged();
		mTweetAdapter.refresh();
	}

	public void doRetrieve() {
		Log.i(TAG, "Attempting retrieve.");

		// 旋转刷新按钮
		animRotate(refreshButton);

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == UserTask.Status.RUNNING) {
			Log.w(TAG, "Already retrieving.");
		} else {
			mRetrieveTask = new RetrieveTask().execute();
		}
	}

	private void onRetrieveBegin() {
		updateProgress(getString(R.string.refreshing));
	}

	private enum RetrieveResult {
		OK, IO_ERROR, AUTH_ERROR, CANCELLED
	}

	private class RetrieveTask extends UserTask<Void, Void, RetrieveResult> {
		@Override
		public void onPreExecute() {
			onRetrieveBegin();
		}

		@Override
		public void onProgressUpdate(Void... progress) {
			draw();
		}

		@Override
		public RetrieveResult doInBackground(Void... params) {
			JSONArray jsonArray;

			String maxId = fetchMaxId(); // getDb().fetchMaxMentionId();

			try {
				jsonArray = getMessageSinceId(maxId); // getApi().getMentionSinceId(maxId);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				return RetrieveResult.IO_ERROR;
			} catch (AuthException e) {
				Log.i(TAG, "Invalid authorization.");
				return RetrieveResult.AUTH_ERROR;
			} catch (ApiException e) {
				Log.e(TAG, e.getMessage(), e);
				return RetrieveResult.IO_ERROR;
			}

			ArrayList<Tweet> tweets = new ArrayList<Tweet>();
			HashSet<String> imageUrls = new HashSet<String>();

			for (int i = 0; i < jsonArray.length(); ++i) {
				if (isCancelled()) {
					return RetrieveResult.CANCELLED;
				}

				Tweet tweet;

				try {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					tweet = Tweet.create(jsonObject);
					tweets.add(tweet);
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage(), e);
					return RetrieveResult.IO_ERROR;
				}

				imageUrls.add(tweet.profileImageUrl);

				if (isCancelled()) {
					return RetrieveResult.CANCELLED;
				}
			}

			addMessages(tweets, false); // getDb().addMentions(tweets, false);

			if (isCancelled()) {
				return RetrieveResult.CANCELLED;
			}

			publishProgress();

			for (String imageUrl : imageUrls) {
				if (!Utils.isEmpty(imageUrl)) {
					// Fetch image to cache.
					try {
						getImageManager().put(imageUrl);
					} catch (IOException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}

				if (isCancelled()) {
					return RetrieveResult.CANCELLED;
				}
			}

			return RetrieveResult.OK;
		}

		@Override
		public void onPostExecute(RetrieveResult result) {
			if (result == RetrieveResult.AUTH_ERROR) {
				logout();
			} else if (result == RetrieveResult.OK) {
				SharedPreferences.Editor editor = mPreferences.edit();
				editor.putLong(Preferences.LAST_TWEET_REFRESH_KEY, Utils
						.getNowTime());
				editor.commit();
				draw();
				goTop();
			} else {
				// Do nothing.
			}

			refreshButton.clearAnimation();
			updateProgress("");
		}
	}

	private class FollowersTask extends UserTask<Void, Void, RetrieveResult> {
		@Override
		public RetrieveResult doInBackground(Void... params) {
			try {
				ArrayList<String> followers = getApi().getFollowersIds();
				getDb().syncFollowers(followers);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				return RetrieveResult.IO_ERROR;
			} catch (AuthException e) {
				Log.i(TAG, "Invalid authorization.");
				return RetrieveResult.AUTH_ERROR;
			} catch (ApiException e) {
				Log.e(TAG, e.getMessage(), e);
				return RetrieveResult.IO_ERROR;
			}

			return RetrieveResult.OK;
		}

		@Override
		public void onPostExecute(RetrieveResult result) {
			if (result == RetrieveResult.OK) {
				SharedPreferences.Editor editor = mPreferences.edit();
				editor.putLong(Preferences.LAST_FOLLOWERS_REFRESH_KEY, Utils
						.getNowTime());
				editor.commit();
			} else {
				// Do nothing.
			}
		}
	}
}