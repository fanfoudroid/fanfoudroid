package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.User;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.ui.base.UserArrayBaseActivity;
import com.ch_linghu.fanfoudroid.ui.base.UserCursorBaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.TweetAdapter;
import com.ch_linghu.fanfoudroid.ui.module.UserArrayAdapter;
import com.ch_linghu.fanfoudroid.weibo.IDs;
import com.ch_linghu.fanfoudroid.weibo.Paging;
import com.ch_linghu.fanfoudroid.weibo.Status;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class FollowersActivity extends UserArrayBaseActivity {
	
	private ListView mUserList;
	private UserArrayAdapter mAdapter;
	private static final String TAG = "FollowersActivity";
	
	private String userId;
	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.FOLLOWERS";
	private static final String USER_ID = "userId";
	private int currentPage=1;
	private int followersCount=0;
	private static final double PRE_PAGE_COUNT=100.0;//官方分页为每页100
	private int pageCount=0;
	
	private String[] ids;
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
		Toast.makeText(getBaseContext(), myself+"@"+getUserId(), Toast.LENGTH_SHORT);
		if(getUserId()==myself){
			setHeaderTitle("关注我的人");
		}
       
	}
	
	public static Intent createIntent(String userId) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(USER_ID, userId);
		return intent;
	}
	
/*
	@Override
	protected Cursor fetchUsers() {
		//根据IDs得到UserInfo
		Cursor userCursor=null;
		
		try {
			
			ids= getApi().getFollowersIDs(userId).getIDs();//收听者全部id
			followersCount=ids.length;//收听者总数
			pageCount=(int)Math.ceil(followersCount/PRE_PAGE_COUNT);//总页数
			userCursor=getDb().getUserInfoByIds(ids);
			Log.i(TAG, "the user's count is "+userCursor.getCount());
			
		} catch (WeiboException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		}
		return userCursor;
	}

	@Override
	public int getDatabaseType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String fetchMaxId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String fetchMinId() {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public List<com.ch_linghu.fanfoudroid.weibo.User> getUsers() throws WeiboException {
		
		return getApi().getFollowersList(this.userId,getNextPage());
	}



	@Override
	public List<com.ch_linghu.fanfoudroid.weibo.User> getUserSinceId(
			String maxId) throws WeiboException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Status> getMoreMessageFromId(String minId)
			throws WeiboException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addUsers(ArrayList<User> tusers) {
		getDb().syncUsers(tusers);
		
	}

	@Override
	protected String[] getIds() {
		
		return ids;
	}
	*/
	
	
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
		return getApi().getFollowersList(userId, page);
	}
	
}
