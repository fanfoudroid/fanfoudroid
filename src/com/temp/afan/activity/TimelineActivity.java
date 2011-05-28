package com.temp.afan.activity;

import java.text.ParseException;
import java.util.Date;

import android.app.LauncherActivity.ListItem;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.app.SimpleImageLoader;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.ui.module.NavBar;
import com.ch_linghu.fanfoudroid.util.TextHelper;

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
        setTimelineType(getIntent());

        // View
        mNavbar = new NavBar(NavBar.HEADER_STYLE_HOME, this);
        mNavbar.setHeaderTitle(getHeaderTitle());

        mListAdapter = getMyListAdapter();
        setListAdapter(mListAdapter);

        loadTimeline();
        autoRefreshList();
    }

    private void loadTimeline() {
        switch (mTimelineType) {
        case TYPE_FRIENDS_TIMELINE:
            Cursor cursor = null;
            ((CursorAdapter) mListAdapter).changeCursor(cursor);
            break;
        case TYPE_PUBLIC_TIMELINE:
            //TODO; arrayAdapter
            break;
        default:
            //TODO;
        }
    }

    private void autoRefreshList() {
        // TODO;
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

    private void setTimelineType(Intent intent) {
        mTimelineType = intent.getIntExtra(EXTRA_TIMELINE_TYPE,
                TYPE_FRIENDS_TIMELINE);
    }

    protected TimelineAdapter getMyListAdapter() {
        switch (mTimelineType) {
        case TYPE_FRIENDS_TIMELINE:
        default:
            return new TimelineCusorAdapter(this);
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

    /**
     * 
     */
    /* package */interface TimelineAdapter extends ListAdapter {
        void doRefresh();

        void doGetMore();
    }

    /**
     * 
     */
    /* package */class TimelineCusorAdapter extends CursorAdapter implements
            TimelineAdapter {

        // TODO
        public static final int COLUMN_ID = 0;
        public static final int COLUMN_TEXT = 0;
        public static final int COLUMN_PROFILE_IMAGE_URL = 0;
        public static final int COLUMEN_FAVORITED = 0;
        public static final int COLUMEN_PIC_THUMB = 0;
        public static final int COLUMN_CREATED = 0;
        public static final int COLUMEN_SOURCE = 0;
        public static final int COLUMN_IN_REPLY_TO_SCREEN_NAME = 0;

        Context mContext;
        private LayoutInflater mInflater;
        private StringBuilder mMetaBuilder;
        // DRAWABLE

        // auto refresh

        private java.text.DateFormat mDateFormat;

        public TimelineCusorAdapter(Context context) {
            super(context, null, true);
            mContext = context;
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mDateFormat = android.text.format.DateFormat.getDateFormat(context);
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

            TextView tweetUserText = (TextView) view
                    .findViewById(R.id.tweet_user_text);
            TextView tweetText = (TextView) view.findViewById(R.id.tweet_text);
            ImageView profileImage = (ImageView) view
                    .findViewById(R.id.profile_image);
            TextView metaText = (TextView) view
                    .findViewById(R.id.tweet_meta_text);
            ImageView fav = (ImageView) view.findViewById(R.id.tweet_fav);
            ImageView has_image = (ImageView) view
                    .findViewById(R.id.tweet_has_image);

            SharedPreferences pref = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(mContext);;
            boolean useProfileImage = pref.getBoolean(
                    Preferences.USE_PROFILE_IMAGE, true);
            tweetUserText.setText(cursor.getString(COLUMN_TEXT));
            TextHelper.setSimpleTweetText(tweetText,
                    cursor.getString(COLUMN_TEXT));

            String profileImageUrl = cursor.getString(COLUMN_PROFILE_IMAGE_URL);
            if (useProfileImage && !TextUtils.isEmpty(profileImageUrl)) {
                SimpleImageLoader.display(profileImage, profileImageUrl);
            } else {
                profileImage.setVisibility(View.GONE);
            }

            if (cursor.getString(COLUMEN_FAVORITED).equals("true")) {
                fav.setVisibility(View.VISIBLE);
            } else {
                fav.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(cursor.getString(COLUMEN_PIC_THUMB))) {
                has_image.setVisibility(View.VISIBLE);
            } else {
                has_image.setVisibility(View.GONE);
            }

            try {
                Date createdAt = mDateFormat.parse(cursor
                        .getString(COLUMN_CREATED));
                metaText.setText(Tweet.buildMetaText(mMetaBuilder, createdAt,
                        cursor.getString(COLUMEN_SOURCE),
                        cursor.getString(COLUMN_IN_REPLY_TO_SCREEN_NAME)));
            } catch (ParseException e) {
                Log.w(TAG, "Invalid created at data.");
            }

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater
                    .inflate(R.layout.statuses_list_item, parent, false);
        }

        public void updateFavorite(ListItem itemView, boolean newFavorite) {
            // TODO Auto-generated method stub
        }

    }

}
