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
package com.ch_linghu.fanfoudroid.ui.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.StatusTable;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskManager;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.module.TweetAdapter;
import com.ch_linghu.fanfoudroid.ui.module.TweetCursorAdapter;
import com.ch_linghu.fanfoudroid.weibo.IDs;
import com.ch_linghu.fanfoudroid.weibo.Status;

/**
 * TwitterCursorBaseLine用于带有静态数据来源（对应数据库的，与twitter表同构的特定表）的展现
 */
public abstract class TwitterCursorBaseActivity extends TwitterListBaseActivity{
	static final String TAG = "TwitterListBaseActivity";

	// Views.
	protected ListView mTweetList;
	protected TweetCursorAdapter mTweetAdapter;
	
	protected View mListHeader;
	protected View mListFooter;
	
	protected TextView loadMoreBtn;
	protected ProgressBar loadMoreGIF;
	protected TextView loadMoreBtnTop;
	protected ProgressBar loadMoreGIFTop;
	
	protected static int lastPosition = 0;

	// Tasks.
	protected TaskManager taskManager = new TaskManager();
	private GenericTask mRetrieveTask;
	private GenericTask mFollowersRetrieveTask;
	private GenericTask mGetMoreTask;
	
	private int mRetrieveCount = 0;
	
	private TaskListener mRetrieveTaskListener = new TaskAdapter(){

		@Override
		public String getName() {
			return "RetrieveTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				SharedPreferences.Editor editor = getPreferences().edit();
				editor.putLong(Preferences.LAST_TWEET_REFRESH_KEY, Utils
						.getNowTime());
				editor.commit();
				//TODO: 1. StatusType(DONE) ; 
				if (mRetrieveCount >= StatusTable.MAX_ROW_NUM){
					//只有在取回的数据大于MAX时才做GC, 因为小于时可以保证数据的连续性
					getDb().gc(getUserId(), getDatabaseType()); // GC
				}
				draw();
				goTop();
			} else {
				// Do nothing.
			}

			// 刷新按钮停止旋转
			getRefreshButton().clearAnimation();
            loadMoreGIFTop.setVisibility(View.GONE);
			updateProgress("");
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveBegin();
		}

		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
			Log.d(TAG, "onProgressUpdate");
			draw();
		}
	};
	private TaskListener mFollowerRetrieveTaskListener = new TaskAdapter(){

		@Override
		public String getName() {
			return "FollowerRetrieve";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				SharedPreferences sp = getPreferences();
				SharedPreferences.Editor editor = sp.edit();
				editor.putLong(Preferences.LAST_FOLLOWERS_REFRESH_KEY,
						Utils.getNowTime());
				editor.commit();
			} else {
				// Do nothing.
			}
		}
	};

	// Refresh data at startup if last refresh was this long ago or greater.
	private static final long REFRESH_THRESHOLD = 5 * 60 * 1000;

	// Refresh followers if last refresh was this long ago or greater.
	private static final long FOLLOWERS_REFRESH_THRESHOLD = 12 * 60 * 60 * 1000;

	abstract protected void markAllRead();

	abstract protected Cursor fetchMessages();
	
	public abstract int getDatabaseType();
	
	public abstract String getUserId();

	public abstract String fetchMaxId();
	public abstract String fetchMinId();

	public abstract int addMessages(ArrayList<Tweet> tweets,
			boolean isUnread);

	public abstract List<Status> getMessageSinceId(String maxId)
			throws HttpException;
	
	public abstract List<Status> getMoreMessageFromId(String minId)
			throws HttpException;

	public static final int CONTEXT_REPLY_ID = Menu.FIRST + 1;
	// public static final int CONTEXT_AT_ID = Menu.FIRST + 2;
	public static final int CONTEXT_RETWEET_ID = Menu.FIRST + 3;
	public static final int CONTEXT_DM_ID = Menu.FIRST + 4;
	public static final int CONTEXT_MORE_ID = Menu.FIRST + 5;
	public static final int CONTEXT_ADD_FAV_ID = Menu.FIRST + 6;
	public static final int CONTEXT_DEL_FAV_ID = Menu.FIRST + 7;

	@Override
	protected void setupState() {
		Cursor cursor;

		cursor = fetchMessages(); // getDb().fetchMentions();
		setTitle(getActivityTitle());
		startManagingCursor(cursor);
		
	    mTweetList = (ListView) findViewById(R.id.tweet_list);

	    //TODO: 需处理没有数据时的情况
	    Log.i("LDS", cursor.getCount()+" cursor count");
	    setupListHeader(true);
	    
		mTweetAdapter = new TweetCursorAdapter(this, cursor);
		mTweetList.setAdapter(mTweetAdapter);
		//? registerOnClickListener(mTweetList);
	}
	
	/**
	 * 绑定listView底部 - 载入更多
	 * NOTE: 必须在listView#setAdapter之前调用
	 */
	protected void setupListHeader(boolean addFooter) {
        
        // Add Header to ListView
        mListHeader = View.inflate(this, R.layout.listview_header, null);
        mTweetList.addHeaderView(mListHeader, null, true);
        mListHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreGIFTop.setVisibility(View.VISIBLE);
                doRetrieve();
            }
        });
        
        //TODO: 完成listView顶部和底部的事件绑定
        mListFooter = View.inflate(this, R.layout.listview_footer, null);
        mTweetList.addFooterView(mListFooter, null, true);
        mListFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreGIF.setVisibility(View.VISIBLE);
                doGetMore();
            }
        });
        
        // Find View
        loadMoreBtn = (TextView)findViewById(R.id.ask_for_more);
        loadMoreGIF = (ProgressBar)findViewById(R.id.rectangleProgressBar);
        loadMoreBtnTop = (TextView)findViewById(R.id.ask_for_more_header);
        loadMoreGIFTop = (ProgressBar)findViewById(R.id.rectangleProgressBar_header);
        
        //loadMoreAnimation = (AnimationDrawable) loadMoreGIF.getIndeterminateDrawable();
    }
	
	@Override
	protected int getLayoutId(){
		return R.layout.main;
	}

	@Override
	protected ListView getTweetList(){
		return mTweetList;
	}

	@Override
	protected TweetAdapter getTweetAdapter(){
		return mTweetAdapter;
	}

	@Override
	protected boolean useBasicMenu(){
		return true;
	}

	@Override
	protected Tweet getContextItemTweet(int position){
		position = position - 1;
		//因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
		if (position >= 0 && position < mTweetAdapter.getCount()){
			Cursor cursor = (Cursor) mTweetAdapter.getItem(position);
			if (cursor == null){
				return null;
			}else{
				return StatusTable.parseCursor(cursor);
			}
		}else{
			return null;
		}
	}

	@Override
	protected void updateTweet(Tweet tweet){
	    // TODO: updateTweet() 在哪里调用的? 目前尚只支持:
	    // updateTweet(String tweetId, ContentValues values) 
	    // setFavorited(String tweetId, String isFavorited)
	    // 看是否还需要增加updateTweet(Tweet tweet)方法
	    
		//对所有相关表的对应消息都进行刷新（如果存在的话）
//		getDb().updateTweet(TwitterDbAdapter.TABLE_FAVORITE, tweet);
//		getDb().updateTweet(TwitterDbAdapter.TABLE_MENTION, tweet);
//		getDb().updateTweet(TwitterDbAdapter.TABLE_TWEET, tweet);
	}

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate.");
		if (super._onCreate(savedInstanceState)){
			goTop(); // skip the header
	
			// Mark all as read.
			// getDb().markAllMentionsRead();
			markAllRead();
	
			boolean shouldRetrieve = false;
	
			//FIXME： 该子类页面全部使用了这个统一的计时器，导致进入Mention等分页面后经常不会自动刷新
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
				// so we instead retrieve (refresh) to see if the message has
				// posted.
				Log.i(TAG,
						"Was last running a retrieve or send task. Let's refresh.");
				shouldRetrieve = true;
			}
	
			if (shouldRetrieve) {
				doRetrieve();
			}
	
			long lastFollowersRefreshTime = mPreferences.getLong(
					Preferences.LAST_FOLLOWERS_REFRESH_KEY, 0);
	
			diff = nowTime - lastFollowersRefreshTime;
			Log.i(TAG, "Last followers refresh was " + diff + " ms ago.");
	
			// Should Refresh Followers
			if (diff > FOLLOWERS_REFRESH_THRESHOLD && 
					(mRetrieveTask == null || mRetrieveTask.getStatus() != GenericTask.Status.RUNNING)) {
				Log.i(TAG, "Refresh followers.");
				doRetrieveFollowers();
			}
			
			return true;
		}else{
			return false;
	}
	
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume.");
		if (lastPosition != 0) {
		    mTweetList.setSelection(lastPosition);
		}
		super.onResume();
		checkIsLogedIn();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		// mTweetEdit.updateCharsRemain();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy.");
		super.onDestroy();
		
        taskManager.cancelAll();
	}
	
	@Override
    protected void onPause() {
		Log.i(TAG, "onPause.");
        super.onPause();
        lastPosition = mTweetList.getFirstVisiblePosition();
    }

    @Override
    protected void onRestart() {
		Log.i(TAG, "onRestart.");
        super.onRestart();
    }

    @Override
    protected void onStart() {
		Log.i(TAG, "onStart.");
        super.onStart();
    }

    @Override
    protected void onStop() {
		Log.i(TAG, "onStop.");
        super.onStop();
    }

	// UI helpers.
	
	@Override
    protected String getActivityTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
	protected void adapterRefresh() {
		mTweetAdapter.notifyDataSetChanged();
		mTweetAdapter.refresh();
	}

	//  Retrieve interface
	public void updateProgress(String progress) {
		mProgressText.setText(progress);
	}
	public void draw() {
		mTweetAdapter.refresh();
	}
	public void goTop() {
        Log.i(TAG, "goTop.");
		mTweetList.setSelection(1);
	}
	
	private void doRetrieveFollowers() {
        Log.i(TAG, "Attempting followers retrieve.");

        if (mFollowersRetrieveTask != null && mFollowersRetrieveTask.getStatus() == GenericTask.Status.RUNNING){
        	return;
        }else{
        	mFollowersRetrieveTask = new FollowersRetrieveTask();
        	mFollowersRetrieveTask.setListener(mFollowerRetrieveTaskListener);
        	mFollowersRetrieveTask.execute();
        	
        	taskManager.addTask(mFollowersRetrieveTask);
        	// Don't need to cancel FollowersTask (assuming it ends properly).
        	mFollowersRetrieveTask.setCancelable(false);
        }
    }
	
	public void onRetrieveBegin() {
		mRetrieveCount = 0;
		updateProgress(getString(R.string.page_status_refreshing));
	}

	public void doRetrieve() {
		Log.i(TAG, "Attempting retrieve.");

		// 旋转刷新按钮
		animRotate(refreshButton);

		if (mRetrieveTask != null && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING){
			return;
		}else{
			mRetrieveTask = new RetrieveTask();
			mRetrieveTask.setListener(mRetrieveTaskListener);
			mRetrieveTask.execute();
			
			// Add Task to manager
			taskManager.addTask(mRetrieveTask);
		}
	}
	// for Retrievable interface
	public ImageButton getRefreshButton() {
		return refreshButton;
	}
	
	private class RetrieveTask extends GenericTask{

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			List<com.ch_linghu.fanfoudroid.weibo.Status> statusList;

			String maxId = fetchMaxId(); // getDb().fetchMaxMentionId();

			try {
				statusList = getMessageSinceId(maxId);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			ArrayList<Tweet> tweets = new ArrayList<Tweet>();
			HashSet<String> imageUrls = new HashSet<String>();
			
			for (com.ch_linghu.fanfoudroid.weibo.Status status : statusList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}

				tweets.add(Tweet.create(status));

				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}

			mRetrieveCount = addMessages(tweets, false); // getDb().addMentions(tweets, false);

			return TaskResult.OK;
		}
		
	}
	
	private class FollowersRetrieveTask extends GenericTask{

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			try {
				// TODO: 目前仅做新API兼容性改动，待完善Follower处理
				IDs followers = getApi().getFollowersIDs();
				List<String> followerIds = Arrays.asList(followers.getIDs());
				getDb().syncFollowers(followerIds);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}
			return TaskResult.OK;
		}
	}
	
	
	// GET MORE TASK
	
	private class GetMoreTask extends GenericTask {
	    @Override
        protected TaskResult _doInBackground(TaskParams... params) {
			List<com.ch_linghu.fanfoudroid.weibo.Status> statusList;

			String minId = fetchMinId(); // getDb().fetchMaxMentionId();

			if(minId == null){
				return TaskResult.FAILED;
			}

			try {
				statusList = getMoreMessageFromId(minId);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			if(statusList == null){
				return TaskResult.FAILED;
			}

			ArrayList<Tweet> tweets = new ArrayList<Tweet>();
			HashSet<String> imageUrls = new HashSet<String>();
			
			for (com.ch_linghu.fanfoudroid.weibo.Status status : statusList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}

				tweets.add(Tweet.create(status));

				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}

			addMessages(tweets, false); // getDb().addMentions(tweets, false);

	        
	        return TaskResult.OK;
	    }
	}
	
	private TaskListener getMoreListener = new TaskAdapter() {
        
        @Override
        public String getName() {
            return "getMore";
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            super.onPostExecute(task, result);
            draw();
            getRefreshButton().clearAnimation();
            loadMoreGIF.setVisibility(View.GONE);
        }
        
    };
    
    public void doGetMore() {
        Log.i(TAG, "Attempting getMore.");

        // 旋转刷新按钮
        animRotate(refreshButton);

        if (mGetMoreTask != null && mGetMoreTask.getStatus() == GenericTask.Status.RUNNING){
            return;
        }else{
            mGetMoreTask = new GetMoreTask();
            mGetMoreTask.setListener(getMoreListener);
            mGetMoreTask.execute();
            
            // Add Task to manager
            taskManager.addTask(mGetMoreTask);
        }
    }
	
}