package com.ch_linghu.fanfoudroid;

import java.text.MessageFormat;

import com.ch_linghu.fanfoudroid.data.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.data.db.UserInfoTable;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.ProfileImageCacheCallback;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.WithHeaderActivity;
import com.ch_linghu.fanfoudroid.weibo.User;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Dino 2011-02-26
 */
public class ProfileActivity extends WithHeaderActivity {
	private static final String TAG = "ProfileActivity";
	private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.PROFILE";

	private GenericTask profileInfoTask;// 获取用户信息

	private String userId;
	private String myself;
	
	private User profileInfo;// 用户信息

	private ImageView profileImageView;// 头像
	private TextView profileName;// 名称
	private TextView profileScreenName;// 昵称
	private TextView userLocation;// 地址
	private TextView userUrl;// url
	private TextView userInfo;// 自述
	private TextView friendsCount;// 好友
	private TextView followersCount;// 收听
	private TextView statusCount;// 消息
	private TextView favouritesCount;// 收藏

	private RelativeLayout friendsLayout;
	private LinearLayout followersLayout;
	private LinearLayout statusesLayout;
	private LinearLayout favouritesLayout;

	private static final String FANFOUROOT = "http://fanfou.com/";
	private static final String USER_ID = "userid";

	private TwitterDatabase db;

	public static Intent createIntent(String userId) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.putExtra(USER_ID, userId);
		return intent;
	}

	private ProfileImageCacheCallback callback = new ProfileImageCacheCallback() {

		@Override
		public void refresh(String url, Bitmap bitmap) {
			profileImageView.setImageBitmap(bitmap);
		}

	};

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {

		Log.d(TAG, "OnCreate start");
		if (super._onCreate(savedInstanceState)){
			setContentView(R.layout.profile);
			initHeader(HEADER_STYLE_HOME);
	
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
	
			myself = TwitterApplication.getMyselfId();
			if (extras != null) {
				this.userId = extras.getString(USER_ID);
			} else {
				this.userId = myself;
			}
			Uri data = intent.getData();
			if (data != null) {
				userId = data.getLastPathSegment();
			}
	
			// 初始化控件
			initControls();
	
			Log.i(TAG, "the userid is " + userId);
			db = this.getDb();
			draw();
			
			return true;
		}else{
			return false;
		}
	}

	private void initControls() {
		profileImageView = (ImageView) findViewById(R.id.profileimage);
		profileName = (TextView) findViewById(R.id.profilename);
		profileScreenName = (TextView) findViewById(R.id.profilescreenname);
		userLocation = (TextView) findViewById(R.id.user_location);
		userUrl = (TextView) findViewById(R.id.user_url);
		userInfo = (TextView) findViewById(R.id.tweet_user_info);
		friendsCount = (TextView) findViewById(R.id.friends_count);
		followersCount = (TextView) findViewById(R.id.followers_count);

		TextView friendsCountTitle = (TextView) findViewById(R.id.friends_count_title);
		TextView followersCountTitle = (TextView) findViewById(R.id.followers_count_title);
		String who;
		if (userId.equals(myself)){
			who = "我";
		} else {
			who = "ta";
		}
		friendsCountTitle.setText(MessageFormat.format(getString(R.string.profile_friends_count_title), who));
		followersCountTitle.setText(MessageFormat.format(getString(R.string.profile_followers_count_title), who));

		statusCount = (TextView) findViewById(R.id.statuses_count);
		favouritesCount = (TextView) findViewById(R.id.favourites_count);

		friendsLayout = (RelativeLayout) findViewById(R.id.friendsLayout);
		followersLayout = (LinearLayout) findViewById(R.id.followersLayout);
		statusesLayout = (LinearLayout) findViewById(R.id.statusesLayout);
		favouritesLayout = (LinearLayout) findViewById(R.id.favouritesLayout);

		// 为按钮面板添加事件
		friendsLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		followersLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//Toast.makeText(getBaseContext(), "跟随他的人", Toast.LENGTH_SHORT).show();
				 Intent intent = FollowersActivity.createIntent(userId);
				 intent.setClass(ProfileActivity.this,
				 FollowersActivity.class);
				 startActivity(intent);

			}
		});

		statusesLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				launchActivity(UserActivity.createIntent(profileInfo.getId(),
						profileInfo.getScreenName()));
			}
		});

		favouritesLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				 Intent intent = FavoritesActivity.createIntent(userId);
				 intent.setClass(ProfileActivity.this,
				 FavoritesActivity.class);
				 startActivity(intent);
			}
		});

		// 刷新
		View.OnClickListener refreshListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doGetProfileInfo();
			}
		};

		refreshButton.setOnClickListener(refreshListener);
	}

	private void draw() {
		Log.i(TAG, "draw");
		bindProfileInfo();
		// doGetProfileInfo();
	}

	@Override
	protected void onResume() {

		super.onResume();
		Log.i(TAG, "onResume.");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "onStart.");

	}

	@Override
	protected void onStop() {

		super.onStop();
		Log.i(TAG, "onStop.");
	}

	/**
	 * 从数据库获取,如果数据库不存在则创建
	 */
	private void bindProfileInfo() {

		if (null != db && db.existsUser(userId)) {

			Cursor cursor = db.getUserInfoById(userId);
			profileInfo = User.parseUser(cursor);
			cursor.close();
			if (profileInfo == null) {
				Log.w(TAG, "cannot get userinfo from userinfotable the id is"
						+ userId);
			}
			bindControl();
		} else {
			doGetProfileInfo();
		}
		

	}

	private void doGetProfileInfo() {
		// 旋转刷新按钮
		animRotate(refreshButton);

		if (profileInfoTask != null
				&& profileInfoTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			profileInfoTask = new GetProfileTask();
			profileInfoTask.setListener(profileInfoTaskListener);
			TaskParams params = new TaskParams();
			profileInfoTask.execute(params);
		}
	}

	private void bindControl() {
		setHeaderTitle("@" + profileInfo.getScreenName());

		profileImageView
				.setImageBitmap(TwitterApplication.mProfileImageCacheManager
						.get(profileInfo.getProfileImageURL().toString(),
								callback));
		
		profileName.setText(profileInfo.getId());
		
		profileScreenName.setText("@"+profileInfo.getScreenName());
		
		String location = profileInfo.getLocation();
		if (location == null || location.length()==0) {
			location = getResources().getString(R.string.profile_location_null);
		}
		userLocation.setText(location);
		
		if (profileInfo.getURL() != null) {
			userUrl.setText(profileInfo.getURL().toString());
		} else {
			userUrl.setText(FANFOUROOT + profileInfo.getId());
		}
		
		String description = profileInfo.getDescription();
		if (description == null || description.length()==0) {
			description = getResources().getString(
					R.string.profile_description_null);
		}
		userInfo.setText(description);

		friendsCount.setText(String.valueOf(profileInfo.getFriendsCount()));
		
		followersCount.setText(String.valueOf(profileInfo.getFollowersCount()));
		
		statusCount.setText(String.valueOf(profileInfo.getStatusesCount()));
		
		favouritesCount
				.setText(String.valueOf(profileInfo.getFavouritesCount()));
		
	}

	private TaskListener profileInfoTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			refreshButton.clearAnimation();

			// 加载成功
			if (result == TaskResult.OK) {
				if (null != db && !db.existsUser(userId)) {
					
						
						com.ch_linghu.fanfoudroid.data.User userinfodb = profileInfo
								.parseUser();
						db.createUserInfo(userinfodb);
				

				} else {
					// 更新用户
					updateUser();
				}
				// 绑定控件
				bindControl();

			}

		}

		@Override
		public String getName() {
			return "GetProfileInfo";
		}

	};

	/**
	 * 更新数据库中的用户
	 * 
	 * @return
	 */
	private boolean updateUser() {
		ContentValues args = new ContentValues();

		args.put(BaseColumns._ID, profileInfo.getName());
		
		args.put(UserInfoTable.FIELD_USER_NAME, profileInfo.getName());
		
		args.put(UserInfoTable.FIELD_USER_SCREEN_NAME,
				profileInfo.getScreenName());
		
		String location = profileInfo.getLocation();
		args.put(UserInfoTable.FIELD_LOCALTION, location);
		
		String description = profileInfo.getDescription();
		args.put(UserInfoTable.FIELD_DESCRIPTION, description);
		
		args.put(UserInfoTable.FIELD_PROFILE_IMAGE_URL,
				profileInfo.getProfileBackgroundImageUrl());

		if (profileInfo.getURL() != null) {
			args.put(UserInfoTable.FIELD_URL, profileInfo.getURL().toString());
		}

		args.put(UserInfoTable.FIELD_PROTECTED, profileInfo.isProtected());
		
		args.put(UserInfoTable.FIELD_FOLLOWERS_COUNT,
				profileInfo.getFollowersCount());
		
		args.put(UserInfoTable.FIELD_LAST_STATUS, profileInfo.getStatusSource());
		
		args.put(UserInfoTable.FIELD_FRIENDS_COUNT,
				profileInfo.getFriendsCount());
		
		args.put(UserInfoTable.FIELD_FAVORITES_COUNT,
				profileInfo.getFavouritesCount());
		
		args.put(UserInfoTable.FIELD_STATUSES_COUNT,
				profileInfo.getStatusesCount());
		
		args.put(UserInfoTable.FIELD_FOLLOWING, profileInfo.isFollowing());
		
		return db.updateUser(profileInfo.getId(), args);
	}

	/**
	 * 获取用户信息task
	 * 
	 * @author Dino
	 * 
	 */
	private class GetProfileTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {

			try {
				profileInfo = getApi().showUser(userId);

			} catch (WeiboException e) {

				Log.e(TAG, e.getMessage());
				return TaskResult.FAILED;
			}
			return TaskResult.OK;
		}

	}
}
