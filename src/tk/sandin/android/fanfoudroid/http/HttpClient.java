package tk.sandin.android.fanfoudroid.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import tk.sandin.android.fanfoudroid.weibo.Weibo;
import tk.sandin.android.fanfoudroid.weibo.WeiboException;
import android.util.Log;

import com.ch_linghu.android.fanfoudroid.Configuration;
import com.ch_linghu.android.fanfoudroid.Utils;

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

	private String mUserId;
	private String mPassword;
	
	private static boolean isAuthenticationEnabled = false;

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
		return isValidCredentials(mUserId, mPassword);
	}

	public String getUserId() {
		return mUserId;
	}
	
	public String getPassword() {
		return mPassword;
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
		mUserId = username;
		mPassword = password;
		mClient.getCredentialsProvider().setCredentials(mAuthScope,
				new UsernamePasswordCredentials(username, password));
		isAuthenticationEnabled = true;
		Log.i("LDS", mClient.toString() );
	}
	
	public Response post(String url, ArrayList<BasicNameValuePair> params) 
			throws WeiboException {
		return httpRequest(url, params, false, METHOD_POST);
	}
	
	 public Response post(String url, ArrayList<BasicNameValuePair> postParams,
             boolean authenticated) throws WeiboException {
		 if (null == postParams) {
			 postParams = new ArrayList<BasicNameValuePair>();
		 }
		 
		 postParams.add(new BasicNameValuePair("source", Weibo.CONSUMER_KEY));
		 
		 return httpRequest(url, postParams, authenticated, METHOD_POST);
	 }
 
	public Response post(String url, boolean authenticated) throws WeiboException {
        return httpRequest(url, null, authenticated, METHOD_POST);
    }

    public Response post(String url) throws
            WeiboException {
        return httpRequest(url, null, false, METHOD_POST);
    }

	public Response get(String url, ArrayList<BasicNameValuePair> params, boolean authenticated) 
			throws WeiboException {
		return httpRequest(url, params, authenticated, METHOD_GET);
	}
	
	public Response get(String url, ArrayList<BasicNameValuePair> params) 
			throws WeiboException {
		return httpRequest(url, params, false, METHOD_GET);
	}
	
	public Response get(String url) 
			throws WeiboException {
		return httpRequest(url, null, false, METHOD_GET);
	}
	
	public Response get(String url,  boolean authenticated) 
			throws WeiboException {
		return httpRequest(url, null, authenticated, METHOD_GET);
	}

    public Response httpRequest(String url, ArrayList<BasicNameValuePair> postParams,
            boolean authenticated, String httpMethod) 
    			throws WeiboException {
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
			try {
				post.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
				method = post;
			} catch (IOException ioe) {
				throw new WeiboException(ioe.getMessage(), ioe);
			}

			// log post data
			if (DEBUG) {
				try {
					log("POST INPUT : " + postParams.toString());
					HttpEntity entity = post.getEntity();
					BufferedReader in = new BufferedReader(new InputStreamReader(
							entity.getContent()));
					String line;
					while ((line = in.readLine()) != null) {
						log("POST ENTITY : " + line);
					}
				} catch (IOException ioe) {
					throw new WeiboException(ioe.getMessage(), ioe);
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

		HttpResponse response = null;
		Response res = null;
		
		try {
			response = mClient.execute(method);
			res = new Response(response);
			
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(), e);
            throw new WeiboException(e.getMessage(), e);
		} catch (IOException ioe) {
            throw new WeiboException(ioe.getMessage(), ioe);
		}

		int statusCode = response.getStatusLine().getStatusCode();
		
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
			String msg = getCause(statusCode) + "\n" + res.asString();
			Log.e(TAG, msg);
			
			throw new WeiboException(msg, statusCode);
		}
		
		return res;
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
    
    public boolean isAuthenticationEnabled(){
        return isAuthenticationEnabled;
    }

	public static void log(String msg){
		if (DEBUG) {
			Log.i(TAG,msg);
		}
	}
	
	public static String encode(String value) throws WeiboException {
		try {
			return URLEncoder.encode(value, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e_e) {
			throw new WeiboException(e_e.getMessage(), e_e);
		}
	}
	
	public static String encodeParameters(ArrayList<BasicNameValuePair> params) throws WeiboException {
        StringBuffer buf = new StringBuffer();
        for (int j = 0; j < params.size(); j++) {
            if (j != 0) { buf.append("&"); }
            try {
                buf.append(URLEncoder.encode(params.get(j).getName(), "UTF-8"))
                        .append("=").append(URLEncoder.encode(params.get(j).getValue(), "UTF-8"));
            } catch (java.io.UnsupportedEncodingException neverHappen) {
            	throw new WeiboException(neverHappen.getMessage(), neverHappen);
            }
        }
        return buf.toString();
	}
	
	


}
