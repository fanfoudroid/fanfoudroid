package com.ch_linghu.fanfoudroid.ui.module;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.app.LazyImageLoader.ImageLoaderCallback;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.app.SimpleImageLoader;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.util.TextHelper;

public class TweetArrayAdapter extends BaseAdapter implements TweetAdapter {
	private static final String TAG = "TweetArrayAdapter";

	protected ArrayList<Tweet> mTweets;
	private Context mContext;
	protected LayoutInflater mInflater;
	protected StringBuilder mMetaBuilder;

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
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;

		SharedPreferences pref = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean useProfileImage = pref.getBoolean(
				Preferences.USE_PROFILE_IMAGE, true);
		boolean useHighlightBackground = pref.getBoolean(
				Preferences.HIGHLIGHT_BACKGROUND, true);
		
		if (convertView == null) {
			view = mInflater.inflate(R.layout.tweet, parent, false);

			ViewHolder holder = new ViewHolder();
			holder.tweetLayout=(LinearLayout) view.findViewById(R.id.tweet_layout);
			holder.tweetUserText = (TextView) view
					.findViewById(R.id.tweet_user_text);
			holder.tweetText = (TextView) view.findViewById(R.id.tweet_text);
			holder.profileLayout = (FrameLayout) view
					.findViewById(R.id.profile_layout);
			holder.profileImage = (ImageView) view
					.findViewById(R.id.profile_image);
			holder.metaText = (TextView) view
					.findViewById(R.id.tweet_meta_text);
			holder.fav = (ImageView) view.findViewById(R.id.tweet_fav);
			holder.has_image = (ImageView) view
					.findViewById(R.id.tweet_has_image);
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

		if (useProfileImage && !TextUtils.isEmpty(profileImageUrl)) {
			holder.profileLayout.setVisibility(View.VISIBLE);
			SimpleImageLoader.display(holder.profileImage, profileImageUrl);
		} else {
			holder.profileLayout.setVisibility(View.GONE);
		}

		holder.metaText.setText(Tweet.buildMetaText(mMetaBuilder,
				tweet.createdAt, tweet.source, tweet.inReplyToScreenName));

		if (tweet.favorited.equals("true")) {
			holder.fav.setVisibility(View.VISIBLE);
		} else {
			holder.fav.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(tweet.thumbnail_pic)) {
			holder.has_image.setVisibility(View.VISIBLE);
		} else {
			holder.has_image.setVisibility(View.GONE);
		}
		
		/**
		 * 添加特殊行的背景色
		 */
		if (useHighlightBackground){
			String myself = TwitterApplication.myselfName;
			StringBuilder b = new StringBuilder();
			b.append("@");
			b.append(myself);
			String to_myself = b.toString();
			
			//FIXME: contains操作影响效率，应该在获得时作判断，置标志，在这里对标志进行直接判断。
			if(holder.tweetUserText.getText().equals(myself)){
				holder.tweetLayout.setBackgroundResource(R.drawable.list_selector_self);
				holder.profileLayout.setBackgroundResource(R.color.self_background);
			}else if(holder.tweetText.getText().toString().contains(to_myself)){
				holder.tweetLayout.setBackgroundResource(R.drawable.list_selector_mention);
				holder.profileLayout.setBackgroundResource(R.color.mention_background);
			}else{
				holder.tweetLayout.setBackgroundResource(android.R.drawable.list_selector_background);
				holder.profileLayout.setBackgroundResource(android.R.color.transparent);
			}
		}else{
			holder.tweetLayout.setBackgroundResource(android.R.drawable.list_selector_background);
			holder.profileLayout.setBackgroundResource(android.R.color.transparent);		
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
