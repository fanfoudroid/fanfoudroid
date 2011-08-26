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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.data.Dm;
import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.fanfou.DirectMessage;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.BaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.NavBar;
import com.ch_linghu.fanfoudroid.ui.module.TweetEdit;
import com.ch_linghu.fanfoudroid.R;

//FIXME: 将WriteDmActivity和WriteActivity进行整合。
/**
 * 撰写私信界面
 * 
 * @author lds
 * 
 */
public class WriteDmActivity extends BaseActivity {

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
	// private AutoCompleteTextView mToEdit;
	private TextView mToEdit;

	private NavBar mNavbar;

	// Task
	private GenericTask mSendTask;

	private TaskListener mSendTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			disableEntry();
			updateProgress(getString(R.string.page_status_updating));
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				mToEdit.setText("");
				mTweetEdit.setText("");
				updateProgress("");
				enableEntry();
				// 发送成功就直接关闭界面
				finish();

				// 关闭软键盘
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mTweetEdit.getEditText()
						.getWindowToken(), 0);
			} else if (result == TaskResult.NOT_FOLLOWED_ERROR) {
				updateProgress(getString(R.string.direct_meesage_status_the_person_not_following_you));
				enableEntry();
			} else if (result == TaskResult.IO_ERROR) {
				// TODO: 什么情况下会抛出IO_ERROR？需要给用户更为具体的失败原因
				updateProgress(getString(R.string.page_status_unable_to_update));
				enableEntry();
			}
		}

		@Override
		public String getName() {
			return "DMSend";
		}
	};

	private FriendsAdapter mFriendsAdapter; // Adapter for To: recipient
											// autocomplete.

	private static final String EXTRA_USER = "user";

	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.DMSW";

	public static Intent createIntent(String user) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		if (!TextUtils.isEmpty(user)) {
			intent.putExtra(EXTRA_USER, user);
		}

		return intent;
	}

	// sub menu
	// protected void createInsertPhotoDialog() {
	//
	// final CharSequence[] items = {
	// getString(R.string.write_label_take_a_picture),
	// getString(R.string.write_label_choose_a_picture) };
	//
	// AlertDialog.Builder builder = new AlertDialog.Builder(this);
	// builder.setTitle(getString(R.string.write_label_insert_picture));
	// builder.setItems(items, new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int item) {
	// // Toast.makeText(getApplicationContext(), items[item],
	// // Toast.LENGTH_SHORT).show();
	// switch (item) {
	// case 0:
	// openImageCaptureMenu();
	// break;
	// case 1:
	// openPhotoLibraryMenu();
	// }
	// }
	// });
	// AlertDialog alert = builder.create();
	// alert.show();
	// }

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate.");
		if (super._onCreate(savedInstanceState)) {
			// init View
			setContentView(R.layout.write_dm);
			mNavbar = new NavBar(NavBar.HEADER_STYLE_WRITE, this);

			// Intent & Action & Extras
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();

			// View
			mProgressText = (TextView) findViewById(R.id.progress_text);
			mTweetEditText = (EditText) findViewById(R.id.tweet_edit);

			TwitterDatabase db = getDb();

			// FIXME: 暂时取消收件人自动完成功能
			// FIXME: 可根据目前以后内容重新完成自动完成功能
			// mToEdit = (AutoCompleteTextView) findViewById(R.id.to_edit);
			// Cursor cursor = db.getFollowerUsernames("");
			// // startManagingCursor(cursor);
			// mFriendsAdapter = new FriendsAdapter(this, cursor);
			// mToEdit.setAdapter(mFriendsAdapter);

			mToEdit = (TextView) findViewById(R.id.to_edit);

			// Update status
			mTweetEdit = new TweetEdit(mTweetEditText,
					(TextView) findViewById(R.id.chars_text));
			mTweetEdit.setOnKeyListener(editEnterHandler);
			mTweetEdit.addTextChangedListener(new MyTextWatcher(
					WriteDmActivity.this));

			// With extras
			if (extras != null) {
				String to = extras.getString(EXTRA_USER);
				if (!TextUtils.isEmpty(to)) {
					mToEdit.setText(to);
					mTweetEdit.requestFocus();
				}
			}

			View.OnClickListener sendListenner = new View.OnClickListener() {
				public void onClick(View v) {
					doSend();
				}
			};

			mSendButton = (Button) findViewById(R.id.send_button);
			mSendButton.setOnClickListener(sendListenner);

			Button mTopSendButton = (Button) findViewById(R.id.top_send_btn);
			mTopSendButton.setOnClickListener(sendListenner);

			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);

		mTweetEdit.updateCharsRemain();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause.");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart.");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume.");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart.");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop.");
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy.");

		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING) {
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
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
	}

	public static Intent createNewTweetIntent(String text) {
		Intent intent = new Intent(NEW_TWEET_ACTION);
		intent.putExtra(EXTRA_TEXT, text);

		return intent;
	}

	private class MyTextWatcher implements TextWatcher {

		private WriteDmActivity _activity;

		public MyTextWatcher(WriteDmActivity activity) {
			_activity = activity;
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			if (s.length() == 0) {
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

	private void doSend() {
		if (mSendTask != null
				&& mSendTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			String to = mToEdit.getText().toString();
			String status = mTweetEdit.getText().toString();

			if (!TextUtils.isEmpty(status) && !TextUtils.isEmpty(to)) {
				mSendTask = new DmSendTask();
				mSendTask.setListener(mSendTaskListener);
				mSendTask.execute();
			} else if (TextUtils.isEmpty(status)) {
				updateProgress(getString(R.string.direct_meesage_status_texting_is_null));
			} else if (TextUtils.isEmpty(to)) {
				updateProgress(getString(R.string.direct_meesage_status_user_is_null));
			}
		}
	}

	private class DmSendTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			try {
				String user = mToEdit.getText().toString();
				String text = mTweetEdit.getText().toString();

				DirectMessage directMessage = getApi().sendDirectMessage(user,
						text);
				Dm dm = Dm.create(directMessage, true);

				// if (!Utils.isEmpty(dm.profileImageUrl)) {
				// // Fetch image to cache.
				// try {
				// getImageManager().put(dm.profileImageUrl);
				// } catch (IOException e) {
				// Log.e(TAG, e.getMessage(), e);
				// }
				// }

				getDb().createDm(dm, false);
			} catch (HttpException e) {
				Log.d(TAG, e.getMessage());
				// TODO: check is this is actually the case.
				return TaskResult.NOT_FOLLOWED_ERROR;
			}

			return TaskResult.OK;
		}
	}

	private static class FriendsAdapter extends CursorAdapter {

		public FriendsAdapter(Context context, Cursor cursor) {
			super(context, cursor);

			mInflater = LayoutInflater.from(context);

			mUserTextColumn = cursor
					.getColumnIndexOrThrow(StatusTable.USER_SCREEN_NAME);
		}

		private LayoutInflater mInflater;

		private int mUserTextColumn;

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = mInflater
					.inflate(R.layout.dropdown_item, parent, false);

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

	}

	private void enableEntry() {
		mTweetEdit.setEnabled(true);
		mSendButton.setEnabled(true);
	}

	private void disableEntry() {
		mTweetEdit.setEnabled(false);
		mSendButton.setEnabled(false);
	}

	// UI helpers.

	private void updateProgress(String progress) {
		mProgressText.setText(progress);
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