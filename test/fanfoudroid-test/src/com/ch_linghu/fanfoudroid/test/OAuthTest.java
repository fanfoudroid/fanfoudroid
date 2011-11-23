package com.ch_linghu.fanfoudroid.test;

import java.util.Scanner;

import junit.framework.Assert;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.ch_linghu.fanfoudroid.UserTimelineActivity;
import com.ch_linghu.fanfoudroid.fanfou.Weibo;
import com.ch_linghu.fanfoudroid.http.HttpClient;
import com.ch_linghu.fanfoudroid.http.Response;

import eriji.com.oauth.OAuthClient;
import eriji.com.oauth.OAuthFileStore;
import eriji.com.oauth.OAuthHelper;

public class OAuthTest extends
		ActivityInstrumentationTestCase2<UserTimelineActivity> {
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

	public OAuthTest() {
		super("", UserTimelineActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		http = new HttpClient("172339248@qq.com", "12345678");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testA() {
		assertTrue(true);
	}

	
	public void testOauth() throws Exception {
		OAuthHelper.main(null);
	}
	

}
