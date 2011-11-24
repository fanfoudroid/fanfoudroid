package eriji.com.oauth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.text.TextUtils;
import android.util.Log;

import com.ch_linghu.fanfoudroid.fanfou.Configuration;
import com.ch_linghu.fanfoudroid.http.Response;
import com.ch_linghu.fanfoudroid.http.ResponseException;

public class XAuthClient extends OAuthClient {
	private static final String TAG = "XAuthClient";
	private static final String CONSUMER_KEY = Configuration
			.getOAuthConsumerKey();
	private static final String CONSUMER_SECRET = Configuration
			.getOAuthConsumerSecret();
	private static final String BASE_URL = Configuration.getOAuthBaseUrl();

	public XAuthClient(String consumer_key, String consumer_secret,
			String base_url, OAuthStore store) {
		super(consumer_key, consumer_secret, base_url, store);
		// TODO Auto-generated constructor stub
	}

	public void retrieveAccessToken(String username, String password)
			throws OAuthStoreException, ClientProtocolException, IOException,
			ResponseException {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(BASE_URL + "/access_token");
		CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
				CONSUMER_KEY, CONSUMER_SECRET);
		List<BasicNameValuePair> params = Arrays.asList(new BasicNameValuePair(
				"x_auth_username", username), new BasicNameValuePair(
				"x_auth_password", password), new BasicNameValuePair(
				"x_auth_mode", "client_auth"));
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("wtf");
		}
		request.setEntity(entity);
		try {
			consumer.sign(request);
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpResponse response = client.execute(request);
		String responseString = Response.entityToString(response.getEntity());

		String[] tmp = TextUtils.split(responseString, "&");
		if (tmp.length < 2) {
			Log.e(TAG, "something wrong with access token response: " + responseString);
			return;
		}
		String token = tmp[0].replace("oauth_token=", "");
		String tokenSerect = tmp[1].replace("oauth_token_secret=", "");
		mAccessToken = new OAuthAccessToken(token, tokenSerect);
		storeAccessToken();

		logger.info("retrieve access token with request token "
				+ mConsumer.getToken() + " " + mAccessToken + " "
				+ mProvider.getAccessTokenEndpointUrl());
	}

	// /////////////////////////

	public static XAuthClient factory() {
		return new XAuthClient(Configuration.getOAuthConsumerKey(),
				Configuration.getOAuthConsumerSecret(),
				Configuration.getOAuthBaseUrl(),
				new OAuthSharedPreferencesStore());
	}

	public static boolean auth(String username, String password) throws Exception {
		XAuthClient xauth = XAuthClient.factory();
		xauth.retrieveAccessToken(username, password);
		return xauth.hasAccessToken();
	}

}
