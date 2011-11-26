package com.ch_linghu.fanfoudroid.test;

import java.util.Date;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.StatusActivity;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.data.Tweet;

public class StatusActivityTest extends
        ActivityInstrumentationTestCase2<StatusActivity> {

    private StatusActivity mActivity;
    private Instrumentation mInstrumentation;

    private final Tweet tweet;
    {
        tweet = new Tweet();
        tweet.id = "status_id";
        tweet.text = "status_text";
        tweet.createdAt = new Date();// "Thu Jan 13 11:17:22 Asia/Shanghai 2011";
        tweet.favorited = "true";
        tweet.inReplyToStatusId = "reply_status_id";
        tweet.inReplyToUserId = "reply_user_id";
        tweet.inReplyToScreenName = "reply_user_screen_name";
        tweet.screenName = "screen_name";
        tweet.profileImageUrl = "http://avatar1.fanfou.com/s0/00/00/00.jpg";
        tweet.userId = "user_id";
        tweet.source = "fanfoudroid";
    }

    public StatusActivityTest() {
        super("com.ch_linghu.fanfoudroid", StatusActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        mInstrumentation = getInstrumentation();
        Context app = mInstrumentation.getTargetContext();

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(app);
        String username = preferences.getString(Preferences.USERNAME_KEY, "");
        assertNotNull(username);

        // Mock Intent
        Intent intent = StatusActivity.createIntent(tweet);
        setActivityIntent(intent);
        mActivity = getActivity();
    }

    @Override
    public void tearDown() throws Exception {
    }

    public void testIntent() throws Exception {
        TextView tweet_text = (TextView) mActivity
                .findViewById(com.ch_linghu.fanfoudroid.R.id.tweet_text);
        TextView tweet_created_at = (TextView) mActivity
                .findViewById(com.ch_linghu.fanfoudroid.R.id.tweet_created_at);
        ImageButton tweet_favorited = (ImageButton) mActivity
                .findViewById(com.ch_linghu.fanfoudroid.R.id.tweet_fav);
        TextView tweet_screen_name = (TextView) mActivity
                .findViewById(com.ch_linghu.fanfoudroid.R.id.tweet_screen_name);
        ImageView profile_image = (ImageView) mActivity
                .findViewById(com.ch_linghu.fanfoudroid.R.id.profile_image);
        TextView tweet_source = (TextView) mActivity
                .findViewById(com.ch_linghu.fanfoudroid.R.id.tweet_source);
        // TODO: test reply

        assertEquals(tweet.text, tweet_text.getText().toString());
        assertEquals(tweet.screenName, tweet_screen_name.getText().toString());
        assertEquals(mActivity.getString(R.string.tweet_source_prefix)
                + tweet.source, tweet_source.getText().toString());
        assertEquals(tweet.favorited.equals("true"),
                tweet_favorited.isEnabled());
        // TODO： 因为是相对时间，断言有时因为延时会失败，改成绝对时间。
        // assertEquals(Utils.getRelativeDate(tweet.createdAt), tweet_created_at.getText().toString());
        // assertEquals(tweet.profileImageUrl, );
    }
}
