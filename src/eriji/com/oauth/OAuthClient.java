package eriji.com.oauth;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.HmacSha1MessageSigner;

import org.apache.http.HttpRequest;

import android.util.Log;

public class OAuthClient {
    protected Logger logger = Logger.getLogger(OAuthClient.class);
    
    protected OAuthConsumer mConsumer = null;
    protected OAuthProvider mProvider = null;
    
    protected OAuthRequestToken mRequestToken = null;
    protected OAuthAccessToken mAccessToken = null;
    
    private OAuthStore mStore = null;
    
    /**
     * @param consumer_key
     * @param consumer_secret
     * @param base_url end with '/'
     */
    public OAuthClient(String consumer_key, String consumer_secret, String base_url, OAuthStore store) {
        this(consumer_key, consumer_secret,
                           base_url + "/request_token",
                           base_url + "/access_token",
                           base_url + "/authorize",
                           store);
    }
    
    public OAuthClient(String consumer_key,
                       String consumer_secret,
                       String requestTokenEndpointUrl,
                       String accessTokenEndpointUrl,
                       String authorizationWebsiteUrl,
                       OAuthStore store)
    {
        mStore = store;
        Log.i("LDS", consumer_key + " " + consumer_secret);
        mConsumer = new CommonsHttpOAuthConsumer(consumer_key, consumer_secret);
        mProvider = new DefaultOAuthProvider(requestTokenEndpointUrl, 
                                             accessTokenEndpointUrl,
                                             authorizationWebsiteUrl);
         // 饭否API服务器目前只支持HMAC_SHA1签名方式
        mConsumer.setMessageSigner(new HmacSha1MessageSigner());
    }
    
    /**
     * 向服务器请求一个request token
     * 
     * @return 返回服务器认证网址
     * @throws OAuthMessageSignerException
     * @throws OAuthNotAuthorizedException
     * @throws OAuthExpectationFailedException
     * @throws OAuthCommunicationException
     * @throws OAuthStoreException 
     */
    public String retrieveRequestToken() throws OAuthMessageSignerException,
                                              OAuthNotAuthorizedException,
                                              OAuthExpectationFailedException,
                                              OAuthCommunicationException,
                                              OAuthStoreException 
    {
        logger.debug("retrieve request token, key/secret : " 
                + mConsumer.getConsumerKey() + " " 
                + mConsumer.getConsumerSecret() + " "
                + mProvider.getRequestTokenEndpointUrl());
        String authUrl = mProvider.retrieveRequestToken(mConsumer, OAuth.OUT_OF_BAND);
        mRequestToken = new OAuthRequestToken(mConsumer.getToken(), 
                                             mConsumer.getTokenSecret());
        storeRequestToken();
        logger.info("Please Go to : " + authUrl);
        return authUrl;
    }
    
    /**
     * 用户在服务器网站上确认授权给request token后, 服务器返回用户pinCode
     * 用户输入此pinCode则表示服务器已经认证
     * 此时利用已被用户授权的这个request token和服务器交换得到access token
     * 
     * @param pinCode oauth_verifier
     * @throws OAuthMessageSignerException
     * @throws OAuthNotAuthorizedException
     * @throws OAuthExpectationFailedException
     * @throws OAuthCommunicationException
     * @throws OAuthClientException 
     * @throws OAuthStoreException 
     */
    public void retrieveAccessToken(String pinCode) throws OAuthMessageSignerException,
                                                           OAuthNotAuthorizedException,
                                                           OAuthExpectationFailedException,
                                                           OAuthCommunicationException,
                                                           OAuthStoreException,
                                                           OAuthClientException
    {
         
        OAuthToken token = getRequestToken();
        mConsumer.setTokenWithSecret(token.getToken(), token.getTokenSecret());
        mProvider.retrieveAccessToken(mConsumer, pinCode);
        mAccessToken = new OAuthAccessToken(mConsumer.getToken(), 
                                            mConsumer.getTokenSecret());
        storeAccessToken();
        
        logger.info("retrieve access token, request token/token secret." 
                + mConsumer.getToken() + " " 
                + mConsumer.getTokenSecret() + " "
                + mProvider.getAccessTokenEndpointUrl());       
    }

    public OAuthConsumer getConsumer() {
        return mConsumer;
    }

    public OAuthProvider getProvider() {
        return mProvider;
    }

    public void setStore(OAuthStore store) {
        mStore = store;
    }

    /**
     * 储存request token
     * 
     * @throws OAuthStoreException
     */
    public void storeRequestToken() throws OAuthStoreException {
        mStore.store(mConsumer.getConsumerKey(), mRequestToken);
    }
    
    /**
     * 储存access token
     * 
     * @throws OAuthStoreException
     */
    public void storeAccessToken() throws OAuthStoreException {
        mStore.store(mConsumer.getConsumerKey(), mAccessToken);
        // TODO: delete request token
    }
    
    /**
     * 取得储存器中的request token
     * 
     * @return request token or NULL
     * @throws OAuthStoreException
     * @throws OAuthClientException
     * @throws OAuthMessageSignerException
     * @throws OAuthNotAuthorizedException
     * @throws OAuthExpectationFailedException
     * @throws OAuthCommunicationException
     */
    public OAuthToken getRequestToken() throws OAuthStoreException, OAuthClientException, OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
        String key = mConsumer.getConsumerKey();
        if (mRequestToken != null) {
            return mRequestToken;
        } else {
            return mStore.get(key, OAuthToken.REQUEST_TOKEN);
        }
    }
    
    /**
     * 取得储存中的access token 
     * 
     * @return access token or NULL
     * @throws OAuthStoreException
     * @throws OAuthClientException
     * @throws OAuthMessageSignerException
     * @throws OAuthNotAuthorizedException
     * @throws OAuthExpectationFailedException
     * @throws OAuthCommunicationException
     */
    public OAuthToken getAccessToken() throws OAuthStoreException, OAuthClientException, OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
        String key = mConsumer.getConsumerKey();
        if (mAccessToken != null) {
            return mAccessToken;
        } else {
            return mStore.get(key, OAuthToken.ACCESSS_TOKEN);
        }
    }
    
    /**
     * 给HttpRequest加入OAuth认证信息
     * 
     * @param request HTTP请求 
     * @throws OAuthClientException
     */
    public void signRequest(HttpRequest request) throws OAuthClientException {
        try {
            OAuthToken accessToken = getAccessToken();
            
            if (null != accessToken) {
                logger.debug("Sign request with access token : " + accessToken);
            
               // OAuthConsumer consumer = new CommonsHttpOAuthConsumer(mConsumer.getConsumerKey(),
               //     mConsumer.getConsumerSecret());
                mConsumer.setTokenWithSecret(accessToken.getToken(), accessToken.getTokenSecret());
                mConsumer.sign(request);
            } else {
                logger.error("Don't has a access token yet.");
                String authUrl = retrieveRequestToken();
                throw new Exception("Unauthorized, please goto " + authUrl );
            }
        } catch (Exception e) {
            throw new OAuthClientException("Cann't sign request: " + e.getMessage(), e);
        }
    }
    
    /**
     * 快速检查储存器已有access token
     * 
     * @return
     */
    public boolean hasAccessToken() {
        return mStore.isExists(mConsumer.getConsumerKey(), OAuthToken.ACCESSS_TOKEN);
    }
    
}
