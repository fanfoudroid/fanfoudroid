package com.ch_linghu.fanfoudroid.helper.utils;

import android.util.Base64;

public class EncryptUtils {
    private static final String SECRET_CODE = "::SECRET::";

    public static String encryptPassword(String password) {
        password += SECRET_CODE;
        return Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
    }

    public static String decryptPassword(String password) {
        password = password.replaceFirst(SECRET_CODE, "");
        return new String(Base64.decode(password, Base64.DEFAULT));
    }

}
