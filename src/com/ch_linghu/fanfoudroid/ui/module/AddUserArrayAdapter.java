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
 * 用于选择@用户的Adapter
 */
public class AddUserArrayAdapter extends BaseAdapter implements TweetAdapter {
	private static final String TAG = "AddUserArrayAdapter";
	private static final String USER_ID = "userId";

	protected ArrayList<User> mUsers;
	private Context mContext;
	protected LayoutInflater mInflater;

	public AddUserArrayAdapter(Context context) {
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
		return mUsers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		public ImageView profileImage;
		public TextView screenName;
		public TextView userId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;

		SharedPreferences pref = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean useProfileImage = pref.getBoolean(
				Preferences.USE_PROFILE_IMAGE, true);

		if (convertView == null) {
			view = mInflater.inflate(R.layout.adduser_item, parent, false);

			ViewHolder holder = new ViewHolder();
			holder.profileImage = (ImageView) view
					.findViewById(R.id.profile_image);
			holder.screenName = (TextView) view.findViewById(R.id.screen_name);
			holder.userId = (TextView) view.findViewById(R.id.user_id);

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
			AddUserArrayAdapter.this.refresh();
		}

	};



	private GenericTask setFollowingTask;


	public Weibo getApi() {
		return TwitterApplication.mApi;
	}

}
