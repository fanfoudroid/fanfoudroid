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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.task.Deletable;
import com.ch_linghu.fanfoudroid.task.Followable;
import com.ch_linghu.fanfoudroid.task.HasFavorite;
import com.ch_linghu.fanfoudroid.task.Retrievable;
import com.ch_linghu.fanfoudroid.task.TaskFactory;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.TwitterCursorBaseActivity;
import com.ch_linghu.fanfoudroid.weibo.Paging;
import com.ch_linghu.fanfoudroid.weibo.Status;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

//TODO: 暂无获取更旧的消息（例如NeedMore()），用户将无法查看更旧的FriendsTimeline内容。
public class TwitterActivity extends TwitterCursorBaseActivity 
		implements Followable, Retrievable, HasFavorite, Deletable {
	private static final String TAG = "TwitterActivity";

	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.TWEETS";
	protected AsyncTask<String,Void,TaskResult> mDeleteTask;
	
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

	// Menu.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	private int CONTEXT_DELETE_ID = getLastContextMenuId() + 1;
	
	@Override
	protected int getLastContextMenuId(){
		return CONTEXT_DELETE_ID;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Tweet tweet = getContextItemTweet(info.position);
		
		if (tweet.userId.equals(getApi().getUserId())){
			menu.add(0, CONTEXT_DELETE_ID, 0, R.string.cmenu_delete);
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
		
		if (item.getItemId() == CONTEXT_DELETE_ID) {
			doDelete(tweet.id);
			return true;
		}else{
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected Cursor fetchMessages() {
		return getDb().fetchAllTweets(TwitterDbAdapter.TABLE_TWEET);
	}

	@Override
	protected String getActivityTitle() {
		return getResources().getString(R.string.page_title_home);
	}

	@Override
	protected void markAllRead() {
		getDb().markAllTweetsRead(TwitterDbAdapter.TABLE_TWEET);
	}
	
	
	// hasRetrieveListTask interface
	@Override
	public void addMessages(ArrayList<Tweet> tweets, boolean isUnread) {
		getDb().addTweets(TwitterDbAdapter.TABLE_TWEET, tweets, isUnread);
	}
	
	@Override
	public String fetchMaxId() {
		// TODO Auto-generated method stub
		return getDb().fetchMaxId(TwitterDbAdapter.TABLE_TWEET);
	}
	
	@Override
	public List<Status> getMessageSinceId(String maxId) throws WeiboException {
		if (maxId != null){
			return getApi().getFriendsTimeline(new Paging(maxId));
		}else{
			return getApi().getFriendsTimeline();
		}
	}

	@Override
	public void onDeleteFailure() {
		Log.e(TAG, "Delete failed");		
	}

	@Override
	public void onDeleteSuccess() {
		mTweetAdapter.refresh();
	}

	private void doDelete(String id) {
		if (mDeleteTask != null && mDeleteTask.getStatus() == AsyncTask.Status.RUNNING) {
			Log.w(TAG, "DeleteTask still running");
		} else {
			if (!Utils.isEmpty(id)) {
//				mFavTask = new FavTask().execute(action, id);
				AsyncTask<String,Void,TaskResult> task = TaskFactory.create(TaskFactory.DELETE_TASK_TYPE, this);
				if (null != task) {
					mDeleteTask = task.execute(id);
				}
			}
		}
	}

}