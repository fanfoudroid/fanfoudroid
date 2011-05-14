package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.utils.*;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.TwitterListBaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.MyListView;
import com.ch_linghu.fanfoudroid.ui.module.TweetAdapter;
import com.ch_linghu.fanfoudroid.ui.module.TweetArrayAdapter;
import com.ch_linghu.fanfoudroid.weibo.Query;
import com.ch_linghu.fanfoudroid.weibo.QueryResult;

public class SearchResultActivity extends TwitterListBaseActivity implements
		MyListView.OnNeedMoreListener {
	private static final String TAG = "SearchActivity";

	// Views.
	private MyListView mTweetList;

	// State.
	private String mSearchQuery;
	private ArrayList<Tweet> mTweets;
	private TweetArrayAdapter mAdapter;
	private int mNextPage = 1;
	private String mLastId = null;

	private static class State {
		State(SearchResultActivity activity) {
			mTweets = activity.mTweets;
			mNextPage = activity.mNextPage;
		}

		public ArrayList<Tweet> mTweets;
		public int mNextPage;
	}

	// Tasks.
	private GenericTask mSearchTask;
	
	private TaskListener mSearchTaskListener = new TaskAdapter(){
		@Override
		public void onPreExecute(GenericTask task) {
			animRotate(refreshButton);   
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
			refreshButton.clearAnimation();   
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				draw();
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
		if (super._onCreate(savedInstanceState)){

			Intent intent = getIntent();
			// Assume it's SEARCH.
			// String action = intent.getAction();
			mSearchQuery = intent.getStringExtra(SearchManager.QUERY);
	
			if (TextUtils.isEmpty(mSearchQuery)) {
				mSearchQuery = intent.getData().getLastPathSegment();
			}
	
			setHeaderTitle(mSearchQuery);
			setTitle(mSearchQuery);
	
	
			State state = (State) getLastNonConfigurationInstance();
	
			if (state != null) {
				mTweets = state.mTweets;
				draw();
			} else {
				doSearch();
			}
			
			return true;
		}else{
			return false;
		}
	}

	@Override
	protected int getLayoutId(){
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

	private void draw() {
		mAdapter.refresh(mTweets);
	}

	private void doSearch() {
		Log.d(TAG, "Attempting search.");
		
		if (mSearchTask != null && mSearchTask.getStatus() == GenericTask.Status.RUNNING){
			return;
		}else{
			mSearchTask = new SearchTask();
			mSearchTask.setListener(mSearchTaskListener);
			mSearchTask.execute();
		}
	}

	private class SearchTask extends GenericTask {
	
		ArrayList<Tweet> mTweets = new ArrayList<Tweet>();

		@Override
		protected TaskResult _doInBackground(TaskParams...params) {
			QueryResult result;

			try {
				Query query = new Query(mSearchQuery);
				if (!TextHelper.isEmpty(mLastId)){
					query.setMaxId(mLastId);
				}
				result = getApi().search(query);//.search(mSearchQuery, mNextPage);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			HashSet<String> imageUrls = new HashSet<String>();

			for (com.ch_linghu.fanfoudroid.weibo.Status status : result.getStatus()) {
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

//			if (isCancelled()) {
//				return TaskResult.CANCELLED;
//			}
//
//			publishProgress();
//
//			// TODO: what if orientation change?
//			ImageManager imageManager = getImageManager();
//			MemoryImageCache imageCache = new MemoryImageCache();
//
//			for (String imageUrl : imageUrls) {
//				if (!Utils.isEmpty(imageUrl)) {
//					// Fetch image to cache.
//					try {
//						Bitmap bitmap = imageManager.fetchImage(imageUrl);
//						imageCache.put(imageUrl, bitmap);
//					} catch (IOException e) {
//						Log.e(TAG, e.getMessage(), e);
//					}
//				}
//
//				if (isCancelled()) {
//					return TaskResult.CANCELLED;
//				}
//			}
//
//			addImages(imageCache);

			return TaskResult.OK;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void needMore() {
		if (!isLastPage()) {
			doSearch();
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
		return (Tweet)mAdapter.getItem(position);
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
		for (Tweet t : mTweets){
			if (t.id.equals(tweet.id)){
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
		mTweetList = (MyListView) findViewById(R.id.tweet_list);
		mAdapter = new TweetArrayAdapter(this);
		mTweetList.setAdapter(mAdapter);
		mTweetList.setOnNeedMoreListener(this);		
	}

	@Override
	public void doRetrieve() {
		doSearch();
	}


}
