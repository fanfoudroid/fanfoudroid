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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.ImageCache;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.ProfileImageCacheCallback;
import com.ch_linghu.fanfoudroid.helper.utils.DateTimeHelper;
import com.ch_linghu.fanfoudroid.helper.utils.PhotoHelper;
import com.ch_linghu.fanfoudroid.helper.utils.TextHelper;
import com.ch_linghu.fanfoudroid.http.HttpClient;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.http.Response;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.task.TweetCommonTask;
import com.ch_linghu.fanfoudroid.ui.base.BaseActivity;
import com.ch_linghu.fanfoudroid.widget.Feedback;
import com.ch_linghu.fanfoudroid.widget.FeedbackFactory;
import com.ch_linghu.fanfoudroid.widget.NavBar;

public class StatusActivity extends BaseActivity {

	private static final String TAG = "StatusActivity";
	private static final String SIS_RUNNING_KEY = "running";
	private static final String PREFS_NAME = "com.ch_linghu.fanfoudroid";

	private static final String EXTRA_TWEET = "tweet";
	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.STATUS";

	static final private int CONTEXT_REFRESH_ID = 0x0001;
	static final private int CONTEXT_CLIPBOARD_ID = 0x0002;
	static final private int CONTEXT_DELETE_ID = 0x0003;

	// Task TODO: tasks
	private GenericTask mReplyTask;
	private GenericTask mStatusTask;
	private GenericTask mPhotoTask; // TODO: 压缩图片，提供获取图片的过程中可取消获取
	private GenericTask mFavTask;
	private GenericTask mDeleteTask;
	
	private NavBar mNavbar;
	private Feedback mFeedback;
	
	private TaskListener mReplyTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			showReplyStatus(replyTweet);
			StatusActivity.this.mFeedback.success("");
		}

		@Override
		public String getName() {
			return "GetReply";
		}

	};

	private TaskListener mStatusTaskListener = new TaskAdapter() {
		
		@Override
		public void onPreExecute(GenericTask task) {
			clean();
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			StatusActivity.this.mFeedback.success("");
			draw();
		}

		@Override
		public String getName() {
			return "GetStatus";
		}

	};
	
	private TaskListener mPhotoTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				status_photo.setImageBitmap(mPhotoBitmap);
			} else {
				status_photo.setVisibility(View.GONE);
			}
			StatusActivity.this.mFeedback.success("");
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return "GetPhoto";
		}
	};
	private TaskListener mFavTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "FavoriteTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				onFavSuccess();
			} else if (result == TaskResult.IO_ERROR) {
				onFavFailure();
			}
		}
	};
	private TaskListener mDeleteTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "DeleteTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				onDeleteSuccess();
			} else if (result == TaskResult.IO_ERROR) {
				onDeleteFailure();
			}
		}
	};
	// View
	private TextView tweet_screen_name;
	private TextView tweet_text;
	private TextView tweet_user_info;
	private ImageView profile_image;
	private TextView tweet_source;
	private TextView tweet_created_at;
	private ImageButton btn_person_more;
	private ImageView status_photo = null; // if exists
	private ViewGroup reply_wrap;
	private TextView reply_status_text = null; // if exists
	private TextView reply_status_date = null; // if exists
	private ImageButton tweet_fav;

	private Tweet tweet = null;
	private Tweet replyTweet = null; // if exists

	private HttpClient mClient;
	private Bitmap mPhotoBitmap = ImageCache.mDefaultBitmap; // if exists

	public static Intent createIntent(Tweet tweet) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.putExtra(EXTRA_TWEET, tweet);
		return intent;
	}

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate.");
		if (super._onCreate(savedInstanceState)) {
			mClient = getApi().getHttpClient();

			// Intent & Action & Extras
			Intent intent = getIntent();
			String action = intent.getAction();
			Bundle extras = intent.getExtras();

			// Must has extras
			if (null == extras) {
				Log.e(TAG, this.getClass().getName() + " must has extras.");
				finish();
				return false;
			}

			setContentView(R.layout.status);
			mNavbar = new NavBar(NavBar.HEADER_STYLE_BACK, this);
			mFeedback = FeedbackFactory.getFeedback(this, FeedbackFactory.PROGRESS_MODE);

			findView();
			bindNavBarListener();

			// Set view with intent data
			this.tweet = extras.getParcelable(EXTRA_TWEET);
			draw();
            
			bindFooterBarListener();
			bindReplyViewListener();

			return true;
		} else {
			return false;
		}
	}
	
	private void findView() {
	    tweet_screen_name = (TextView) findViewById(R.id.tweet_screen_name);
        tweet_user_info = (TextView) findViewById(R.id.tweet_user_info);
        tweet_text = (TextView) findViewById(R.id.tweet_text);
        tweet_source = (TextView) findViewById(R.id.tweet_source);
        profile_image = (ImageView) findViewById(R.id.profile_image);
        tweet_created_at = (TextView) findViewById(R.id.tweet_created_at);
        btn_person_more = (ImageButton) findViewById(R.id.person_more);
        tweet_fav = (ImageButton) findViewById(R.id.tweet_fav);

        reply_wrap = (ViewGroup) findViewById(R.id.reply_wrap);
        reply_status_text = (TextView) findViewById(R.id.reply_status_text);
        reply_status_date = (TextView) findViewById(R.id.reply_tweet_created_at);
        status_photo = (ImageView) findViewById(R.id.status_photo);
	}
	
	private void bindNavBarListener() {
	    mNavbar.getRefreshButton().setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doGetStatus(tweet.id);
                    }
                });
	}

	private void bindFooterBarListener() {

		// person_more
		btn_person_more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = ProfileActivity.createIntent(tweet.userId);
				startActivity(intent);
			}
		});

		// Footer bar
		TextView footer_btn_share = (TextView) findViewById(R.id.footer_btn_share);
		TextView footer_btn_reply = (TextView) findViewById(R.id.footer_btn_reply);
		TextView footer_btn_retweet = (TextView) findViewById(R.id.footer_btn_retweet);
		TextView footer_btn_fav = (TextView) findViewById(R.id.footer_btn_fav);
		TextView footer_btn_more = (TextView) findViewById(R.id.footer_btn_more);

		// 分享
		footer_btn_share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(
						Intent.EXTRA_TEXT,
						String.format("@%s %s", tweet.screenName,
								TextHelper.getSimpleTweetText(tweet.text)));
				startActivity(Intent.createChooser(intent,
						getString(R.string.cmenu_share)));
			}
		});

		// 回复
		footer_btn_reply.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = WriteActivity.createNewReplyIntent(
						tweet.screenName, tweet.id);
				startActivity(intent);
			}
		});

		// 转发
		footer_btn_retweet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = WriteActivity.createNewRepostIntent(
						StatusActivity.this, tweet.text, tweet.screenName,
						tweet.id);
				startActivity(intent);
			}
		});

		// 收藏/取消收藏
		footer_btn_fav.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (tweet.favorited.equals("true")) {
					doFavorite("del", tweet.id);
				} else {
					doFavorite("add", tweet.id);
				}
			}
		});

		// TODO: 更多操作
		registerForContextMenu(footer_btn_more);
		footer_btn_more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openContextMenu(v);
			}
		});
	}

	private void bindReplyViewListener() {
		// 点击回复消息打开新的Status界面
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!TextHelper.isEmpty(tweet.inReplyToStatusId)) {
					if (replyTweet == null) {
						Log.w(TAG, "Selected item not available.");
					} else {
						launchActivity(StatusActivity.createIntent(replyTweet));
					}
				}
			}
		};
		reply_wrap.setOnClickListener(listener);
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

		if (mReplyTask != null
				&& mReplyTask.getStatus() == GenericTask.Status.RUNNING) {
			mReplyTask.cancel(true);
		}
		if (mPhotoTask != null
				&& mPhotoTask.getStatus() == GenericTask.Status.RUNNING) {
			mPhotoTask.cancel(true);
		}
		if (mFavTask != null
				&& mFavTask.getStatus() == GenericTask.Status.RUNNING) {
			mFavTask.cancel(true);
		}

		super.onDestroy();
	}

	private ProfileImageCacheCallback callback = new ProfileImageCacheCallback() {

		@Override
		public void refresh(String url, Bitmap bitmap) {
			profile_image.setImageBitmap(bitmap);
		}

	};

	private void clean() {
		tweet_screen_name.setText("");
		tweet_text.setText("");
		tweet_created_at.setText("");
		tweet_source.setText("");
		tweet_user_info.setText("");
		tweet_fav.setEnabled(false);
		profile_image.setImageBitmap(ImageCache.mDefaultBitmap);
		status_photo.setVisibility(View.GONE);
		
		ViewGroup reply_wrap = (ViewGroup) findViewById(R.id.reply_wrap);
		reply_wrap.setVisibility(View.GONE);
	}
	
	private void draw() {
		Log.d(TAG, "draw");

		String PHOTO_PREVIEW_TYPE_NONE = getString(R.string.pref_photo_preview_type_none);
		String PHOTO_PREVIEW_TYPE_THUMBNAIL = getString(R.string.pref_photo_preview_type_thumbnail);
		String PHOTO_PREVIEW_TYPE_MIDDLE = getString(R.string.pref_photo_preview_type_middle);
		String PHOTO_PREVIEW_TYPE_ORIGINAL = getString(R.string.pref_photo_preview_type_original);

		SharedPreferences pref = getPreferences();
		String photoPreviewSize = pref.getString(Preferences.PHOTO_PREVIEW,
				PHOTO_PREVIEW_TYPE_ORIGINAL);
		boolean forceShowAllImage = pref.getBoolean(
				Preferences.FORCE_SHOW_ALL_IMAGE, false);

		tweet_screen_name.setText(tweet.screenName);
		TextHelper.setTweetText(tweet_text, tweet.text);
		tweet_created_at.setText(DateTimeHelper.getRelativeDate(tweet.createdAt));
		tweet_source.setText(getString(R.string.tweet_source_prefix)
				+ tweet.source);
		tweet_user_info.setText(tweet.userId);
		boolean isFav = (tweet.favorited.equals("true")) ? true : false;
		tweet_fav.setEnabled(isFav);

		// Bitmap mProfileBitmap =
		// TwitterApplication.mImageManager.get(tweet.profileImageUrl);
		profile_image
				.setImageBitmap(TwitterApplication.mProfileImageCacheManager
						.get(tweet.profileImageUrl, callback));

		// has photo
		if (!photoPreviewSize.equals(PHOTO_PREVIEW_TYPE_NONE)) {
			String photoLink;
			boolean isPageLink = false;
			if (photoPreviewSize.equals(PHOTO_PREVIEW_TYPE_THUMBNAIL)) {
				photoLink = tweet.thumbnail_pic;
			} else if (photoPreviewSize.equals(PHOTO_PREVIEW_TYPE_MIDDLE)) {
				photoLink = tweet.bmiddle_pic;
			} else if (photoPreviewSize.equals(PHOTO_PREVIEW_TYPE_ORIGINAL)) {
				photoLink = tweet.original_pic;
			} else {
				Log.e(TAG, "Invalid Photo Preview Size Type");
				photoLink = "";
			}

			// 如果选用了强制显示则再尝试分析图片链接
			if (forceShowAllImage) {
				photoLink = PhotoHelper
						.getPhotoPageLink(tweet.text, photoPreviewSize);
				isPageLink = true;
			}

			if (!TextHelper.isEmpty(photoLink)) {
				status_photo.setVisibility(View.VISIBLE);
				status_photo.setImageBitmap(mPhotoBitmap);
				doGetPhoto(photoLink, isPageLink);
			}
		} else {
			status_photo.setVisibility(View.GONE);
		}
		// has reply
		if (!TextHelper.isEmpty(tweet.inReplyToStatusId)) {
			ViewGroup reply_wrap = (ViewGroup) findViewById(R.id.reply_wrap);
			reply_wrap.setVisibility(View.VISIBLE);
			reply_status_text = (TextView) findViewById(R.id.reply_status_text);
			reply_status_date = (TextView) findViewById(R.id.reply_tweet_created_at);
			doGetReply(tweet.inReplyToStatusId);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mReplyTask != null
				&& mReplyTask.getStatus() == GenericTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
	}

	private String fetchWebPage(String url) throws HttpException {
		Log.d(TAG, "Fetching WebPage: " + url);

		Response res = mClient.get(url);
		return res.asString();
	}

	private Bitmap fetchPhotoBitmap(String url) throws HttpException,
			IOException {
		Log.d(TAG, "Fetching Photo: " + url);
		Response res = mClient.get(url);

		InputStream is = res.asStream();
		Bitmap bitmap = BitmapFactory.decodeStream(is);
		is.close();

		return bitmap;
	}

	private void doGetReply(String status_id) {
		Log.d(TAG, "Attempting get status task.");
		mFeedback.start("");

		if (mReplyTask != null
				&& mReplyTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mReplyTask = new GetReplyTask();
			mReplyTask.setListener(mReplyTaskListener);

			TaskParams params = new TaskParams();
			params.put("reply_id", status_id);
			mReplyTask.execute(params);
		}
	}

	private class GetReplyTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			com.ch_linghu.fanfoudroid.weibo.Status status;
			try {
				String reply_id = param.getString("reply_id");

				if (!TextHelper.isEmpty(reply_id)) {
					// 首先查看是否在数据库中，如不在再去获取
					replyTweet = getDb().queryTweet(reply_id, -1);
					if (replyTweet == null) {
						status = getApi().showStatus(reply_id);
						replyTweet = Tweet.create(status);
					}
				}
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			return TaskResult.OK;
		}
	}

	private void doGetStatus(String status_id) {
		Log.d(TAG, "Attempting get status task.");
		mFeedback.start("");

		if (mStatusTask != null
				&& mStatusTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mStatusTask = new GetStatusTask();
			mStatusTask.setListener(mStatusTaskListener);

			TaskParams params = new TaskParams();
			params.put("id", status_id);
			mStatusTask.execute(params);
		}
	}

	private class GetStatusTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			com.ch_linghu.fanfoudroid.weibo.Status status;
			try {
				String id = param.getString("id");

				if (!TextHelper.isEmpty(id)) {
					status = getApi().showStatus(id);
					mFeedback.update(80);
					tweet = Tweet.create(status);
				}
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			mFeedback.update(99);
			return TaskResult.OK;
		}
	}

	private void doGetPhoto(String photoPageURL, boolean isPageLink) {
	    mFeedback.start("");
	    
		if (mPhotoTask != null
				&& mPhotoTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mPhotoTask = new GetPhotoTask();
			mPhotoTask.setListener(mPhotoTaskListener);

			TaskParams params = new TaskParams();
			params.put("photo_url", photoPageURL);
			params.put("is_page_link", isPageLink);
			mPhotoTask.execute(params);
		}
	}

	private class GetPhotoTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			try {
				String photoURL = param.getString("photo_url");
				boolean isPageLink = param.getBoolean("is_page_link");
				if (!TextHelper.isEmpty(photoURL)) {
					if (isPageLink) {
						String pageHtml = fetchWebPage(photoURL);
						String photoSrcURL = PhotoHelper.getPhotoURL(pageHtml);
						if (photoSrcURL != null) {
							mPhotoBitmap = fetchPhotoBitmap(photoSrcURL);
						}
					} else {
						mPhotoBitmap = fetchPhotoBitmap(photoURL);
					}
				}
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}
			return TaskResult.OK;
		}
	}

	private void showReplyStatus(Tweet tweet) {
		if (tweet != null) {
			String text = tweet.screenName + " : " + tweet.text;
			TextHelper.setSimpleTweetText(reply_status_text, text);
			reply_status_date.setText(DateTimeHelper.getRelativeDate(tweet.createdAt));
		} else {
			String msg = MessageFormat.format(
					getString(R.string.status_status_reply_cannot_display),
					this.tweet.inReplyToScreenName);
			reply_status_text.setText(msg);
		}
	}

	public void onDeleteFailure() {
		Log.e(TAG, "Delete failed");
	}

	public void onDeleteSuccess() {
		finish();
	}

	// for HasFavorite interface

	public void doFavorite(String action, String id) {
		if (mFavTask != null
				&& mFavTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			if (!TextHelper.isEmpty(id)) {
				Log.d(TAG, "doFavorite.");
				mFavTask = new TweetCommonTask.FavoriteTask(this);
				mFavTask.setListener(mFavTaskListener);

				TaskParams param = new TaskParams();
				param.put("action", action);
				param.put("id", id);
				mFavTask.execute(param);
			}
		}
	}

	public void onFavSuccess() {
		// updateProgress(getString(R.string.refreshing));
		if (((TweetCommonTask.FavoriteTask) mFavTask).getType().equals(
				TweetCommonTask.FavoriteTask.TYPE_ADD)) {
			tweet.favorited = "true";
			tweet_fav.setEnabled(true);
		} else {
			tweet.favorited = "false";
			tweet_fav.setEnabled(false);
		}
	}

	public void onFavFailure() {
		// updateProgress(getString(R.string.refreshing));
	}

	private void doDelete(String id) {

		if (mDeleteTask != null
				&& mDeleteTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mDeleteTask = new TweetCommonTask.DeleteTask(this);
			mDeleteTask.setListener(mDeleteTaskListener);

			TaskParams params = new TaskParams();
			params.put("id", id);
			mDeleteTask.execute(params);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_REFRESH_ID:
			doGetStatus(tweet.id);
			return true;
		case CONTEXT_CLIPBOARD_ID:
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(TextHelper.getSimpleTweetText(tweet.text));
			return true;
		case CONTEXT_DELETE_ID:
			doDelete(tweet.id);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderIcon(android.R.drawable.ic_menu_more);
		menu.setHeaderTitle(getString(R.string.cmenu_more));

		menu.add(0, CONTEXT_REFRESH_ID, 0, R.string.omenu_refresh);
		menu.add(0, CONTEXT_CLIPBOARD_ID, 0, R.string.cmenu_clipboard);

		if (tweet.userId.equals(TwitterApplication.getMyselfId())) {
			menu.add(0, CONTEXT_DELETE_ID, 0, R.string.cmenu_delete);
		}

	}
}