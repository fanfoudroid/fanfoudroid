package eriji.com.oauth;

public class OAuthToken {
    public static final String REQUEST_TOKEN = "request_token";
    public static final String ACCESSS_TOKEN = "access_token";
    
    private String mToken = null;
    private String mTokenSecret = null;
    private String mTokenType;
    
    public OAuthToken(String token, String tokenSecret) 
    {
        mToken = token;
        mTokenSecret = tokenSecret;
    }
    
    public OAuthToken(String token, String tokenSecret, String tokenType) 
    {
        this(token, tokenSecret);
        mTokenType = tokenType;
    }

    public String getToken() {
        return mToken;
    }

    public String getTokenSecret() {
        return mTokenSecret;
    }
    
    public String getTokenType() {
        return mTokenType;
    }

    @Override
    public String toString() {
        return "OAuthToken [mToken=" + mToken + ", mTokenSecret="
                + mTokenSecret + "]";
    }


}
