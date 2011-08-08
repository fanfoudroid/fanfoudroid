/**
 * 
 */
package com.ch_linghu.fanfoudroid.ui.module;

import java.text.ParseException;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.app.SimpleImageLoader;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.db.StatusTable;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.util.TextHelper;

public class TweetCursorAdapter extends CursorAdapter implements TweetAdapter {
	private static final String TAG = "TweetCursorAdapter";

	private Context mContext;
	
	private static final int COMMON_BACKGROUND_COLOR = 0xFFFFFFFF;
	private static final int SELF_BACKGROUND_COLOR = 0xFFC9E6F8;
	private static final int MENTION_BACKGROUND_COLOR = 0xFFFAF4BE;
	

	public TweetCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		mContext = context;

		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}

		if (cursor != null) {
			// TODO: 可使用:
			// Tweet tweet = StatusTable.parseCursor(cursor);

			mUserTextColumn = cursor
					.getColumnIndexOrThrow(StatusTable.USER_SCREEN_NAME);
			mTextColumn = cursor.getColumnIndexOrThrow(StatusTable.TEXT);
			mProfileImageUrlColumn = cursor
					.getColumnIndexOrThrow(StatusTable.PROFILE_IMAGE_URL);
			mCreatedAtColumn = cursor
					.getColumnIndexOrThrow(StatusTable.CREATED_AT);
			mSourceColumn = cursor.getColumnIndexOrThrow(StatusTable.SOURCE);
			mInReplyToScreenName = cursor
					.getColumnIndexOrThrow(StatusTable.IN_REPLY_TO_SCREEN_NAME);
			mFavorited = cursor.getColumnIndexOrThrow(StatusTable.FAVORITED);
			mThumbnailPic = cursor.getColumnIndexOrThrow(StatusTable.PIC_THUMB);
			mMiddlePic = cursor.getColumnIndexOrThrow(StatusTable.PIC_MID);
			mOriginalPic = cursor.getColumnIndexOrThrow(StatusTable.PIC_ORIG);
		}
		mMetaBuilder = new StringBuilder();
	}

	private LayoutInflater mInflater;

	private int mUserTextColumn;
	private int mTextColumn;
	private int mProfileImageUrlColumn;
	private int mCreatedAtColumn;
	private int mSourceColumn;
	private int mInReplyToScreenName;
	private int mFavorited;
	private int mThumbnailPic;
	private int mMiddlePic;
	private int mOriginalPic;

	private StringBuilder mMetaBuilder;

	/*
	 * private ProfileImageCacheCallback callback = new
	 * ProfileImageCacheCallback(){
	 * 
	 * @Override public void refresh(String url, Bitmap bitmap) {
	 * TweetCursorAdapter.this.refresh(); }
	 * 
	 * };
	 */

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.tweet, parent, false);

		TweetCursorAdapter.ViewHolder holder = new ViewHolder();
		holder.tweetUserText = (TextView) view
				.findViewById(R.id.tweet_user_text);
		holder.tweetText = (TextView) view.findViewById(R.id.tweet_text);
		holder.profileLayout = (FrameLayout) view.findViewById(R.id.profile_layout);
		holder.profileImage = (ImageView) view.findViewById(R.id.profile_image);
		holder.metaText = (TextView) view.findViewById(R.id.tweet_meta_text);
		holder.fav = (ImageView) view.findViewById(R.id.tweet_fav);
		holder.has_image = (ImageView) view.findViewById(R.id.tweet_has_image);
		holder.tweetLayout=(LinearLayout)view.findViewById(R.id.tweet_layout);
		
		view.setTag(holder);

		return view;
	}

	private static class ViewHolder {
		public LinearLayout tweetLayout;
		public TextView tweetUserText;
		public TextView tweetText;
		public FrameLayout profileLayout;
		public ImageView profileImage;
		public TextView metaText;
		public ImageView fav;
		public ImageView has_image;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TweetCursorAdapter.ViewHolder holder = (TweetCursorAdapter.ViewHolder) view
				.getTag();

		SharedPreferences pref = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(mContext);;
		boolean useProfileImage = pref.getBoolean(
				Preferences.USE_PROFILE_IMAGE, true);
		holder.tweetUserText.setText(cursor.getString(mUserTextColumn));
		TextHelper.setSimpleTweetText(holder.tweetText,
				cursor.getString(mTextColumn));
		
		/**
		 * 添加特殊行的背景色
		 */
		if(holder.tweetUserText.getText().equals(TwitterApplication.getMyselfName())){
			holder.tweetLayout.setBackgroundColor(SELF_BACKGROUND_COLOR);
			holder.profileLayout.setBackgroundColor(SELF_BACKGROUND_COLOR);
		}else if(holder.tweetText.getText().toString().contains("@"+TwitterApplication.getMyselfName())){
			holder.tweetLayout.setBackgroundColor(MENTION_BACKGROUND_COLOR);
			holder.profileLayout.setBackgroundColor(MENTION_BACKGROUND_COLOR);
		}else{
			holder.tweetLayout.setBackgroundColor(COMMON_BACKGROUND_COLOR);
			holder.profileLayout.setBackgroundColor(COMMON_BACKGROUND_COLOR);
		}
		

		String profileImageUrl = cursor.getString(mProfileImageUrlColumn);
		if (useProfileImage && !TextUtils.isEmpty(profileImageUrl)) {
			holder.profileLayout.setVisibility(View.VISIBLE);
			SimpleImageLoader.display(holder.profileImage, profileImageUrl);
		} else {
			holder.profileLayout.setVisibility(View.GONE);
		}

		if (cursor.getString(mFavorited).equals("true")) {
			holder.fav.setVisibility(View.VISIBLE);
		} else {
			holder.fav.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(cursor.getString(mThumbnailPic))) {
			holder.has_image.setVisibility(View.VISIBLE);
		} else {
			holder.has_image.setVisibility(View.GONE);
		}

		try {
			Date createdAt = TwitterDatabase.DB_DATE_FORMATTER.parse(cursor
					.getString(mCreatedAtColumn));
			holder.metaText.setText(Tweet.buildMetaText(mMetaBuilder,
					createdAt, cursor.getString(mSourceColumn),
					cursor.getString(mInReplyToScreenName)));
		} catch (ParseException e) {
			Log.w(TAG, "Invalid created at data.");
		}
		
		
		
	}

	@Override
	public void refresh() {
		getCursor().requery();
	}
}