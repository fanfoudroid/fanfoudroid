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

package com.ch_linghu.fanfoudroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.http.HttpClient;
import com.ch_linghu.fanfoudroid.ui.base.WithHeaderActivity;
import com.ch_linghu.fanfoudroid.ui.module.TweetEdit;
import com.ch_linghu.fanfoudroid.weibo.Weibo;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;
import com.google.android.photostream.UserTask;

public class WriteActivity extends WithHeaderActivity {

	public static final String NEW_TWEET_ACTION = "com.ch_linghu.fanfoudroid.NEW";
	public static final String EXTRA_TEXT = "text";
	public static final String REPLY_ID = "reply_id"; 

	private static final String TAG = "WriteActivity";
	private static final String SIS_RUNNING_KEY = "running";
	private static final String PREFS_NAME = "com.ch_linghu.fanfoudroid";
	
	// View
	private TweetEdit mTweetEdit;
	private EditText mTweetEditText;
	private TextView mProgressText;
	private Button mSendButton;
	private Button backgroundButton;
	private Button chooseImagesButton;

	// Picture
	private boolean withPic = false;
	private File mFile;
	private Uri mImageUri;
	private ImageView mPreview;
	private static final int MAX_BITMAP_SIZE = 400;

	// Task
	private UserTask<String, Void, SendResult> mSendTask;

	private String _reply_id;

	// sub menu
	protected void createInsertPhotoDialog() {

		final CharSequence[] items = { getString(R.string.write_label_take_a_picture),
				getString(R.string.write_label_choose_a_picture) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.write_label_insert_picture));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				// Toast.makeText(getApplicationContext(), items[item],
				// Toast.LENGTH_SHORT).show();
				switch (item) {
				case 0:
					openImageCaptureMenu();
					break;
				case 1:
					openPhotoLibraryMenu();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void getPic(Intent intent, Bundle extras) {
		
		//  Cann't insert two pictures
		chooseImagesButton.setEnabled(false);
		// layout for picture mode
		changeStyleWithPic();
		
		withPic = true;
		mFile = null;

		if (Intent.ACTION_SEND.equals(intent.getAction()) && extras != null) {
			mImageUri = (Uri) extras.getParcelable("uri");
			//String filename = extras.getString("filename");
			//mFile = new File(filename);
			//TODO: 需要进一步细化
			mFile = bitmapToFile(createThumbnailBitmap(mImageUri, 1024));
			mPreview.setImageBitmap(createThumbnailBitmap(mImageUri,
					MAX_BITMAP_SIZE));
		}

		if (mFile == null) {
			updateProgress("Could not locate picture file. Sorry!");
			disableEntry();
		}

	}
	
	private File bitmapToFile(Bitmap bitmap) {
      	File file = new File(Environment.getExternalStorageDirectory(), "upload.jpg");
        try {
            FileOutputStream out=new FileOutputStream(file);
            if(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)){
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Sorry, the file can not be created");
            return null;
        } catch (IOException e) {
            Log.e(TAG, "IOException occurred when save upload file");
            return null;
        }
        return file;
	}

	private void changeStyleWithPic() {
		mPreview.setLayoutParams(
			new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1f)
		);
		mPreview.setVisibility(View.VISIBLE);
		mTweetEditText.setLayoutParams(
			new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 2f)
		);
	}

	/**
	 * 制作微缩图
	 * @param uri
	 * @param size
	 * @return
	 */
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate.");
		super.onCreate(savedInstanceState);

		// init View
		setContentView(R.layout.write);
		initHeader(HEADER_STYLE_WRITE);
		
		// Intent & Action & Extras
		Intent intent = getIntent();
		String action = intent.getAction();
		Bundle extras = intent.getExtras();

		_reply_id = null;
		
		// View
		mProgressText = (TextView) findViewById(R.id.progress_text);
		mTweetEditText = (EditText) findViewById(R.id.tweet_edit);

		// 插入图片
		chooseImagesButton = (Button) findViewById(R.id.choose_images_button);
		chooseImagesButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "chooseImagesButton onClick");
				createInsertPhotoDialog();
			}
		});

		// 后台发送
		backgroundButton = (Button) findViewById(R.id.send_in_backgroud_button);
		backgroundButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(WriteActivity.this, TwitterActivity.class);
				startActivity(intent);
			}
		});
		
		// With picture
		mPreview = (ImageView) findViewById(R.id.preview);
		if (Intent.ACTION_SEND.equals(intent.getAction()) && extras != null) {
			getPic(intent, extras);
		}

		// Update status
		mTweetEdit = new TweetEdit(mTweetEditText,
				(TextView) findViewById(R.id.chars_text));
		mTweetEdit.setOnKeyListener(tweetEnterHandler);
		mTweetEdit
				.addTextChangedListener(new MyTextWatcher(WriteActivity.this));

		if (NEW_TWEET_ACTION.equals(action)) {
			mTweetEdit.setText(intent.getStringExtra(EXTRA_TEXT));
			_reply_id = intent.getStringExtra(REPLY_ID);
		}

		mSendButton = (Button) findViewById(R.id.send_button);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doSend(WriteActivity.this._reply_id);
				WriteActivity.this._reply_id = null;
			}
		});
		
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause.");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG, "onRestart.");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume.");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "onStart.");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop.");
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy.");

		if (mSendTask != null
				&& mSendTask.getStatus() == UserTask.Status.RUNNING) {
			// Doesn't really cancel execution (we let it continue running).
			// See the SendTask code for more details.
			mSendTask.cancel(true);
		}

		// Don't need to cancel FollowersTask (assuming it ends properly).

		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mSendTask != null
				&& mSendTask.getStatus() == UserTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
	}

	public static Intent createNewTweetIntent(String text) {
		Intent intent = new Intent(NEW_TWEET_ACTION);
		intent.putExtra(EXTRA_TEXT, text);

		return intent;
	}
	
	public static Intent createNewReplyIntent(String screenName, String replyId) {
	    String replyTo = "@" + screenName + " ";
        Intent intent = new Intent(WriteActivity.NEW_TWEET_ACTION);
        intent.putExtra(WriteActivity.EXTRA_TEXT, replyTo);
        intent.putExtra(WriteActivity.REPLY_ID, replyId);
        
        return intent;
	}
	
	public static Intent createNewReTweetIntent(Context content, String tweetText, String screenName, String replyId) {
	    SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(content);
	    
	    String prefix = mPreferences.getString(Preferences.RT_PREFIX_KEY,
                content.getString(R.string.pref_rt_prefix_default));
        String retweet = " "
                + prefix
                + " @"
                + screenName
                + " "
                + tweetText.replaceAll("<.*?>", "");//TODO: 使用更好的方法对TEXT进行格式化
        Intent intent = new Intent(WriteActivity.NEW_TWEET_ACTION);
        intent.putExtra(WriteActivity.EXTRA_TEXT, retweet);
        intent.putExtra(WriteActivity.REPLY_ID, replyId);
        
        return intent;
    }
	

	private class MyTextWatcher implements TextWatcher {

		private WriteActivity _activity;

		public MyTextWatcher(WriteActivity activity) {
			_activity = activity;
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() == 0) {
				_activity._reply_id = null;
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

		}

	}

	private View.OnKeyListener tweetEnterHandler = new View.OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER
					|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
				if (event.getAction() == KeyEvent.ACTION_UP) {
					WriteActivity t = (WriteActivity) (v.getContext());
					doSend(t._reply_id);
				}
				return true;
			}
			return false;
		}
	};

	private void doSend(String _reply_to) {
		Log.i(TAG, "doSend");
		if (mSendTask != null
				&& mSendTask.getStatus() == UserTask.Status.RUNNING) {
			Log.w(TAG, "Already sending.");
		} else {
			String status = mTweetEdit.getText().toString();

			if (!Utils.isEmpty(status) || withPic ) {
				mSendTask = new SendTask().execute(_reply_to);
			}
		}
	}

	private enum SendResult {
		OK, IO_ERROR, AUTH_ERROR, CANCELLED, FAILURE
	}

	private class SendTask extends UserTask<String, Void, SendResult> {
		@Override
		public void onPreExecute() {
			onSendBegin();
		}

		@Override
		public SendResult doInBackground(String... params) {
			
			try {
				String _reply_to = params[0];
				String status = mTweetEdit.getText().toString();
				
				Weibo api = TwitterApplication.nApi;
				
				// Update Status
				if (withPic) {
					api.updateStatus(status, mFile);
				} else {
					api.updateStatus(status, _reply_to);
				}
				
			} catch (WeiboException e) {
				Log.e(TAG, e.getMessage(), e);
				
				if (e.getStatusCode() == HttpClient.NOT_AUTHORIZED) {
					return SendResult.AUTH_ERROR;
				}
				return SendResult.FAILURE;
			}
			
			/*
			try {
				String _reply_to = params[0];
				String status = mTweetEdit.getText().toString();
				
				if (withPic) {
					getApi().postTwitPic(mFile, status);
				} else {
					JSONObject jsonObject = getApi().update(status, _reply_to);
					Tweet tweet = Tweet.create(jsonObject);

					if (!Utils.isEmpty(tweet.profileImageUrl)) {
						// Fetch image to cache.
						try {
							getImageManager().put(tweet.profileImageUrl);
						} catch (IOException e) {
							Log.e(TAG, e.getMessage(), e);
						}
					}
				}

				// getDb().createTweet(tweet, false);
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
			
			*/

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
			} else if (result == SendResult.FAILURE) {
				onSendFailure();
			}
		}
	}

	private void onSendBegin() {
		disableEntry();
		updateProgress(getString(R.string.page_status_updating));
		backgroundButton.setVisibility(View.VISIBLE);
	}

	private void onSendSuccess() {
		mTweetEdit.setText("");
		updateProgress(getString(R.string.page_status_update_success));
		backgroundButton.setVisibility(View.INVISIBLE);
		enableEntry();
		// doRetrieve();
		// draw();
		// goTop();
		try {
			Thread.currentThread();
			Thread.sleep(500);
			updateProgress("");
		} catch (InterruptedException e) {
			Log.i(TAG, e.getMessage());
		}
	}

	private void onSendFailure() {
		updateProgress(getString(R.string.page_status_unable_to_update));
		enableEntry();
	}

	private void enableEntry() {
		mTweetEdit.setEnabled(true);
		mSendButton.setEnabled(true);
		chooseImagesButton.setEnabled(true);
	}

	private void disableEntry() {
		mTweetEdit.setEnabled(false);
		mSendButton.setEnabled(false);
		chooseImagesButton.setEnabled(false);
	}

	// UI helpers.

	private void updateProgress(String progress) {
		mProgressText.setText(progress);
	}

}