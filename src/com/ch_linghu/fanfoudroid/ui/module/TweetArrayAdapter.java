package com.ch_linghu.fanfoudroid.ui.module;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.app.LazyImageLoader.ImageLoaderCallback;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.util.TextHelper;

public class TweetArrayAdapter extends BaseAdapter implements TweetAdapter {
	private static final String TAG = "TweetArrayAdapter";

	protected ArrayList<Tweet> mTweets;
	private Context mContext;
	protected LayoutInflater mInflater;
	protected StringBuilder mMetaBuilder;
	
	private ImageLoaderCallback callback = new ImageLoaderCallback(){

		@Override
		public void refresh(String url, Bitmap bitmap) {
			TweetArrayAdapter.this.refresh();			
		}
		
	};

	public TweetArrayAdapter(Context context) {
		mTweets = new ArrayList<Tweet>();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mMetaBuilder = new StringBuilder();
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
		public ImageView has_image;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		
		SharedPreferences pref = TwitterApplication.mPref;  //PreferenceManager.getDefaultSharedPreferences(mContext);
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
			holder.has_image = (ImageView) view.findViewById(R.id.tweet_has_image);
			view.setTag(holder);
		} else {
			view = convertView;
		}

		ViewHolder holder = (ViewHolder) view.getTag();
		
		Tweet tweet = mTweets.get(position);

		holder.tweetUserText.setText(tweet.screenName);
		TextHelper.setSimpleTweetText(holder.tweetText, tweet.text);
		// holder.tweetText.setText(tweet.text, BufferType.SPANNABLE);

		String profileImageUrl = tweet.profileImageUrl;

		if (useProfileImage){
			if (!TextHelper.isEmpty(profileImageUrl)) {
				holder.profileImage.setImageBitmap(TwitterApplication.mImageLoader
						.get(profileImageUrl, callback));
			}
		}else{
			holder.profileImage.setVisibility(View.GONE);
		}

		holder.metaText.setText(Tweet.buildMetaText(mMetaBuilder,
				tweet.createdAt, tweet.source, tweet.inReplyToScreenName));

		if (tweet.favorited.equals("true")) {
			holder.fav.setVisibility(View.VISIBLE);
		} else {
			holder.fav.setVisibility(View.GONE);
		}
		
		if (!TextHelper.isEmpty(tweet.thumbnail_pic)) {
			holder.has_image.setVisibility(View.VISIBLE);
		} else {
			holder.has_image.setVisibility(View.GONE);
		}

		return view;
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
