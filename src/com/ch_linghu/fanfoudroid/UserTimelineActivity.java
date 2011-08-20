package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.fanfou.Paging;
import com.ch_linghu.fanfoudroid.fanfou.User;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.http.HttpRefusedException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.TwitterListBaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.Feedback;
import com.ch_linghu.fanfoudroid.ui.module.FeedbackFactory;
import com.ch_linghu.fanfoudroid.ui.module.FeedbackFactory.FeedbackType;
import com.ch_linghu.fanfoudroid.ui.module.TweetArrayAdapter;
import com.ch_linghu.fanfoudroid.R;
import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

public class UserTimelineActivity extends TwitterListBaseActivity {
	
	private static class State {
		State(UserTimelineActivity activity) {
			mTweets = activity.mTweets;
			mMaxId = activity.mMaxId;
		}

		public ArrayList<Tweet> mTweets;
		public String mMaxId;
	}
	
	private static final String TAG = UserTimelineActivity.class
			.getSimpleName();

	private Feedback mFeedback;

	private static final String EXTRA_USERID = "userID";
	private static final String EXTRA_NAME_SHOW = "showName";
	private static final String SIS_RUNNING_KEY = "running";

	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.USERTIMELINE";

	public static Intent createIntent(String userID, String showName) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.putExtra(EXTRA_USERID, userID);
		intent.putExtra(EXTRA_NAME_SHOW, showName);
		return intent;
	}

	// State.
	private User mUser;
	private String mUserID;
	private String mShowName;
	private ArrayList<Tweet> mTweets = new ArrayList<Tweet>();
	private String mMaxId = "";

	// Views.
	private View footerView;
	private PullToRefreshListView mTweetList;
	private ProgressBar loadMoreGIF;
	private TweetArrayAdapter mAdapter;
	
	// 记录服务器拒绝访问的信息
	private String msg;
	private static final int LOADINGFLAG = 1;
	private static final int SUCCESSFLAG = 2;
	private static final int NETWORKERRORFLAG = 3;
	private static final int AUTHERRORFLAG = 4;

	// Tasks.
	private GenericTask mRetrieveTask;
	private GenericTask mLoadMoreTask;

	private TaskListener mRetrieveTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveBegin();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			mTweetList.onRefreshComplete();
			
			if (result == TaskResult.AUTH_ERROR) {
				mFeedback.failed("登录失败, 请重新登录.");
				return;
			} else if (result == TaskResult.OK) {
				draw();
				goTop();
		} else if (result == TaskResult.IO_ERROR) {
				mFeedback.failed("更新失败.");
			}
			mFeedback.success("");
		}

		@Override
		public String getName() {
			return "UserTimelineRetrieve";
		}
	};

	private TaskListener mLoadMoreTaskListener = new TaskAdapter() {

		@Override
		public void onPreExecute(GenericTask task) {
	        loadMoreGIF.setVisibility(View.VISIBLE);
			onLoadMoreBegin();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
	        loadMoreGIF.setVisibility(View.GONE);
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				mFeedback.success("");
				draw();
			}
		}

		@Override
		public String getName() {
			return "UserTimelineLoadMoreTask";
		}
	};

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "_onCreate()...");
		if (super._onCreate(savedInstanceState)) {
			mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);

			Intent intent = getIntent();
			// get user id
			mUserID = intent.getStringExtra(EXTRA_USERID);
			// show username in title
			mShowName = intent.getStringExtra(EXTRA_NAME_SHOW);

			// Set header title
			mNavbar.setHeaderTitle("@" + mShowName);

			boolean wasRunning = isTrue(savedInstanceState, SIS_RUNNING_KEY);

			State state = (State) getLastNonConfigurationInstance();

			if (state != null) {
				// 此处要求mTweets不为空，最好确保profile页面消息为0时不能进入这个页面
				mTweets = state.mTweets;
				mMaxId = state.mMaxId;
				if (!mTweets.isEmpty() && !wasRunning) {
					draw();
				}
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
		Log.d(TAG, "onDestroy.");
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

	@Override
	protected void draw() {
		mAdapter.refresh(mTweets);
	}

	public void goTop() {
		Log.d(TAG, "goTop.");
		mTweetList.setSelection(1);
	}

	public void doRetrieve() {
		Log.d(TAG, "Attempting retrieve.");
		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mRetrieveTask = new UserTimelineRetrieveTask();
			mRetrieveTask.setListener(mRetrieveTaskListener);
			mRetrieveTask.execute();
		}
	}

	private void doLoadMore() {
		Log.d(TAG, "Attempting load more.");

		if (mLoadMoreTask != null
				&& mLoadMoreTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mLoadMoreTask = new UserTimelineLoadMoreTask();
			mLoadMoreTask.setListener(mLoadMoreTaskListener);
			mLoadMoreTask.execute();
		}
	}

	private void onRetrieveBegin() {
		mFeedback.start("");
		mTweetList.prepareForRefresh();
		// 更新查询状态显示
	}

	private void onLoadMoreBegin() {
		mFeedback.start("");
	}

	private class UserTimelineRetrieveTask extends GenericTask {
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			List<com.ch_linghu.fanfoudroid.fanfou.Status> statusList;
			try {
				statusList = getApi().getUserTimeline(mUserID);
				mUser = getApi().showUser(mUserID);
				mFeedback.update(60);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				Throwable cause = e.getCause();
				if (cause instanceof HttpRefusedException) {
					// AUTH ERROR
					msg = ((HttpRefusedException) cause).getError()
							.getMessage();
					return TaskResult.AUTH_ERROR;
				} else {
					return TaskResult.IO_ERROR;
				}
			}
			mFeedback.update(100 - (int) Math.floor(statusList.size() * 2)); // 60~100
			mTweets.clear();
			for (com.ch_linghu.fanfoudroid.fanfou.Status status : statusList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				Tweet tweet;
				tweet = Tweet.create(status);
				mMaxId = tweet.id;
				mTweets.add(tweet);
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}
			return TaskResult.OK;
		}
	}

	private class UserTimelineLoadMoreTask extends GenericTask {
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			List<com.ch_linghu.fanfoudroid.fanfou.Status> statusList;
			try {
				Paging paging = new Paging();
				paging.setMaxId(mMaxId);
				statusList = getApi().getUserTimeline(mUserID, paging);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				Throwable cause = e.getCause();
				if (cause instanceof HttpRefusedException) {
					// AUTH ERROR
					msg = ((HttpRefusedException) cause).getError()
							.getMessage();
					return TaskResult.AUTH_ERROR;
				} else {
					return TaskResult.IO_ERROR;
				}
			}

			for (com.ch_linghu.fanfoudroid.fanfou.Status status : statusList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				Tweet tweet;
				tweet = Tweet.create(status);
				mMaxId = tweet.id;
				mTweets.add(tweet);
			}
			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}
			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}
			return TaskResult.OK;
		}
	}

	public void doGetMore() {
		doLoadMore();
	}

	@Override
	protected String getActivityTitle() {
		return "@" + mShowName;
	}

	@Override
	protected Tweet getContextItemTweet(int position) {

		if (position >= 1 && position <= mAdapter.getCount()) {
			return (Tweet) mAdapter.getItem(position - 1);
		} else {
			return null;
		}
	}

	@Override
	protected int getLayoutId() {
		return R.layout.user_timeline;
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
		mTweetList = (PullToRefreshListView) findViewById(R.id.tweet_list);
		mTweetList.setAdapter(mAdapter);

    	mTweetList.setOnRefreshListener(new OnRefreshListener(){
    		@Override
    		public void onRefresh(){
    			doRetrieve();
    		}
    	});

		// Add Footer to ListView
		footerView = (View)View.inflate(this,
				R.layout.listview_footer, null);
		mTweetList.addFooterView(footerView);
        loadMoreGIF = (ProgressBar) findViewById(R.id.rectangleProgressBar);
	}

	@Override
	protected void updateTweet(Tweet tweet) {
		// 该方法作用？
	}

	@Override
	protected boolean useBasicMenu() {
		return true;
	}

    @Override
    protected void specialItemClicked(int position) {
        // 注意 mTweetAdapter.getCount 和 mTweetList.getCount的区别
        // 前者仅包含数据的数量（不包括foot和head），后者包含foot和head
        // 因此在同时存在foot和head的情况下，list.count = adapter.count + 2
        if (position == 0) {
            doRetrieve();
        } else if (position == mTweetList.getCount() - 1) {
            // 最后一个Item(footer)
            doGetMore();
        }
    }
}