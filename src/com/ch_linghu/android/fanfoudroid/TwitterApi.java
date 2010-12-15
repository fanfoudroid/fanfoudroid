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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

public class TwitterApi {
  private static final String TAG = "TwitterApi";

  private static final String UPDATE_URL = "http://api.fanfou.com/statuses/update.json";
  private static final String VERIFY_CREDENTIALS_URL = "http://api.fanfou.com/account/verify_credentials.json";
  private static final String FRIENDS_TIMELINE_URL = "http://api.fanfou.com/statuses/friends_timeline.json";
  private static final String DIRECT_MESSAGES_URL = "http://api.fanfou.com/direct_messages.json";
  private static final String DIRECT_MESSAGES_SENT_URL = "http://api.fanfou.com/direct_messages/sent.json";
  private static final String DIRECT_MESSAGES_DESTROY_URL = "http://api.fanfou.com/direct_messages/destroy/%d.json";
  private static final String DIRECT_MESSAGES_NEW_URL = "http://api.fanfou.com/direct_messages/new.json";
  private static final String FOLLOWERS_IDS_URL = "http://api.fanfou.com/followers/ids.json";
  private static final String USER_TIMELINE_URL = "http://api.fanfou.com/statuses/user_timeline.json";
  private static final String FRIENDSHIPS_EXISTS_URL = "http://api.fanfou.com/friendships/exists.json";
  private static final String FRIENDSHIPS_CREATE_URL = "http://api.fanfou.com/friendships/create/%s.json";
  private static final String FRIENDSHIPS_DESTROY_URL = "http://api.fanfou.com/friendships/destroy/%s.json";
  private static final String SEARCH_URL = "http://api.fanfou.com/search.json";

  private static final String UPLOAD_AND_POST_URL = "http://api.fanfou.com/photos/upload.json";

  private static final String TWITTER_HOST = "api.fanfou.com";

  private DefaultHttpClient mClient;
  private AuthScope mAuthScope;

  private String mUsername;
  private String mPassword;

  private static final String METHOD_GET = "GET";
  private static final String METHOD_POST = "POST";
  private static final String METHOD_DELETE = "DELETE";

  public static final int RETRIEVE_LIMIT = 50;

  public class AuthException extends Exception {
    private static final long serialVersionUID = 1703735789572778599L;
  }

  public class ApiException extends Exception {
    public int mCode;

    public ApiException(int code, String string) {
      super(string);

      mCode = code;
    }

    private static final long serialVersionUID = -3755642135241860532L;
  }

  private static final int CONNECTION_TIMEOUT_MS = 30 * 1000;
  private static final int SOCKET_TIMEOUT_MS = 30 * 1000;

  public TwitterApi() {
    prepareHttpClient();
  }

  public static boolean isValidCredentials(String username, String password) {
    return !Utils.isEmpty(username) && !Utils.isEmpty(password);
  }

  public boolean isLoggedIn() {
    return isValidCredentials(mUsername, mPassword);
  }

  public String getUsername() {
    return mUsername;
  }

  private void prepareHttpClient() {
    mAuthScope = new AuthScope(TWITTER_HOST, AuthScope.ANY_PORT);
    mClient = new DefaultHttpClient();
    BasicScheme basicScheme = new BasicScheme();
    AuthSchemeRegistry authRegistry = new AuthSchemeRegistry();
    authRegistry
        .register(basicScheme.getSchemeName(), new BasicSchemeFactory());
    mClient.setAuthSchemes(authRegistry);
    mClient.setCredentialsProvider(new BasicCredentialsProvider());
  }

  public void setCredentials(String username, String password) {
    mUsername = username;
    mPassword = password;
    mClient.getCredentialsProvider().setCredentials(mAuthScope,
        new UsernamePasswordCredentials(username, password));
  }

  public void postTwitPic(File file, String message) throws IOException,
      AuthException, ApiException {
    URI uri;

    try {
      uri = new URI(UPLOAD_AND_POST_URL);
    } catch (URISyntaxException e) {
      Log.e(TAG, e.getMessage(), e);
      throw new IOException("Invalid URL.");
    }

    DefaultHttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(uri);
    MultipartEntity entity = new MultipartEntity();
    entity.addPart("username", new StringBody(mUsername));
    entity.addPart("password", new StringBody(mPassword));
    // Don't try this. Server does not appear to support chunking.
    // entity.addPart("media", new InputStreamBody(imageStream, "media"));
    entity.addPart("media", new FileBody(file));
    entity.addPart("message", new StringBody(message));
    post.setEntity(entity);

    HttpConnectionParams.setConnectionTimeout(post.getParams(),
        CONNECTION_TIMEOUT_MS);
    HttpConnectionParams.setSoTimeout(post.getParams(), SOCKET_TIMEOUT_MS);

    HttpResponse response;

    try {
      response = client.execute(post);
    } catch (ClientProtocolException e) {
      Log.e(TAG, e.getMessage(), e);
      throw new IOException("HTTP protocol error.");
    }

    int statusCode = response.getStatusLine().getStatusCode();

    if (statusCode != 200) {
      Log.e(TAG, Utils.stringifyStream(response.getEntity().getContent()));
      throw new IOException("Non OK response code: " + statusCode);
    }

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    Document doc;

    try {
      db = dbf.newDocumentBuilder();
      doc = db.parse(response.getEntity().getContent());
    } catch (ParserConfigurationException e) {
      throw new IOException("Could not parse response.");
    } catch (IllegalStateException e) {
      throw new IOException("Could not parse response.");
    } catch (SAXException e) {
      throw new IOException("Could not parse response.");
    }

    Element root = doc.getDocumentElement();
    root.normalize();

    if (!"rsp".equals(root.getTagName())) {
      throw new IOException("Could not parse response.");
    }

    String stat = root.getAttribute("stat");

    if ("fail".equals(stat)) {
      NodeList list = root.getChildNodes();

      for (int i = 0; i < list.getLength(); i++) {
        if (list.item(i).getNodeName().equals("err")) {
          Node err = list.item(i);
          String code = err.getAttributes().getNamedItem("code").getNodeValue();
          String msg = err.getAttributes().getNamedItem("msg").getNodeValue();
          throw new ApiException(Integer.parseInt(code), msg);
        }
      }

      throw new IOException("Could not parse error response.");
    }
  }

  // TODO: return a custom object that has a finish method
  // that calls finish on the HttpEntity and stream.
  private InputStream requestData(String url, String httpMethod,
      ArrayList<NameValuePair> params) throws IOException, AuthException,
      ApiException {
    Log.i(TAG, "Sending " + httpMethod + " request to " + url);

    URI uri;

    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      Log.e(TAG, e.getMessage(), e);
      throw new IOException("Invalid URL.");
    }

    HttpUriRequest method;

    if (METHOD_POST.equals(httpMethod)) {
      HttpPost post = new HttpPost(uri);
      // See this:
      // http://groups.google.com/group/twitter-development-talk/browse_thread/
      // thread/e178b1d3d63d8e3b
      post.getParams().setBooleanParameter("http.protocol.expect-continue",
          false);
      post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
      method = post;
    } else if (METHOD_DELETE.equals(httpMethod)) {
      method = new HttpDelete(uri);
    } else {
      method = new HttpGet(uri);
    }

    HttpConnectionParams.setConnectionTimeout(method.getParams(),
        CONNECTION_TIMEOUT_MS);
    HttpConnectionParams.setSoTimeout(method.getParams(), SOCKET_TIMEOUT_MS);

    HttpResponse response;

    try {
      response = mClient.execute(method);
    } catch (ClientProtocolException e) {
      Log.e(TAG, e.getMessage(), e);
      throw new IOException("HTTP protocol error.");
    }

    int statusCode = response.getStatusLine().getStatusCode();

    if (statusCode == 401) {
      throw new AuthException();
    } else if (statusCode == 403) {
      try {
        JSONObject json = new JSONObject(Utils.stringifyStream(response
            .getEntity().getContent()));
        throw new ApiException(statusCode, json.getString("error"));
      } catch (IllegalStateException e) {
        throw new IOException("Could not parse error response.");
      } catch (JSONException e) {
        throw new IOException("Could not parse error response.");
      }
    } else if (statusCode != 200) {
      Log.e(TAG, Utils.stringifyStream(response.getEntity().getContent()));
      throw new IOException("Non OK response code: " + statusCode);
    }

    return response.getEntity().getContent();
  }

  public void login(String username, String password) throws IOException,
      AuthException, ApiException {
    Log.i(TAG, "Login attempt for " + username);
    setCredentials(username, password);
    InputStream data = requestData(VERIFY_CREDENTIALS_URL, METHOD_GET, null);
    data.close();
  }

  public void logout() {
    setCredentials("", "");
  }

  public JSONArray getTimeline() throws IOException, AuthException,
      ApiException {
    Log.i(TAG, "Requesting friends timeline.");

    String url = FRIENDS_TIMELINE_URL + "?format=html&count="
        + URLEncoder.encode(RETRIEVE_LIMIT + "", HTTP.UTF_8);

    InputStream data = requestData(url, METHOD_GET, null);
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
      AuthException, ApiException {
    Log.i(TAG, "Requesting friends timeline since id.");

    String url = FRIENDS_TIMELINE_URL + "?format=html&count="
        + URLEncoder.encode(RETRIEVE_LIMIT + "", HTTP.UTF_8);

    if (sinceId != null) {
      url += "&since_id=" + URLEncoder.encode(sinceId + "", HTTP.UTF_8);
    }

    InputStream data = requestData(url, METHOD_GET, null);
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

  public JSONArray getDirectMessages() throws IOException, AuthException,
      ApiException {
    Log.i(TAG, "Requesting direct messages.");

    InputStream data = requestData(DIRECT_MESSAGES_URL, METHOD_GET, null);
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

  public JSONArray getDirectMessagesSent() throws IOException, AuthException,
      ApiException {
    Log.i(TAG, "Requesting sent direct messages.");

    InputStream data = requestData(DIRECT_MESSAGES_SENT_URL, METHOD_GET, null);
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

  public JSONObject destroyDirectMessage(long id) throws IOException,
      AuthException, ApiException {
    Log.i(TAG, "Deleting direct message: " + id);

    String url = String.format(DIRECT_MESSAGES_DESTROY_URL, id);

    InputStream data = requestData(url, METHOD_DELETE, null);
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
      throws IOException, AuthException, ApiException {
    Log.i(TAG, "Sending dm.");

    ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("user", user));
    params.add(new BasicNameValuePair("text", text));

    InputStream data = requestData(DIRECT_MESSAGES_NEW_URL, METHOD_POST, params);
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

  public JSONObject update(String status, String reply_to) throws IOException, AuthException,
      ApiException {
    Log.i(TAG, "Updating status.");

    ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("status", status));
    params.add(new BasicNameValuePair("source", "fanfoudroid"));
    if (reply_to != null && !reply_to.equals("")){
    	params.add(new BasicNameValuePair("in_reply_to_status_id", reply_to));
    }

    InputStream data = requestData(UPDATE_URL, METHOD_POST, params);
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
      throws IOException, AuthException, ApiException {
    Log.i(TAG, "Requesting DMs since id.");

    String url = isSent ? DIRECT_MESSAGES_SENT_URL : DIRECT_MESSAGES_URL;

    if (sinceId != null) {
      url += "?since_id=" + URLEncoder.encode(sinceId + "", HTTP.UTF_8);
    }

    InputStream data = requestData(url, METHOD_GET, null);
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
      AuthException, ApiException {
    Log.i(TAG, "Requesting followers ids.");

    InputStream data = requestData(FOLLOWERS_IDS_URL, METHOD_GET, null);
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
      AuthException, ApiException {
    Log.i(TAG, "Requesting user timeline.");

    String url = USER_TIMELINE_URL + "?screen_name="
        + URLEncoder.encode(user, HTTP.UTF_8)
        + "&page=" + URLEncoder.encode(page + "", HTTP.UTF_8)
        + "&format=html";

    InputStream data = requestData(url, METHOD_GET, null);
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
      AuthException, ApiException {
    Log.i(TAG, "Check follows.");

    String url = FRIENDSHIPS_EXISTS_URL + "?user_a="
        + URLEncoder.encode(a, HTTP.UTF_8)
        + "&user_b="
        + URLEncoder.encode(b, HTTP.UTF_8);

    InputStream data = requestData(url, METHOD_GET, null);

    try {
      return "true".equals(Utils.stringifyStream(data).trim());
    } finally {
      data.close();
    }
  }

  public JSONObject createFriendship(String id) throws IOException,
      AuthException, ApiException {
    Log.i(TAG, "Following: " + id);

    String url = String.format(FRIENDSHIPS_CREATE_URL, id);

    InputStream data = requestData(url, METHOD_POST,
        new ArrayList<NameValuePair>());
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
      AuthException, ApiException {
    Log.i(TAG, "Unfollowing: " + id);

    String url = String.format(FRIENDSHIPS_DESTROY_URL, id);

    InputStream data = requestData(url, METHOD_DELETE, null);
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

  public JSONArray search(String query, int page) throws IOException, AuthException,
      ApiException {
    Log.i(TAG, "Searching.");

    String url = SEARCH_URL + "?q=" + URLEncoder.encode(query, HTTP.UTF_8)
        + "&page=" + URLEncoder.encode(page + "", HTTP.UTF_8);

    InputStream data = requestData(url, METHOD_GET, null);
    JSONArray json = null;

    try {
      JSONObject object = new JSONObject(Utils.stringifyStream(data));
      json = object.getJSONArray("results");
    } catch (JSONException e) {
      Log.e(TAG, e.getMessage(), e);
      throw new IOException("Could not parse JSON.");
    } finally {
      data.close();
    }

    return json;
  }

}
