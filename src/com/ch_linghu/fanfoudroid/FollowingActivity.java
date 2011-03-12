package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.List;

import com.ch_linghu.fanfoudroid.data.User;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.ui.base.UserArrayBaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.UserArrayAdapter;
import com.ch_linghu.fanfoudroid.weibo.Paging;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ListView;

public class FollowingActivity extends UserArrayBaseActivity {

	private ListView mUserList;
	private UserArrayAdapter mAdapter;
	private String userId;
	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.FOLLOWING";
	private static final String USER_ID = "userId";
	private int currentPage=1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		   Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				this.userId = extras.getString(USER_ID);
			} else {
				// 获取登录用户id
//				SharedPreferences preferences = PreferenceManager
//						.getDefaultSharedPreferences(this);
//				userId = preferences.getString(Preferences.CURRENT_USER_ID,
//						TwitterApplication.mApi.getUserId());
				userId=TwitterApplication.getMyselfId();
			}
			Uri data = intent.getData();
			if (data != null) {
				userId = data.getLastPathSegment();
			}
			super.onCreate(savedInstanceState);
			
			String myself = TwitterApplication.getMyselfId();
			if(getUserId()==myself){
				setHeaderTitle("我关注的人");
			}
	}
	
	public static Intent createIntent(String userId) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(USER_ID, userId);
		return intent;
	}
	
	
	@Override
	public Paging getNextPage() {
		currentPage+=1;
		return new Paging(currentPage);
	}
	

	@Override
	protected String getUserId() {
		return this.userId;
	}

	@Override
	public Paging getCurrentPage() {

		return new Paging(this.currentPage);
	}

	@Override
	protected List<com.ch_linghu.fanfoudroid.weibo.User> getUsers(
			String userId, Paging page) throws WeiboException {
		return getApi().getFriendsStatuses(userId, page);
	}

}
