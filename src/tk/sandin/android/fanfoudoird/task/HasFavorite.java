package tk.sandin.android.fanfoudoird.task;

import android.content.SharedPreferences;

import com.ch_linghu.android.fanfoudroid.ImageManager;
import com.ch_linghu.android.fanfoudroid.Tweet;
import com.ch_linghu.android.fanfoudroid.TwitterApi;
import com.ch_linghu.android.fanfoudroid.TwitterApplication;
import com.ch_linghu.android.fanfoudroid.TwitterDbAdapter;

public interface HasFavorite {
	// Global
	static TwitterApi mApi = TwitterApplication.mApi;
	static TwitterDbAdapter mDb = TwitterApplication.mDb; 
	SharedPreferences getPreferences();
	
	void logout();
	
	ImageManager getImageManager();
	
	// Callback
	void onFavSuccess();
	void onFavFailure();

}
