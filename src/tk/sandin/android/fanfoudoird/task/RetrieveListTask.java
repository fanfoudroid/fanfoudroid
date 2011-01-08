package tk.sandin.android.fanfoudoird.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.ch_linghu.android.fanfoudroid.TwitterApi.ApiException;
import com.ch_linghu.android.fanfoudroid.TwitterApi.AuthException;
import com.ch_linghu.android.fanfoudroid.data.Tweet;
import com.ch_linghu.android.fanfoudroid.helper.Preferences;
import com.ch_linghu.android.fanfoudroid.helper.Utils;

public class RetrieveListTask extends AsyncTask<Void, Integer, TaskResult> {
	
	private static final String TAG = "RetrieveListTask";
	private Retrievable activity = null;
	
	public RetrieveListTask(Retrievable activity) {
		super();
		this.activity = activity;
	}
	
	@Override
	public void onPreExecute() {
		activity.onRetrieveBegin();
	}

	@Override
	public void onProgressUpdate(Integer... progress) {
		activity.draw();
	}

	@Override
	public TaskResult doInBackground(Void... params) {
		JSONArray jsonArray;

		String maxId = activity.fetchMaxId(); // getDb().fetchMaxMentionId();

		try {
			jsonArray = activity.getMessageSinceId(maxId); // getApi().getMentionSinceId(maxId);
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

		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		HashSet<String> imageUrls = new HashSet<String>();

		for (int i = 0; i < jsonArray.length(); ++i) {
			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}

			Tweet tweet;

			try {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				tweet = Tweet.create(jsonObject);
				tweets.add(tweet);
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			imageUrls.add(tweet.profileImageUrl);

			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}
		}

		activity.addMessages(tweets, false); // getDb().addMentions(tweets, false);

		if (isCancelled()) {
			return TaskResult.CANCELLED;
		}

		publishProgress();

		for (String imageUrl : imageUrls) {
			if (!Utils.isEmpty(imageUrl)) {
				// Fetch image to cache.
				try {
					activity.getImageManager().put(imageUrl);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}
		}

		return TaskResult.OK;
	}

	@Override
	public void onPostExecute(TaskResult result) {
		if (result == TaskResult.AUTH_ERROR) {
			activity.logout();
		} else if (result == TaskResult.OK) {
			SharedPreferences.Editor editor = activity.getPreferences().edit();
			editor.putLong(Preferences.LAST_TWEET_REFRESH_KEY, Utils
					.getNowTime());
			editor.commit();
			activity.draw();
			activity.goTop();
		} else {
			// Do nothing.
		}

		activity.getRefreshButton().clearAnimation();
		activity.updateProgress("");
	}
}
