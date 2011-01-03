package com.ch_linghu.android.fanfoudroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
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
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class HttpClient {
	
	private static final String TAG = "HttpClient";

	private static final int OK = 200;// OK: Success!
	private static final int NOT_MODIFIED = 304;// Not Modified: There was no new data to return.
	private static final int BAD_REQUEST = 400;// Bad Request: The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.
	private static final int NOT_AUTHORIZED = 401;// Not Authorized: Authentication credentials were missing or incorrect.
	private static final int FORBIDDEN = 403;// Forbidden: The request is understood, but it has been refused.  An accompanying error message will explain why.
	private static final int NOT_FOUND = 404;// Not Found: The URI requested is invalid or the resource requested, such as a user, does not exists.
	private static final int NOT_ACCEPTABLE = 406;// Not Acceptable: Returned by the Search API when an invalid format is specified in the request.
	private static final int INTERNAL_SERVER_ERROR = 500;// Internal Server Error: Something is broken.  Please post to the group so the Weibo team can investigate.
	private static final int BAD_GATEWAY = 502;// Bad Gateway: Weibo is down or being upgraded.
	private static final int SERVICE_UNAVAILABLE = 503;// Service Unavailable: The Weibo servers are up, but overloaded with requests. Try again later. The search and trend methods use this to indicate when you are being rate limited.
	
	private static final String TWITTER_HOST = "api.fanfou.com";

	private final static boolean DEBUG = Configuration.debug;

	private DefaultHttpClient mClient;
	private AuthScope mAuthScope;

	private String mUsername;
	private String mPassword;

	private static final String METHOD_GET = "GET";
	private static final String METHOD_POST = "POST";
	private static final String METHOD_DELETE = "DELETE";

	private static final int CONNECTION_TIMEOUT_MS = 30 * 1000;
	private static final int SOCKET_TIMEOUT_MS = 30 * 1000;
	
	public static final int RETRIEVE_LIMIT = 20;


	public HttpClient(String user_id, String password) {
		prepareHttpClient();
		setCredentials(user_id, password);
	}

	public static boolean isValidCredentials(String username, String password) {
		return !Utils.isEmpty(username) && !Utils.isEmpty(password);
	}
	
	public void login(String username, String password) throws IOException, WeiboException {
		Log.i(TAG, "Login attempt for " + username);
		setCredentials(username, password);
		//InputStream data = requestData(VERIFY_CREDENTIALS_URL, METHOD_GET, null);
		//data.close();
	}

	
	public void logout() {
		setCredentials("", "");
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
		authRegistry.register(basicScheme.getSchemeName(),
				new BasicSchemeFactory());
		mClient.setAuthSchemes(authRegistry);
		mClient.setCredentialsProvider(new BasicCredentialsProvider());
	}

	public void setCredentials(String username, String password) {
		mUsername = username;
		mPassword = password;
		mClient.getCredentialsProvider().setCredentials(mAuthScope,
				new UsernamePasswordCredentials(username, password));
	}
	
	public InputStream post(String url, ArrayList<NameValuePair> params) 
			throws WeiboException, IOException {
		return httpRequest(url, params, false, METHOD_POST);
	}
	
	public InputStream get(String url) 
			throws WeiboException, IOException {
		return httpRequest(url, null, false, METHOD_GET);
	}

    public InputStream httpRequest(String url, ArrayList<NameValuePair> postParams,
            boolean authenticated, String httpMethod) 
    			throws WeiboException, IOException {
		Log.i(TAG, "Sending " + httpMethod + " request to " + url);

		URI uri;

		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new WeiboException("Invalid URL.");
		}

		HttpUriRequest method;
		log("[Request]");

		if (METHOD_POST.equals(httpMethod)) {
			HttpPost post = new HttpPost(uri);
			// See this:
			// http://groups.google.com/group/twitter-development-talk/browse_thread/
			// thread/e178b1d3d63d8e3b
			post.getParams().setBooleanParameter(
					"http.protocol.expect-continue", false);
			post.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
			method = post;

			// log post data
			if (DEBUG) {
				log("POST INPUT : " + postParams.toString());
				HttpEntity entity = post.getEntity();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						entity.getContent()));
				String line;
				while ((line = in.readLine()) != null) {
					log("POST ENTITY : " + line);
				}
			}

		} else if (METHOD_DELETE.equals(httpMethod)) {
			method = new HttpDelete(uri);
		} else {
			method = new HttpGet(uri);
		}

		HttpConnectionParams.setConnectionTimeout(method.getParams(),
				CONNECTION_TIMEOUT_MS);
		HttpConnectionParams
				.setSoTimeout(method.getParams(), SOCKET_TIMEOUT_MS);

		HttpResponse response;

		try {
			response = mClient.execute(method);
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("HTTP protocol error.");
		}
		
		int statusCode = response.getStatusLine().getStatusCode();
		InputStream response_content = response.getEntity().getContent();

		// DEBUG MODE
		if (DEBUG) {
			//TODO: request headers is null
			// log request URI and header 
			log("[" + method.getMethod() + "] " + method.getURI());
			Header[] rHeaders = method.getAllHeaders();
			for (Header h : rHeaders) {
				log(h.getName() + " : " + h.getValue());
			}
			
			// log response header
			log("[Response]");
			Header[] headers = response.getAllHeaders();
			for (Header h : headers) {
				log(h.getName() + " : " + h.getValue());
			}
	
			// log responseContent
			log("StatusCode : " + statusCode);
			/*
			BufferedReader content = new BufferedReader(new InputStreamReader(
					 response_content. ));
			String line;
			while ((line = content.readLine()) != null) {
				log("ResponseContent : " + line);
			}
			*/
			log("------------------------------------------");
		}

		if (statusCode != OK) {
			String msg = Utils.stringifyStream(response.getEntity().getContent());
			Log.e(TAG, msg);
			
			//TODO: uncomment
//			throw new Exception(statusCode, getCause(statusCode) + "\n" + msg);
		}
		
		return response_content;
	}
	
    private static String getCause(int statusCode){
        String cause = null;
        switch(statusCode){
            case NOT_MODIFIED:
                break;
            case BAD_REQUEST:
                cause = "The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.";
                break;
            case NOT_AUTHORIZED:
                cause = "Authentication credentials were missing or incorrect.";
                break;
            case FORBIDDEN:
                cause = "The request is understood, but it has been refused.  An accompanying error message will explain why.";
                break;
            case NOT_FOUND:
                cause = "The URI requested is invalid or the resource requested, such as a user, does not exists.";
                break;
            case NOT_ACCEPTABLE:
                cause = "Returned by the Search API when an invalid format is specified in the request.";
                break;
            case INTERNAL_SERVER_ERROR:
                cause = "Something is broken.  Please post to the group so the Weibo team can investigate.";
                break;
            case BAD_GATEWAY:
                cause = "Weibo is down or being upgraded.";
                break;
            case SERVICE_UNAVAILABLE:
                cause = "Service Unavailable: The Weibo servers are up, but overloaded with requests. Try again later. The search and trend methods use this to indicate when you are being rate limited.";
                break;
            default:
                cause = "";
        }
        return statusCode + ":" + cause;
    }

	public static void log(String msg){
		if (DEBUG) {
			Log.i(TAG,msg);
		}
	}


}
