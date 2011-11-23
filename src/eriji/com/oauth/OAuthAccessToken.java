package eriji.com.oauth;

public class OAuthAccessToken extends OAuthToken {

    public OAuthAccessToken(String token, String tokenSecret) {
        super(token, tokenSecret, OAuthToken.ACCESSS_TOKEN);
    }
    

}
