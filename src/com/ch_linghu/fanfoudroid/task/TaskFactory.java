package com.ch_linghu.fanfoudroid.task;

import android.app.Activity;
import android.os.AsyncTask;

public class TaskFactory {

	public static final int FOLLOWERS_TASK_TYPE = 1;
	public static final int RETRIEVE_LIST_TASK_TYPE = 2;
	public static final int FAVORITE_TASK_TYPE = 3;

	public static AsyncTask create(int taskType, Activity activity) {

		switch (taskType) {
		case FOLLOWERS_TASK_TYPE:
			if (activity instanceof Followable) {
				return new FollowersTask((Followable) activity);
			}
			break;
		case RETRIEVE_LIST_TASK_TYPE:
			if (activity instanceof Retrievable) {
				return new RetrieveListTask((Retrievable) activity);
			}
			break;
		case FAVORITE_TASK_TYPE:
			if (activity instanceof Retrievable) {
				return new FavoriteTask((HasFavorite) activity);
			}
			break;
		}

		return null;
	}
}
