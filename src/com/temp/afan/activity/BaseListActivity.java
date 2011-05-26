package com.temp.afan.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.ui.module.NavBar;
import com.temp.afan.Account;

/**
 * Base ListActivity
 */
public class BaseListActivity extends ListActivity implements
        OnItemClickListener {
    public static final String TAG = "TimelineList";

    private static final String EXTRA_TIMELINE_TYPE = "com.ch_linghu.fanfoudroid.activity.MAILBOX_TYPE";

    // Views
    protected ListView mListView;
    protected NavBar mNavbar;

    // Tasks

    public static void actionTimeline(Context context, int timelineType) {
        context.startActivity(createIntent(context, timelineType));
    }

    public static Intent createIntent(Context context, int timelineType) {
        Intent intent = new Intent(context, BaseListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (timelineType != -1)
            intent.putExtra(EXTRA_TIMELINE_TYPE, timelineType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mListView = getListView();
        mNavbar = new NavBar(NavBar.HEADER_STYLE_HOME, this);

        mListView.setOnItemClickListener(this);
        registerForContextMenu(mListView);

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Account.checkIsLogined()) {
            LoginActivity.actionStart(this);
            finish();
            return;
        }

        // restoreListPosition();
        // autoRefreshList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // cancel all tasks
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        super.onCreateContextMenu(menu, v, menuInfo);

        // getMenuInflater().inflate(R.menu.message_list_context_drafts, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        // MessageListItem itemView = (MessageListItem) info.targetView;
        /*
         * switch (item.getItemId()) { case .. }
         */

        return super.onContextItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
    }
}
