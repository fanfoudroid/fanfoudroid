package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.os.Bundle;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.StatusTablesInfo.StatusTable;
import com.ch_linghu.fanfoudroid.ui.base.TwitterCursorBaseActivity;
import com.ch_linghu.fanfoudroid.weibo.Paging;
import com.ch_linghu.fanfoudroid.weibo.Status;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

/**
 * 随便看看
 * @author jmx
 *
 */
public class BrowseActivity extends TwitterCursorBaseActivity {
	private static final String TAG = "BrowseActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setHeaderTitle(getActivityTitle());
	}

	@Override
	protected String getActivityTitle() {
		return getResources().getString(R.string.page_title_browse);
	}

	@Override
	public void addMessages(ArrayList<Tweet> tweets, boolean isUnread) {
	    getDb().putTweets(tweets, StatusTable.TYPE_BROWSE, isUnread);
	}

	@Override
	public String fetchMaxId() {
	    return getDb().fetchMaxTweetId(StatusTable.TYPE_BROWSE);
	}

	@Override
	protected Cursor fetchMessages() {
		return getDb().fetchAllTweets(StatusTable.TYPE_BROWSE);
	}

	@Override
	public List<Status> getMessageSinceId(String maxId) throws WeiboException {
		return getApi().getPublicTimeline();
	}

	@Override
	protected void markAllRead() {
		getDb().markAllTweetsRead(StatusTable.TYPE_BROWSE);
	}

	@Override
	public String fetchMinId() {
		//随便看看没有获取更多的功能
		return null;
	}

	@Override
	public List<Status> getMoreMessageFromId(String minId)
			throws WeiboException {
		//随便看看没有获取更多的功能
		return null;
	}

	@Override
	public int getDatabaseType() {
		return StatusTable.TYPE_BROWSE;
	}

}
