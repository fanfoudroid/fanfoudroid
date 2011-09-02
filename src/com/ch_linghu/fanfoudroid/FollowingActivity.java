package com.ch_linghu.fanfoudroid;

import java.text.MessageFormat;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.ch_linghu.fanfoudroid.data.User;
import com.ch_linghu.fanfoudroid.fanfou.Paging;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.ui.base.UserArrayBaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.UserArrayAdapter;
import com.ch_linghu.fanfoudroid.R;

public class FollowingActivity extends UserArrayBaseActivity {

	private ListView mUserList;
	private UserArrayAdapter mAdapter;
	private String userId;
	private String userName;
	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.FOLLOWING";
	private static final String USER_ID = "userId";
	private static final String USER_NAME = "userName";
	private int currentPage = 1;
	String myself = "";

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			this.userId = extras.getString(USER_ID);
			this.userName = extras.getString(USER_NAME);
		} else {
			// 获取登录用户id
			userId = TwitterApplication.getMyselfId();
			userName = TwitterApplication.getMyselfName();
		}

		if (super._onCreate(savedInstanceState)) {

			myself = TwitterApplication.getMyselfId();
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

	/*
	 * 添加取消关注按钮
	 * 
	 * @see
	 * com.ch_linghu.fanfoudroid.ui.base.UserListBaseActivity#onCreateContextMenu
	 * (android.view.ContextMenu, android.view.View,
	 * android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (getUserId() == myself) {
			AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			User user = getContextItemUser(info.position);
			menu.add(
					0,
					CONTENT_DEL_FRIEND,
					0,
					getResources().getString(
							R.string.cmenu_user_addfriend_prefix)
							+ user.screenName
							+ getResources().getString(
									R.string.cmenu_user_friend_suffix));

		}

	}

	public static Intent createIntent(String userId, String userName) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(USER_ID, userId);
		intent.putExtra(USER_NAME, userName);
		return intent;
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

}
