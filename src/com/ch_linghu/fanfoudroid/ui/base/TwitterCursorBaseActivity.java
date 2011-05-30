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
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.fanfou.IDs;
import com.ch_linghu.fanfoudroid.fanfou.Status;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskManager;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.module.FlingGestureListener;
import com.ch_linghu.fanfoudroid.ui.module.MyActivityFlipper;
import com.ch_linghu.fanfoudroid.ui.module.SimpleFeedback;
import com.ch_linghu.fanfoudroid.ui.module.TweetAdapter;
import com.ch_linghu.fanfoudroid.ui.module.TweetCursorAdapter;
import com.ch_linghu.fanfoudroid.ui.module.Widget;
import com.ch_linghu.fanfoudroid.util.DateTimeHelper;
import com.ch_linghu.fanfoudroid.util.DebugTimer;
import com.ch_linghu.fanfoudroid.util.MiscHelper;

/**
 * TwitterCursorBaseLine用于带有静态数据来源（对应数据库的，与twitter表同构的特定表）的展现
 */
public abstract class TwitterCursorBaseActivity extends TwitterListBaseActivity {
  

    static final String TAG = "TwitterCursorBaseActivity";

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

    private TaskListener mRetrieveTaskListener = new TaskAdapter() {

        @Override
        public String getName() {
            return "RetrieveTask";
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            if (result == TaskResult.AUTH_ERROR) {
                mFeedback.failed("登录信息出错");
                logout();
            } else if (result == TaskResult.OK) {
                // TODO: XML处理, GC压力
                SharedPreferences.Editor editor = getPreferences().edit();
                editor.putLong(Preferences.LAST_TWEET_REFRESH_KEY,
                        DateTimeHelper.getNowTime());
                editor.commit();
                // TODO: 1. StatusType(DONE) ;
                if (mRetrieveCount >= StatusTable.MAX_ROW_NUM) {
                    // 只有在取回的数据大于MAX时才做GC, 因为小于时可以保证数据的连续性
                    getDb().gc(getUserId(), getDatabaseType()); // GC
                }
                draw();
                if (task == mRetrieveTask) {
                    goTop();
                }
            } else if (result == TaskResult.IO_ERROR) {
                // FIXME: bad smell
                if (task == mRetrieveTask) {
                    mFeedback.failed(((RetrieveTask) task).getErrorMsg());
                } else if (task == mGetMoreTask) {
                    mFeedback.failed(((GetMoreTask) task).getErrorMsg());
                }
            } else {
                // do nothing
            }

            // 刷新按钮停止旋转
            loadMoreGIFTop.setVisibility(View.GONE);
            loadMoreGIF.setVisibility(View.GONE);

            // DEBUG
            if (TwitterApplication.DEBUG) {
                DebugTimer.stop();
                Log.v("DEBUG", DebugTimer.getProfileAsString());
            }
        }

        @Override
        public void onPreExecute(GenericTask task) {
            mRetrieveCount = 0;

            if (TwitterApplication.DEBUG) {
                DebugTimer.start();
            }
        }

        @Override
        public void onProgressUpdate(GenericTask task, Object param) {
            Log.d(TAG, "onProgressUpdate");
            draw();
        }
    };

    private TaskListener mFollowerRetrieveTaskListener = new TaskAdapter() {

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
                        DateTimeHelper.getNowTime());
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

    public abstract int addMessages(ArrayList<Tweet> tweets, boolean isUnread);

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

        // TODO: 需处理没有数据时的情况
        Log.d("LDS", cursor.getCount() + " cursor count");
        setupListHeader(true);

        mTweetAdapter = new TweetCursorAdapter(this, cursor);
        mTweetList.setAdapter(mTweetAdapter);
        // ? registerOnClickListener(mTweetList);

    }

    /**
     * 绑定listView底部 - 载入更多 NOTE: 必须在listView#setAdapter之前调用
     */
    protected void setupListHeader(boolean addFooter) {
        // Add Header to ListView
        mListHeader = View.inflate(this, R.layout.listview_header, null);
        mTweetList.addHeaderView(mListHeader, null, true);

        // Add Footer to ListView
        mListFooter = View.inflate(this, R.layout.listview_footer, null);
        mTweetList.addFooterView(mListFooter, null, true);

        // Find View
        loadMoreBtn = (TextView) findViewById(R.id.ask_for_more);
        loadMoreGIF = (ProgressBar) findViewById(R.id.rectangleProgressBar);
        loadMoreBtnTop = (TextView) findViewById(R.id.ask_for_more_header);
        loadMoreGIFTop = (ProgressBar) findViewById(R.id.rectangleProgressBar_header);

    }

    @Override
    protected void specialItemClicked(int position) {
        // 注意 mTweetAdapter.getCount 和 mTweetList.getCount的区别
        // 前者仅包含数据的数量（不包括foot和head），后者包含foot和head
        // 因此在同时存在foot和head的情况下，list.count = adapter.count + 2
        if (position == 0) {
            // 第一个Item(header)
            loadMoreGIFTop.setVisibility(View.VISIBLE);
            doRetrieve();
        } else if (position == mTweetList.getCount() - 1) {
            // 最后一个Item(footer)
            loadMoreGIF.setVisibility(View.VISIBLE);
            doGetMore();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.main;
    }

    @Override
    protected ListView getTweetList() {
        return mTweetList;
    }

    @Override
    protected TweetAdapter getTweetAdapter() {
        return mTweetAdapter;
    }

    @Override
    protected boolean useBasicMenu() {
        return true;
    }

    @Override
    protected Tweet getContextItemTweet(int position) {
        position = position - 1;
        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
        if (position >= 0 && position < mTweetAdapter.getCount()) {
            Cursor cursor = (Cursor) mTweetAdapter.getItem(position);
            if (cursor == null) {
                return null;
            } else {
                return StatusTable.parseCursor(cursor);
            }
        } else {
            return null;
        }
    }

    @Override
    protected void updateTweet(Tweet tweet) {
        // TODO: updateTweet() 在哪里调用的? 目前尚只支持:
        // updateTweet(String tweetId, ContentValues values)
        // setFavorited(String tweetId, String isFavorited)
        // 看是否还需要增加updateTweet(Tweet tweet)方法

        // 对所有相关表的对应消息都进行刷新（如果存在的话）
        // getDb().updateTweet(TwitterDbAdapter.TABLE_FAVORITE, tweet);
        // getDb().updateTweet(TwitterDbAdapter.TABLE_MENTION, tweet);
        // getDb().updateTweet(TwitterDbAdapter.TABLE_TWEET, tweet);
    }

    @Override
    protected boolean _onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate.");
        if (super._onCreate(savedInstanceState)) {
            goTop(); // skip the header

            // Mark all as read.
            // getDb().markAllMentionsRead();
            markAllRead();

            boolean shouldRetrieve = false;

            // FIXME： 该子类页面全部使用了这个统一的计时器，导致进入Mention等分页面后经常不会自动刷新
            long lastRefreshTime = mPreferences.getLong(
                    Preferences.LAST_TWEET_REFRESH_KEY, 0);
            long nowTime = DateTimeHelper.getNowTime();

            long diff = nowTime - lastRefreshTime;
            Log.d(TAG, "Last refresh was " + diff + " ms ago.");

            if (diff > REFRESH_THRESHOLD) {
                shouldRetrieve = true;
            } else if (MiscHelper.isTrue(savedInstanceState, SIS_RUNNING_KEY)) {
                // Check to see if it was running a send or retrieve task.
                // It makes no sense to resend the send request (don't want
                // dupes)
                // so we instead retrieve (refresh) to see if the message has
                // posted.
                Log.d(TAG,
                        "Was last running a retrieve or send task. Let's refresh.");
                shouldRetrieve = true;
            }

            if (shouldRetrieve) {
                doRetrieve();
            }

            long lastFollowersRefreshTime = mPreferences.getLong(
                    Preferences.LAST_FOLLOWERS_REFRESH_KEY, 0);

            diff = nowTime - lastFollowersRefreshTime;
            Log.d(TAG, "Last followers refresh was " + diff + " ms ago.");

            // FIXME: 目前还没有对Followers列表做逻辑处理，因此暂时去除对Followers的获取。
            // 未来需要实现@用户提示时，对Follower操作需要做一次review和refactoring
            // 现在频繁会出现主键冲突的问题。
            //
            // Should Refresh Followers
            // if (diff > FOLLOWERS_REFRESH_THRESHOLD
            // && (mRetrieveTask == null || mRetrieveTask.getStatus() !=
            // GenericTask.Status.RUNNING)) {
            // Log.d(TAG, "Refresh followers.");
            // doRetrieveFollowers();
            // }

            // 手势识别
            registerGestureListener();

            return true;
        } else {
            return false;
        }

    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume.");
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
        Log.d(TAG, "onDestroy.");
        super.onDestroy();

        taskManager.cancelAll();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause.");
        super.onPause();
        lastPosition = mTweetList.getFirstVisiblePosition();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart.");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart.");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop.");
     
        super.onStop();
       
    }

    // UI helpers.

    @Override
    protected String getActivityTitle() {
        return null;
    }

    @Override
    protected void adapterRefresh() {
        mTweetAdapter.notifyDataSetChanged();
        mTweetAdapter.refresh();
    }

    // Retrieve interface
    public void updateProgress(String progress) {
        mProgressText.setText(progress);
    }

    public void draw() {
        mTweetAdapter.refresh();
    }

    public void goTop() {
        Log.d(TAG, "goTop.");
        mTweetList.setSelection(1);
    }

    private void doRetrieveFollowers() {
        Log.d(TAG, "Attempting followers retrieve.");

        if (mFollowersRetrieveTask != null
                && mFollowersRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mFollowersRetrieveTask = new FollowersRetrieveTask();
            mFollowersRetrieveTask.setListener(mFollowerRetrieveTaskListener);
            mFollowersRetrieveTask.execute();

            taskManager.addTask(mFollowersRetrieveTask);
            // Don't need to cancel FollowersTask (assuming it ends properly).
            mFollowersRetrieveTask.setCancelable(false);
        }
    }

    public void doRetrieve() {
        Log.d(TAG, "Attempting retrieve.");

        if (mRetrieveTask != null
                && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mRetrieveTask = new RetrieveTask();
            mRetrieveTask.setFeedback(mFeedback);
            mRetrieveTask.setListener(mRetrieveTaskListener);
            mRetrieveTask.execute();

            // Add Task to manager
            taskManager.addTask(mRetrieveTask);
        }
    }

    private class RetrieveTask extends GenericTask {
        private String _errorMsg;

        public String getErrorMsg() {
            return _errorMsg;
        }

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            List<com.ch_linghu.fanfoudroid.fanfou.Status> statusList;

            try {
                String maxId = fetchMaxId(); // getDb().fetchMaxMentionId();
                statusList = getMessageSinceId(maxId);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            ArrayList<Tweet> tweets = new ArrayList<Tweet>();
            for (com.ch_linghu.fanfoudroid.fanfou.Status status : statusList) {
                if (isCancelled()) {
                    return TaskResult.CANCELLED;
                }
                tweets.add(Tweet.create(status));
                if (isCancelled()) {
                    return TaskResult.CANCELLED;
                }
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, tweets));
            mRetrieveCount = addMessages(tweets, false);

            return TaskResult.OK;
        }

    }

    private class FollowersRetrieveTask extends GenericTask {

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
        private String _errorMsg;

        public String getErrorMsg() {
            return _errorMsg;
        }

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            List<com.ch_linghu.fanfoudroid.fanfou.Status> statusList;

            String minId = fetchMinId(); // getDb().fetchMaxMentionId();

            if (minId == null) {
                return TaskResult.FAILED;
            }

            try {
                statusList = getMoreMessageFromId(minId);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            if (statusList == null) {
                return TaskResult.FAILED;
            }

            ArrayList<Tweet> tweets = new ArrayList<Tweet>();
            publishProgress(SimpleFeedback.calProgressBySize(40, 20, tweets));

            for (com.ch_linghu.fanfoudroid.fanfou.Status status : statusList) {
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

    public void doGetMore() {
        Log.d(TAG, "Attempting getMore.");

        if (mGetMoreTask != null
                && mGetMoreTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mGetMoreTask = new GetMoreTask();
            mGetMoreTask.setFeedback(mFeedback);
            mGetMoreTask.setListener(mRetrieveTaskListener);
            mGetMoreTask.execute();

            // Add Task to manager
            taskManager.addTask(mGetMoreTask);
        }
    }

    // ////////////////// Gesture test /////////////////////////////////////
    private static boolean useGestrue;
    {
        useGestrue = TwitterApplication.mPref.getBoolean(
                Preferences.USE_GESTRUE, false);
        if (useGestrue) {
            Log.v(TAG, "Using Gestrue!");
        } else {
            Log.v(TAG, "Not Using Gestrue!");
        }
    }
    private String dir="";
    class FlingGestureListener2 extends SimpleOnGestureListener implements
            OnTouchListener {
        private static final String TAG = "FlipperGestureListener";

        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_DISTANCE = 350;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        private Widget.OnGestureListener mListener;
        private GestureDetector gDetector;

        public FlingGestureListener2(Context context,
                Widget.OnGestureListener listener) {
            this(context, listener, null);
        }

        public FlingGestureListener2(Context context,
                Widget.OnGestureListener listener, GestureDetector gDetector) {
            if (gDetector == null) {
                gDetector = new GestureDetector(context, this);
            }
            this.gDetector = gDetector;
            mListener = listener;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            Log.d(TAG, "On fling");
            boolean result = super.onFling(e1, e2, velocityX, velocityY);

            final float xDistance = Math.abs(e1.getX() - e2.getX());
            final float yDistance = Math.abs(e1.getY() - e2.getY());
            velocityX = Math.abs(velocityX);
            velocityY = Math.abs(velocityY);

            try {
                if (xDistance > SWIPE_MAX_DISTANCE
                        || yDistance > SWIPE_MAX_DISTANCE) {
                    Log.d(TAG, "OFF_PATH");
                    return result;
                }

                if (velocityX > SWIPE_THRESHOLD_VELOCITY
                        && xDistance > SWIPE_MIN_DISTANCE) {
                    if (e1.getX() > e2.getX()) {
                        Log.d(TAG, "<------");
                        result = mListener.onFlingLeft(e1, e1, velocityX,
                                velocityY);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    } else {
                        Log.d(TAG, "------>");
                        result = mListener.onFlingRight(e1, e1, velocityX,
                                velocityY);
                        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    }
                } else if (velocityY > SWIPE_THRESHOLD_VELOCITY
                        && yDistance > SWIPE_MIN_DISTANCE) {
                    if (e1.getY() > e2.getY()) {
                        Log.d(TAG, "up");
                        result = mListener.onFlingUp(e1, e1, velocityX,
                                velocityY);
                    } else {
                        Log.d(TAG, "down");
                        result = mListener.onFlingDown(e1, e1, velocityX,
                                velocityY);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "onFling error " + e.getMessage());
            }
            
            Log.i(TAG, "push_right");
            return result;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // TODO Auto-generated method stub
            super.onLongPress(e);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Log.d("FLING", "On Touch");

            // Within the MyGestureListener class you can now manage the
            // event.getAction() codes.

            // Note that we are now calling the gesture Detectors onTouchEvent.
            // And given we've set this class as the GestureDetectors listener
            // the onFling, onSingleTap etc methods will be executed.
            return gDetector.onTouchEvent(event);
        }

        public GestureDetector getDetector() {
            return gDetector;
        }
    }
    
    
    @Override
    public void finish() {
        
        super.finish();
        
       
    }

    protected FlingGestureListener2 myGestureListener = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (useGestrue && myGestureListener != null) {
            return myGestureListener.getDetector().onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    // use it in _onCreate
    private void registerGestureListener() {
        if (useGestrue) {
            myGestureListener = new FlingGestureListener2(this,
                    MyActivityFlipper.create(this));
            getTweetList().setOnTouchListener(myGestureListener);

        }
    }

}