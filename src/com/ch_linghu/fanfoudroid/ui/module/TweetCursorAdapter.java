/**
 * 
 */
package com.ch_linghu.fanfoudroid.ui.module;

import java.text.ParseException;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.ch_linghu.fanfoudroid.data.db.StatusTableInfo.StatusTable;
import com.ch_linghu.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.Utils;

public class TweetCursorAdapter extends CursorAdapter implements TweetAdapter {
	private static final String TAG = "TweetCursorAdapter";
	
	private Context mContext;

	public TweetCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		mContext = context;

		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}

		if (cursor != null) {
			mUserTextColumn = cursor
					.getColumnIndexOrThrow(StatusTable.FIELD_USER_SCREEN_NAME);
			mTextColumn = cursor
					.getColumnIndexOrThrow(StatusTable.FIELD_TEXT);
			mProfileImageUrlColumn = cursor
					.getColumnIndexOrThrow(StatusTable.FIELD_PROFILE_IMAGE_URL);
			mCreatedAtColumn = cursor
					.getColumnIndexOrThrow(StatusTable.FIELD_CREATED_AT);
			mSourceColumn = cursor
					.getColumnIndexOrThrow(StatusTable.FIELD_SOURCE);
			mInReplyToScreenName = cursor
					.getColumnIndexOrThrow(StatusTable.FIELD_IN_REPLY_TO_SCREEN_NAME);
			mFavorited = cursor
					.getColumnIndexOrThrow(StatusTable.FIELD_FAVORITED);
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

	private StringBuilder mMetaBuilder;

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.tweet, parent, false);

		TweetCursorAdapter.ViewHolder holder = new ViewHolder();
		holder.tweetUserText = (TextView) view
				.findViewById(R.id.tweet_user_text);
		holder.tweetText = (TextView) view.findViewById(R.id.tweet_text);
		holder.profileImage = (ImageView) view.findViewById(R.id.profile_image);
		holder.metaText = (TextView) view.findViewById(R.id.tweet_meta_text);
		holder.fav = (ImageView) view.findViewById(R.id.tweet_fav);
		view.setTag(holder);

		return view;
	}

	private static class ViewHolder {
		public TextView tweetUserText;
		public TextView tweetText;
		public ImageView profileImage;
		public TextView metaText;
		public ImageView fav;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TweetCursorAdapter.ViewHolder holder = (TweetCursorAdapter.ViewHolder) view
				.getTag();
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);;
		boolean useProfileImage = pref.getBoolean(Preferences.USE_PROFILE_IMAGE, true);
		holder.tweetUserText.setText(cursor.getString(mUserTextColumn));
		Utils.setSimpleTweetText(holder.tweetText, cursor.getString(mTextColumn));

		String profileImageUrl = cursor.getString(mProfileImageUrlColumn);

		if (useProfileImage){
			if (!Utils.isEmpty(profileImageUrl)) {
				holder.profileImage.setImageBitmap(TwitterApplication.mImageManager
						.get(profileImageUrl));
			}
		}else{
			holder.profileImage.setVisibility(View.GONE);
		}
		
		if (cursor.getString(mFavorited).equals("true")) {
			holder.fav.setVisibility(View.VISIBLE);
		} else {
			holder.fav.setVisibility(View.INVISIBLE);
		}

		try {
			Date createdAt = TwitterDbAdapter.DB_DATE_FORMATTER.parse(cursor
					.getString(mCreatedAtColumn));
			holder.metaText.setText(Tweet.buildMetaText(mMetaBuilder,
					createdAt, cursor.getString(mSourceColumn), cursor
							.getString(mInReplyToScreenName)));
		} catch (ParseException e) {
			Log.w(TAG, "Invalid created at data.");
		}
	}

	@Override
	public void refresh() {
		getCursor().requery();
	}
}