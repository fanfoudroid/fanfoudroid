package com.ch_linghu.android.fanfoudroid;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.photostream.UserTask;

public class UserActivity extends WithHeaderActivity implements MyListView.OnNeedMoreListener {

  private static final String TAG = "UserActivity";

  // State.
  private String mUsername;
  private String mMe;
  private ArrayList<Tweet> mTweets;
  private User mUser;
  private Boolean mIsFollowing;
  private Boolean mIsFollower = false;
  private int mNextPage = 1;
  private Bitmap mProfileBitmap;

  private static class State {
    State(UserActivity activity) {
      mTweets = activity.mTweets;
      mUser = activity.mUser;
      mIsFollowing = activity.mIsFollowing;
      mIsFollower = activity.mIsFollower;
      mNextPage = activity.mNextPage;
      mProfileBitmap = activity.mProfileBitmap;
    }

    public ArrayList<Tweet> mTweets;
    public User mUser;
    public boolean mIsFollowing;
    public boolean mIsFollower;
    public int mNextPage;
    public Bitmap mProfileBitmap;
  }

  // Views.
  private MyListView mTweetList;
  private TextView mProgressText;
  private TextView mUserText;
  private TextView mNameText;
  private ImageView mProfileImage;
  private Button mFollowButton;

  private TweetAdapter mAdapter;

  // Tasks.
  private UserTask<Void, Void, TaskResult> mRetrieveTask;
  private UserTask<Void, Void, TaskResult> mFriendshipTask;
  private UserTask<Void, Void, TaskResult> mLoadMoreTask;

  private static final String EXTRA_USER = "user";

  private static final String LAUNCH_ACTION = "com.ch_linghu.android.fanfoudroid.USER";

  public static Intent createIntent(String user) {
    Intent intent = new Intent(LAUNCH_ACTION);
    intent.putExtra(EXTRA_USER, user);

    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!getApi().isLoggedIn()) {
      Log.i(TAG, "Not logged in.");
      handleLoggedOut();
      return;
    }

    // set UI
    setContentView(R.layout.user);
    initHeader(HEADER_STYLE_HOME, this);

    // user name
    mMe = TwitterApplication.mApi.getUsername();
    
    // 提示框
    mProgressText = (TextView) findViewById(R.id.progress_text);
    
    // Add Header to ListView
    mTweetList 	  = (MyListView) findViewById(R.id.tweet_list);
    View header = View.inflate(this, R.layout.user_header, null);
    mTweetList.addHeaderView(header);
    
    // 用户栏（用户名/头像）
    mUserText 	  = (TextView) findViewById(R.id.tweet_user_text);
    mNameText 	  = (TextView) findViewById(R.id.realname_text);
    mProfileImage = (ImageView) findViewById(R.id.profile_image);
    
    // follow button
    mFollowButton = (Button) findViewById(R.id.follow_button);
    mFollowButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        confirmFollow();
      }
    });

    Intent intent = getIntent();
    Uri data = intent.getData();

    mUsername = intent.getStringExtra(EXTRA_USER);

    if (TextUtils.isEmpty(mUsername)) {
      mUsername = data.getLastPathSegment();
    }

    setTitle("@" + mUsername);
    mUserText.setText("@" + mUsername);

    mTweets = new ArrayList<Tweet>();
    mAdapter = new TweetAdapter(this);
    mTweetList.setAdapter(mAdapter);
    registerForContextMenu(mTweetList);
    mTweetList.setOnNeedMoreListener(this);

    State state = (State) getLastNonConfigurationInstance();

    boolean wasRunning = Utils.isTrue(savedInstanceState, SIS_RUNNING_KEY);

    if (state != null && !wasRunning) {
      mTweets = state.mTweets;
      mUser = state.mUser;
      mIsFollowing = state.mIsFollowing;
      mIsFollower = state.mIsFollower;
      mNextPage = state.mNextPage;
      mProfileBitmap = state.mProfileBitmap;
      draw();
    } else {
      doRetrieve();
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

  private static final String SIS_RUNNING_KEY = "running";

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (mRetrieveTask != null
        && mRetrieveTask.getStatus() == UserTask.Status.RUNNING) {
      outState.putBoolean(SIS_RUNNING_KEY, true);
    }
  }

  @Override
  protected void onDestroy() {
    Log.i(TAG, "onDestroy.");

    if (mRetrieveTask != null
        && mRetrieveTask.getStatus() == UserTask.Status.RUNNING) {
      mRetrieveTask.cancel(true);
    }

    if (mFriendshipTask != null
        && mFriendshipTask.getStatus() == UserTask.Status.RUNNING) {
      mFriendshipTask.cancel(true);
    }

    if (mLoadMoreTask != null
        && mLoadMoreTask.getStatus() == UserTask.Status.RUNNING) {
      mLoadMoreTask.cancel(true);
    }

    super.onDestroy();
  }


  // UI helpers.

  private void updateProgress(String progress) {
    mProgressText.setText(progress);
  }

  private void draw() {
    if (mProfileBitmap != null) {
      mProfileImage.setImageBitmap(mProfileBitmap);
    }

    mAdapter.refresh(mTweets);

    if (mUser != null) {
      mNameText.setText(mUser.name);
    }

    if (mUsername.equalsIgnoreCase(mMe)) {
      mFollowButton.setVisibility(View.GONE);
    } else if (mIsFollowing != null) {
      mFollowButton.setVisibility(View.VISIBLE);

      if (mIsFollowing) {
        mFollowButton.setText(R.string.unfollow);
      } else {
        mFollowButton.setText(R.string.follow);
      }
    }
  }


  private enum TaskResult {
    OK, IO_ERROR, AUTH_ERROR, CANCELLED
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

  private void doLoadMore() {
    Log.i(TAG, "Attempting load more.");

    if (mLoadMoreTask != null
        && mLoadMoreTask.getStatus() == UserTask.Status.RUNNING) {
      Log.w(TAG, "Already loading more.");
    } else {
      mLoadMoreTask = new LoadMoreTask().execute();
    }
  }

  private void onRetrieveBegin() {
    updateProgress(getString(R.string.refreshing));
  }

  private void onLoadMoreBegin() {
    updateProgress(getString(R.string.get_more));
    animRotate(refreshButton);
  }

  private class RetrieveTask extends UserTask<Void, Void, TaskResult> {
    @Override
    public void onPreExecute() {
      onRetrieveBegin();
    }

    ArrayList<Tweet> mTweets = new ArrayList<Tweet>();

    @Override
    public TaskResult doInBackground(Void... params) {
      JSONArray jsonArray;

      TwitterApi api = getApi();
      ImageManager imageManager = getImageManager();

      try {
        jsonArray = api.getUserTimeline(mUsername, mNextPage);
        
      } catch (SocketTimeoutException e) {
    	Log.e(TAG, e.getMessage(), e);
    	Toast.makeText(getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
    	return TaskResult.IO_ERROR;
      } catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      } catch (WeiboException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      } 

      for (int i = 0; i < jsonArray.length(); ++i) {
        if (isCancelled()) {
          return TaskResult.CANCELLED;
        }

        Tweet tweet;

        try {
          JSONObject jsonObject = jsonArray.getJSONObject(i);
          tweet = Tweet.create(jsonObject);
          mTweets.add(tweet);

          if (mUser == null) {
            mUser = User.create(jsonObject.getJSONObject("user"));
          }
        } catch (JSONException e) {
          Log.e(TAG, e.getMessage(), e);
          return TaskResult.IO_ERROR;
        }

        if (isCancelled()) {
          return TaskResult.CANCELLED;
        }
      }

      addTweets(mTweets);

      if (isCancelled()) {
        return TaskResult.CANCELLED;
      }

      publishProgress();

      if (!Utils.isEmpty(mUser.profileImageUrl)) {
        try {
          setProfileBitmap(imageManager.fetchImage(mUser.profileImageUrl));
        } catch (IOException e) {
          Log.e(TAG, e.getMessage(), e);
        }
      }

      if (isCancelled()) {
        return TaskResult.CANCELLED;
      }

      publishProgress();

      try {
        mIsFollowing = api.isFollows(mMe, mUsername);
        mIsFollower = api.isFollows(mUsername, mMe);
      } catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      } catch (WeiboException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      }

      if (isCancelled()) {
        return TaskResult.CANCELLED;
      }

      return TaskResult.OK;
    }

    @Override
    public void onProgressUpdate(Void... progress) {
      draw();
    }

    @Override
    public void onPostExecute(TaskResult result) {
      if (result == TaskResult.AUTH_ERROR) {
        updateProgress(getString(R.string.This_person_has_protected_their_updates));

        return;
      } else if (result == TaskResult.OK) {
    	refreshButton.clearAnimation();
        draw();
      } else {
        // Do nothing.
      }

      updateProgress("");
    }
  }

  private class LoadMoreTask extends UserTask<Void, Void, TaskResult> {
    @Override
    public void onPreExecute() {
      onLoadMoreBegin();
    }

    ArrayList<Tweet> mTweets = new ArrayList<Tweet>();

    @Override
    public TaskResult doInBackground(Void... params) {
      JSONArray jsonArray;

      TwitterApi api = getApi();

      try {
        jsonArray = api.getUserTimeline(mUsername, mNextPage);
      } catch (SocketTimeoutException e) {
    	Log.e(TAG, e.getMessage(), e);
    	Toast.makeText(getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
    	return TaskResult.IO_ERROR;
      }
      catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      } catch (WeiboException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      }

      for (int i = 0; i < jsonArray.length(); ++i) {
        if (isCancelled()) {
          return TaskResult.CANCELLED;
        }

        Tweet tweet;

        try {
          JSONObject jsonObject = jsonArray.getJSONObject(i);
          tweet = Tweet.create(jsonObject);
          mTweets.add(tweet);
        } catch (JSONException e) {
          Log.e(TAG, e.getMessage(), e);
          return TaskResult.IO_ERROR;
        }
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

    @Override
    public void onProgressUpdate(Void... progress) {
      draw();
    }

    @Override
    public void onPostExecute(TaskResult result) {
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
  }

  private class FriendshipTask extends UserTask<Void, Void, TaskResult> {

    private boolean mIsDestroy;

    public FriendshipTask(boolean isDestroy) {
      mIsDestroy = isDestroy;
    }

    @Override
    public void onPreExecute() {
      mFollowButton.setEnabled(false);

      if (mIsDestroy) {
        updateProgress(getString(R.string.unfollowing) + "...");
      } else {
        updateProgress(getString(R.string.following) + "...");
      }
    }

    @Override
    public TaskResult doInBackground(Void... params) {
      JSONObject jsonObject;

      String id = mUser.id;

      TwitterApi api = getApi();

      try {
        if (mIsDestroy) {
          jsonObject = api.destroyFriendship(id);
        } else {
          jsonObject = api.createFriendship(id);
        }
      } catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      } catch (WeiboException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      }

      if (isCancelled()) {
        return TaskResult.CANCELLED;
      }

      try {
        User.create(jsonObject);
      } catch (JSONException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      }

      if (isCancelled()) {
        return TaskResult.CANCELLED;
      }

      return TaskResult.OK;
    }

    @Override
    public void onPostExecute(TaskResult result) {
      if (result == TaskResult.AUTH_ERROR) {
        logout();
      } else if (result == TaskResult.OK) {
        mIsFollowing = !mIsFollowing;
        draw();
      } else {
        // Do nothing.
      }

      mFollowButton.setEnabled(true);
      updateProgress("");
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem item = menu.add(0, OPTIONS_MENU_ID_REFRESH, 0, R.string.refresh);
    item.setIcon(R.drawable.refresh);

    item = menu.add(0, OPTIONS_MENU_ID_DM, 0, R.string.dm);
    item.setIcon(android.R.drawable.ic_menu_send);

    item = menu.add(0, OPTIONS_MENU_ID_FOLLOW, 0, R.string.follow);
    item.setIcon(android.R.drawable.ic_menu_add);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem item = menu.findItem(OPTIONS_MENU_ID_DM);
    item.setEnabled(mIsFollower);

    item = menu.findItem(OPTIONS_MENU_ID_FOLLOW);

    if (mIsFollowing == null) {
      item.setEnabled(false);
      item.setTitle(R.string.follow);
      item.setIcon(android.R.drawable.ic_menu_add);
    } else if (mIsFollowing) {
      item.setTitle(R.string.unfollow);
      item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    } else {
      item.setTitle(R.string.follow);
      item.setIcon(android.R.drawable.ic_menu_add);
    }

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case OPTIONS_MENU_ID_REFRESH:
      doRetrieve();
      return true;
    case OPTIONS_MENU_ID_DM:
      launchActivity(DmActivity.createIntent(mUsername));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private static final int CONTEXT_REPLY_ID = 0;
  private static final int CONTEXT_RETWEET_ID = 1;
  private static final int CONTEXT_DM_ID = 2;

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.add(0, CONTEXT_REPLY_ID, 0, R.string.reply);
    menu.add(0, CONTEXT_RETWEET_ID, 0, R.string.retweet);

    MenuItem item = menu.add(0, CONTEXT_DM_ID, 0, R.string.dm);
    item.setEnabled(mIsFollower);
   
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
    case CONTEXT_REPLY_ID:
      String replyTo = "@" + tweet.screenName + " ";
      launchNewTweetActivity(replyTo);
      return true;
    case CONTEXT_RETWEET_ID:
      String retweet = getString(R.string.pref_rt_prefix_default)+ " @" + tweet.screenName + " " + tweet.text;
      launchNewTweetActivity(retweet);
      return true;
    case CONTEXT_DM_ID:
      launchActivity(DmActivity.createIntent(mUsername));
      return true;
    default:
      return super.onContextItemSelected(item);
    }
  }

  private void launchNewTweetActivity(String text) {
    launchActivity(WriteActivity.createNewTweetIntent(text));
  }


  private static final int DIALOG_CONFIRM = 0;

  private void confirmFollow() {
    showDialog(DIALOG_CONFIRM);
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    AlertDialog dialog = new AlertDialog.Builder(this).create();

    dialog.setTitle(R.string.friendship);
    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Doesn't matter", mConfirmListener);
    dialog.setButton(AlertDialog.BUTTON_NEUTRAL,
        getString(R.string.cancel), mCancelListener);
    dialog.setMessage("FOO");

    return dialog;
  }

  @Override
  protected void onPrepareDialog(int id, Dialog dialog) {
    super.onPrepareDialog(id, dialog);

    AlertDialog confirmDialog = (AlertDialog) dialog;

    String action = mIsFollowing ? getString(R.string.unfollow) :
        getString(R.string.follow);
    String message = action + " " + mUsername + "?";

    (confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE)).setText(action);
    confirmDialog.setMessage(message);
  }

  private DialogInterface.OnClickListener mConfirmListener = new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int whichButton) {
      toggleFollow();
    }
  };

  private DialogInterface.OnClickListener mCancelListener = new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int whichButton) {
    }
  };

  private void toggleFollow() {
    if (mFriendshipTask != null
        && mFriendshipTask.getStatus() == UserTask.Status.RUNNING) {
      Log.w(TAG, "Already updating friendship.");
      return;
    }

    mFriendshipTask = new FriendshipTask(mIsFollowing).execute();

    // TODO: should we do a timeline refresh here?
  }

  private static class TweetAdapter extends TweetArrayAdapter {
    public TweetAdapter(Context context) {
      super(context, null);
    }

    private static class ViewHolder {
      public TextView tweetText;
      public TextView metaText;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view;

      if (convertView == null) {
        view = mInflater.inflate(R.layout.user_tweet, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.tweetText = (TextView) view.findViewById(R.id.tweet_text);
        holder.metaText = (TextView) view.findViewById(R.id.tweet_meta_text);
        view.setTag(holder);
      } else {
        view = convertView;
      }

      ViewHolder holder = (ViewHolder) view.getTag();

      Tweet tweet = mTweets.get(position);

      Utils.setTweetText(holder.tweetText, tweet.text);

      holder.metaText.setText(Tweet.buildMetaText(mMetaBuilder,
          tweet.createdAt, tweet.source, tweet.inReplyToScreenName));

      return view;
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

  private synchronized void setProfileBitmap(Bitmap bitmap) {
    mProfileBitmap = bitmap;
  }
  
 

}
