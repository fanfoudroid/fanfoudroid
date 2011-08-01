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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.User;
import com.ch_linghu.fanfoudroid.db.UserInfoTable;
import com.ch_linghu.fanfoudroid.fanfou.Paging;
import com.ch_linghu.fanfoudroid.fanfou.Status;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskManager;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.module.SimpleFeedback;
import com.ch_linghu.fanfoudroid.ui.module.TweetAdapter;
import com.ch_linghu.fanfoudroid.ui.module.UserCursorAdapter;
import com.ch_linghu.fanfoudroid.util.DateTimeHelper;

/**
 * TwitterCursorBaseLine用于带有静态数据来源（对应数据库的，与twitter表同构的特定表）的展现
 */
public abstract class UserCursorBaseActivity extends UserListBaseActivity {

	/**
	 * 第一种方案：(采取第一种) 暂不放在数据库中，直接从Api读取。
	 * 
	 * 第二种方案： 麻烦的是api数据与数据库同步，当收听人数比较多的时候，一次性读取太费流量 按照饭否api每次分页100人
	 * 当收听数<100时先从数据库一次性根据API返回的ID列表读取数据，如果数据库中的收听数<总数，那么从API中读取所有用户信息并同步到数据库中。
	 * 当收听数>100时采取分页加载，先按照id
	 * 获取数据库里前100用户,如果用户数量<100则从api中加载，从page=1开始下载，同步到数据库中，单击更多继续从数据库中加载
	 * 当数据库中的数据读取到最后一页后，则从api中加载并更新到数据库中。 单击刷新按钮则从api加载并同步到数据库中
	 * 
	 */
	static final String TAG = "UserCursorBaseActivity";

	// Views.
	protected ListView mUserList;
	protected UserCursorAdapter mUserListAdapter;

	protected TextView loadMoreBtn;
	protected ProgressBar loadMoreGIF;
	protected TextView loadMoreBtnTop;
	protected ProgressBar loadMoreGIFTop;

	protected static int lastPosition = 0;

	// Tasks.
	protected TaskManager taskManager = new TaskManager();
	private GenericTask mRetrieveTask;
	private GenericTask mFollowersRetrieveTask;
	private GenericTask mGetMoreTask;// 每次十个用户

	protected abstract String getUserId();// 获得用户id

	private TaskListener mRetrieveTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "RetrieveTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				SharedPreferences.Editor editor = getPreferences().edit();
				editor.putLong(Preferences.LAST_TWEET_REFRESH_KEY,
						DateTimeHelper.getNowTime());
				editor.commit();
				// TODO: 1. StatusType(DONE) ; 2. 只有在取回的数据大于MAX时才做GC,
				// 因为小于时可以保证数据的连续性

				// FIXME： gc需要带owner
				// getDb().gc(getDatabaseType()); // GC
				draw();
				goTop();
			} else {
				// Do nothing.
			}

			// loadMoreGIFTop.setVisibility(View.GONE);
			updateProgress("");
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveBegin();
		}

		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
			Log.d(TAG, "onProgressUpdate");
			draw();
		}
	};
	private TaskListener mFollowerRetrieveTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "FollowerRetrieve";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				SharedPreferences sp = getPreferences();
				SharedPreferences.Editor editor = sp.edit();
				editor.putLong(Preferences.LAST_FOLLOWERS_REFRESH_KEY,
						DateTimeHelper.getNowTime());
				editor.commit();
			} else {
				// Do nothing.
			}
		}
	};

	// Refresh data at startup if last refresh was this long ago or greater.
	private static final long REFRESH_THRESHOLD = 5 * 60 * 1000;

	// Refresh followers if last refresh was this long ago or greater.
	private static final long FOLLOWERS_REFRESH_THRESHOLD = 12 * 60 * 60 * 1000;

	abstract protected Cursor fetchUsers();

	public abstract int getDatabaseType();

	public abstract String fetchMaxId();

	public abstract String fetchMinId();

	public abstract List<com.ch_linghu.fanfoudroid.fanfou.User> getUsers()
			throws HttpException;

	public abstract void addUsers(
			ArrayList<com.ch_linghu.fanfoudroid.data.User> tusers);

	// public abstract List<Status> getMessageSinceId(String maxId)
	// throws WeiboException;
	public abstract List<com.ch_linghu.fanfoudroid.fanfou.User> getUserSinceId(
			String maxId) throws HttpException;

	public abstract List<Status> getMoreMessageFromId(String minId)
			throws HttpException;

	public abstract Paging getNextPage();// 下一页数

	public abstract Paging getCurrentPage();// 当前页数

	protected abstract String[] getIds();

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

		cursor = fetchUsers(); //
		setTitle(getActivityTitle());
		startManagingCursor(cursor);

		mUserList = (ListView) findViewById(R.id.follower_list);

		// TODO: 需处理没有数据时的情况
		Log.d("LDS", cursor.getCount() + " cursor count");
		setupListHeader(true);

		mUserListAdapter = new UserCursorAdapter(this, cursor);
		mUserList.setAdapter(mUserListAdapter);
		// ? registerOnClickListener(mTweetList);
	}

	/**
	 * 绑定listView底部 - 载入更多 NOTE: 必须在listView#setAdapter之前调用
	 */
	protected void setupListHeader(boolean addFooter) {

		// Add footer to Listview
		View footer = View.inflate(this, R.layout.listview_footer, null);
		mUserList.addFooterView(footer, null, true);

		// Find View
		loadMoreBtn = (TextView) findViewById(R.id.ask_for_more);
		loadMoreGIF = (ProgressBar) findViewById(R.id.rectangleProgressBar);
		// loadMoreBtnTop = (TextView)findViewById(R.id.ask_for_more_header);
		// loadMoreGIFTop =
		// (ProgressBar)findViewById(R.id.rectangleProgressBar_header);

		// loadMoreAnimation = (AnimationDrawable)
		// loadMoreGIF.getIndeterminateDrawable();
	}

	@Override
	protected void specialItemClicked(int position) {
		if (position == mUserList.getCount() - 1) {
			// footer
			loadMoreGIF.setVisibility(View.VISIBLE);
			doGetMore();
		}
	}

	@Override
	protected int getLayoutId() {
		return R.layout.follower;
	}

	@Override
	protected ListView getUserList() {
		return mUserList;
	}

	@Override
	protected TweetAdapter getUserAdapter() {
		return mUserListAdapter;
	}

	@Override
	protected boolean useBasicMenu() {
		return true;
	}

	protected User getContextItemUser(int position) {
		// position = position - 1;
		// 加入footer跳过footer
		if (position < mUserListAdapter.getCount()) {
			Cursor cursor = (Cursor) mUserListAdapter.getItem(position);
			if (cursor == null) {
				return null;
			} else {
				return UserInfoTable.parseCursor(cursor);
			}
		} else {
			return null;
		}
	}

	@Override
	protected void updateTweet(Tweet tweet) {
		// TODO: updateTweet() 在哪里调用的? 目前尚只支持:
		// updateTweet(String tweetId, ContentValues values)
		// setFavorited(String tweetId, String isFavorited)
		// 看是否还需要增加updateTweet(Tweet tweet)方法

		// 对所有相关表的对应消息都进行刷新（如果存在的话）
		// getDb().updateTweet(TwitterDbAdapter.TABLE_FAVORITE, tweet);
		// getDb().updateTweet(TwitterDbAdapter.TABLE_MENTION, tweet);
		// getDb().updateTweet(TwitterDbAdapter.TABLE_TWEET, tweet);
	}

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate.");
		if (super._onCreate(savedInstanceState)) {

			goTop(); // skip the header

			boolean shouldRetrieve = false;

			// FIXME： 该子类页面全部使用了这个统一的计时器，导致进入Mention等分页面后经常不会自动刷新
			long lastRefreshTime = mPreferences.getLong(
					Preferences.LAST_TWEET_REFRESH_KEY, 0);
			long nowTime = DateTimeHelper.getNowTime();

			long diff = nowTime - lastRefreshTime;
			Log.d(TAG, "Last refresh was " + diff + " ms ago.");
			/*
			 * if (diff > REFRESH_THRESHOLD) { shouldRetrieve = true; } else if
			 * (Utils.isTrue(savedInstanceState, SIS_RUNNING_KEY)) { // Check to
			 * see if it was running a send or retrieve task. // It makes no
			 * sense to resend the send request (don't want dupes) // so we
			 * instead retrieve (refresh) to see if the message has // posted.
			 * Log.d(TAG,
			 * "Was last running a retrieve or send task. Let's refresh.");
			 * shouldRetrieve = true; }
			 */
			shouldRetrieve = true;
			if (shouldRetrieve) {
				doRetrieve();
			}

			long lastFollowersRefreshTime = mPreferences.getLong(
					Preferences.LAST_FOLLOWERS_REFRESH_KEY, 0);

			diff = nowTime - lastFollowersRefreshTime;
			Log.d(TAG, "Last followers refresh was " + diff + " ms ago.");

			/*
			 * if (diff > FOLLOWERS_REFRESH_THRESHOLD && (mRetrieveTask == null
			 * || mRetrieveTask.getStatus() != GenericTask.Status.RUNNING)) {
			 * Log.d(TAG, "Refresh followers."); doRetrieveFollowers(); }
			 */
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume.");
		if (lastPosition != 0) {
			mUserList.setSelection(lastPosition);
		}
		super.onResume();
		checkIsLogedIn();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
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
		Log.d(TAG, "onDestroy.");
		super.onDestroy();

		taskManager.cancelAll();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause.");
		super.onPause();
		lastPosition = mUserList.getFirstVisiblePosition();
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart.");
		super.onRestart();
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart.");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop.");
		super.onStop();
	}

	// UI helpers.

	@Override
	protected String getActivityTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void adapterRefresh() {
		mUserListAdapter.notifyDataSetChanged();
		mUserListAdapter.refresh();
	}

	// Retrieve interface
	public void updateProgress(String progress) {
		mProgressText.setText(progress);
	}

	public void draw() {
		mUserListAdapter.refresh();
	}

	public void goTop() {
		Log.d(TAG, "goTop.");
		mUserList.setSelection(1);
	}

	private void doRetrieveFollowers() {
		Log.d(TAG, "Attempting followers retrieve.");

		if (mFollowersRetrieveTask != null
				&& mFollowersRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mFollowersRetrieveTask = new FollowersRetrieveTask();
			mFollowersRetrieveTask.setListener(mFollowerRetrieveTaskListener);
			mFollowersRetrieveTask.execute();

			taskManager.addTask(mFollowersRetrieveTask);
			// Don't need to cancel FollowersTask (assuming it ends properly).
			mFollowersRetrieveTask.setCancelable(false);
		}
	}

	public void onRetrieveBegin() {
		updateProgress(getString(R.string.page_status_refreshing));
	}

	public void doRetrieve() {
		Log.d(TAG, "Attempting retrieve.");

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mRetrieveTask = new RetrieveTask();
			mRetrieveTask.setListener(mRetrieveTaskListener);
			mRetrieveTask.setFeedback(mFeedback);
			mRetrieveTask.execute();

			// Add Task to manager
			taskManager.addTask(mRetrieveTask);
		}
	}

	/**
	 * TODO：从API获取当前Followers，并同步到数据库
	 * 
	 * @author Dino
	 * 
	 */
	private class RetrieveTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {

			Log.d(TAG, "load RetrieveTask");

			List<com.ch_linghu.fanfoudroid.fanfou.User> usersList = null;
			try {
				usersList = getApi().getFollowersList(getUserId(),
						getCurrentPage());
			} catch (HttpException e) {
				e.printStackTrace();
			}
			publishProgress(SimpleFeedback.calProgressBySize(40, 20, usersList));

			ArrayList<User> users = new ArrayList<User>();

			for (com.ch_linghu.fanfoudroid.fanfou.User user : usersList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}

				users.add(User.create(user));

				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			addUsers(users);

			return TaskResult.OK;
		}

	}

	private class FollowersRetrieveTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			try {
				Log.d(TAG, "load FollowersErtrieveTask");
				List<com.ch_linghu.fanfoudroid.fanfou.User> t_users = getUsers();
				getDb().syncWeiboUsers(t_users);

			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}
			return TaskResult.OK;
		}
	}

	/**
	 * TODO:需要重写,获取下一批用户,按页分100页一次
	 * 
	 * @author Dino
	 * 
	 */
	private class GetMoreTask extends GenericTask {
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			Log.d(TAG, "load RetrieveTask");

			List<com.ch_linghu.fanfoudroid.fanfou.User> usersList = null;
			try {
				usersList = getApi().getFollowersList(getUserId(),
						getNextPage());

			} catch (HttpException e) {

				e.printStackTrace();
			}
			publishProgress(SimpleFeedback.calProgressBySize(40, 20, usersList));

			ArrayList<User> users = new ArrayList<User>();
			for (com.ch_linghu.fanfoudroid.fanfou.User user : usersList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}

				users.add(User.create(user));

				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			addUsers(users);

			return TaskResult.OK;
		}
	}

	private TaskListener getMoreListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "getMore";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			draw();
			loadMoreGIF.setVisibility(View.GONE);
		}

	};

	public void doGetMore() {
		Log.d(TAG, "Attempting getMore.");

		if (mGetMoreTask != null
				&& mGetMoreTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mGetMoreTask = new GetMoreTask();
			mGetMoreTask.setFeedback(mFeedback);
			mGetMoreTask.setListener(getMoreListener);
			mGetMoreTask.execute();

			// Add Task to manager
			taskManager.addTask(mGetMoreTask);
		}
	}

}