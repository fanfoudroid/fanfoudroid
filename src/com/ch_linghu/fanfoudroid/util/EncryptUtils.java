package com.ch_linghu.fanfoudroid.util;


public class EncryptUtils {
    private static final String SECRET_CODE = "::SECRET::";

    public static String encryptPassword(String password) {
        //return Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
        return password;
    }

    public static String decryptPassword(String password) {
        //return new String(Base64.decode(password, Base64.DEFAULT));
        return password;
    }

}
