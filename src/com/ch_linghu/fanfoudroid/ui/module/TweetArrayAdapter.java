package com.ch_linghu.fanfoudroid.ui.module;

import java.util.ArrayList;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.helper.ImageCache;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TweetArrayAdapter extends BaseAdapter implements TweetAdapter {
	private static final String TAG = "TweetArrayAdapter";

	protected ArrayList<Tweet> mTweets;
	private Context mContext;
	protected LayoutInflater mInflater;
	protected StringBuilder mMetaBuilder;
	protected ImageCache mImageCache;

	public TweetArrayAdapter(Context context, ImageCache imageCache) {
		mTweets = new ArrayList<Tweet>();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mMetaBuilder = new StringBuilder();
		mImageCache = imageCache;
	}

	@Override
	public int getCount() {
		return mTweets.size();
	}

	@Override
	public Object getItem(int position) {
		return mTweets.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		public TextView tweetUserText;
		public TextView tweetText;
		public ImageView profileImage;
		public TextView metaText;
		public ImageView fav;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean useProfileImage = pref.getBoolean(Preferences.USE_PROFILE_IMAGE, true);
		
		if (convertView == null) {
			view = mInflater.inflate(R.layout.tweet, parent, false);

			ViewHolder holder = new ViewHolder();
			holder.tweetUserText = (TextView) view
					.findViewById(R.id.tweet_user_text);
			holder.tweetText = (TextView) view.findViewById(R.id.tweet_text);
			holder.profileImage = (ImageView) view
					.findViewById(R.id.profile_image);
			holder.metaText = (TextView) view
					.findViewById(R.id.tweet_meta_text);
			holder.fav = (ImageView) view.findViewById(R.id.tweet_fav);
			view.setTag(holder);
		} else {
			view = convertView;
		}

		ViewHolder holder = (ViewHolder) view.getTag();
		
		Tweet tweet = mTweets.get(position);

		holder.tweetUserText.setText(tweet.screenName);
		Utils.setSimpleTweetText(holder.tweetText, tweet.text);
		// holder.tweetText.setText(tweet.text, BufferType.SPANNABLE);

		if (mImageCache != null) {
			String profileImageUrl = tweet.profileImageUrl;

			if (useProfileImage){
				if (!Utils.isEmpty(profileImageUrl)) {
					holder.profileImage.setImageBitmap(mImageCache
							.get(profileImageUrl));
				}
			}else{
				holder.profileImage.setVisibility(View.GONE);
			}
		}else{
			holder.profileImage.setImageBitmap(ImageCache.mDefaultBitmap);
		}

		holder.metaText.setText(Tweet.buildMetaText(mMetaBuilder,
				tweet.createdAt, tweet.source, tweet.inReplyToScreenName));

		if (tweet.favorited.equals("true")) {
			holder.fav.setVisibility(View.VISIBLE);
		} else {
			holder.fav.setVisibility(View.INVISIBLE);
		}

		return view;
	}

	public void refresh(ArrayList<Tweet> tweets, ImageCache imageCache) {
		mImageCache = imageCache;
		refresh(tweets);
	}

	public void refresh(ArrayList<Tweet> tweets) {
		mTweets = tweets;
		notifyDataSetChanged();
	}

	@Override
	public void refresh() {
		notifyDataSetChanged();
	}
}
