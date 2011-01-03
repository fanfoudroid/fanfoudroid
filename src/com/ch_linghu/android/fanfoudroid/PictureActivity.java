package com.ch_linghu.android.fanfoudroid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.photostream.UserTask;

public class PictureActivity extends BaseActivity {

  private static final String TAG = "PictureActivity";
  
  private static final String LAUNCH_ACTION = "android.intent.action.SEND";

  private ImageView mPreview;
  private TweetEdit mTweetEdit;
  private ImageButton mSendButton;

  private TextView mProgressText;

  private Uri mImageUri;
  private File mFile;

  private UserTask<Void, Void, TaskResult> mSendTask;

  private static final int MAX_BITMAP_SIZE = 480;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!getApi().isLoggedIn()) {
      Log.i(TAG, "Not logged in.");
      // handleLoggedOut();
      showLogin();
      finish();

      return;
    }

    setContentView(R.layout.picture);

    mPreview = (ImageView) findViewById(R.id.preview);

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

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();

    mFile = null;

    if (Intent.ACTION_SEND.equals(intent.getAction()) && extras != null) {
      mImageUri = (Uri) extras.getParcelable("uri");
      String filename = extras.getString("filename");
      mFile = new File(filename);
      mPreview.setImageBitmap(createThumbnailBitmap(mImageUri,
            MAX_BITMAP_SIZE));
    }

    if (mFile == null) {
      updateProgress("Could not locate picture file. Sorry!");
      disableEntry();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (!getApi().isLoggedIn()) {
      Log.i(TAG, "Not logged in.");
      // I forgot why I did this.
      // handleLoggedOut();
      showLogin();
      finish();

      return;
    }
  }

  @Override
  protected void onDestroy() {
    Log.i(TAG, "onDestroy.");

    if (mSendTask != null && mSendTask.getStatus() == UserTask.Status.RUNNING) {
      // Doesn't really cancel execution (we let it continue running).
      // See the SendTask code for more details.
      mSendTask.cancel(true);
    }

    super.onDestroy();
  }

  // UI helpers.

  private void updateProgress(String progress) {
    mProgressText.setText(progress);
  }

  private void enableEntry() {
    mTweetEdit.setEnabled(true);
    mSendButton.setEnabled(true);
  }

  private void disableEntry() {
    mTweetEdit.setEnabled(false);
    mSendButton.setEnabled(false);
  }

  private enum TaskResult {
    OK, IO_ERROR, AUTH_ERROR, CANCELLED, API_ERROR
  }

  private void doSend() {
    if (mSendTask != null && mSendTask.getStatus() == UserTask.Status.RUNNING) {
      Log.w(TAG, "Already sending.");
    } else {
      mSendTask = new SendTask().execute();
    }
  }

  private class SendTask extends UserTask<Void, Void, TaskResult> {
    private String apiErrorMessage;

    @Override
    public void onPreExecute() {
      disableEntry();
      updateProgress("Posting pic...");
    }

    @Override
    public TaskResult doInBackground(Void... params) {
      try {
        String status = mTweetEdit.getText().toString();
        getApi().postTwitPic(mFile, status);
      } catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
        return TaskResult.IO_ERROR;
      } catch (WeiboException e) {
        Log.e(TAG, e.getMessage(), e);
        apiErrorMessage = e.getMessage();
        return TaskResult.API_ERROR;
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
      } else if (result == TaskResult.API_ERROR) {
        updateProgress(apiErrorMessage);
        enableEntry();
      } else if (result == TaskResult.OK) {
        updateProgress("Picture has been posted");
      } else if (result == TaskResult.IO_ERROR) {
        updateProgress("Unable to post pic");
        enableEntry();
      }
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    // TODO: What a hack!
    menu.clear();

    MenuItem item = menu.add(0, OPTIONS_MENU_ID_TWEETS, 0, R.string.tweets);
    item.setIcon(android.R.drawable.ic_menu_view);

    item = menu.add(0, OPTIONS_MENU_ID_ABOUT, 0, R.string.about);
    item.setIcon(android.R.drawable.ic_menu_info_details);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case OPTIONS_MENU_ID_TWEETS:
      launchActivity(TwitterActivity.createNewTaskIntent(this));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private Bitmap createThumbnailBitmap(Uri uri, int size) {
    InputStream input = null;

    try {
      input = getContentResolver().openInputStream(uri);
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(input, null, options);
      input.close();

      // Compute the scale.
      int scale = 1;
      while ((options.outWidth / scale > size)
          || (options.outHeight / scale > size)) {
        scale *= 2;
      }

      options.inJustDecodeBounds = false;
      options.inSampleSize = scale;

      input = getContentResolver().openInputStream(uri);

      return BitmapFactory.decodeStream(input, null, options);
    } catch (IOException e) {
      Log.w(TAG, e);

      return null;
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          Log.w(TAG, e);
        }
      }
    }
  }

}
