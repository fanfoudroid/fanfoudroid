package com.temp.afan.activity;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.ui.module.NavBar;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;

/**
 * TimeLine Type:
 * <ul>
 * <li>FRIENDS_TIMELINE, 主页
 * <li>USER_TIMELINE, 个人空间
 * <li>PUBLIC_TIMELINE, 随便看看
 * <li>MENTIONS, 提到我的
 * <li>SEARCH, 搜索结果
 * <li>FAVORITES, 收藏
 */
public class TimelineActivity extends BaseListActivity {
    public static final String TAG = "TimelineList";

    public static final String EXTRA_TIMELINE_TYPE = "com.ch_linghu.fanfoudroid.TIMELINE_TYPE";

    public static final int TYPE_FRIENDS_TIMELINE = 0;
    public static final int TYPE_USER_TIMELINE = 1;
    public static final int TYPE_PUBLIC_TIMELINE = 2;
    public static final int TYPE_MENTIONS = 3;
    public static final int TYPE_SEARCH = 4;
    public static final int TYPE_FAVORITES = 5;
    private static final int[] mHeaderTitle = new int[] {
            R.string.header_title_friends_timeline,
            R.string.header_title_friends_timeline,
            R.string.header_title_friends_timeline,
            R.string.header_title_friends_timeline,
            R.string.header_title_friends_timeline, };
    private int mTimelineType = 0;

    // Views
    private NavBar mNavbar;

    private TimelineHandler mHandler;
    private TimelineAdapter mListAdapter;

    // Tasks
    private RefreshListTask mRefreshListTask;
    private GetMoreTask mGetMoreTask;

    public static void actionTimeline(Context context, int timelineType) {
        context.startActivity(createIntent(context, timelineType));
    }

    public static Intent createIntent(Context context, int timelineType) {
        Intent intent = new Intent(context, BaseListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_TIMELINE_TYPE, timelineType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statuses_list);
        Intent intent = getIntent();
        mTimelineType = intent.getIntExtra(EXTRA_TIMELINE_TYPE,
                TYPE_FRIENDS_TIMELINE);

        // View
        mNavbar = new NavBar(NavBar.HEADER_STYLE_HOME, this);
        mNavbar.setHeaderTitle(getHeaderTitle());

        mListAdapter = getMyListAdapter();
        setListAdapter(mListAdapter);
    }

    private void onRefresh() {
        mRefreshListTask = new RefreshListTask();
        mRefreshListTask.execute();
    }

    private void onGetMore() {
    }

    private void onDelete() {
    }

    private void onSetFavorite(long messageId, boolean newFavorite) {

    }

    protected TimelineAdapter getMyListAdapter() {
        switch (mTimelineType) {
        case TYPE_FRIENDS_TIMELINE:
        default:
            Cursor cursor = null;
            return new TimelineCusorAdapter(this, cursor);
        }
    }

    protected String getHeaderTitle() {
        int i = (mTimelineType < mHeaderTitle.length) ? mTimelineType : 0;
        return getResources().getString(mHeaderTitle[i]);
    }

    // TASKS

    /**
     * 刷新列表(获取最新消息)
     */
    private class RefreshListTask extends AsyncTask<Long, Void, Integer> {

        @Override
        protected Integer doInBackground(Long... params) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    /**
     * 载入更多列表项(请求旧消息)
     */
    private class GetMoreTask extends AsyncTask<Long, Void, Integer> {

        @Override
        protected Integer doInBackground(Long... params) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    /**
     * 删除单条信息
     */
    private class DeleteTask extends AsyncTask<Long, Void, Integer> {

        @Override
        protected Integer doInBackground(Long... params) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    /**
     * 收藏单条信息
     */
    private class SetFavoriteTask extends AsyncTask<Long, Void, Integer> {

        @Override
        protected Integer doInBackground(Long... params) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    /**
     * Handler for UI-thread operations
     */
    /* package */class TimelineHandler extends Handler {

        private static final int MSG_LOAD_ITEMS = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_LOAD_ITEMS:
                mListAdapter.doRefresh();
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    /* package */interface TimelineAdapter extends ListAdapter {
        void doRefresh();

        void doGetMore();
    }

    /* package */class TimelineCusorAdapter extends CursorAdapter implements
            TimelineAdapter {

        public TimelineCusorAdapter(Context context, Cursor c) {
            super(context, c);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void doRefresh() {
            // TODO Auto-generated method stub

        }

        @Override
        public void doGetMore() {
            // TODO Auto-generated method stub

        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // TODO Auto-generated method stub

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
