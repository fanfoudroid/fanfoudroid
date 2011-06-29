package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.dao.StatusDAO;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data2.Status;
import com.ch_linghu.fanfoudroid.data2.StatusHelper;
import com.ch_linghu.fanfoudroid.fanfou.Paging;
import com.ch_linghu.fanfoudroid.fanfou.User;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.http.HttpRefusedException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.Refreshable;
import com.ch_linghu.fanfoudroid.ui.base.TwitterListBaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.Feedback;
import com.ch_linghu.fanfoudroid.ui.module.FeedbackFactory;
import com.ch_linghu.fanfoudroid.ui.module.FeedbackFactory.FeedbackType;
import com.ch_linghu.fanfoudroid.ui.module.MyListView;
import com.ch_linghu.fanfoudroid.ui.module.TweetArrayAdapter;

public class UserTimelineActivity extends TwitterListBaseActivity implements
        MyListView.OnNeedMoreListener, Refreshable {

    private static final String TAG = UserTimelineActivity.class
            .getSimpleName();

    private Feedback mFeedback;
    private StatusHelper mStatusUtils;
    private StatusDAO mStatusDAO;

    private static final String EXTRA_USERID = "userID";
    private static final String EXTRA_NAME_SHOW = "showName";
    private static final String SIS_RUNNING_KEY = "running";

    private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.USERTIMELINE";

    public static Intent createIntent(String userID, String showName) {
        Intent intent = new Intent(LAUNCH_ACTION);
        intent.putExtra(EXTRA_USERID, userID);
        intent.putExtra(EXTRA_NAME_SHOW, showName);
        return intent;
    }

    // State.
    private User mUser;
    private String mUserID;
    private String mShowName;
    private ArrayList<Tweet> mTweets;
    private int mNextPage = 1;

    // Views.
    private TextView headerView;
    private TextView footerView;
    private MyListView mTweetList;
    // 记录服务器拒绝访问的信息
    private String msg;
    private static final int LOADINGFLAG = 1;
    private static final int SUCCESSFLAG = 2;
    private static final int NETWORKERRORFLAG = 3;
    private static final int AUTHERRORFLAG = 4;
    private TweetArrayAdapter mAdapter;

    // Tasks.
    private GenericTask mRetrieveTask;
    private GenericTask mLoadMoreTask;

    private TaskListener mRetrieveTaskListener = new TaskAdapter() {
        @Override
        public void onPreExecute(GenericTask task) {
            onRetrieveBegin();
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            if (result == TaskResult.AUTH_ERROR) {
                mFeedback.failed("登录失败, 请重新登录.");
                updateHeader(AUTHERRORFLAG);
                return;
            } else if (result == TaskResult.OK) {
                updateHeader(SUCCESSFLAG);
                updateFooter(SUCCESSFLAG);
                draw();
                goTop();
            } else if (result == TaskResult.IO_ERROR) {
                mFeedback.failed("更新失败.");
                updateHeader(NETWORKERRORFLAG);
            }
            mFeedback.success("");
        }

        @Override
        public String getName() {
            return "UserTimelineRetrieve";
        }
    };

    private TaskListener mLoadMoreTaskListener = new TaskAdapter() {

        @Override
        public void onPreExecute(GenericTask task) {
            onLoadMoreBegin();
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            if (result == TaskResult.AUTH_ERROR) {
                logout();
            } else if (result == TaskResult.OK) {
                mFeedback.success("");
                updateFooter(SUCCESSFLAG);
                draw();
            }
        }

        @Override
        public String getName() {
            return "UserTimelineLoadMoreTask";
        }
    };

    @Override
    protected boolean _onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "_onCreate()...");
        if (super._onCreate(savedInstanceState)) {
            mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);
            mStatusUtils = new StatusHelper(this);
            mStatusDAO = new StatusDAO(this);

            Intent intent = getIntent();
            // get user id
            mUserID = intent.getStringExtra(EXTRA_USERID);
            // show username in title
            mShowName = intent.getStringExtra(EXTRA_NAME_SHOW);

            // Set header title
            mNavbar.setHeaderTitle("@" + mShowName);

            boolean wasRunning = isTrue(savedInstanceState, SIS_RUNNING_KEY);

            // 此处要求mTweets不为空，最好确保profile页面消息为0时不能进入这个页面
            if (!mTweets.isEmpty() && !wasRunning) {
                updateHeader(SUCCESSFLAG);
                draw();
            } else {
                doRetrieve();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
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
    protected void onDestroy() {
        Log.d(TAG, "onDestroy.");
        if (mRetrieveTask != null
                && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            mRetrieveTask.cancel(true);
        }
        if (mLoadMoreTask != null
                && mLoadMoreTask.getStatus() == GenericTask.Status.RUNNING) {
            mLoadMoreTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    protected void draw() {
        mAdapter.refresh(mTweets);
    }

    public void goTop() {
        Log.d(TAG, "goTop.");
        mTweetList.setSelection(1);
    }

    public void doRetrieve() {
        Log.d(TAG, "Attempting retrieve.");
        if (mRetrieveTask != null
                && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mRetrieveTask = new UserTimelineRetrieveTask();
            mRetrieveTask.setListener(mRetrieveTaskListener);
            mRetrieveTask.execute();
        }
    }

    private void doLoadMore() {
        Log.d(TAG, "Attempting load more.");

        if (mLoadMoreTask != null
                && mLoadMoreTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            mLoadMoreTask = new UserTimelineLoadMoreTask();
            mLoadMoreTask.setListener(mLoadMoreTaskListener);
            mLoadMoreTask.execute();
        }
    }

    private void onRetrieveBegin() {
        mFeedback.start("");
        // 更新查询状态显示
        updateHeader(LOADINGFLAG);
        updateFooter(LOADINGFLAG);
        List<Status> statusList = mStatusDAO.getOneGroupStatus(
                TwitterApplication.getMyselfId(), mUserID,
                Status.TYPE_USER);
        for (com.ch_linghu.fanfoudroid.data2.Status status : statusList) {
            Tweet tweet;
            tweet = Tweet.create(status);
            mTweets.add(tweet);
        }
        mTweets.clear();
        addTweets(mTweets);
    }

    private void onLoadMoreBegin() {
        mFeedback.start("");
    }

    private class UserTimelineRetrieveTask extends GenericTask {
        ArrayList<Tweet> mTweets = new ArrayList<Tweet>();

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            List<com.ch_linghu.fanfoudroid.data2.Status> statusList = null;
            boolean hasNew = false;
            try {
                hasNew = mStatusUtils.getNewUserTimeline(mUserID);
                if (hasNew) {
                    statusList = mStatusDAO
                            .getOneGroupStatus(
                                    TwitterApplication.getMyselfId(),
                                    mUserID,
                                    com.ch_linghu.fanfoudroid.data2.Status.TYPE_USER);
                }
                mUser = getApi().showUser(mUserID);
                mFeedback.update(60);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                Throwable cause = e.getCause();
                if (cause instanceof HttpRefusedException) {
                    // AUTH ERROR
                    msg = ((HttpRefusedException) cause).getError()
                            .getMessage();
                    return TaskResult.AUTH_ERROR;
                } else {
                    return TaskResult.IO_ERROR;
                }
            }
            mFeedback.update(100 - (int) Math.floor(statusList.size() * 2)); // 60~100
            if (hasNew) {
                for (com.ch_linghu.fanfoudroid.data2.Status status : statusList) {
                    if (isCancelled()) {
                        return TaskResult.CANCELLED;
                    }
                    Tweet tweet;
                    tweet = Tweet.create(status);
                    mTweets.clear();
                    mTweets.add(tweet);
                    if (isCancelled()) {
                        return TaskResult.CANCELLED;
                    }
                }
                addTweets(mTweets);
            }
            if (isCancelled()) {
                return TaskResult.CANCELLED;
            }
            return TaskResult.OK;
        }
    }

    private class UserTimelineLoadMoreTask extends GenericTask {
        ArrayList<Tweet> mTweets = new ArrayList<Tweet>();

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            List<com.ch_linghu.fanfoudroid.data2.Status> statusList;
            try {
                statusList = mStatusUtils.getMoreUserTimeline(mUserID,
                        mNextPage);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                Throwable cause = e.getCause();
                if (cause instanceof HttpRefusedException) {
                    // AUTH ERROR
                    msg = ((HttpRefusedException) cause).getError()
                            .getMessage();
                    return TaskResult.AUTH_ERROR;
                } else {
                    return TaskResult.IO_ERROR;
                }
            }

            for (com.ch_linghu.fanfoudroid.data2.Status status : statusList) {
                if (isCancelled()) {
                    return TaskResult.CANCELLED;
                }
                Tweet tweet;
                tweet = Tweet.create(status);
                mTweets.add(tweet);
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
        // do more时没有更多时
        if (tweets.size() == 0) {
            mNextPage = -1;
            return;
        }
        mTweets.addAll(tweets);
        ++mNextPage;
    }

    @Override
    protected String getActivityTitle() {
        return "@" + mShowName;
    }

    @Override
    protected Tweet getContextItemTweet(int position) {

        if (position >= 1 && position <= mAdapter.getCount()) {
            return (Tweet) mAdapter.getItem(position - 1);
        } else {
            return null;
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.user_timeline;
    }

    @Override
    protected com.ch_linghu.fanfoudroid.ui.module.TweetAdapter getTweetAdapter() {
        return mAdapter;
    }

    @Override
    protected ListView getTweetList() {
        return mTweetList;
    }

    @Override
    protected void setupState() {
        mTweets = new ArrayList<Tweet>();
        mAdapter = new TweetArrayAdapter(this);
        mTweetList = (MyListView) findViewById(R.id.tweet_list);
        // Add Header to ListView
        headerView = (TextView) TextView.inflate(this,
                R.layout.user_timeline_header, null);
        mTweetList.addHeaderView(headerView);
        // Add Footer to ListView
        footerView = (TextView) TextView.inflate(this,
                R.layout.user_timeline_footer, null);
        mTweetList.addFooterView(footerView);
        mTweetList.setAdapter(mAdapter);
        mTweetList.setOnNeedMoreListener(this);
    }

    @Override
    protected void updateTweet(Tweet tweet) {
        // 该方法作用？
    }

    @Override
    protected boolean useBasicMenu() {
        return true;
    }

    private void updateHeader(int flag) {
        if (flag == LOADINGFLAG) {
            // 重新刷新页面时从第一页开始获取数据 --- phoenix
            mNextPage = 1;
            mTweets.clear();
            mAdapter.refresh(mTweets);
            headerView.setText(getResources()
                    .getString(R.string.search_loading));
        }
        if (flag == SUCCESSFLAG) {
            headerView.setText(getResources().getString(
                    R.string.user_query_status_success));
        }
        if (flag == NETWORKERRORFLAG) {
            headerView.setText(getResources().getString(
                    R.string.login_status_network_or_connection_error));
        }
        if (flag == AUTHERRORFLAG) {
            headerView.setText(msg);
        }
    }

    private void updateFooter(int flag) {
        if (flag == LOADINGFLAG) {
            footerView.setText("该用户总共？条消息");
        }
        if (flag == SUCCESSFLAG) {
            footerView.setText("该用户总共" + mUser.getStatusesCount() + "条消息，当前显示"
                    + mTweets.size() + "条。");
        }
    }
}