package com.ch_linghu.fanfoudroid.test;


import android.test.ActivityInstrumentationTestCase2;

import com.ch_linghu.fanfoudroid.UserTimelineActivity;
import com.ch_linghu.fanfoudroid.http.HttpClient;

public class HttpClientTest extends ActivityInstrumentationTestCase2<UserTimelineActivity> { 

	private static final String TAG = "HttpClientTest";
	
	private static final String TEST_URL = "http://api.fanfou.com/help/test.json";
	private static final String PUBLIC_TIMELINE_URL = "http://api.fanfou.com/statuses/public_timeline.json";
	private static final String VERIFY_URL = "http://api.fanfou.com/account/verify_credentials.json";
	
	private HttpClient http;
	
	 public HttpClientTest() {
         super("", UserTimelineActivity.class);
	 }
	 /*
	
    @Override
	protected void setUp() throws Exception {
		super.setUp();
		http = new HttpClient("172339248@qq.com", "12345678");
		Log.i("LDS", this.getActivity().toString());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testHttpRequest() throws Exception {
		Response r = http.httpRequest(TEST_URL, null, false, "GET");
		assertEquals(200, r.getStatusCode());
		
		Response r2 = http.httpRequest(PUBLIC_TIMELINE_URL, null, false, "GET");
		JSONArray json = r2.asJSONArray();
		assertNotNull(json);
	}
	
	public void testGet() throws Exception {
		Response r = http.get(VERIFY_URL);
		assertEquals(200, r.getStatusCode());
	}
*/
}
