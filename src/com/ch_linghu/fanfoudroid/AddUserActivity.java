package com.ch_linghu.fanfoudroid;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ch_linghu.fanfoudroid.data.User;
import com.ch_linghu.fanfoudroid.fanfou.Paging;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.ui.base.AddUserArrayBaseActivity;

public class AddUserActivity extends AddUserArrayBaseActivity {

	private String userId;
	private String userName;
//	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.FOLLOWING";
	private static final String USER_ID = "userId";
	private static final String USER_NAME = "userName";
	private int currentPage = 1;
	String myself = "";
	
	private ArrayList<com.ch_linghu.fanfoudroid.data.User> allUserList;
	
	static final String TAG = "AddUserActivity";

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			this.userId = extras.getString(USER_ID);
			this.userName = extras.getString(USER_NAME);
		} else {
			// 获取登录用户id
			userId = TwitterApplication.getMyselfId(false);
			userName = TwitterApplication.getMyselfName(false);
		}

		if (super._onCreate(savedInstanceState)) {

			myself = TwitterApplication.getMyselfId(false);
			if (getUserId() == myself) {
				mNavbar.setHeaderTitle(MessageFormat.format(
						getString(R.string.profile_friends_count_title), "我"));
			} else {
				mNavbar.setHeaderTitle(MessageFormat.format(
						getString(R.string.profile_friends_count_title),
						userName));
			}
			return true;
		} else {
			return false;
		}
	}


	@Override
	public Paging getNextPage() {
		currentPage += 1;
		return new Paging(currentPage);
	}

	@Override
	protected String getUserId() {
		return this.userId;
	}

	@Override
	public Paging getCurrentPage() {
		currentPage = 1;
		return new Paging(this.currentPage);
	}

	@Override
	protected List<com.ch_linghu.fanfoudroid.fanfou.User> getUsers(
			String userId, Paging page) throws HttpException {
		return getApi().getFriendsStatuses(userId, page);
	}
	
	/*
	 * TODO：单击列表项
	 */
	protected void registerOnClickListener(ListView listView) {

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				User user = getContextItemUser(position);
				if (user == null) {
					Log.w(TAG, "selected item not available");
					specialItemClicked(position);
				} else {
					Intent addUserIntent = new Intent();
					addUserIntent.putExtra("addedUserName",user.screenName);
					setResult(RESULT_OK,addUserIntent); 
					
					finish();
				}
			}
		});
	}

}
