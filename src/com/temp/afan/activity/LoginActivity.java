package com.temp.afan.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Login Page
 */
public class LoginActivity extends Activity {

    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

}
