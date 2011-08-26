package com.ch_linghu.fanfoudroid.ui.module;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.app.LazyImageLoader.ImageLoaderCallback;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.data.User;
import com.ch_linghu.fanfoudroid.fanfou.Weibo;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;

/*
 * 用于用户的Adapter
 */
public class UserArrayAdapter extends BaseAdapter implements TweetAdapter {
	private static final String TAG = "UserArrayAdapter";
	private static final String USER_ID = "userId";

	protected ArrayList<User> mUsers;
	private Context mContext;
	protected LayoutInflater mInflater;

	public UserArrayAdapter(Context context) {
		mUsers = new ArrayList<User>();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mUsers.size();
	}

	@Override
	public Object getItem(int position) {
		return mUsers.get(position==0?0:position-1);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private static class ViewHolder {
		public ImageView profileImage;
		public TextView screenName;
		public TextView userId;
		public TextView lastStatus;
		public TextView followBtn;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;

		SharedPreferences pref = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean useProfileImage = pref.getBoolean(
				Preferences.USE_PROFILE_IMAGE, true);

		if (convertView == null) {
			view = mInflater.inflate(R.layout.follower_item, parent, false);

			ViewHolder holder = new ViewHolder();
			holder.profileImage = (ImageView) view
					.findViewById(R.id.profile_image);
			holder.screenName = (TextView) view.findViewById(R.id.screen_name);
			holder.userId = (TextView) view.findViewById(R.id.user_id);
			// holder.lastStatus = (TextView)
			// view.findViewById(R.id.last_status);
			holder.followBtn = (TextView) view.findViewById(R.id.follow_btn);

			view.setTag(holder);
		} else {
			view = convertView;
		}

		ViewHolder holder = (ViewHolder) view.getTag();

		final User user = mUsers.get(position);

		String profileImageUrl = user.profileImageUrl;
		if (useProfileImage) {
			if (!TextUtils.isEmpty(profileImageUrl)) {
				holder.profileImage
						.setImageBitmap(TwitterApplication.mImageLoader.get(
								profileImageUrl, callback));
			}
		} else {
			holder.profileImage.setVisibility(View.GONE);
		}
		// holder.profileImage.setImageBitmap(ImageManager.mDefaultBitmap);
		holder.screenName.setText(user.screenName);
		holder.userId.setText(user.id);
		// holder.lastStatus.setText(user.lastStatus);

		holder.followBtn.setText(user.isFollowing ? mContext
				.getString(R.string.general_del_friend) : mContext
				.getString(R.string.general_add_friend));

		holder.followBtn
				.setOnClickListener(user.isFollowing ? new OnClickListener() {

					@Override
					public void onClick(View v) {
						// Toast.makeText(mContext, user.name+"following",
						// Toast.LENGTH_SHORT).show();
						delFriend(user.id);
					}
				} : new OnClickListener() {

					@Override
					public void onClick(View v) {
						// Toast.makeText(mContext, user.name+"not following",
						// Toast.LENGTH_SHORT).show();

						addFriend(user.id);
					}
				});
		return view;
	}

	public void refresh(ArrayList<User> users) {
		mUsers = (ArrayList<User>)users.clone();
		notifyDataSetChanged();
	}

	@Override
	public void refresh() {
		notifyDataSetChanged();
	}

	private ImageLoaderCallback callback = new ImageLoaderCallback() {

		@Override
		public void refresh(String url, Bitmap bitmap) {
			UserArrayAdapter.this.refresh();
		}

	};

	/**
	 * 取消关注
	 * 
	 * @param id
	 */
	private void delFriend(final String id) {
		Builder diaBuilder = new AlertDialog.Builder(mContext).setTitle("关注提示")
				.setMessage("确实要取消关注吗?");
		diaBuilder.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (cancelFollowingTask != null
								&& cancelFollowingTask.getStatus() == GenericTask.Status.RUNNING) {
							return;
						} else {
							cancelFollowingTask = new CancelFollowingTask();
							cancelFollowingTask
									.setListener(cancelFollowingTaskLinstener);
							TaskParams params = new TaskParams();
							params.put(USER_ID, id);
							cancelFollowingTask.execute(params);
						}
					}
				});
		diaBuilder.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

					}
				});
		Dialog dialog = diaBuilder.create();
		dialog.show();
	}

	private GenericTask cancelFollowingTask;

	/**
	 * 取消关注
	 * 
	 * @author Dino
	 * 
	 */
	private class CancelFollowingTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			try {
				// TODO:userid
				String userId = params[0].getString(USER_ID);
				getApi().destroyFriendship(userId);
			} catch (HttpException e) {
				Log.w(TAG, "create friend ship error");
				return TaskResult.FAILED;
			}
			return TaskResult.OK;
		}

	}

	private TaskListener cancelFollowingTaskLinstener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				// followingBtn.setText("添加关注");
				// isFollowingText.setText(getResources().getString(
				// R.string.profile_notfollowing));
				// followingBtn.setOnClickListener(setfollowingListener);
				Toast.makeText(mContext, "取消关注成功", Toast.LENGTH_SHORT).show();

			} else if (result == TaskResult.FAILED) {
				Toast.makeText(mContext, "取消关注失败", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

	};

	private GenericTask setFollowingTask;

	/**
	 * 设置关注
	 * 
	 * @param id
	 */
	private void addFriend(String id) {
		Builder diaBuilder = new AlertDialog.Builder(mContext).setTitle("关注提示")
				.setMessage("确实要添加关注吗?");
		diaBuilder.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (setFollowingTask != null
								&& setFollowingTask.getStatus() == GenericTask.Status.RUNNING) {
							return;
						} else {
							setFollowingTask = new SetFollowingTask();
							setFollowingTask
									.setListener(setFollowingTaskLinstener);
							TaskParams params = new TaskParams();
							setFollowingTask.execute(params);
						}

					}
				});
		diaBuilder.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

					}
				});
		Dialog dialog = diaBuilder.create();
		dialog.show();
	}

	/**
	 * 设置关注
	 * 
	 * @author Dino
	 * 
	 */
	private class SetFollowingTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {

			try {
				String userId = params[0].getString(USER_ID);
				getApi().createFriendship(userId);

			} catch (HttpException e) {
				Log.w(TAG, "create friend ship error");
				return TaskResult.FAILED;
			}

			return TaskResult.OK;
		}

	}

	private TaskListener setFollowingTaskLinstener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				// followingBtn.setText("取消关注");
				// isFollowingText.setText(getResources().getString(
				// R.string.profile_isfollowing));
				// followingBtn.setOnClickListener(cancelFollowingListener);
				Toast.makeText(mContext, "关注成功", Toast.LENGTH_SHORT).show();

			} else if (result == TaskResult.FAILED) {
				Toast.makeText(mContext, "关注失败", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

	};

	public Weibo getApi() {
		return TwitterApplication.mApi;
	}

}
