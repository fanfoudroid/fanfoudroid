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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.ch_linghu.android.fanfoudroid.TwitterApi.ApiException;
import com.ch_linghu.android.fanfoudroid.TwitterApi.AuthException;
import com.google.android.photostream.UserTask;

public class MentionActivity extends WithHeaderActivity implements Refreshable {
  private static final String TAG = "MentActivity";

  // Views.
  private ListView mTweetList;
  private TweetAdapter mTweetAdapter;

  private TweetEdit mTweetEdit;
  private ImageButton mSendButton;

  private TextView mProgressText;

  // State.
  private int mState;

  private static final int STATE_ALL = 0;
  private static final int STATE_REPLIES = 1;

  // Tasks.
  private UserTask<Void, Void, RetrieveResult> mRetrieveTask;
  
  private UserTask<Void, Void, RetrieveResult> mFollowersRetrieveTask;
  private UserTask<String, Void, SendResult> mFavTask;

  // Refresh data at startup if last refresh was this long ago or greater.
  private static final long REFRESH_THRESHOLD = 5 * 60 * 1000;

  // Refresh followers if last refresh was this long ago or greater.
  private static final long FOLLOWERS_REFRESH_THRESHOLD = 12 * 60 * 60 * 1000;

  private static final String LAUNCH_ACTION = "com.ch_linghu.android.fanfoudroid.TWEETS";
    
  static final int DIALOG_WRITE_ID = 0;
  
  public static Intent createIntent(Context context) {
    Intent intent = new Intent(LAUNCH_ACTION);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    return intent;
  }

  public static Intent createNewTaskIntent(Context context) {
    Intent intent = createIntent(context);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    return intent;
  }


  private boolean isReplies() {
    return mState == STATE_REPLIES;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!getApi().isLoggedIn()) {
      Log.i(TAG, "Not logged in.");
      handleLoggedOut();
      return;
    }

    setContentView(R.layout.main);
    initHeader(HEADER_STYLE_HOME, this);

    mState = mPreferences.getInt(
        Preferences.TWITTER_ACTIVITY_STATE_KEY, STATE_ALL);

    mTweetList = (ListView) findViewById(R.id.tweet_list);


//  });
    
    // 提示栏
    mProgressText = (TextView) findViewById(R.id.progress_text);
   
    // Mark all as read.
    getDb().markAllMentionsRead();

    setupState();

    registerForContextMenu(mTweetList);

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
      // so we instead retrieve (refresh) to see if the message has posted.
      Log.i(TAG, "Was last running a retrieve or send task. Let's refresh.");
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

    mTweetEdit.updateCharsRemain();
  }

  @Override
  protected void onDestroy() {
    Log.i(TAG, "onDestroy.");

    if (mRetrieveTask != null
        && mRetrieveTask.getStatus() == UserTask.Status.RUNNING) {
      mRetrieveTask.cancel(true);
    }

    if (mFavTask != null
            && mFavTask.getStatus() == UserTask.Status.RUNNING) {
          mFavTask.cancel(true);
    }
    
    // Don't need to cancel FollowersTask (assuming it ends properly).

    super.onDestroy();
  }

  // UI helpers.

  private void updateProgress(String progress) {
    mProgressText.setText(progress);
  }

  private void setupState() {
    Cursor cursor;

    cursor = getDb().fetchMentions();
    setTitle("@" + getApi().getUsername());

    startManagingCursor(cursor);

    mTweetAdapter = new TweetAdapter(this, cursor);
    mTweetList.setAdapter(mTweetAdapter);
  }

  private static final int CONTEXT_REPLY_ID   = Menu.FIRST + 1;
//  private static final int CONTEXT_AT_ID 	  = Menu.FIRST + 2;
  private static final int CONTEXT_RETWEET_ID = Menu.FIRST + 3;
  private static final int CONTEXT_DM_ID 	  = Menu.FIRST + 4;
  private static final int CONTEXT_MORE_ID 	  = Menu.FIRST + 5;
  private static final int CONTEXT_ADD_FAV_ID = Menu.FIRST + 6;
  private static final int CONTEXT_DEL_FAV_ID = Menu.FIRST + 7;
  
  private String _reply_id;

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    Cursor cursor = (Cursor) mTweetAdapter.getItem(info.position);
    long userId = cursor.getLong(cursor
        .getColumnIndexOrThrow(TwitterDbAdapter.KEY_USER_ID));
    String user = cursor.getString(cursor
        .getColumnIndexOrThrow(TwitterDbAdapter.KEY_USER));

    menu.add(0, CONTEXT_MORE_ID, 0, user+" 的空间");
    menu.add(0, CONTEXT_REPLY_ID, 0, R.string.reply);
    menu.add(0, CONTEXT_RETWEET_ID, 0, R.string.retweet);
    menu.add(0, CONTEXT_DM_ID, 0, R.string.dm);
    
    String favorited = cursor.getString(cursor
            .getColumnIndexOrThrow(TwitterDbAdapter.KEY_FAVORITED));
    if (favorited.equals("true")){
    	menu.add(0, CONTEXT_DEL_FAV_ID, 0, R.string.del_fav);
    }else{
    	menu.add(0, CONTEXT_ADD_FAV_ID, 0, R.string.add_fav);    	
    }
    
    //MenuItem item = menu.add(0, CONTEXT_DM_ID, 0, R.string.dm);
    //item.setEnabled(getDb().isFollower(userId));
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    Cursor cursor = (Cursor) mTweetAdapter.getItem(info.position);

    if (cursor == null) {
      Log.w(TAG, "Selected item not available.");
      return super.onContextItemSelected(item);
    }

    switch (item.getItemId()) {
    case CONTEXT_MORE_ID:
      String who = cursor.getString(
          cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_USER_ID));
      launchActivity(UserActivity.createIntent(who));

      return true;
    case CONTEXT_REPLY_ID:
    {
      int userIndex = cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_USER);
      _reply_id = cursor.getString(
    		  cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_ID));
      // TODO: this isn't quite perfect. It leaves extra empty spaces if you
      // perform the reply action again.
      String replyTo = "@" + cursor.getString(userIndex) + " ";
      //String text = mTweetEdit.getText();
      //text = replyTo + " " + text.replace(replyTo, "");
      //mTweetEdit.setTextAndFocus(text, false);
      Intent intent = new Intent(WriteActivity.NEW_TWEET_ACTION, null, this, WriteActivity.class);
      intent.putExtra(WriteActivity.EXTRA_TEXT, replyTo);
      intent.putExtra(WriteActivity.REPLY_ID, _reply_id);
      startActivity(intent);

      return true;
    }
    case CONTEXT_RETWEET_ID:
    {
      _reply_id = cursor.getString(
	          cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_ID));
      String prefix = mPreferences.getString(Preferences.RT_PREFIX_KEY, getString(R.string.pref_rt_prefix_default));
      String retweet = " " + prefix + " @"
          + cursor.getString(cursor
              .getColumnIndexOrThrow(TwitterDbAdapter.KEY_USER))
          + " "
          + cursor.getString(cursor
              .getColumnIndexOrThrow(TwitterDbAdapter.KEY_TEXT)).replaceAll("<.*?>", "");
      Intent intent = new Intent(WriteActivity.NEW_TWEET_ACTION, null, this, WriteActivity.class);
      intent.putExtra(WriteActivity.EXTRA_TEXT, retweet);
      intent.putExtra(WriteActivity.REPLY_ID, _reply_id);
      startActivity(intent);

      return true; 
    }
    case CONTEXT_DM_ID:
      String user = cursor.getString(cursor
          .getColumnIndexOrThrow(TwitterDbAdapter.KEY_USER_ID));
      launchActivity(DmActivity.createIntent(user));
      return true;
    case CONTEXT_ADD_FAV_ID:
    {
    	String id = cursor.getString(cursor
    	     .getColumnIndexOrThrow(TwitterDbAdapter.KEY_ID));
    	doFavorite("add", id);
    }
    	return true;
    case CONTEXT_DEL_FAV_ID:
    {
    	String id = cursor.getString(cursor
       	     .getColumnIndexOrThrow(TwitterDbAdapter.KEY_ID));
       	doFavorite("del", id);
    }
    	return true;
    default:
      return super.onContextItemSelected(item);
    }
  }

  private static class TweetAdapter extends CursorAdapter {

    public TweetAdapter(Context context, Cursor cursor) {
      super(context, cursor);

      mInflater = LayoutInflater.from(context);

      mUserTextColumn = cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_USER);
      mTextColumn = cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_TEXT);
      mProfileImageUrlColumn = cursor
          .getColumnIndexOrThrow(TwitterDbAdapter.KEY_PROFILE_IMAGE_URL);
      mCreatedAtColumn = cursor
          .getColumnIndexOrThrow(TwitterDbAdapter.KEY_CREATED_AT);
      mSourceColumn = cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_SOURCE);
      mInReplyToScreenName = cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_IN_REPLY_TO_SCREEN_NAME);
      mFavorited = cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_FAVORITED);

      mMetaBuilder = new StringBuilder();
    }

    private LayoutInflater mInflater;

    private int mUserTextColumn;
    private int mTextColumn;
    private int mProfileImageUrlColumn;
    private int mCreatedAtColumn;
    private int mSourceColumn;
    private int mInReplyToScreenName;
    private int mFavorited;

    private StringBuilder mMetaBuilder;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      View view = mInflater.inflate(R.layout.tweet, parent, false);

      ViewHolder holder = new ViewHolder();
      holder.tweetUserText = (TextView) view.findViewById(R.id.tweet_user_text);
      holder.tweetText = (TextView) view.findViewById(R.id.tweet_text);
      holder.profileImage = (ImageView) view.findViewById(R.id.profile_image);
      holder.metaText = (TextView) view.findViewById(R.id.tweet_meta_text);
      holder.fav = (ImageView) view.findViewById(R.id.tweet_fav);
      view.setTag(holder);

      return view;
    }

    private static class ViewHolder {
      public TextView tweetUserText;
      public TextView tweetText;
      public ImageView profileImage;
      public TextView metaText;
      public ImageView fav;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      ViewHolder holder = (ViewHolder) view.getTag();

      holder.tweetUserText.setText(cursor.getString(mUserTextColumn));
      Utils.setTweetText(holder.tweetText, cursor.getString(mTextColumn));

      String profileImageUrl = cursor.getString(mProfileImageUrlColumn);

      if (!Utils.isEmpty(profileImageUrl)) {
        holder.profileImage.setImageBitmap(TwitterApplication.mImageManager.get(
            profileImageUrl));
      }

      if (cursor.getString(mFavorited).equals("true")){
      	holder.fav.setVisibility(View.VISIBLE);
      }else{
      	holder.fav.setVisibility(View.INVISIBLE);    	
      }
      
      try {
        Date createdAt = TwitterDbAdapter.DB_DATE_FORMATTER.parse(cursor
            .getString(mCreatedAtColumn));
        holder.metaText.setText(Tweet.buildMetaText(mMetaBuilder, createdAt,
            cursor.getString(mSourceColumn), cursor.getString(mInReplyToScreenName)));
      } catch (ParseException e) {
        Log.w(TAG, "Invalid created at data.");
      }
    }

    public void refresh() {
      getCursor().requery();
    }

  }

  private void draw() {
    mTweetAdapter.refresh();
  }

  private void goTop() {
    mTweetList.setSelection(0);
  }

  private void enableEntry() {
    mTweetEdit.setEnabled(true);
    mSendButton.setEnabled(true);
  }

  private void disableEntry() {
    mTweetEdit.setEnabled(false);
    mSendButton.setEnabled(false);
  }

  // Actions.

  private void doFavorite(String action, String id) {	  
    if (mFavTask != null && mFavTask.getStatus() == UserTask.Status.RUNNING) {
        Log.w(TAG, "FavTask still running");
      } else {
    	  if (!Utils.isEmpty(id)){
    		  mFavTask = new FavTask().execute(action, id);
    	  }
      }
  }

  private enum SendResult {
    OK, IO_ERROR, AUTH_ERROR, CANCELLED
  }

  private class FavTask extends UserTask<String, Void, SendResult> {
    @Override
    public void onPreExecute() {
      //onSendBegin();
    }

    @Override
    public SendResult doInBackground(String... params) {
      try {
    	String action = params[0];
    	String id = params[1];
    	JSONObject jsonObject = null;
    	if (action.equals("add")){
    		jsonObject = getApi().addFavorite(id);
    	}else{
    		jsonObject = getApi().delFavorite(id);
    	}
    	
        Tweet tweet = Tweet.create(jsonObject);

        if (!Utils.isEmpty(tweet.profileImageUrl)) {
          // Fetch image to cache.
          try {
            getImageManager().put(tweet.profileImageUrl);
          } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
          }
        }

        getDb().updateTweet(tweet);
      } catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
        return SendResult.IO_ERROR;
      } catch (AuthException e) {
        Log.i(TAG, "Invalid authorization.");
        return SendResult.AUTH_ERROR;
      } catch (JSONException e) {
        Log.w(TAG, "Could not parse JSON after sending update.");
        return SendResult.IO_ERROR;
      } catch (ApiException e) {
        Log.e(TAG, e.getMessage(), e);
        return SendResult.IO_ERROR;
      }

      return SendResult.OK;
    }

    @Override
    public void onPostExecute(SendResult result) {
      if (isCancelled()) {
        // Canceled doesn't really mean "canceled" in this task.
        // We want the request to complete, but don't want to update the
        // activity (it's probably dead).
        return;
      }

      if (result == SendResult.AUTH_ERROR) {
        logout();
      } else if (result == SendResult.OK) {
        onSendSuccess();
      } else if (result == SendResult.IO_ERROR) {
        onSendFailure();
      }
    }

    private void onSendSuccess() {
  	  //updateProgress(getString(R.string.refreshing));
  	  mTweetAdapter.notifyDataSetChanged();
  	  mTweetAdapter.refresh();
    }
    private void onSendFailure() {
        //updateProgress(getString(R.string.refreshing));
    }
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

      String maxId = getDb().fetchMaxMentionId();

      try {
        jsonArray = getApi().getMentionSinceId(maxId);
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

      getDb().addMentions(tweets, false);

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
        editor.putLong(Preferences.LAST_TWEET_REFRESH_KEY, Utils.getNowTime());
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

  // Menu.

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem item = menu.add(0, OPTIONS_MENU_ID_REFRESH, 0, R.string.refresh);
    item.setIcon(R.drawable.refresh);

    item = menu.add(0, OPTIONS_MENU_ID_TWEETS, 0, R.string.tweets);
    item.setIcon(android.R.drawable.ic_menu_view);
    
    item = menu.add(0, OPTIONS_MENU_ID_DM, 0, R.string.dm);
    item.setIcon(android.R.drawable.ic_menu_send);

    /*
    item = menu.add(0, OPTIONS_MENU_ID_TOGGLE_REPLIES, 0,
        R.string.show_at_replies);
    item.setIcon(android.R.drawable.ic_menu_zoom);
    */

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    /*
    MenuItem item = menu.findItem(OPTIONS_MENU_ID_TOGGLE_REPLIES);

    if (isReplies()) {
      item.setIcon(R.drawable.ic_menu_zoom_out);
      item.setTitle(R.string.show_all);
    } else {
      item.setIcon(android.R.drawable.ic_menu_zoom);
      item.setTitle(R.string.show_at_replies);
    }
    */

    return super.onPrepareOptionsMenu(menu);
  }

  public void toggleShowReplies() {
    mState = mState == STATE_REPLIES ? STATE_ALL : STATE_REPLIES;

    SharedPreferences.Editor editor = mPreferences.edit();
    editor.putInt(Preferences.TWITTER_ACTIVITY_STATE_KEY, mState);
    editor.commit();

    setupState();
  }

  private static final String INTENT_MODE = "mode";
  private static final int MODE_REPLIES = 1;

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case OPTIONS_MENU_ID_REFRESH:
      doRetrieve();
      return true;
    case OPTIONS_MENU_ID_TWEETS:
        launchActivity(TwitterActivity.createIntent(this));
        return true;      
    case OPTIONS_MENU_ID_DM:
      launchActivity(DmActivity.createIntent());
      return true;
    case OPTIONS_MENU_ID_TOGGLE_REPLIES:
      toggleShowReplies();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  // Various handlers.

 

}