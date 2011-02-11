package com.ch_linghu.fanfoudroid.ui.module;

import java.util.ArrayList;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.data.User;
import com.ch_linghu.fanfoudroid.helper.ImageCache;
import com.ch_linghu.fanfoudroid.helper.ImageManager;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.ProfileImageCacheCallback;
import com.ch_linghu.fanfoudroid.helper.ProfileImageCacheManager;
import com.ch_linghu.fanfoudroid.helper.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//TODO: 目前仅为示例实现
public class UserArrayAdapter extends BaseAdapter {
	private static final String TAG = "TweetArrayAdapter";

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
		public TextView lastStatus;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean useProfileImage = pref.getBoolean(Preferences.USE_PROFILE_IMAGE, true);
		
		if (convertView == null) {
			view = mInflater.inflate(R.layout.follower_item, parent, false);

			ViewHolder holder = new ViewHolder();
			holder.profileImage = (ImageView) view.findViewById(R.id.profile_image);
			holder.screenName = (TextView) view.findViewById(R.id.screen_name);
			holder.userId = (TextView) view.findViewById(R.id.user_id);
			holder.lastStatus = (TextView) view.findViewById(R.id.last_status);
			view.setTag(holder);
		} else {
			view = convertView;
		}

		ViewHolder holder = (ViewHolder) view.getTag();
		
		User user = mUsers.get(position);

		//String profileImageUrl = user.profileImageUrl;

		holder.profileImage.setImageBitmap(ImageManager.mDefaultBitmap);
		holder.screenName.setText(user.screenName);
		holder.userId.setText(user.id);
		holder.lastStatus.setText(user.lastStatus);

		return view;
	}


	public void refresh(ArrayList<User> users) {
		mUsers = users;
		notifyDataSetChanged();
	}
}
