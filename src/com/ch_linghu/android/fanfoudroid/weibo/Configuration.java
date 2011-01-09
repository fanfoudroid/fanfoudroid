/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.ch_linghu.android.fanfoudroid.weibo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.Properties;

import junit.runner.Version;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Configuration {
    private static Properties defaultProperty;

    static {
        init();
    }

    /*package*/ static void init() {
        defaultProperty = new Properties();
//        defaultProperty.setProperty("fanfoudroid.debug", "false");
        defaultProperty.setProperty("fanfoudroid.debug", "true");
        defaultProperty.setProperty("fanfoudroid.source", "fanfoudroid");
        //defaultProperty.setProperty("fanfoudroid.clientVersion","");
        defaultProperty.setProperty("fanfoudroid.clientURL", "http://sandin.tk/fanfoudroid.xml");
        defaultProperty.setProperty("fanfoudroid.http.userAgent", "fanfoudroid 1.0");
        //defaultProperty.setProperty("fanfoudroid.user","");
        //defaultProperty.setProperty("fanfoudroid.password","");
        defaultProperty.setProperty("fanfoudroid.http.useSSL", "false");
        //defaultProperty.setProperty("fanfoudroid.http.proxyHost","");
        defaultProperty.setProperty("fanfoudroid.http.proxyHost.fallback", "http.proxyHost");
        //defaultProperty.setProperty("fanfoudroid.http.proxyUser","");
        //defaultProperty.setProperty("fanfoudroid.http.proxyPassword","");
        //defaultProperty.setProperty("fanfoudroid.http.proxyPort","");
        defaultProperty.setProperty("fanfoudroid.http.proxyPort.fallback", "http.proxyPort");
        defaultProperty.setProperty("fanfoudroid.http.connectionTimeout", "20000");
        defaultProperty.setProperty("fanfoudroid.http.readTimeout", "120000");
        defaultProperty.setProperty("fanfoudroid.http.retryCount", "3");
        defaultProperty.setProperty("fanfoudroid.http.retryIntervalSecs", "10");
        //defaultProperty.setProperty("fanfoudroid.oauth.consumerKey","");
        //defaultProperty.setProperty("fanfoudroid.oauth.consumerSecret","");
        defaultProperty.setProperty("fanfoudroid.async.numThreads", "1");
        defaultProperty.setProperty("fanfoudroid.clientVersion", "1.0");
        try {
            // Android platform should have dalvik.system.VMRuntime in the classpath.
            // @see http://developer.android.com/reference/dalvik/system/VMRuntime.html
            Class.forName("dalvik.system.VMRuntime");
            defaultProperty.setProperty("fanfoudroid.dalvik", "true");
        } catch (ClassNotFoundException cnfe) {
            defaultProperty.setProperty("fanfoudroid.dalvik", "false");
        }
        DALVIK = getBoolean("fanfoudroid.dalvik");
        String t4jProps = "fanfoudroid.properties";
        boolean loaded = loadProperties(defaultProperty, "." + File.separatorChar + t4jProps) ||
                loadProperties(defaultProperty, Configuration.class.getResourceAsStream("/WEB-INF/" + t4jProps)) ||
                loadProperties(defaultProperty, Configuration.class.getResourceAsStream("/" + t4jProps));
    }

    private static boolean loadProperties(Properties props, String path) {
        try {
            File file = new File(path);
            if(file.exists() && file.isFile()){
                props.load(new FileInputStream(file));
                return true;
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    private static boolean loadProperties(Properties props, InputStream is) {
        try {
            props.load(is);
            return true;
        } catch (Exception ignore) {
        }
        return false;
    }

    private static boolean DALVIK;


    public static boolean isDalvik() {
        return DALVIK;
    }

    public static boolean useSSL() {
        return getBoolean("fanfoudroid.http.useSSL");
    }
    public static String getScheme(){
        return useSSL() ? "https://" : "http://";
    }

    public static String getCilentVersion() {
        return getProperty("fanfoudroid.clientVersion");
    }

    public static String getCilentVersion(String clientVersion) {
        return getProperty("fanfoudroid.clientVersion", clientVersion);
    }

    public static String getSource() {
        return getProperty("fanfoudroid.source");
    }

    public static String getSource(String source) {
        return getProperty("fanfoudroid.source", source);
    }

    public static String getProxyHost() {
        return getProperty("fanfoudroid.http.proxyHost");
    }

    public static String getProxyHost(String proxyHost) {
        return getProperty("fanfoudroid.http.proxyHost", proxyHost);
    }

    public static String getProxyUser() {
        return getProperty("fanfoudroid.http.proxyUser");
    }

    public static String getProxyUser(String user) {
        return getProperty("fanfoudroid.http.proxyUser", user);
    }

    public static String getClientURL() {
        return getProperty("fanfoudroid.clientURL");
    }

    public static String getClientURL(String clientURL) {
        return getProperty("fanfoudroid.clientURL", clientURL);
    }

    public static String getProxyPassword() {
        return getProperty("fanfoudroid.http.proxyPassword");
    }

    public static String getProxyPassword(String password) {
        return getProperty("fanfoudroid.http.proxyPassword", password);
    }

    public static int getProxyPort() {
        return getIntProperty("fanfoudroid.http.proxyPort");
    }

    public static int getProxyPort(int port) {
        return getIntProperty("fanfoudroid.http.proxyPort", port);
    }

    public static int getConnectionTimeout() {
        return getIntProperty("fanfoudroid.http.connectionTimeout");
    }

    public static int getConnectionTimeout(int connectionTimeout) {
        return getIntProperty("fanfoudroid.http.connectionTimeout", connectionTimeout);
    }

    public static int getReadTimeout() {
        return getIntProperty("fanfoudroid.http.readTimeout");
    }

    public static int getReadTimeout(int readTimeout) {
        return getIntProperty("fanfoudroid.http.readTimeout", readTimeout);
    }

    public static int getRetryCount() {
        return getIntProperty("fanfoudroid.http.retryCount");
    }

    public static int getRetryCount(int retryCount) {
        return getIntProperty("fanfoudroid.http.retryCount", retryCount);
    }

    public static int getRetryIntervalSecs() {
        return getIntProperty("fanfoudroid.http.retryIntervalSecs");
    }

    public static int getRetryIntervalSecs(int retryIntervalSecs) {
        return getIntProperty("fanfoudroid.http.retryIntervalSecs", retryIntervalSecs);
    }

    public static String getUser() {
        return getProperty("fanfoudroid.user");
    }

    public static String getUser(String userId) {
        return getProperty("fanfoudroid.user", userId);
    }

    public static String getPassword() {
        return getProperty("fanfoudroid.password");
    }

    public static String getPassword(String password) {
        return getProperty("fanfoudroid.password", password);
    }

    public static String getUserAgent() {
        return getProperty("fanfoudroid.http.userAgent");
    }

    public static String getUserAgent(String userAgent) {
        return getProperty("fanfoudroid.http.userAgent", userAgent);
    }

    public static String getOAuthConsumerKey() {
        return getProperty("fanfoudroid.oauth.consumerKey");
    }

    public static String getOAuthConsumerKey(String consumerKey) {
        return getProperty("fanfoudroid.oauth.consumerKey", consumerKey);
    }

    public static String getOAuthConsumerSecret() {
        return getProperty("fanfoudroid.oauth.consumerSecret");
    }

    public static String getOAuthConsumerSecret(String consumerSecret) {
        return getProperty("fanfoudroid.oauth.consumerSecret", consumerSecret);
    }

    public static boolean getBoolean(String name) {
        String value = getProperty(name);
        return Boolean.valueOf(value);
    }

    public static int getIntProperty(String name) {
        String value = getProperty(name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public static int getIntProperty(String name, int fallbackValue) {
        String value = getProperty(name, String.valueOf(fallbackValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public static long getLongProperty(String name) {
        String value = getProperty(name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public static String getProperty(String name) {
        return getProperty(name, null);
    }

    public static String getProperty(String name, String fallbackValue) {
        String value;
        try {
            value = System.getProperty(name, fallbackValue);
            if (null == value) {
                value = defaultProperty.getProperty(name);
            }
            if (null == value) {
                String fallback = defaultProperty.getProperty(name + ".fallback");
                if (null != fallback) {
                    value = System.getProperty(fallback);
                }
            }
        } catch (AccessControlException ace) {
            // Unsigned applet cannot access System properties
            value = fallbackValue;
        }
        return replace(value);
    }

    private static String replace(String value) {
        if (null == value) {
            return value;
        }
        String newValue = value;
        int openBrace = 0;
        if (-1 != (openBrace = value.indexOf("{", openBrace))) {
            int closeBrace = value.indexOf("}", openBrace);
            if (closeBrace > (openBrace + 1)) {
                String name = value.substring(openBrace + 1, closeBrace);
                if (name.length() > 0) {
                    newValue = value.substring(0, openBrace) + getProperty(name)
                            + value.substring(closeBrace + 1);

                }
            }
        }
        if (newValue.equals(value)) {
            return value;
        } else {
            return replace(newValue);
        }
    }

    public static int getNumberOfAsyncThreads() {
        return getIntProperty("fanfoudroid.async.numThreads");
    }

    public static boolean getDebug() {
        return getBoolean("fanfoudroid.debug");

    }
}
