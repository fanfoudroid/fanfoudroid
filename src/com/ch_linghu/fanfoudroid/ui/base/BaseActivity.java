package com.ch_linghu.fanfoudroid.ui.base;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;

import com.ch_linghu.fanfoudroid.AboutDialog;
import com.ch_linghu.fanfoudroid.FanfouWidget;
import com.ch_linghu.fanfoudroid.LoginActivity;
import com.ch_linghu.fanfoudroid.PreferencesActivity;
import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterActivity;
import com.ch_linghu.fanfoudroid.TwitterApplication;
import com.ch_linghu.fanfoudroid.WriteActivity;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.helper.ImageManager;
import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.service.TwitterService;
import com.ch_linghu.fanfoudroid.weibo.Weibo;

/**
 * A BaseActivity has common routines and variables for an Activity that
 * contains a list of tweets and a text input field.
 * 
 * Not the cleanest design, but works okay for several Activities in this app.
 */

public class BaseActivity extends Activity {

    private static final String TAG = "BaseActivity";

    protected SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _onCreate(savedInstanceState);
    }

    // 因为onCreate方法无法返回状态，因此无法进行状态判断，
    // 为了能对上层返回的信息进行判断处理，我们使用_onCreate代替真正的
    // onCreate进行工作。onCreate仅在顶层调用_onCreate。
    protected boolean _onCreate(Bundle savedInstanceState) {
        if (!checkIsLogedIn()) {
            return false;
        } else {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            mPreferences = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(this);

            manageUpdateChecks();

            // No Titlebar
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            requestWindowFeature(Window.FEATURE_PROGRESS);

            return true;
        }
    }

    protected void handleLoggedOut() {
        if (isTaskRoot()) {
            showLogin();
        } else {
            setResult(RESULT_LOGOUT);
        }

        finish();
    }

    public TwitterDatabase getDb() {
        return TwitterApplication.mDb;
    }

    public Weibo getApi() {
        return TwitterApplication.mApi;
    }

    public SharedPreferences getPreferences() {
        return mPreferences;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected boolean isLoggedIn() {
        return getApi().isLoggedIn();
    }

    private static final int RESULT_LOGOUT = RESULT_FIRST_USER + 1;

    // Retrieve interface

    // public ImageManager getImageManager() {
    // return TwitterApplication.mImageManager;
    // }

    private void _logout() {
        TwitterService.unschedule(BaseActivity.this);

        getDb().clearData();
        getApi().reset();

        // Clear SharedPreferences
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        editor.commit();

        // TODO: 提供用户手动情况所有缓存选项
        TwitterApplication.mProfileImageCacheManager.getImageManager().clear();

        // TODO: cancel notifications.
        TwitterService.unschedule(BaseActivity.this);

        handleLoggedOut();
    }

    public void logout() {
        Dialog dialog = new AlertDialog.Builder(BaseActivity.this)
                .setTitle("提示").setMessage("确实要注销吗?")
                .setPositiveButton("确定", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        _logout();
                    }

                }).setNegativeButton("取消", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }

                }).create();
        dialog.show();
    }

    protected void showLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        // TODO: might be a hack?
        intent.putExtra(Intent.EXTRA_INTENT, getIntent());

        startActivity(intent);
    }

    protected void manageUpdateChecks() {
        boolean isEnabled = mPreferences.getBoolean(
                Preferences.CHECK_UPDATES_KEY, false);

        if (isEnabled) {
            // TODO: 为什么要每次切换页面都重新schedule一遍?
            TwitterService.schedule(this);
        } else if (!TwitterService.isWidgetEnabled()) {
            TwitterService.unschedule(this);
        }
    }

    // Menus.

    protected static final int OPTIONS_MENU_ID_LOGOUT = 1;
    protected static final int OPTIONS_MENU_ID_PREFERENCES = 2;
    protected static final int OPTIONS_MENU_ID_ABOUT = 3;
    protected static final int OPTIONS_MENU_ID_SEARCH = 4;
    protected static final int OPTIONS_MENU_ID_REPLIES = 5;
    protected static final int OPTIONS_MENU_ID_DM = 6;
    protected static final int OPTIONS_MENU_ID_TWEETS = 7;
    protected static final int OPTIONS_MENU_ID_TOGGLE_REPLIES = 8;
    protected static final int OPTIONS_MENU_ID_FOLLOW = 9;
    protected static final int OPTIONS_MENU_ID_UNFOLLOW = 10;
    protected static final int OPTIONS_MENU_ID_IMAGE_CAPTURE = 11;
    protected static final int OPTIONS_MENU_ID_PHOTO_LIBRARY = 12;
    protected static final int OPTIONS_MENU_ID_EXIT = 13;

    /**
     * 如果增加了Option Menu常量的数量，则必须重载此方法， 以保证其他人使用常量时不产生重复
     * 
     * @return 最大的Option Menu常量
     */
    protected int getLastOptionMenuId() {
        return OPTIONS_MENU_ID_EXIT;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // SubMenu submenu =
        // menu.addSubMenu(R.string.write_label_insert_picture);
        // submenu.setIcon(android.R.drawable.ic_menu_gallery);
        //
        // submenu.add(0, OPTIONS_MENU_ID_IMAGE_CAPTURE, 0,
        // R.string.write_label_take_a_picture);
        // submenu.add(0, OPTIONS_MENU_ID_PHOTO_LIBRARY, 0,
        // R.string.write_label_choose_a_picture);
        //
        // MenuItem item = menu.add(0, OPTIONS_MENU_ID_SEARCH, 0,
        // R.string.omenu_search);
        // item.setIcon(android.R.drawable.ic_search_category_default);
        // item.setAlphabeticShortcut(SearchManager.MENU_KEY);

        MenuItem item;
        item = menu.add(0, OPTIONS_MENU_ID_PREFERENCES, 0,
                R.string.omenu_settings);
        item.setIcon(android.R.drawable.ic_menu_preferences);

        item = menu.add(0, OPTIONS_MENU_ID_LOGOUT, 0, R.string.omenu_signout);
        item.setIcon(android.R.drawable.ic_menu_revert);

        item = menu.add(0, OPTIONS_MENU_ID_ABOUT, 0, R.string.omenu_about);
        item.setIcon(android.R.drawable.ic_menu_info_details);

        item = menu.add(0, OPTIONS_MENU_ID_EXIT, 0, R.string.omenu_exit);
        item.setIcon(android.R.drawable.ic_menu_rotate);

        return true;
    }

    protected static final int REQUEST_CODE_LAUNCH_ACTIVITY = 0;
    protected static final int REQUEST_CODE_PREFERENCES = 1;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case OPTIONS_MENU_ID_LOGOUT:
            logout();
            return true;
        case OPTIONS_MENU_ID_SEARCH:
            onSearchRequested();
            return true;
        case OPTIONS_MENU_ID_PREFERENCES:
            Intent launchPreferencesIntent = new Intent().setClass(this,
                    PreferencesActivity.class);
            startActivityForResult(launchPreferencesIntent,
                    REQUEST_CODE_PREFERENCES);
            return true;
        case OPTIONS_MENU_ID_ABOUT:
            AboutDialog.show(this);
            return true;
        case OPTIONS_MENU_ID_EXIT:
            exit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void exit() {
        TwitterService.unschedule(this);
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }

    protected void launchActivity(Intent intent) {
        // TODO: probably don't need this result chaining to finish upon logout.
        // since the subclasses have to check in onResume.
        startActivityForResult(intent, REQUEST_CODE_LAUNCH_ACTIVITY);
    }

    protected void launchDefaultActivity() {
        Intent intent = new Intent();
        intent.setClass(this, TwitterActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PREFERENCES && resultCode == RESULT_OK) {
            manageUpdateChecks();
        } else if (requestCode == REQUEST_CODE_LAUNCH_ACTIVITY
                && resultCode == RESULT_LOGOUT) {
            Log.d(TAG, "Result logout.");
            handleLoggedOut();
        }
    }

    protected boolean checkIsLogedIn() {
        if (!getApi().isLoggedIn()) {
            Log.d(TAG, "Not logged in.");
            handleLoggedOut();
            return false;
        }
        return true;
    }

}
