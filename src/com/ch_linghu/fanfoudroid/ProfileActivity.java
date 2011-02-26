package com.ch_linghu.fanfoudroid;

import java.net.URL;

import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.ProfileImageCacheCallback;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.WithHeaderActivity;
import com.ch_linghu.fanfoudroid.ui.module.TweetCursorAdapter;
import com.ch_linghu.fanfoudroid.weibo.User;
import com.ch_linghu.fanfoudroid.weibo.WeiboException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
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
	private GenericTask profileImageTask;// 获取用户图片
	private GenericTask profileInfoTask;// 获取用户信息
	private User profileInfo;// 用户信息
	private Bitmap profileImage;// 用户头像

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

	private ProgressDialog dialog;

	private static final String FANFOUROOT = "http://fanfou.com/";

	private ProfileImageCacheCallback callback = new ProfileImageCacheCallback() {

		@Override
		public void refresh(String url, Bitmap bitmap) {
			profileImageView.refreshDrawableState();
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.myprofile);
		initHeader(HEADER_STYLE_BACK);
		// try {
		// //User showUser = getApi().showUser(getApi().getUserId());
		// //Toast.makeText(this, showUser.getName(), Toast.LENGTH_LONG).show();
		// } catch (WeiboException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		initControls();
		draw();
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
				// TODO Auto-generated method stub

			}
		});

		statusesLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		favouritesLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void draw() {
		Log.i(TAG, "draw");
		dialog = ProgressDialog.show(ProfileActivity.this, "", "数据加载中....");
		doGetProfileInfo();

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

	private void doGetProfileInfo() {
		// 旋转刷新按钮
		// animRotate(refreshButton);

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

	private void doGetProfileImage(String imageUrl) {
		// 旋转刷新按钮
		// animRotate(refreshButton);
		if (profileImageTask != null
				&& profileImageTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {

			profileImageTask = new GetProfileImageTask();
			profileImageTask.setListener(profileImageTaskListener);
			TaskParams params = new TaskParams();
			params.put("imageUrl", imageUrl);
			profileImageTask.execute(params);

		}
	}

	private TaskListener profileInfoTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			// 加载成功
			if (result == TaskResult.OK) {

				if (profileInfo != null) {
					String imageurl = profileInfo.getProfileImageURL()
							.toString();
					doGetProfileImage(imageurl);
				}

				// Toast.makeText(getBaseContext(),
				// "用户名:"+profileInfo.getName(), Toast.LENGTH_LONG).show();
				profileName.setText(profileInfo.getId());
				profileScreenName.setText(profileInfo.getScreenName());
				userLocation.setText(profileInfo.getLocation());
				if (profileInfo.getURL() != null) {
					userUrl.setText(profileInfo.getURL().toString());
				} else {
					userUrl.setText(FANFOUROOT + profileInfo.getId());
				}
				if (profileInfo.getDescription() != null
						&& profileInfo.getDescription() == "") {
					userInfo.setText(R.string.profile_description_null);
				} else {
					userInfo.setText(profileInfo.getDescription());
				}

				friendsCount.setText(String.valueOf(profileInfo
						.getFriendsCount()));
				followersCount.setText(String.valueOf(profileInfo
						.getFollowersCount()));
				statusCount.setText(String.valueOf(profileInfo
						.getStatusesCount()));
				favouritesCount.setText(String.valueOf(profileInfo
						.getFavouritesCount()));
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		}

		@Override
		public String getName() {
			return "GetInfo";
		}

	};

	private TaskListener profileImageTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {

			if (result == TaskResult.OK) {
				Log.d(TAG, "pic load complete");
				profileImageView.setImageBitmap(profileImage);

			}
		}

		@Override
		public String getName() {

			return "GetProfileImage";
		}

	};

	/**
	 * 获取用户信息task
	 * 
	 * @author Dino
	 * 
	 */
	private class GetProfileTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			// TODO
			try {
				profileInfo = getApi().showUser(getApi().getUserId());
			} catch (WeiboException e) {

				return TaskResult.FAILED;
			}
			return TaskResult.OK;
		}
	}

	/**
	 * 获取用户头像task
	 * 
	 * @author Dino
	 * 
	 */
	private class GetProfileImageTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			try {
				String imageUrl = param.getString("imageUrl");
				Log.d(TAG, imageUrl);
				profileImage = TwitterApplication.mProfileImageCacheManager
						.get(imageUrl, callback);

			} catch (WeiboException e) {
				Log.e(TAG, e.getMessage());
				return TaskResult.FAILED;
			}
			return TaskResult.OK;
		}

	}

}
