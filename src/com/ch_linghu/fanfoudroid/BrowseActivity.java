package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.db.StatusTable;
import com.ch_linghu.fanfoudroid.helper.Preferences;
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
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (super._onCreate(savedInstanceState)){
			setHeaderTitle(getActivityTitle());
			return true;
		}else{
			return false;
		}
	}

	@Override
	protected String getActivityTitle() {
		return getResources().getString(R.string.page_title_browse);
	}

	@Override
	public void addMessages(ArrayList<Tweet> tweets, boolean isUnread) {
	    getDb().putTweets(tweets, getUserId(), StatusTable.TYPE_BROWSE, isUnread);
	}

	@Override
	public String fetchMaxId() {
	    return getDb().fetchMaxTweetId(getUserId(), StatusTable.TYPE_BROWSE);
	}

	@Override
	protected Cursor fetchMessages() {
		return getDb().fetchAllTweets(getUserId(), StatusTable.TYPE_BROWSE);
	}

	@Override
	public List<Status> getMessageSinceId(String maxId) throws WeiboException {
		return getApi().getPublicTimeline();
	}

	@Override
	protected void markAllRead() {
		getDb().markAllTweetsRead(getUserId(), StatusTable.TYPE_BROWSE);
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

	@Override
	public String getUserId() {
		return TwitterApplication.getMyselfId();
	}

}
