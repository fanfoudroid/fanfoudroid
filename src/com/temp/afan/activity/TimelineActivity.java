package com.temp.afan.activity;

import android.app.LauncherActivity.ListItem;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * TimeLine Type:
 *  - FRIENDS_TIMELINE
 *  - USER_TIMELINE
 *  - PUBLIC_TIMELINE
 *  - MENTIONS
 *  - SEARCH
 *  - FAVORITES
 * 
 */
public class TimelineActivity extends BaseListActivity {
    public static final String TAG = "TimelineList";
    
    public static final int FRIENDS_TIMELINE = 0;
    public static final int USER_TIMELINE = 1;
    public static final int PUBLIC_TIMELINE = 2;
    public static final int MENTIONS = 3;
    public static final int SEARCH = 4;
    public static final int FAVORITES = 5;
    
    // views
    
    private TimelineHandler mHandler;
    private TimelineAdapter mListAdapter;
    
    // tasks
    private RefreshListTask mRefreshListTask;
    private GetMoreTask mGetMoreTask;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        //Cursor cursor = new StatusDAO().fetchStatuses();
        //mListAdapter = new TimelineAdapter(this, cursor);
        //setListAdapter(mListAdapter);
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
    /* package */ class TimelineHandler extends Handler {
        
        private static final int MSG_LOAD_ITEMS = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_LOAD_ITEMS:
                mListAdapter.doRequery();
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
    
    
    /**
     * Status List Cursor Adapter
     */
    /* package */ class TimelineAdapter extends CursorAdapter {

        public TimelineAdapter(Context context, Cursor c) {
            super(context, c);
            // TODO Auto-generated constructor stub
        }

        public void doRequery() {
            
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
        
        public void updateFavorite(ListItem itemView, boolean newFavorite) {
            
        }
    }

}
