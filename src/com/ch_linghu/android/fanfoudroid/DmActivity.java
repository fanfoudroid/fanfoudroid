package com.ch_linghu.android.fanfoudroid;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.photostream.UserTask;

public class DmActivity extends BaseActivity {

  private static final String TAG = "DmActivity";

  // Views.
  private ListView mTweetList;
  private Adapter mAdapter;
  // Adapter for To: recipient autocomplete.
  private FriendsAdapter mFriendsAdapter;

  private AutoCompleteTextView mToEdit;
  private TweetEdit mTweetEdit;
  private ImageButton mSendButton;

  private TextView mProgressText;

  // Tasks.
  private UserTask<Void, Void, TaskResult> mRetrieveTask;
  private UserTask<String, Void, TaskResult> mDeleteTask;
  private UserTask<Void, Void, TaskResult> mSendTask;

  // Refresh data at startup if last refresh was this long ago or greater.
  private static final long REFRESH_THRESHOLD = 5 * 60 * 1000;

  private static final String EXTRA_USER = "user";

  private static final String LAUNCH_ACTION = "com.ch_linghu.android.fanfoudroid.DMS";

  public static Intent createIntent() {
    return createIntent("");
  }

  public static Intent createIntent(String user) {
    Intent intent = new Intent(LAUNCH_ACTION);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    if (!Utils.isEmpty(user)) {
      intent.putExtra(EXTRA_USER, user);
    }

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

    setContentView(R.layout.dm);

    mTweetList = (ListView) findViewById(R.id.tweet_list);

    TwitterDbAdapter db = getDb();

    mToEdit = (AutoCompleteTextView) findViewById(R.id.to_edit);
    Cursor cursor = db.getFollowerUsernames("");
    // startManagingCursor(cursor);
    mFriendsAdapter = new FriendsAdapter(this, cursor);
    mToEdit.setAdapter(mFriendsAdapter);

    mTweetEdit = new TweetEdit((EditText) findViewById(R.id.tweet_edit),
        (TextView) findViewById(R.id.chars_text));

    mTweetEdit.setOnKeyListener(editEnterHandler);

    mProgressText = (TextView) findViewById(R.id.progress_text);

    mSendButton = (ImageButton) findViewById(R.id.send_button);
    mSendButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        doSend();
      }
    });

    // Mark all as read.
    db.markAllDmsRead();

    setupAdapter();

    boolean shouldRetrieve = false;

    long lastRefreshTime = mPreferences.getLong(
        Preferences.LAST_DM_REFRESH_KEY, 0);
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

    Bundle extras = getIntent().getExtras();

    if (extras != null) {
      String to = extras.getString(EXTRA_USER);
      if (!Utils.isEmpty(to)) {
        mToEdit.setText(to);
        mTweetEdit.requestFocus();
      }
    }

    // Want to be able to focus on the items with the trackball.
    // That way, we can navigate up and down by changing item focus.
    mTweetList.setItemsCanFocus(true);
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

  private static final String SIS_RUNNING_KEY = "running";

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (mRetrieveTask != null
        && mRetrieveTask.getStatus() == UserTask.Status.RUNNING) {
      outState.putBoolean(SIS_RUNNING_KEY, true);
    } else if (mSendTask != null
        && mSendTask.getStatus() == UserTask.Status.RUNNING) {
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

    if (mSendTask != null && mSendTask.getStatus() == UserTask.Status.RUNNING) {
      // Doesn't really cancel execution (we let it continue running).
      // See the SendTask code for more details.
      mSendTask.cancel(true);
    }
    if (mRetrieveTask != null
        && mRetrieveTask.getStatus() == UserTask.Status.RUNNING) {
      mRetrieveTask.cancel(true);
    }
    if (mDeleteTask != null
        && mDeleteTask.getStatus() == UserTask.Status.RUNNING) {
      mDeleteTask.cancel(true);
    }

    super.onDestroy();
  }

  // UI helpers.

  private void updateProgress(String progress) {
    mProgressText.setText(progress);
  }

  private void setupAdapter() {
    Cursor cursor = getDb().fetchAllDms();
    startManagingCursor(cursor);

    mAdapter = new Adapter(this, cursor);
    mTweetList.setAdapter(mAdapter);
    registerForContextMenu(mTweetList);
  }

  private void draw() {
    mAdapter.refresh();
  }

  private void goTop() {
    mTweetList.setSelection(0);
  }

  private void enableEntry() {
    mToEdit.setEnabled(true);
    mTweetEdit.setEnabled(true);
    mSendButton.setEnabled(true);
  }

  private void disableEntry() {
    mToEdit.setEnabled(false);
    mTweetEdit.setEnabled(false);
    mSendButton.setEnabled(false);
  }

  private enum TaskResult {
    OK, IO_ERROR, AUTH_ERROR, CANCELLED, NOT_FOLLOWED_ERROR
  }

  private void doSend() {
    if (mSendTask != null && mSendTask.getStatus() == UserTask.Status.RUNNING) {
      Log.w(TAG, "Already sending.");
    } else {
      String to = mToEdit.getText().toString();
      String status = mTweetEdit.getText().toString();

      if (!Utils.isEmpty(status) && !Utils.isEmpty(to)) {
        mSendTask = new SendTask().execute();
      }
    }
  }

  private void doRetrieve() {
    Log.i(TAG, "Attempting retrieve.");

    if (mRetrieveTask != null
        && mRetrieveTask.getStatus() == UserTask.Status.RUNNING) {
      Log.w(TAG, "Already retrieving.");
    } else {
      mRetrieveTask = new RetrieveTask().execute();
    }
  }

  private void onRetrieveBegin() {
    updateProgress("Refreshing...");
  }

  private class RetrieveTask extends UserTask<Void, Void, TaskResult> {
    @Override
    public void onPreExecute() {
      onRetrieveBegin();
    }

    @Override
    public void onProgressUpdate(Void... progress) {
      draw();
    }

    @Override
    public TaskResult doInBackground(Void... params) {
      JSONArray jsonArray;

      ArrayList<Dm> dms = new ArrayList<Dm>();

      TwitterDbAdapter db = getDb();
      TwitterApi api = getApi();
      ImageManager imageManager = getImageManager();

      String maxId = db.fetchMaxDmId(false);

      HashSet<String> imageUrls = new HashSet<String>();

      try {
        jsonArray = api.getDmsSinceId(maxId, false);
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

        Dm dm;

        try {
          JSONObject jsonObject = jsonArray.getJSONObject(i);
          dm = Dm.create(jsonObject, false);
          dms.add(dm);
        } catch (JSONException e) {
          Log.e(TAG, e.getMessage(), e);
          return TaskResult.IO_ERROR;
        }

        imageUrls.add(dm.profileImageUrl);

        if (isCancelled()) {
          return TaskResult.CANCELLED;
        }
      }

      maxId = db.fetchMaxDmId(true);

      try {
        jsonArray = api.getDmsSinceId(maxId, true);
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

        Dm dm;

        try {
          JSONObject jsonObject = jsonArray.getJSONObject(i);
          dm = Dm.create(jsonObject, true);
          dms.add(dm);
        } catch (JSONException e) {
          Log.e(TAG, e.getMessage(), e);
          return TaskResult.IO_ERROR;
        }

        imageUrls.add(dm.profileImageUrl);

        if (isCancelled()) {
          return TaskResult.CANCELLED;
        }
      }

      db.addDms(dms, false);

      if (isCancelled()) {
        return TaskResult.CANCELLED;
      }

      publishProgress();

      for (String imageUrl : imageUrls) {
        if (!Utils.isEmpty(imageUrl)) {
          // Fetch image to cache.
          try {
            imageManager.put(imageUrl);
          } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
          }
        }

        if (isCancelled()) {
          return TaskResult.CANCELLED;
        }
      }

      return TaskResult.OK;
    }

    @Override
    public void onPostExecute(TaskResult result) {
      if (result == TaskResult.AUTH_ERROR) {
        logout();
      } else if (result == TaskResult.OK) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(Preferences.LAST_DM_REFRESH_KEY, Utils.getNowTime());
        editor.commit();
        draw();
        goTop();
      } else {
        // Do nothing.
      }

      updateProgress("");
    }
  }

  private static class Adapter extends CursorAdapter {

    public Adapter(Context context, Cursor cursor) {
      super(context, cursor);

      mInflater = LayoutInflater.from(context);

      mUserTextColumn = cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_USER);
      mTextColumn = cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_TEXT);
      mProfileImageUrlColumn = cursor
          .getColumnIndexOrThrow(TwitterDbAdapter.KEY_PROFILE_IMAGE_URL);
      mCreatedAtColumn = cursor
          .getColumnIndexOrThrow(TwitterDbAdapter.KEY_CREATED_AT);
      mIsSentColumn = cursor
          .getColumnIndexOrThrow(TwitterDbAdapter.KEY_IS_SENT);
    }

    private LayoutInflater mInflater;

    private int mUserTextColumn;
    private int mTextColumn;
    private int mProfileImageUrlColumn;
    private int mIsSentColumn;
    private int mCreatedAtColumn;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      View view = mInflater.inflate(R.layout.tweet, parent, false);

      ViewHolder holder = new ViewHolder();
      holder.userText = (TextView) view.findViewById(R.id.tweet_user_text);
      holder.tweetText = (TextView) view.findViewById(R.id.tweet_text);
      holder.profileImage = (ImageView) view.findViewById(R.id.profile_image);
      holder.metaText = (TextView) view.findViewById(R.id.tweet_meta_text);
      view.setTag(holder);

      return view;
    }

    class ViewHolder {
      public TextView userText;
      public TextView tweetText;
      public ImageView profileImage;
      public TextView metaText;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      ViewHolder holder = (ViewHolder) view.getTag();

      int isSent = cursor.getInt(mIsSentColumn);
      String user = cursor.getString(mUserTextColumn);

      if (isSent == 0) {
        holder.userText.setText("from " + user);
      } else {
        holder.userText.setText("to " + user);
      }

      Utils.setTweetText(holder.tweetText, cursor.getString(mTextColumn));

      String profileImageUrl = cursor.getString(mProfileImageUrlColumn);

      if (!Utils.isEmpty(profileImageUrl)) {
        holder.profileImage.setImageBitmap(TwitterApplication.mImageManager.get(profileImageUrl));
      }

      try {
        holder.metaText.setText(Utils
            .getRelativeDate(TwitterDbAdapter.DB_DATE_FORMATTER.parse(cursor
                .getString(mCreatedAtColumn))));
      } catch (ParseException e) {
        Log.w(TAG, "Invalid created at data.");
      }
    }

    public void refresh() {
      getCursor().requery();
    }

  }

  private class SendTask extends UserTask<Void, Void, TaskResult> {
    @Override
    public void onPreExecute() {
      disableEntry();
      updateProgress("Sending DM...");
    }

    @Override
    public TaskResult doInBackground(Void... params) {
      try {
        String user = mToEdit.getText().toString();
        String text = mTweetEdit.getText().toString();

        JSONObject jsonObject = getApi().sendDirectMessage(user, text);
        Dm dm = Dm.create(jsonObject, true);

        if (!Utils.isEmpty(dm.profileImageUrl)) {
          // Fetch image to cache.
          try {
            getImageManager().put(dm.profileImageUrl);
          } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
          }
        }

        getDb().createDm(dm, false);
      } catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      } catch (JSONException e) {
        Log.w(TAG, "Could not parse JSON after sending update.");
        return TaskResult.IO_ERROR;
      } catch (WeiboException e) {
        Log.i(TAG, e.getMessage());
        // TODO: check is this is actually the case.
        return TaskResult.NOT_FOLLOWED_ERROR;
      }

      return TaskResult.OK;
    }

    @Override
    public void onPostExecute(TaskResult result) {
      if (isCancelled()) {
        // Canceled doesn't really mean "canceled" in this task.
        // We want the request to complete, but don't want to update the
        // activity (it's probably dead).
        return;
      }

      if (result == TaskResult.AUTH_ERROR) {
        logout();
      } else if (result == TaskResult.OK) {
        mToEdit.setText("");
        mTweetEdit.setText("");
        updateProgress("");
        enableEntry();
        draw();
        goTop();
      } else if (result == TaskResult.NOT_FOLLOWED_ERROR) {
        updateProgress("Unable to send. Is the person following you?");
        enableEntry();
      } else if (result == TaskResult.IO_ERROR) {
        updateProgress("Unable to send");
        enableEntry();
      }
    }
  }

  private static class FriendsAdapter extends CursorAdapter {

    public FriendsAdapter(Context context, Cursor cursor) {
      super(context, cursor);

      mInflater = LayoutInflater.from(context);

      mUserTextColumn = cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_USER);
    }

    private LayoutInflater mInflater;

    private int mUserTextColumn;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      View view = mInflater.inflate(R.layout.dropdown_item, parent, false);

      ViewHolder holder = new ViewHolder();
      holder.userText = (TextView) view.findViewById(android.R.id.text1);
      view.setTag(holder);

      return view;
    }

    class ViewHolder {
      public TextView userText;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      ViewHolder holder = (ViewHolder) view.getTag();

      holder.userText.setText(cursor.getString(mUserTextColumn));
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
      String filter = constraint == null ? "" : constraint.toString();

      return TwitterApplication.mDb.getFollowerUsernames(filter);
    }

    @Override
    public String convertToString(Cursor cursor) {
      return cursor.getString(mUserTextColumn);
    }

    public void refresh() {
      getCursor().requery();
    }

  }

  // Menu.

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem item = menu.add(0, OPTIONS_MENU_ID_REFRESH, 0, R.string.refresh);
    item.setIcon(R.drawable.refresh);

    item = menu.add(0, OPTIONS_MENU_ID_TWEETS, 0, R.string.tweets);
    item.setIcon(android.R.drawable.ic_menu_view);

    item = menu.add(0, OPTIONS_MENU_ID_REPLIES, 0,
            R.string.show_at_replies);
        item.setIcon(android.R.drawable.ic_menu_revert);
        

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case OPTIONS_MENU_ID_REFRESH:
      doRetrieve();
      return true;
    case OPTIONS_MENU_ID_TWEETS:
      launchActivity(TwitterActivity.createIntent(this));
      return true;
    case OPTIONS_MENU_ID_REPLIES:
        launchActivity(MentionActivity.createIntent(this));
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private static final int CONTEXT_REPLY_ID = 0;
  private static final int CONTEXT_DELETE_ID = 1;

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.add(0, CONTEXT_REPLY_ID, 0, R.string.reply);
    menu.add(0, CONTEXT_DELETE_ID, 0, R.string.delete);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    Cursor cursor = (Cursor) mAdapter.getItem(info.position);

    if (cursor == null) {
      Log.w(TAG, "Selected item not available.");
      return super.onContextItemSelected(item);
    }

    switch (item.getItemId()) {
    case CONTEXT_REPLY_ID:
      String user = cursor.getString(cursor
          .getColumnIndexOrThrow(TwitterDbAdapter.KEY_USER));
      mToEdit.setText(user);
      mTweetEdit.requestFocus();

      return true;
    case CONTEXT_DELETE_ID:
      int idIndex = cursor.getColumnIndexOrThrow(TwitterDbAdapter.KEY_ID);
      String id = cursor.getString(idIndex);
      doDestroy(id);

      return true;
    default:
      return super.onContextItemSelected(item);
    }
  }

  private void doDestroy(String id) {
    Log.i(TAG, "Attempting delete.");

    if (mDeleteTask != null
        && mDeleteTask.getStatus() == UserTask.Status.RUNNING) {
      Log.w(TAG, "Already deleting.");
    } else {
      mDeleteTask = new DeleteTask().execute(new String[] { id });
    }
  }

  private class DeleteTask extends UserTask<String, Void, TaskResult> {
    @Override
    public void onPreExecute() {
      updateProgress("Deleting...");
    }

    @Override
    public TaskResult doInBackground(String... params) {
      String id = params[0];

      try {
        JSONObject json = getApi().destroyDirectMessage(id);
        Dm.create(json, false);
        getDb().deleteDm(id);
      } catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      } catch (JSONException e) {
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
    public void onPostExecute(TaskResult result) {
      if (result == TaskResult.AUTH_ERROR) {
        logout();
      } else if (result == TaskResult.OK) {
        mAdapter.refresh();
      } else {
        // Do nothing.
      }

      updateProgress("");
    }
  }

  private View.OnKeyListener editEnterHandler = new View.OnKeyListener() {
    public boolean onKey(View v, int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_ENTER
          || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
          doSend();
        }
        return true;
      }
      return false;
    }
  };

}
