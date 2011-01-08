package tk.sandin.android.fanfoudoird.task;

import android.content.SharedPreferences;

import com.ch_linghu.android.fanfoudroid.TwitterApi;
import com.ch_linghu.android.fanfoudroid.TwitterApplication;
import com.ch_linghu.android.fanfoudroid.data.Tweet;
import com.ch_linghu.android.fanfoudroid.data.db.TwitterDbAdapter;
import com.ch_linghu.android.fanfoudroid.helper.ImageManager;

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
