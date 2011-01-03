/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ch_linghu.android.fanfoudroid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class TwitterApi {
	private static final String TAG = "TwitterApi";

	private static final String UPDATE_URL = "http://api.fanfou.com/statuses/update.json";
	private static final String DESTROY_STATUS_URL = "http://api.fanfou.com/statuses/destroy/%s.json";
	private static final String FAVORITES_URL = "http://api.fanfou.com/favorites.json";
	private static final String ADD_FAV_URL = "http://api.fanfou.com/favorites/create/%s.json";
	private static final String DEL_FAV_URL = "http://api.fanfou.com/favorites/destroy/%s.json";
	private static final String VERIFY_CREDENTIALS_URL = "http://api.fanfou.com/account/verify_credentials.json";
	private static final String FRIENDS_TIMELINE_URL = "http://api.fanfou.com/statuses/friends_timeline.json";
	private static final String REPLIES_URL = "http://api.fanfou.com/statuses/replies.json";
	private static final String DIRECT_MESSAGES_URL = "http://api.fanfou.com/direct_messages.json";
	private static final String DIRECT_MESSAGES_SENT_URL = "http://api.fanfou.com/direct_messages/sent.json";
	private static final String DIRECT_MESSAGES_DESTROY_URL = "http://api.fanfou.com/direct_messages/destroy/%s.json";
	private static final String DIRECT_MESSAGES_NEW_URL = "http://api.fanfou.com/direct_messages/new.json";
	private static final String FOLLOWERS_IDS_URL = "http://api.fanfou.com/followers/ids.json";
	private static final String USER_TIMELINE_URL = "http://api.fanfou.com/statuses/user_timeline.json";
	private static final String FRIENDSHIPS_EXISTS_URL = "http://api.fanfou.com/friendships/exists.json";
	private static final String FRIENDSHIPS_CREATE_URL = "http://api.fanfou.com/friendships/create/%s.json";
	private static final String FRIENDSHIPS_DESTROY_URL = "http://api.fanfou.com/friendships/destroy/%s.json";
	private static final String SEARCH_URL = "http://api.fanfou.com/search/public_timeline.json";
	private static final String USER_SHOW_URL = "http://api.fanfou.com/users/show.json";
	private static final String UPLOAD_AND_POST_URL = "http://api.fanfou.com/photos/upload.json";
	
	private static final String FANFOU_SOURCE = "fanfoudriod";

	private HttpClient http;
	public static final int RETRIEVE_LIMIT = 20;
	
	private String mUsername;
	private String mPassword;

	public TwitterApi(String username, String password) {
		http = new HttpClient(username, password);
		mUsername = username;
		mPassword = password;
	}
	
	public HttpClient getHttp(){
		return http;
	}

	//---------------------------------------------------------------
	
	public TwitterApi(){}
	
	public String getUsername() {
		return mUsername;
	}
	
	
	public static boolean isValidCredentials(String username, String password) {
		return !Utils.isEmpty(username) && !Utils.isEmpty(password);
	}

	public boolean isLoggedIn() {
		return isValidCredentials(mUsername, mPassword);
	}
	
	public void login(String username, String password) throws IOException,
	WeiboException {
Log.i(TAG, "Login attempt for " + username);
http.setCredentials(username, password);
InputStream data = http.get(VERIFY_CREDENTIALS_URL);
data.close();
}

public void logout() {
http.setCredentials("", "");
}
	
	public void postTwitPic(File file, String message) throws IOException,
			WeiboException {
		URI uri;
		Log.i(TAG, "Updating status WITH a picture.");

		try {
			uri = new URI(UPLOAD_AND_POST_URL);
		} catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Invalid URL.");
		}
		
		/*

		//DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(uri);
		MultipartEntity entity = new MultipartEntity();
		entity.addPart("source", new StringBody(FANFOU_SOURCE));
		// Don't try this. Server does not appear to support chunking.
		// entity.addPart("media", new InputStreamBody(imageStream, "media"));
		entity.addPart("photo", new FileBody(file));
		entity.addPart("status", new StringBody(message));
		post.setEntity(entity);

		HttpConnectionParams.setConnectionTimeout(post.getParams(),
				CONNECTION_TIMEOUT_MS);
		HttpConnectionParams.setSoTimeout(post.getParams(), SOCKET_TIMEOUT_MS);

		HttpResponse response;

		try {
			response = mClient.execute(post);
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("HTTP protocol error.");
		}

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode != 200) {
			Log
					.e(TAG, Utils.stringifyStream(response.getEntity()
							.getContent()));
			throw new IOException("Non OK response code: " + statusCode);
		}
		
		*/
	}
	//---------------------------------------------------------------
	
	public User showUser(String id) throws IOException,
			WeiboException, JSONException {
		Log.i(TAG, "Requesting friends timeline.");
		
		String url = USER_SHOW_URL;
//		if (id != null) {
//			url = String.format(USER_SHOW_URL, id);
//		} else {
//			url = USER_SHOW_URL;
//		}
		
		/*////////////////////////////////////////////////////////////////
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("status", status));
		params.add(new BasicNameValuePair("source", FANFOU_SOURCE));
		if (reply_to != null && !reply_to.equals("")) {
			params.add(new BasicNameValuePair("in_reply_to_status_id",
							reply_to));
		}
		InputStream data = http.get(UPDATE_URL, METHOD_POST, params);
		////////////////////////////////////////////////////////////////*/

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("id", id));
		InputStream data = http.post(url, params);
		JSONObject json = null;
		
		try {
			json = new JSONObject(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return User.create(json);
	}
	
	public User showUser() throws IOException, WeiboException, JSONException {
		return showUser(null);
	}


	public JSONArray getTimeline() throws IOException, WeiboException {
		Log.i(TAG, "Requesting friends timeline.");

		String url = FRIENDS_TIMELINE_URL + "?format=html&count="
				+ URLEncoder.encode(RETRIEVE_LIMIT + "", HTTP.UTF_8);

		InputStream data = http.get(url);
		JSONArray json = null;

		try {
			json = new JSONArray(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public JSONArray getTimelineSinceId(String sinceId) throws IOException,
			WeiboException {
		Log.i(TAG, "Requesting friends timeline since id.");

		String url = FRIENDS_TIMELINE_URL + "?format=html&count="
				+ URLEncoder.encode(RETRIEVE_LIMIT + "", HTTP.UTF_8);

		if (sinceId != null) {
			url += "&since_id=" + URLEncoder.encode(sinceId + "", HTTP.UTF_8);
		}

		InputStream data = http.get(url);
		JSONArray json = null;

		try {
			json = new JSONArray(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public JSONArray getMention() throws IOException, WeiboException {
		Log.i(TAG, "Requesting friends timeline.");

		String url = REPLIES_URL + "?format=html&count="
				+ URLEncoder.encode(RETRIEVE_LIMIT + "", HTTP.UTF_8);

		InputStream data = http.get(url);
		JSONArray json = null;

		try {
			json = new JSONArray(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public JSONArray getMentionSinceId(String sinceId) throws IOException,
			WeiboException {
		Log.i(TAG, "Requesting friends timeline since id.");

		String url = REPLIES_URL + "?format=html&count="
				+ URLEncoder.encode(RETRIEVE_LIMIT + "", HTTP.UTF_8);

		if (sinceId != null) {
			url += "&since_id=" + URLEncoder.encode(sinceId + "", HTTP.UTF_8);
		}

		InputStream data = http.get(url);
		JSONArray json = null;

		try {
			json = new JSONArray(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public JSONArray getDirectMessages() throws IOException, WeiboException{
		Log.i(TAG, "Requesting direct messages.");

		InputStream data = http.get(DIRECT_MESSAGES_URL);
		JSONArray json = null;

		try {
			json = new JSONArray(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public JSONArray getDirectMessagesSent() throws IOException, WeiboException {
		Log.i(TAG, "Requesting sent direct messages.");

		InputStream data = http.get(DIRECT_MESSAGES_SENT_URL);
		JSONArray json = null;

		try {
			json = new JSONArray(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public JSONObject destroyDirectMessage(String id) throws IOException,
			WeiboException {
		Log.i(TAG, "Deleting direct message: " + id);

		String url = String.format(DIRECT_MESSAGES_DESTROY_URL, id);

		InputStream data = http.get(url);
		JSONObject json = null;

		try {
			json = new JSONObject(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public JSONObject sendDirectMessage(String user, String text)
			throws IOException, WeiboException{
		Log.i(TAG, "Sending dm.");

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user", user));
		params.add(new BasicNameValuePair("text", text));

		InputStream data = http.post(DIRECT_MESSAGES_NEW_URL, params);
		JSONObject json = null;

		try {
			json = new JSONObject(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public JSONObject update(String status, String reply_to)
			throws IOException, WeiboException {
		Log.i(TAG, "Updating status.");

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("status", status));
		params.add(new BasicNameValuePair("source", FANFOU_SOURCE));
		if (reply_to != null && !reply_to.equals("")) {
			params.add(new BasicNameValuePair("in_reply_to_status_id",
							reply_to));
		}

		InputStream data = http.post(UPDATE_URL, params);
		Log.i("LDS", data.toString());
		JSONObject json = null;

		try {
			json = new JSONObject(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}
	
	public JSONObject update(String status)
			throws IOException, WeiboException {
		return update(status, "");
	}
	
	
	public JSONObject destroyStatus(String statusId) 
			throws IOException, WeiboException {
		Log.i(TAG, "Destory Status , id = " + statusId);
		String url = String.format(DESTROY_STATUS_URL, statusId);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("source", FANFOU_SOURCE));
		InputStream data = http.post(url, params);
		JSONObject json = null;
		
		try {
			json = new JSONObject(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}
	
	public JSONObject addFavorite(String id)
			throws IOException, WeiboException {
		Log.i(TAG, "Add favorite.");
		String url = String.format(ADD_FAV_URL, id);

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("source", FANFOU_SOURCE));
		InputStream data = http.post(url, params);
		JSONObject json = null;

		try {
			json = new JSONObject(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public JSONObject delFavorite(String id)
			throws IOException, WeiboException {
		Log.i(TAG, "delete favorite.");
		String url = String.format(DEL_FAV_URL, id);
	
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("source", FANFOU_SOURCE));
		InputStream data = http.post(url, params);
		JSONObject json = null;
	
		try {
			json = new JSONObject(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}
	
		return json;
	}

	public JSONArray getDmsSinceId(String sinceId, boolean isSent)
			throws IOException, WeiboException {
		Log.i(TAG, "Requesting DMs since id.");

		String url = isSent ? DIRECT_MESSAGES_SENT_URL : DIRECT_MESSAGES_URL;

		if (sinceId != null) {
			url += "?since_id=" + URLEncoder.encode(sinceId + "", HTTP.UTF_8);
		}

		InputStream data = http.get(url);
		JSONArray json = null;

		try {
			json = new JSONArray(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public ArrayList<String> getFollowersIds() throws IOException,
			WeiboException {
		Log.i(TAG, "Requesting followers ids.");

		InputStream data = http.get(FOLLOWERS_IDS_URL);
		ArrayList<String> followers = new ArrayList<String>();

		try {
			JSONArray jsonArray = new JSONArray(Utils.stringifyStream(data));
			for (int i = 0; i < jsonArray.length(); ++i) {
				followers.add(jsonArray.getString(i));
			}
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return followers;
	}

	public JSONArray getUserTimeline(String user, int page) throws IOException,
			WeiboException {
		Log.i(TAG, "Requesting user timeline.");

		String url = USER_TIMELINE_URL + "?screen_name="
				+ URLEncoder.encode(user, HTTP.UTF_8) + "&page="
				+ URLEncoder.encode(page + "", HTTP.UTF_8) + "&format=html"
				+ "&count=" + URLEncoder.encode(RETRIEVE_LIMIT + "");
		
		
		InputStream data = http.get(url);
		JSONArray json = null;

		try {
			json = new JSONArray(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public boolean isFollows(String a, String b) throws IOException,
			WeiboException {
		Log.i(TAG, "Check follows.");

		String url = FRIENDSHIPS_EXISTS_URL + "?user_a="
				+ URLEncoder.encode(a, HTTP.UTF_8) + "&user_b="
				+ URLEncoder.encode(b, HTTP.UTF_8);

		InputStream data = http.get(url);

		try {
			return "true".equals(Utils.stringifyStream(data).trim());
		} finally {
			data.close();
		}
	}

	public JSONObject createFriendship(String id) throws IOException,
			WeiboException {
		Log.i(TAG, "Following: " + id);

		String url = String.format(FRIENDSHIPS_CREATE_URL, id);

		InputStream data = http.post(url, new ArrayList<NameValuePair>());
		JSONObject json = null;

		try {
			json = new JSONObject(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public JSONObject destroyFriendship(String id) throws IOException,
			WeiboException {
		Log.i(TAG, "Unfollowing: " + id);

		String url = String.format(FRIENDSHIPS_DESTROY_URL, id);

		InputStream data = http.get(url);
		JSONObject json = null;

		try {
			json = new JSONObject(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

	public JSONArray search(String query, int page) throws IOException,
			WeiboException{
		Log.i(TAG, "Searching.");
		
		String url = SEARCH_URL + "?q=" + URLEncoder.encode(query, HTTP.UTF_8)
				+ "&page=" + URLEncoder.encode(page + "", HTTP.UTF_8);

		InputStream data = http.get(url);
		JSONArray json = null;

		try {
			json = new JSONArray(Utils.stringifyStream(data));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
		} finally {
			data.close();
		}

		return json;
	}

}
