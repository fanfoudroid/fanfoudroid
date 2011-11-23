package eriji.com.oauth;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class OAuthFileStore implements OAuthStore {
    private Logger logger = Logger.getLogger(OAuthFileStore.class);
    
    private static final String TOKEN_KEY = "token";
    private static final String SECRET_KEY = "secret";
    
    
    private String mCacheDir = null;

    public OAuthFileStore(String cacheDir) {
        File dir = new File(cacheDir);
        if (! dir.exists()) {
            dir.mkdir();
        } 
        mCacheDir = dir.getAbsolutePath();
    }

    @Override
    public void store(String key, OAuthToken token) throws OAuthStoreException
    {
        String file = mCacheDir + "/" + key + "_" + token.getTokenType();
        
        try {
            Properties p = new Properties();
            p.setProperty(TOKEN_KEY, token.getToken());
            p.setProperty(SECRET_KEY, token.getTokenSecret());

            FileWriter fw = new FileWriter(file);
            p.store(fw, key + "'s " +  token.getTokenType() );
            fw.close();
            logger.info("Store " + token.getTokenType() + ": " + token + " into " + file);
        } catch (IOException ioe) {
            throw new OAuthStoreException("Cann't store token into " + file);
        }
    }

    @Override
    public OAuthToken get(String key, String tokenType) throws OAuthStoreException {
        // TODO Auto-generated method stub
        try {
            File file = new File(mCacheDir + "/" + key + "_" + tokenType);
            if (file.exists()) {
                Properties p = new Properties();
                FileReader fr = new FileReader(file);
                p.load(fr);
                
                OAuthToken token = new OAuthToken(p.getProperty(TOKEN_KEY),
                                                  p.getProperty(SECRET_KEY));
                logger.info("Get " + tokenType + ": " + token);
                return token;
            }
        } catch (IOException ioe) {
            throw new OAuthStoreException("Cann't get token: " + key);
        } 
        return null;
    }

    @Override
    public boolean isExists(String key, String tokenType) {
        File file = new File(mCacheDir + "/" + key + "_" + tokenType);
        return file.exists();
    }

}
