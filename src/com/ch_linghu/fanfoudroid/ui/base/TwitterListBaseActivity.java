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

/**
 * AbstractTwitterListBaseLine用于抽象tweets List的展现
 * UI基本元素要求：一个ListView用于tweet列表
 *               一个ProgressText用于提示信息
 */
package com.ch_linghu.fanfoudroid.ui.base;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.DmActivity;
import com.ch_linghu.fanfoudroid.MentionActivity;
import com.ch_linghu.fanfoudroid.StatusActivity;
import com.ch_linghu.fanfoudroid.TwitterActivity;
import com.ch_linghu.fanfoudroid.UserActivity;
import com.ch_linghu.fanfoudroid.WriteActivity;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.task.TaskFactory;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.module.TweetAdapter;

public abstract class TwitterListBaseActivity extends WithHeaderActivity {
	static final String TAG = "TwitterListBaseActivity";

	protected TextView mProgressText;

	protected static final int STATE_ALL = 0;

	// Tasks.
	protected AsyncTask<String, Void, TaskResult> mFavTask;

	static final int DIALOG_WRITE_ID = 0;
	
	abstract protected int getLayoutId();
	abstract protected ListView getTweetList();
	abstract protected TweetAdapter getTweetAdapter();
	abstract protected void setupState();

	abstract protected String getActivityTitle();
	abstract protected boolean useBasicMenu();
	
	abstract protected Tweet getContextItemTweet(int position);

	abstract protected void updateTweet(Tweet tweet);

	public static final int CONTEXT_REPLY_ID = Menu.FIRST + 1;
	// public static final int CONTEXT_AT_ID = Menu.FIRST + 2;
	public static final int CONTEXT_RETWEET_ID = Menu.FIRST + 3;
	public static final int CONTEXT_DM_ID = Menu.FIRST + 4;
	public static final int CONTEXT_MORE_ID = Menu.FIRST + 5;
	public static final int CONTEXT_ADD_FAV_ID = Menu.FIRST + 6;
	public static final int CONTEXT_DEL_FAV_ID = Menu.FIRST + 7;
	
	/**
	 * 如果增加了Context Menu常量的数量，则必须重载此方法，
	 * 以保证其他人使用常量时不产生重复
	 * @return 最大的Context Menu常量
	 */
	protected int getLastContextMenuId(){
		return CONTEXT_DEL_FAV_ID;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getLayoutId());
		initHeader(HEADER_STYLE_HOME);

		mPreferences.getInt(Preferences.TWITTER_ACTIVITY_STATE_KEY, STATE_ALL);

		// 提示栏
		mProgressText = (TextView) findViewById(R.id.progress_text);
		
		setupState();

		registerForContextMenu(getTweetList());
		registerOnClickListener(getTweetList());		
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkIsLogedIn();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (useBasicMenu()){
			AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			Tweet tweet = getContextItemTweet(info.position);
			
			if (tweet == null) {
				Log.w(TAG, "Selected item not available.");
				return;
			}
			
			menu.add(0, CONTEXT_MORE_ID, 0, tweet.screenName + getResources().getString(R.string.cmenu_user_profile_prefix));
			menu.add(0, CONTEXT_REPLY_ID, 0, R.string.cmenu_reply);
			menu.add(0, CONTEXT_RETWEET_ID, 0, R.string.cmenu_retweet);
			menu.add(0, CONTEXT_DM_ID, 0, R.string.cmenu_direct_message);
	
			if (tweet.favorited.equals("true")) {
				menu.add(0, CONTEXT_DEL_FAV_ID, 0, R.string.cmenu_del_fav);
			} else {
				menu.add(0, CONTEXT_ADD_FAV_ID, 0, R.string.cmenu_add_fav);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Tweet tweet = getContextItemTweet(info.position);

		if (tweet == null) {
			Log.w(TAG, "Selected item not available.");
			return super.onContextItemSelected(item);
		}

		switch (item.getItemId()) {
		case CONTEXT_MORE_ID:
			launchActivity(UserActivity.createIntent(tweet.userId, tweet.screenName));
			return true;
		case CONTEXT_REPLY_ID: {
			// TODO: this isn't quite perfect. It leaves extra empty spaces if
			// you
			// perform the reply action again.
			String replyTo = "@" + tweet.screenName + " ";
			Intent intent = new Intent(WriteActivity.NEW_TWEET_ACTION, null,
					this, WriteActivity.class);
			intent.putExtra(WriteActivity.EXTRA_TEXT, replyTo);
			intent.putExtra(WriteActivity.REPLY_ID, tweet.id);
			startActivity(intent);

			return true;
		}
		case CONTEXT_RETWEET_ID:
			String prefix = mPreferences.getString(Preferences.RT_PREFIX_KEY,
					getString(R.string.pref_rt_prefix_default));
			String retweet = " "
					+ prefix
					+ " @"
					+ tweet.screenName
					+ " "
					+ tweet.text.replaceAll("<.*?>", "");//TODO: 使用更好的方法对TEXT进行格式化
			Intent intent = new Intent(WriteActivity.NEW_TWEET_ACTION, null,
					this, WriteActivity.class);
			intent.putExtra(WriteActivity.EXTRA_TEXT, retweet);
			intent.putExtra(WriteActivity.REPLY_ID, tweet.id);
			startActivity(intent);

			return true;
		case CONTEXT_DM_ID:
			launchActivity(DmActivity.createIntent(tweet.userId));
			return true;
		case CONTEXT_ADD_FAV_ID: 
			doFavorite("add", tweet.id);
			return true;
		case CONTEXT_DEL_FAV_ID: 
			doFavorite("del", tweet.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case OPTIONS_MENU_ID_REFRESH:
			//FIXME: 菜单中的刷新项是否可以删除了？
//			doRetrieve();
			return true;
		case OPTIONS_MENU_ID_TWEETS:
			launchActivity(TwitterActivity.createIntent(this));
			return true;
	      case OPTIONS_MENU_ID_REPLIES:
	        launchActivity(MentionActivity.createIntent(this));
	        return true;
	      case OPTIONS_MENU_ID_DM:
	        launchActivity(DmActivity.createIntent());
	        return true;
	    }

		return super.onOptionsItemSelected(item);
	}

	private void draw() {
		getTweetAdapter().refresh();
	}

	private void goTop() {
		getTweetList().setSelection(0);
	}

	private void doFavorite(String action, String id) {
		if (mFavTask != null && mFavTask.getStatus() == AsyncTask.Status.RUNNING) {
			Log.w(TAG, "FavTask still running");
		} else {
			if (!Utils.isEmpty(id)) {
//				mFavTask = new FavTask().execute(action, id);
				AsyncTask<String,Void,TaskResult> task = TaskFactory.create(TaskFactory.FAVORITE_TASK_TYPE, this);
				if (null != task) {
					mFavTask = task.execute(action, id);
				}
			}
		}
	}
	
	protected void adapterRefresh(){
		getTweetAdapter().refresh();
	}
	
	// for HasFavorite interface
	public void onFavSuccess() {
		// updateProgress(getString(R.string.refreshing));
		adapterRefresh();
	}

	public void onFavFailure() {
		// updateProgress(getString(R.string.refreshing));
	}
	
	protected void registerOnClickListener(ListView listView) {

		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Tweet tweet = getContextItemTweet(position);
		
				if (tweet == null) {
					Log.w(TAG, "Selected item not available.");
				}
				
				// TODO: launch statusActivity with real data
				launchActivity(StatusActivity.createIntent(tweet));
			}
		});

	}
}