package com.ch_linghu.fanfoudroid;

import java.util.ArrayList;
import java.util.List;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.User;
import com.ch_linghu.fanfoudroid.ui.base.UserCursorBaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.UserArrayAdapter;
import com.ch_linghu.fanfoudroid.weibo.Status;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

public class FollowersActivity extends UserCursorBaseActivity {
	
	private ListView mUserList;
	private UserArrayAdapter mAdapter;
	private static final String TAG = "FollowersActivity";

	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.FOLLOWERS";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
       // setContentView(R.layout.follower);
        
        setHeaderTitle("饭否fanfou.com");
        
//        //FIXME: 以下代码仅用于示例用途
//        ArrayList<User> users = new ArrayList<User>();
//        for(int i = 0; i < 32; ++i){
//        	User user = new User();
//        	user.id = "test" + i;
//        	user.screenName = "测试" + i;
//        	user.lastStatus = "谢谢万能的饭否 转@葡萄球君 万能的饭否告诉您 是位于埃及西奈半岛南端的一座城市，在红海与西奈山之间的海岸地带。距伊斯梅利亚大约四小时车程，距塔巴三小时车程。 转@和菜头 沙姆沙伊赫是哪里？";
//        	users.add(user);
//        }
//        
//        mAdapter = new UserArrayAdapter(this);
//        mUserList = (ListView)findViewById(R.id.follower_list);
//        mUserList.setAdapter(mAdapter);
//        mAdapter.refresh(users);
        
	}
	
	public static Intent createIntent(Context context) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		return intent;
	}
	
	

	@Override
	protected void markAllRead() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Cursor fetchUsers() {
		// TODO Auto-generated method stub
		return getDb().getAllUserInfo();
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
		
		return getApi().getFriendsStatuses();
	}

	@Override
	public void addMessages(ArrayList<Tweet> tweets, boolean isUnread) {
		// TODO Auto-generated method stub
		
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

}
