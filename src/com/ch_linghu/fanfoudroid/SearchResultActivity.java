package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.fanfou.Query;
import com.ch_linghu.fanfoudroid.fanfou.QueryResult;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.TwitterListBaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.SimpleFeedback;
import com.ch_linghu.fanfoudroid.ui.module.TweetAdapter;
import com.ch_linghu.fanfoudroid.ui.module.TweetArrayAdapter;
import com.ch_linghu.fanfoudroid.R;
import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

public class SearchResultActivity extends TwitterListBaseActivity {
	private static final String TAG = "SearchActivity";

	// Views.
	private PullToRefreshListView mTweetList;
	private View mListFooter;
	private ProgressBar loadMoreGIF;
	

	// State.
	private String mSearchQuery;
	private ArrayList<Tweet> mTweets;
	private TweetArrayAdapter mAdapter;
	private int mNextPage = 1;
	private String mLastId = null;
	private boolean mIsGetMore = false;

	private static class State {
		State(SearchResultActivity activity) {
			mTweets = activity.mTweets;
			mNextPage = activity.mNextPage;
			mLastId = activity.mLastId;
		}

		public ArrayList<Tweet> mTweets;
		public int mNextPage;
		public String mLastId;
	}

	// Tasks.
	private GenericTask mSearchTask;

	private TaskListener mSearchTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			if (mIsGetMore){
				loadMoreGIF.setVisibility(View.VISIBLE);
			}

			if (mNextPage == 1) {
				updateProgress(getString(R.string.page_status_refreshing));
			} else {
				updateProgress(getString(R.string.page_status_refreshing));
			}
		}

		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
			draw();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
            loadMoreGIF.setVisibility(View.GONE);
			mTweetList.onRefreshComplete();

			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				draw();
				if (!mIsGetMore){
					mTweetList.setSelection(1);
				}
			} else {
				// Do nothing.
			}

			updateProgress("");
		}

		@Override
		public String getName() {
			return "SearchTask";
		}
	};

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (super._onCreate(savedInstanceState)) {

			Intent intent = getIntent();
			// Assume it's SEARCH.
			// String action = intent.getAction();
			mSearchQuery = intent.getStringExtra(SearchManager.QUERY);

			if (TextUtils.isEmpty(mSearchQuery)) {
				mSearchQuery = intent.getData().getLastPathSegment();
			}

			mNavbar.setHeaderTitle(mSearchQuery);
			setTitle(mSearchQuery);

			State state = (State) getLastNonConfigurationInstance();

			if (state != null) {
				mTweets = state.mTweets;
				mNextPage = state.mNextPage;
				mLastId = state.mLastId;
				draw();
			} else {
				doSearch(false);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	protected int getLayoutId() {
		return R.layout.main;
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
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy.");

		if (mSearchTask != null
				&& mSearchTask.getStatus() == GenericTask.Status.RUNNING) {
			mSearchTask.cancel(true);
		}

		super.onDestroy();
	}

	// UI helpers.

	private void updateProgress(String progress) {
		mProgressText.setText(progress);
	}

	@Override
	protected void draw() {
		mAdapter.refresh(mTweets);
	}

	private void doSearch(boolean isGetMore) {
		Log.d(TAG, "Attempting search.");

		if (mSearchTask != null
				&& mSearchTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mIsGetMore = isGetMore;
			mSearchTask = new SearchTask();
			mSearchTask.setFeedback(mFeedback);
			mSearchTask.setListener(mSearchTaskListener);
			mSearchTask.execute();
		}
	}

	private class SearchTask extends GenericTask {

		ArrayList<Tweet> mTweets = new ArrayList<Tweet>();

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			QueryResult result;

			try {
				Query query = new Query(mSearchQuery);
				if (!TextUtils.isEmpty(mLastId)) {
					query.setMaxId(mLastId);
				}
				result = getApi().search(query);// .search(mSearchQuery,
												// mNextPage);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}
			List<com.ch_linghu.fanfoudroid.fanfou.Status> statuses = result
					.getStatus();
			HashSet<String> imageUrls = new HashSet<String>();

			publishProgress(SimpleFeedback.calProgressBySize(40, 20, statuses));

			for (com.ch_linghu.fanfoudroid.fanfou.Status status : statuses) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}

				Tweet tweet;

				tweet = Tweet.create(status);
				mLastId = tweet.id;
				mTweets.add(tweet);
				imageUrls.add(tweet.profileImageUrl);

				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}

			addTweets(mTweets);

			// if (isCancelled()) {
			// return TaskResult.CANCELLED;
			// }
			//
			// publishProgress();
			//
			// // TODO: what if orientation change?
			// ImageManager imageManager = getImageManager();
			// MemoryImageCache imageCache = new MemoryImageCache();
			//
			// for (String imageUrl : imageUrls) {
			// if (!Utils.isEmpty(imageUrl)) {
			// // Fetch image to cache.
			// try {
			// Bitmap bitmap = imageManager.fetchImage(imageUrl);
			// imageCache.put(imageUrl, bitmap);
			// } catch (IOException e) {
			// Log.e(TAG, e.getMessage(), e);
			// }
			// }
			//
			// if (isCancelled()) {
			// return TaskResult.CANCELLED;
			// }
			// }
			//
			// addImages(imageCache);

			return TaskResult.OK;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	public void doGetMore() {
		if (!isLastPage()) {
			doSearch(true);
		}
	}

	public boolean isLastPage() {
		return mNextPage == -1;
	}

	@Override
	protected void adapterRefresh() {
		mAdapter.refresh(mTweets);
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
		return mSearchQuery;
	}

	@Override
	protected Tweet getContextItemTweet(int position) {
		if (position > 0 && position < mAdapter.getCount()) {
			Tweet item = (Tweet) mAdapter.getItem(position - 1);
			if (item == null) {
				return null;
			} else {
				return item;
			}
		} else {
			return null;
		}
	}

	@Override
	protected TweetAdapter getTweetAdapter() {
		return mAdapter;
	}

	@Override
	protected ListView getTweetList() {
		return mTweetList;
	}

	@Override
	protected void updateTweet(Tweet tweet) {
		// TODO Simple and stupid implementation
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

	@Override
	protected void setupState() {
		mTweets = new ArrayList<Tweet>();
		mTweetList = (PullToRefreshListView) findViewById(R.id.tweet_list);
		mAdapter = new TweetArrayAdapter(this);
		mTweetList.setAdapter(mAdapter);

    	mTweetList.setOnRefreshListener(new OnRefreshListener(){
    		@Override
    		public void onRefresh(){
    			doRetrieve();
    		}
    	});
    	
        // Add Footer to ListView
        mListFooter = View.inflate(this, R.layout.listview_footer, null);
        mTweetList.addFooterView(mListFooter, null, true);
        loadMoreGIF = (ProgressBar) findViewById(R.id.rectangleProgressBar);
	}

	@Override
	public void doRetrieve() {
		doSearch(false);
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
