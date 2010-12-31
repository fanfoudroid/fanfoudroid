package com.ch_linghu.android.fanfoudroid;

import java.io.File;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;

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
    
    // No Titlebar
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	requestWindowFeature(Window.FEATURE_PROGRESS);
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
  protected static final int OPTIONS_MENU_ID_IMAGE_CAPTURE = 12;
  protected static final int OPTIONS_MENU_ID_PHOTO_LIBRARY = 13;
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    SubMenu submenu = menu.addSubMenu(R.string.photo);
    submenu.setIcon(android.R.drawable.ic_menu_gallery);
    
    submenu.add(0, OPTIONS_MENU_ID_IMAGE_CAPTURE, 0, R.string.image_capture);
    submenu.add(0, OPTIONS_MENU_ID_PHOTO_LIBRARY, 0, R.string.photo_library);
    
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
  private static final int REQUEST_IMAGE_CAPTURE = 2;
  private static final int REQUEST_PHOTO_LIBRARY = 3;

  private File mImageFile;
  private Uri mImageUri;
  
  protected void openImageCaptureMenu() {
	  try {  
		// TODO: API < 1.6, images size too small
      	mImageFile = new File(Environment.getExternalStorageDirectory(), "upload.jpg");
      	mImageUri = Uri.fromFile(mImageFile);
          Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
          intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
          startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);  
      } catch (Exception e) {  
          Log.e(TAG, e.getMessage());  
      }  
  }
  
  protected void openPhotoLibraryMenu() {
	  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
      intent.setType("image/*");
      startActivityForResult(intent, REQUEST_PHOTO_LIBRARY); 
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case OPTIONS_MENU_ID_IMAGE_CAPTURE:
    {
    	openImageCaptureMenu();
        return true;
    }
    case OPTIONS_MENU_ID_PHOTO_LIBRARY:
    {
    	openPhotoLibraryMenu();
    	return true;
    }
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

  private String getRealPathFromURI(Uri contentUri) {
          String[] proj = { MediaStore.Images.Media.DATA };
          Cursor cursor = managedQuery(contentUri, proj, null, null, null);
          int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
          cursor.moveToFirst();
          return cursor.getString(column_index);
      }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_CODE_PREFERENCES && resultCode == RESULT_OK) {
      manageUpdateChecks();
    } else if (requestCode == REQUEST_CODE_LAUNCH_ACTIVITY && resultCode == RESULT_LOGOUT) {
      Log.i(TAG, "Result logout.");
      handleLoggedOut();
    } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	Bundle bundle = new Bundle();
    	bundle.putParcelable("uri", mImageUri);
    	bundle.putString("filename", mImageFile.getPath());
    	
    	intent.setClass(this, WriteActivity.class);
    	intent.putExtras(bundle);
        startActivity(intent);  
    } else if (requestCode == REQUEST_PHOTO_LIBRARY && resultCode == RESULT_OK){
    	mImageUri = data.getData();
    	if (mImageUri.getScheme().equals("content")){
        	String filePath = getRealPathFromURI(mImageUri);
        	mImageFile = new File(filePath);    	
    	}else{
    		//suppose that we got a file:// URI, convert it to content:// URI
    		String filePath = mImageUri.getPath();
    		mImageFile = new File(filePath);
    		mImageUri = Uri.fromFile(mImageFile);
    	}

    	Intent intent = new Intent(Intent.ACTION_SEND);
    	Bundle bundle = new Bundle();
    	bundle.putParcelable("uri", mImageUri);
    	bundle.putString("filename", mImageFile.getPath());
    	
//    	intent.setClass(this, PictureActivity.class);
    	intent.setClass(this, WriteActivity.class);
    	intent.putExtras(bundle);
        startActivity(intent);  	
    }
  }

}
