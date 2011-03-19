package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.Refreshable;
import com.ch_linghu.fanfoudroid.ui.base.TwitterListBaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.MyListView;
import com.ch_linghu.fanfoudroid.ui.module.TweetArrayAdapter;
import com.ch_linghu.fanfoudroid.weibo.Paging;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

public class UserActivity extends TwitterListBaseActivity implements
		MyListView.OnNeedMoreListener, Refreshable {

	private static final String TAG = "UserActivity";

	// State.
	private String mUsername;
	private String mScreenName;
	private ArrayList<Tweet> mTweets;
	private int mNextPage = 1;

	private static class State {
		State(UserActivity activity) {
			mTweets = activity.mTweets;
			mNextPage = activity.mNextPage;
		}

		public ArrayList<Tweet> mTweets;
		public int mNextPage;
	}

	// Views.
	private TextView headerView;
	private MyListView mTweetList;
	private static final int LOADINGFLAG = 1;
	private static final int SUCCESSFLAG = 2;
	private static final int NETWORKERRORFLAG = 3;
	private static final int AUTHERRORFLAG = 4;
	private TweetArrayAdapter mAdapter;

	// Tasks.
	private GenericTask mRetrieveTask;
	private GenericTask mLoadMoreTask;

	private TaskListener mRetrieveTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveBegin();
		}

		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
			draw();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			refreshButton.clearAnimation();
			if (result == TaskResult.AUTH_ERROR) {
				updateHeader(AUTHERRORFLAG);
				return;
			} else if (result == TaskResult.OK) {
				updateHeader(SUCCESSFLAG);
				draw();
			} else if (result == TaskResult.IO_ERROR) {
				updateHeader(NETWORKERRORFLAG);
			}

			updateProgress("");
		}

		@Override
		public String getName() {
			return "UserRetrieve";
		}
	};

	private void updateHeader(int flag) {
		if (flag == LOADINGFLAG) {
			//重新刷新页面时从第一页开始获取数据 --- phoenix
			mNextPage = 1;
			mTweets.clear();
			mAdapter.refresh(mTweets);
			headerView.setText(getResources()
					.getString(R.string.search_loading));
		}
		if (flag == SUCCESSFLAG) {
			headerView.setText(getResources().getString(
					R.string.user_query_status_success));
		}
		if (flag == NETWORKERRORFLAG) {
			headerView.setText(getResources().getString(
					R.string.login_status_network_or_connection_error));
		}
		if (flag == AUTHERRORFLAG) {
			headerView
					.setText(getResources()
							.getString(
									R.string.user_prompt_this_person_has_protected_their_updates));
		}
	}

	private TaskListener mLoadMoreTaskListener = new TaskAdapter() {

		@Override
		public void onPreExecute(GenericTask task) {
			onLoadMoreBegin();
		}

		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
			draw();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				refreshButton.clearAnimation();
				draw();
			} else {
				// Do nothing.
			}

			updateProgress("");
		}

		@Override
		public String getName() {
			return "UserLoadMoreTask";
		}
	};

	private static final String EXTRA_USER = "user";
	private static final String EXTRA_NAME_SCREEN = "name";

	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.USER";

	public static Intent createIntent(String user, String name) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.putExtra(EXTRA_USER, user);
		intent.putExtra(EXTRA_NAME_SCREEN, name);

		return intent;
	}

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "_onCreate()...");
		if (super._onCreate(savedInstanceState)) {

			Intent intent = getIntent();
			Uri data = intent.getData();

			// Input username
			mUsername = intent.getStringExtra(EXTRA_USER);
			mScreenName = intent.getStringExtra(EXTRA_NAME_SCREEN);

			if (TextUtils.isEmpty(mUsername)) {
				mUsername = data.getLastPathSegment();
			}

			// Set header title
			String header_title = (!TextUtils.isEmpty(mScreenName)) ? mScreenName
					: mUsername;
			setHeaderTitle("@" + header_title);

			setTitle("@" + mUsername);

			State state = (State) getLastNonConfigurationInstance();

			boolean wasRunning = Utils.isTrue(savedInstanceState,
					SIS_RUNNING_KEY);

			if (state != null && !wasRunning) {
				mTweets = state.mTweets;
				mNextPage = state.mNextPage;
				updateHeader(SUCCESSFLAG);
				draw();
			} else {
				doRetrieve();
			}

			return true;
		} else {
			return false;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		checkIsLogedIn();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return createState();
	}

	private synchronized State createState() {
		return new State(this);
	}

	private static final String SIS_RUNNING_KEY = "running";

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy.");

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			mRetrieveTask.cancel(true);
		}

		if (mLoadMoreTask != null
				&& mLoadMoreTask.getStatus() == GenericTask.Status.RUNNING) {
			mLoadMoreTask.cancel(true);
		}

		super.onDestroy();
	}

	// UI helpers.

	private void updateProgress(String progress) {
		mProgressText.setText(progress);
	}

	private void draw() {
		mAdapter.refresh(mTweets);
	}

	public void doRetrieve() {
		Log.i(TAG, "Attempting retrieve.");

		// 旋转刷新按钮
		animRotate(refreshButton);
		//更新查询状态显示
		updateHeader(LOADINGFLAG);

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mRetrieveTask = new UserRetrieveTask();
			mRetrieveTask.setListener(mRetrieveTaskListener);
			mRetrieveTask.execute();
		}
	}

	private void doLoadMore() {
		Log.i(TAG, "Attempting load more.");

		if (mLoadMoreTask != null
				&& mLoadMoreTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mLoadMoreTask = new UserLoadMoreTask();
			mLoadMoreTask.setListener(mLoadMoreTaskListener);
			mLoadMoreTask.execute();
		}
	}

	private void onRetrieveBegin() {
		updateProgress(getString(R.string.page_status_refreshing));
	}

	private void onLoadMoreBegin() {
		updateProgress(getString(R.string.page_status_refreshing));
		animRotate(refreshButton);
	}

	private class UserRetrieveTask extends GenericTask {
		ArrayList<Tweet> mTweets = new ArrayList<Tweet>();

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			List<com.ch_linghu.fanfoudroid.weibo.Status> statusList;

			try {
				statusList = getApi().getUserTimeline(mUsername,
						new Paging(mNextPage));
			} catch (WeiboException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			for (com.ch_linghu.fanfoudroid.weibo.Status status : statusList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}

				Tweet tweet;

				tweet = Tweet.create(status);
				mTweets.add(tweet);

				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}

			addTweets(mTweets);

			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}

			publishProgress();

			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}

			return TaskResult.OK;
		}
	}

	private class UserLoadMoreTask extends GenericTask {
		ArrayList<Tweet> mTweets = new ArrayList<Tweet>();

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			List<com.ch_linghu.fanfoudroid.weibo.Status> statusList;

			try {
				statusList = getApi().getUserTimeline(mUsername,
						new Paging(mNextPage));
			} catch (WeiboException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			for (com.ch_linghu.fanfoudroid.weibo.Status status : statusList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}

				Tweet tweet;

				tweet = Tweet.create(status);
				mTweets.add(tweet);
			}

			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}

			addTweets(mTweets);

			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}

			return TaskResult.OK;
		}
	}

	@Override
	public void needMore() {
		if (!isLastPage()) {
			doLoadMore();
		}
	}

	public boolean isLastPage() {
		return mNextPage == -1;
	}

	private synchronized void addTweets(ArrayList<Tweet> tweets) {
		if (tweets.size() == 0) {
			mNextPage = -1;
			return;
		}

		mTweets.addAll(tweets);

		++mNextPage;
	}

	@Override
	protected String getActivityTitle() {
		return "@" + mUsername;
	}

	@Override
	protected Tweet getContextItemTweet(int position) {
		if (position >= 1) {
			return (Tweet) mAdapter.getItem(position - 1);
		} else {
			return null;
		}
	}

	@Override
	protected int getLayoutId() {
		return R.layout.user;
	}

	@Override
	protected com.ch_linghu.fanfoudroid.ui.module.TweetAdapter getTweetAdapter() {
		return mAdapter;
	}

	@Override
	protected ListView getTweetList() {
		return mTweetList;
	}

	@Override
	protected void setupState() {
		mTweets = new ArrayList<Tweet>();
		mAdapter = new TweetArrayAdapter(this);
		mTweetList = (MyListView) findViewById(R.id.tweet_list);
		// Add Header to ListView
		headerView = (TextView) TextView.inflate(this, R.layout.user_header,
				null);
		mTweetList.addHeaderView(headerView);
		mTweetList.setAdapter(mAdapter);
		mTweetList.setOnNeedMoreListener(this);
	}

	@Override
	protected void updateTweet(Tweet tweet) {
		for (Tweet t : mTweets) {
			if (t.id.equals(tweet.id)) {
				t.favorited = tweet.favorited;
				break;
			}
		}
	}

	@Override
	protected boolean useBasicMenu() {
		return true;
	}

}