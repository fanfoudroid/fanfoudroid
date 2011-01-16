package com.ch_linghu.fanfoudroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.ImageManager;
import com.ch_linghu.fanfoudroid.helper.MemoryImageCache;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.ui.base.TwitterListBaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.MyListView;
import com.ch_linghu.fanfoudroid.ui.module.TweetAdapter;
import com.ch_linghu.fanfoudroid.ui.module.TweetArrayAdapter;
import com.ch_linghu.fanfoudroid.weibo.Query;
import com.ch_linghu.fanfoudroid.weibo.QueryResult;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;
import com.google.android.photostream.UserTask;

public class SearchActivity extends TwitterListBaseActivity implements
		MyListView.OnNeedMoreListener {
	private static final String TAG = "SearchActivity";

	// Views.
	private MyListView mTweetList;

	// State.
	private String mSearchQuery;
	private ArrayList<Tweet> mTweets;
	private TweetArrayAdapter mAdapter;
	private MemoryImageCache mImageCache;
	private int mNextPage = 1;

	private static class State {
		State(SearchActivity activity) {
			mTweets = activity.mTweets;
			mNextPage = activity.mNextPage;
			mImageCache = activity.mImageCache;
		}

		public ArrayList<Tweet> mTweets;
		public int mNextPage;
		public MemoryImageCache mImageCache;
	}

	// Tasks.
	private UserTask<Void, Void, RetrieveResult> mSearchTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//setContentView(R.layout.search);

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
			mImageCache = state.mImageCache;
			draw();
		} else {
			doSearch();
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
		Log.i(TAG, "onDestroy.");

		if (mSearchTask != null
				&& mSearchTask.getStatus() == UserTask.Status.RUNNING) {
			mSearchTask.cancel(true);
		}

		super.onDestroy();
	}

	// UI helpers.

	private void updateProgress(String progress) {
		mProgressText.setText(progress);
	}

	private void draw() {
		mAdapter.refresh(mTweets, mImageCache);
	}

	private enum RetrieveResult {
		OK, IO_ERROR, AUTH_ERROR, CANCELLED
	}

	private void doSearch() {
		Log.i(TAG, "Attempting search.");

		if (mSearchTask != null
				&& mSearchTask.getStatus() == UserTask.Status.RUNNING) {
			Log.w(TAG, "Already searching.");
		} else {
			mSearchTask = new SearchTask().execute();
		}
	}

	private class SearchTask extends UserTask<Void, Void, RetrieveResult> {
		@Override
		public void onPreExecute() {
			if (mNextPage == 1) {
				updateProgress(getString(R.string.page_status_refreshing));
			} else {
				updateProgress(getString(R.string.page_status_refreshing));
			}
		}

		ArrayList<Tweet> mTweets = new ArrayList<Tweet>();

		@Override
		public RetrieveResult doInBackground(Void... params) {
			QueryResult result;

			try {
				Query query = new Query(mSearchQuery);
				query.setPage(mNextPage);
				result = getApi().search(query);//.search(mSearchQuery, mNextPage);
			} catch (WeiboException e) {
				Log.e(TAG, e.getMessage(), e);
				return RetrieveResult.IO_ERROR;
			}

			HashSet<String> imageUrls = new HashSet<String>();

			for (com.ch_linghu.fanfoudroid.weibo.Status status : result.getStatus()) {
				if (isCancelled()) {
					return RetrieveResult.CANCELLED;
				}

				Tweet tweet;

				tweet = Tweet.create(status);
				mTweets.add(tweet);
				imageUrls.add(tweet.profileImageUrl);

				if (isCancelled()) {
					return RetrieveResult.CANCELLED;
				}
			}

			addTweets(mTweets);

			if (isCancelled()) {
				return RetrieveResult.CANCELLED;
			}

			publishProgress();

			// TODO: what if orientation change?
			ImageManager imageManager = getImageManager();
			MemoryImageCache imageCache = new MemoryImageCache();

			for (String imageUrl : imageUrls) {
				if (!Utils.isEmpty(imageUrl)) {
					// Fetch image to cache.
					try {
						Bitmap bitmap = imageManager.fetchImage(imageUrl);
						imageCache.put(imageUrl, bitmap);
					} catch (IOException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}

				if (isCancelled()) {
					return RetrieveResult.CANCELLED;
				}
			}

			addImages(imageCache);

			return RetrieveResult.OK;
		}

		@Override
		public void onProgressUpdate(Void... progress) {
			draw();
		}

		@Override
		public void onPostExecute(RetrieveResult result) {
			if (result == RetrieveResult.AUTH_ERROR) {
				logout();
			} else if (result == RetrieveResult.OK) {
				draw();
			} else {
				// Do nothing.
			}

			updateProgress("");
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

	private synchronized void addImages(MemoryImageCache imageCache) {
		if (mImageCache == null) {
			mImageCache = imageCache;
		} else {
			mImageCache.putAll(imageCache);
		}
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
		mAdapter = new TweetArrayAdapter(this, mImageCache);
		mTweetList.setAdapter(mAdapter);
		mTweetList.setOnNeedMoreListener(this);		
	}


}
