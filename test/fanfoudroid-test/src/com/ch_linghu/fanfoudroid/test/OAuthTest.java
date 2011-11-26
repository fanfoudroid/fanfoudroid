package com.ch_linghu.fanfoudroid.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.ch_linghu.fanfoudroid.LoginActivity;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.http.HttpClient;

import eriji.com.oauth.OAuthSharedPreferencesStore;
import eriji.com.oauth.OAuthToken;
import eriji.com.oauth.XAuthClient;

public class OAuthTest extends
		ActivityInstrumentationTestCase2<LoginActivity> {
	private static final String TAG = "OAuthTest";
	private String request_token_url = "http://fanfou.com/oauth/request_token";
	private String authorize_url = "http://fanfou.com/oauth/authorize";
	private String access_token_url = "http://fanfou.com/oauth/access_token";

	/**
	 * 因为隐私考虑，仓库没有该文件，必须使用本地版本, 文件范本如下：
	 * ------------------------------------------------- # NOTE: DO NOT PUSH
	 * THIS FILE TO THE SERVER
	 * 
	 * consumer_key=XXXXXXXXXXXXXXXXXXXXXXXXXX
	 * consumer_secret=XXXXXXXXXXXXXXXXXXXXXXX
	 * --------------------------------------------------
	 */
	private static final String prop = "do_not_push_to_the_server.properties";

	private HttpClient http;
	private Activity mActivity;

	public OAuthTest() {
		super("com.ch_linghu.fanfoudroid", LoginActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		http = new HttpClient("172339248@qq.com", "12345678");
		mActivity = this.getActivity();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	

	public void testOauth() throws Exception {
		//OAuthHelper.main(null);
		assertTrue(XAuthClient.auth("172339248@qq.com", "12345678"));
	}
	
	public void testOAuthSharedPreferencesStore() throws Exception {
		String token = "foo";
		String tokenSecret = "bar";
		String key = "storeKey";
		
		OAuthToken oauthToken = new OAuthToken(token, tokenSecret);
		OAuthSharedPreferencesStore store = new OAuthSharedPreferencesStore();
		store.store(key, oauthToken);
		assertTrue(store.isExists(key, null));
		
		OAuthToken restoreToken = store.get(key, null);
		assertEquals(oauthToken, restoreToken);
		
		// cleanup
		TwitterApplication.mPref.edit().putString(key, "");
	}
	

}
