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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OAuthToken other = (OAuthToken) obj;
		if (mToken == null) {
			if (other.mToken != null)
				return false;
		} else if (!mToken.equals(other.mToken))
			return false;
		if (mTokenSecret == null) {
			if (other.mTokenSecret != null)
				return false;
		} else if (!mTokenSecret.equals(other.mTokenSecret))
			return false;
		if (mTokenType == null) {
			if (other.mTokenType != null)
				return false;
		} else if (!mTokenType.equals(other.mTokenType))
			return false;
		return true;
	}


}
