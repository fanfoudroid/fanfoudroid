package com.ch_linghu.android.fanfoudroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import com.google.android.photostream.UserTask;

public class SearchActivity extends BaseActivity implements MyListView.OnNeedMoreListener {
  private static final String TAG = "SearchActivity";

  // Views.
  private MyListView mTweetList;
  private TextView mProgressText;

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

    if (!getApi().isLoggedIn()) {
      Log.i(TAG, "Not logged in.");
      handleLoggedOut();
      return;
    }

    setContentView(R.layout.search);

    Intent intent = getIntent();
    // Assume it's SEARCH.
    // String action = intent.getAction();
    mSearchQuery = intent.getStringExtra(SearchManager.QUERY);

    if (TextUtils.isEmpty(mSearchQuery)) {
      mSearchQuery = intent.getData().getLastPathSegment();
    }

    setTitle(mSearchQuery);

    mTweets = new ArrayList<Tweet>();
    mTweetList = (MyListView) findViewById(R.id.tweet_list);
    mAdapter = new TweetArrayAdapter(this, mImageCache);
    mTweetList.setAdapter(mAdapter);
    registerForContextMenu(mTweetList);
    mTweetList.setOnNeedMoreListener(this);

    mProgressText = (TextView) findViewById(R.id.progress_text);

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
  protected void onResume() {
    super.onResume();

    if (!getApi().isLoggedIn()) {
      Log.i(TAG, "Not logged in.");
      handleLoggedOut();
      return;
    }
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

    if (mSearchTask != null && mSearchTask.getStatus() == UserTask.Status.RUNNING) {
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
        updateProgress("Searching...");
      } else {
        updateProgress("Getting more...");
      }
    }

    ArrayList<Tweet> mTweets = new ArrayList<Tweet>();

    @Override
    public RetrieveResult doInBackground(Void... params) {
      JSONArray jsonArray;

      try {
        jsonArray = getApi().search(mSearchQuery, mNextPage);
      } catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
        return RetrieveResult.IO_ERROR;
      } catch (FanfouException e) {
        Log.e(TAG, e.getMessage(), e);
        return RetrieveResult.IO_ERROR;
      }

      HashSet<String> imageUrls = new HashSet<String>();

      for (int i = 0; i < jsonArray.length(); ++i) {
        if (isCancelled()) {
          return RetrieveResult.CANCELLED;
        }

        Tweet tweet;

        try {
          JSONObject jsonObject = jsonArray.getJSONObject(i);
          tweet = Tweet.create(jsonObject);
          mTweets.add(tweet);
          imageUrls.add(tweet.profileImageUrl);
        } catch (JSONException e) {
          Log.e(TAG, e.getMessage(), e);
          return RetrieveResult.IO_ERROR;
        }

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
    MenuItem item = menu.add(0, OPTIONS_MENU_ID_REFRESH, 0, R.string.refresh);
    item.setIcon(R.drawable.refresh);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case OPTIONS_MENU_ID_REFRESH:
      doSearch();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private static final int CONTEXT_MORE_ID = 3;
  private static final int CONTEXT_REPLY_ID = 0;
  private static final int CONTEXT_RETWEET_ID = 1;
  @SuppressWarnings("unused")
  private static final int CONTEXT_DM_ID = 2;

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    Tweet tweet = (Tweet) mAdapter.getItem(info.position);
    menu.add(0, CONTEXT_MORE_ID, 0, tweet.screenName);
    menu.add(0, CONTEXT_REPLY_ID, 0, R.string.reply);
    menu.add(0, CONTEXT_RETWEET_ID, 0, R.string.retweet);

    /*
    MenuItem item = menu.add(0, CONTEXT_DM_ID, 0, R.string.dm);
    item.setEnabled(mIsFollower);
    */
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    Tweet tweet = (Tweet) mAdapter.getItem(info.position);

    if (tweet == null) {
      Log.w(TAG, "Selected item not available.");
      return super.onContextItemSelected(item);
    }

    switch (item.getItemId()) {
    case CONTEXT_MORE_ID:
      launchActivity(UserActivity.createIntent(tweet.screenName));
      return true;
    case CONTEXT_REPLY_ID:
      String replyTo = "@" + tweet.screenName + " ";
      launchNewTweetActivity(replyTo);
      return true;
    case CONTEXT_RETWEET_ID:
      String retweet = "RT @" + tweet.screenName + " " + tweet.text;
      launchNewTweetActivity(retweet);
      return true;
    /*
    case CONTEXT_DM_ID:
      launchActivity(DmActivity.createIntent(mUsername));
      return true;
      */
    default:
      return super.onContextItemSelected(item);
    }
  }

  private void launchNewTweetActivity(String text) {
    launchActivity(WriteActivity.createNewTweetIntent(text));
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

}
