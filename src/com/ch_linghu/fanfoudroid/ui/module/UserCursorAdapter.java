/**
 * 
 */
package com.ch_linghu.fanfoudroid.ui.module;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.db.UserInfoTable;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.ProfileImageCacheCallback;
import com.ch_linghu.fanfoudroid.helper.Utils;

public class UserCursorAdapter extends CursorAdapter implements TweetAdapter {
	private static final String TAG = "TweetCursorAdapter";
	
	private Context mContext;

	public UserCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		mContext = context;

		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}

		if (cursor != null) {
			
			 mScreenNametColumn=cursor.getColumnIndexOrThrow(UserInfoTable.FIELD_USER_SCREEN_NAME);
		 mUserIdColumn=cursor.getColumnIndexOrThrow(UserInfoTable._ID);
			 mProfileImageUrlColumn=cursor.getColumnIndexOrThrow(UserInfoTable.FIELD_PROFILE_IMAGE_URL);
		// mLastStatusColumn=cursor.getColumnIndexOrThrow(UserInfoTable.FIELD_LAST_STATUS);

			
		}
		mMetaBuilder = new StringBuilder();
	}

	
	private LayoutInflater mInflater;

	private int mScreenNametColumn;
	private int mUserIdColumn;
	private int mProfileImageUrlColumn;
	//private int mLastStatusColumn;

	

	private StringBuilder mMetaBuilder;
	
	private ProfileImageCacheCallback callback = new ProfileImageCacheCallback(){

		@Override
		public void refresh(String url, Bitmap bitmap) {
			UserCursorAdapter.this.refresh();
		}
		
	};

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.follower_item, parent, false);
Log.i(TAG,"load newView");
		UserCursorAdapter.ViewHolder holder = new ViewHolder();
		holder.screenName=(TextView) view.findViewById(R.id.screen_name);
		holder.profileImage=(ImageView)view.findViewById(R.id.profile_image);
		//holder.lastStatus=(TextView) view.findViewById(R.id.last_status);
		holder.userId=(TextView) view.findViewById(R.id.user_id);
		view.setTag(holder);

		return view;
	}

	private static class ViewHolder {

		public TextView screenName;
		public TextView userId;
		public TextView lastStatus;
		public ImageView profileImage;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		UserCursorAdapter.ViewHolder holder = (UserCursorAdapter.ViewHolder) view
				.getTag();
		Log.i(TAG, "cursor count="+cursor.getCount());
		Log.i(TAG,"holder is null?"+(holder==null?"yes":"no"));
		SharedPreferences pref = TwitterApplication.mPref;  //PreferenceManager.getDefaultSharedPreferences(mContext);;
		boolean useProfileImage = pref.getBoolean(Preferences.USE_PROFILE_IMAGE, true);
		String profileImageUrl = cursor.getString(mProfileImageUrlColumn);
		if (useProfileImage){
		if (!Utils.isEmpty(profileImageUrl)) {
			holder.profileImage.setImageBitmap(TwitterApplication.mProfileImageCacheManager
					.get(profileImageUrl, callback));
		}
		}else{
			holder.profileImage.setVisibility(View.GONE);
		}
		holder.screenName.setText(cursor.getString(mScreenNametColumn));
		holder.userId.setText(cursor.getString(mUserIdColumn));
	}

	@Override
	public void refresh() {
		getCursor().requery();
	}
}