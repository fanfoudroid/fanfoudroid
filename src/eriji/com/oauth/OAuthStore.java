package eriji.com.oauth;

public interface OAuthStore {
    
    void store(String key, OAuthToken token) throws OAuthStoreException;
    
    OAuthToken get(String key, String TokenType) throws OAuthStoreException;
    
    boolean isExists(String key, String tokenType);
}
