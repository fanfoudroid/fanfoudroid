package com.ch_linghu.android.fanfoudroid;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * A BaseActivity has common routines and variables for an Activity
 * that contains a list of tweets and a text input field.
 *
 * Not the cleanest design, but works okay for several Activities in this app.
 */

public class BaseActivity extends Activity {

  private static final String TAG = "BaseActivity";

  protected SharedPreferences mPreferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    manageUpdateChecks();
  }

  protected void handleLoggedOut() {
    if (isTaskRoot()) {
      showLogin();
    } else {
      setResult(RESULT_LOGOUT);
    }

    finish();
  }

  protected ImageManager getImageManager() {
    return TwitterApplication.mImageManager;
  }

  protected TwitterDbAdapter getDb() {
    return TwitterApplication.mDb;
  }

  protected TwitterApi getApi() {
    return TwitterApplication.mApi;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  protected boolean isLoggedIn() {
    return getApi().isLoggedIn();
  }

  private static final int RESULT_LOGOUT = RESULT_FIRST_USER + 1;

  protected void logout() {
    TwitterService.unschedule(this);

    getDb().clearData();
    getApi().logout();

    SharedPreferences.Editor editor = mPreferences.edit();
    editor.clear();
    editor.commit();

    getImageManager().clear();

    // TODO: cancel notifications.

    handleLoggedOut();
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
      TwitterService.schedule(this);
    } else {
      TwitterService.unschedule(this);
    }
  }

  // Menus.

  protected static final int OPTIONS_MENU_ID_LOGOUT = 1;
  protected static final int OPTIONS_MENU_ID_PREFERENCES = 2;
  protected static final int OPTIONS_MENU_ID_ABOUT = 3;
  protected static final int OPTIONS_MENU_ID_REFRESH = 4;
  protected static final int OPTIONS_MENU_ID_REPLIES = 5;
  protected static final int OPTIONS_MENU_ID_DM = 6;
  protected static final int OPTIONS_MENU_ID_TWEETS = 7;
  protected static final int OPTIONS_MENU_ID_TOGGLE_REPLIES = 8;
  protected static final int OPTIONS_MENU_ID_FOLLOW = 9;
  protected static final int OPTIONS_MENU_ID_UNFOLLOW = 10;
  protected static final int OPTIONS_MENU_ID_SEARCH = 11;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    MenuItem item = menu.add(0, OPTIONS_MENU_ID_SEARCH, 0, R.string.search);
    item.setIcon(android.R.drawable.ic_search_category_default);
    item.setAlphabeticShortcut(SearchManager.MENU_KEY);

    item = menu.add(0, OPTIONS_MENU_ID_PREFERENCES, 0, R.string.settings);
    item.setIcon(android.R.drawable.ic_menu_preferences);

    item = menu.add(0, OPTIONS_MENU_ID_LOGOUT, 0, R.string.signout);
    item.setIcon(android.R.drawable.ic_menu_revert);

    item = menu.add(0, OPTIONS_MENU_ID_ABOUT, 0, R.string.about);
    item.setIcon(android.R.drawable.ic_menu_info_details);

    return true;
  }

  private static final int REQUEST_CODE_LAUNCH_ACTIVITY = 0;
  private static final int REQUEST_CODE_PREFERENCES = 1;

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
      startActivityForResult(launchPreferencesIntent, REQUEST_CODE_PREFERENCES);
      return true;
    case OPTIONS_MENU_ID_ABOUT:
      AboutDialog.show(this);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  protected void launchActivity(Intent intent) {
    // TODO: probably don't need this result chaining to finish upon logout.
    // since the subclasses have to check in onResume.
    startActivityForResult(intent, REQUEST_CODE_LAUNCH_ACTIVITY);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_CODE_PREFERENCES && resultCode == RESULT_OK) {
      manageUpdateChecks();
    } else if (requestCode == REQUEST_CODE_LAUNCH_ACTIVITY && resultCode == RESULT_LOGOUT) {
      Log.i(TAG, "Result logout.");
      handleLoggedOut();
    }
  }

}
