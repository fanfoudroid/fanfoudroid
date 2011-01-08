package tk.sandin.android.fanfoudoird.task;

import java.io.IOException;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.ch_linghu.android.fanfoudroid.TwitterApi.ApiException;
import com.ch_linghu.android.fanfoudroid.TwitterApi.AuthException;
import com.ch_linghu.android.fanfoudroid.helper.Preferences;
import com.ch_linghu.android.fanfoudroid.helper.Utils;

public class FollowersTask extends AsyncTask<Void, Void, TaskResult> {
	
	private static final String TAG = "FollowersTask";
	private Followable activity = null;
	
	public FollowersTask(Followable activity) {
		super();
		this.activity = activity;
	}
	
	@Override
	public TaskResult doInBackground(Void... params) {
		try {
			ArrayList<String> followers = activity.mApi.getFollowersIds();
			activity.mDb.syncFollowers(followers);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			return TaskResult.IO_ERROR;
		} catch (AuthException e) {
			Log.i(TAG, "Invalid authorization.");
			return TaskResult.AUTH_ERROR;
		} catch (ApiException e) {
			Log.e(TAG, e.getMessage(), e);
			return TaskResult.IO_ERROR;
		}

		return TaskResult.OK;
	}

	@Override
	public void onPostExecute(TaskResult result) {
		if (result == TaskResult.OK) {
			SharedPreferences sp = activity.getPreferences();
			SharedPreferences.Editor editor = sp.edit();
			editor.putLong(Preferences.LAST_FOLLOWERS_REFRESH_KEY, Utils
					.getNowTime());
			editor.commit();
		} else {
			// Do nothing.
		}
	}
}
