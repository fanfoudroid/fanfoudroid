package com.ch_linghu.android.fanfoudroid;

import java.security.AccessControlException;
import java.util.Properties;

public class Configuration {

	// TODO: delete it
	// public static final boolean debug = false;
	public static final boolean debug = true;
	
	private static Properties defaultProperty;

	static {
		init();
	}

	/* package */static void init() {
		defaultProperty = new Properties();
		// defaultProperty.setProperty("weibo4j.debug", "false");
		defaultProperty.setProperty("fanfoudroid.debug", "true");
		defaultProperty.setProperty("fanfoudroid.source", "fanfoudroid");
	}


	public static boolean getDebug() {
		return getBoolean("fanfoudroid.debug");

	}
	
    public static String getSource() {
        return getProperty("fanfoudroid.source");
    }
    
    public static String getSource(String source) {
        return getProperty("weibo4j.source", source);
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

}