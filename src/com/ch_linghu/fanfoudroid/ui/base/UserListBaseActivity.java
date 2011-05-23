package com.ch_linghu.fanfoudroid.ui.base;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ch_linghu.fanfoudroid.DmActivity;
import com.ch_linghu.fanfoudroid.MentionActivity;
import com.ch_linghu.fanfoudroid.ProfileActivity;
import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.TwitterActivity;
import com.ch_linghu.fanfoudroid.UserTimelineActivity;
import com.ch_linghu.fanfoudroid.WriteActivity;
import com.ch_linghu.fanfoudroid.WriteDmActivity;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.User;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.task.TweetCommonTask;
import com.ch_linghu.fanfoudroid.ui.module.Feedback;
import com.ch_linghu.fanfoudroid.ui.module.FeedbackFactory;
import com.ch_linghu.fanfoudroid.ui.module.NavBar;
import com.ch_linghu.fanfoudroid.ui.module.TweetAdapter;
import com.ch_linghu.fanfoudroid.ui.module.FeedbackFactory.FeedbackType;
import com.ch_linghu.fanfoudroid.util.TextHelper;

public abstract class UserListBaseActivity extends BaseActivity implements
        Refreshable {
    static final String TAG = "TwitterListBaseActivity";

    protected TextView mProgressText;

    protected NavBar mNavbar;
    protected Feedback mFeedback;

    protected static final int STATE_ALL = 0;
    protected static final String SIS_RUNNING_KEY = "running";
    private static final String USER_ID = "userId";

    // Tasks.
    protected GenericTask mFavTask;
    private TaskListener mFavTaskListener = new TaskAdapter() {

        @Override
        public String getName() {
            return "FavoriteTask";
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            if (result == TaskResult.AUTH_ERROR) {
                logout();
            } else if (result == TaskResult.OK) {
                onFavSuccess();
            } else if (result == TaskResult.IO_ERROR) {
                onFavFailure();
            }
        }
    };

    static final int DIALOG_WRITE_ID = 0;

    abstract protected int getLayoutId();
    abstract protected ListView getUserList();
    abstract protected TweetAdapter getUserAdapter();
    abstract protected void setupState();
    abstract protected String getActivityTitle();
    abstract protected boolean useBasicMenu();
    abstract protected User getContextItemUser(int position);
    abstract protected void updateTweet(Tweet tweet);

    protected abstract String getUserId();// 获得用户id

    public static final int CONTENT_PROFILE_ID = Menu.FIRST + 1;
    public static final int CONTENT_STATUS_ID = Menu.FIRST + 2;
    public static final int CONTENT_DEL_FRIEND = Menu.FIRST + 3;
    public static final int CONTENT_ADD_FRIEND = Menu.FIRST + 4;
    public static final int CONTENT_SEND_DM = Menu.FIRST + 5;
    public static final int CONTENT_SEND_MENTION = Menu.FIRST + 6;

    /**
     * 如果增加了Context Menu常量的数量，则必须重载此方法， 以保证其他人使用常量时不产生重复
     * 
     * @return 最大的Context Menu常量
     */
    // protected int getLastContextMenuId(){
    // return CONTEXT_DEL_FAV_ID;
    // }

    @Override
    protected boolean _onCreate(Bundle savedInstanceState) {
        if (super._onCreate(savedInstanceState)) {
            setContentView(getLayoutId());
            mNavbar = new NavBar(NavBar.HEADER_STYLE_HOME, this);
            mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);

            mPreferences.getInt(Preferences.TWITTER_ACTIVITY_STATE_KEY,
                    STATE_ALL);

            mProgressText = (TextView) findViewById(R.id.progress_text);

            setupState();

            registerForContextMenu(getUserList());
            registerOnClickListener(getUserList());

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkIsLogedIn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mFavTask != null
                && mFavTask.getStatus() == GenericTask.Status.RUNNING) {
            mFavTask.cancel(true);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (useBasicMenu()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
            User user = getContextItemUser(info.position);

            if (user == null) {
                Log.w(TAG, "Selected item not available.");
                return;
            }
            menu.add(0, CONTENT_PROFILE_ID, 0,
                    user.screenName + getResources().getString(
                                    R.string.cmenu_user_profile_prefix));
            menu.add(0, CONTENT_STATUS_ID, 0, user.screenName
                    + getResources().getString(R.string.cmenu_user_status));
            menu.add(0, CONTENT_SEND_MENTION, 0,
                    getResources().getString(R.string.cmenu_user_send_prefix)
                            + user.screenName
                            + getResources().getString(
                                    R.string.cmenu_user_sendmention_suffix));
            menu.add(0, CONTENT_SEND_DM, 0,
                    getResources().getString(R.string.cmenu_user_send_prefix)
                            + user.screenName
                            + getResources().getString(
                                    R.string.cmenu_user_senddm_suffix));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        User user = getContextItemUser(info.position);

        if (user == null) {
            Log.w(TAG, "Selected item not available.");
            return super.onContextItemSelected(item);
        }

        switch (item.getItemId()) {
        case CONTENT_PROFILE_ID:
            launchActivity(ProfileActivity.createIntent(user.id));
            return true;

        case CONTENT_STATUS_ID:
            launchActivity(UserTimelineActivity
                    .createIntent(user.id, user.name));
            return true;
        case CONTENT_DEL_FRIEND:
            delFriend(user.id);
            return true;
        case CONTENT_ADD_FRIEND:
            addFriend(user.id);
            return true;
        case CONTENT_SEND_MENTION:
            launchActivity(WriteActivity.createNewTweetIntent(String.format(
                    "@%s ", user.screenName)));
            return true;
        case CONTENT_SEND_DM:
            launchActivity(WriteDmActivity.createIntent(user.id));
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    /**
     * 取消关注
     * 
     * @param id
     */
    private void delFriend(final String id) {
        Builder diaBuilder = new AlertDialog.Builder(UserListBaseActivity.this)
                .setTitle("关注提示").setMessage("确实要取消关注吗?");
        diaBuilder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cancelFollowingTask != null
                                && cancelFollowingTask.getStatus() == GenericTask.Status.RUNNING) {
                            return;
                        } else {
                            cancelFollowingTask = new CancelFollowingTask();
                            cancelFollowingTask
                                    .setListener(cancelFollowingTaskLinstener);
                            TaskParams params = new TaskParams();
                            params.put(USER_ID, id);
                            cancelFollowingTask.execute(params);
                        }
                    }
                });
        diaBuilder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        Dialog dialog = diaBuilder.create();
        dialog.show();
    }

    private GenericTask cancelFollowingTask;

    /**
     * 取消关注
     * 
     * @author Dino
     * 
     */
    private class CancelFollowingTask extends GenericTask {

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            try {
                // TODO:userid
                String userId = params[0].getString(USER_ID);
                getApi().destroyFriendship(userId);
            } catch (HttpException e) {
                Log.w(TAG, "create friend ship error");
                return TaskResult.FAILED;
            }
            return TaskResult.OK;
        }

    }

    private TaskListener cancelFollowingTaskLinstener = new TaskAdapter() {
        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            if (result == TaskResult.OK) {
                // followingBtn.setText("添加关注");
                // isFollowingText.setText(getResources().getString(
                // R.string.profile_notfollowing));
                // followingBtn.setOnClickListener(setfollowingListener);
                Toast.makeText(getBaseContext(), "取消关注成功", Toast.LENGTH_SHORT)
                        .show();

            } else if (result == TaskResult.FAILED) {
                Toast.makeText(getBaseContext(), "取消关注失败", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

    };

    private GenericTask setFollowingTask;

    /**
     * 设置关注
     * 
     * @param id
     */
    private void addFriend(String id) {
        Builder diaBuilder = new AlertDialog.Builder(UserListBaseActivity.this)
                .setTitle("关注提示").setMessage("确实要添加关注吗?");
        diaBuilder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (setFollowingTask != null
                                && setFollowingTask.getStatus() == GenericTask.Status.RUNNING) {
                            return;
                        } else {
                            setFollowingTask = new SetFollowingTask();
                            setFollowingTask
                                    .setListener(setFollowingTaskLinstener);
                            TaskParams params = new TaskParams();
                            setFollowingTask.execute(params);
                        }

                    }
                });
        diaBuilder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        Dialog dialog = diaBuilder.create();
        dialog.show();
    }

    /**
     * 设置关注
     * 
     * @author Dino
     * 
     */
    private class SetFollowingTask extends GenericTask {

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {

            try {
                String userId = params[0].getString(USER_ID);
                getApi().createFriendship(userId);

            } catch (HttpException e) {
                Log.w(TAG, "create friend ship error");
                return TaskResult.FAILED;
            }

            return TaskResult.OK;
        }

    }

    private TaskListener setFollowingTaskLinstener = new TaskAdapter() {

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            if (result == TaskResult.OK) {
                // followingBtn.setText("取消关注");
                // isFollowingText.setText(getResources().getString(
                // R.string.profile_isfollowing));
                // followingBtn.setOnClickListener(cancelFollowingListener);
                Toast.makeText(getBaseContext(), "关注成功", Toast.LENGTH_SHORT)
                        .show();

            } else if (result == TaskResult.FAILED) {
                Toast.makeText(getBaseContext(), "关注失败", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case OPTIONS_MENU_ID_TWEETS:
            launchActivity(TwitterActivity.createIntent(this));
            return true;
        case OPTIONS_MENU_ID_REPLIES:
            launchActivity(MentionActivity.createIntent(this));
            return true;
        case OPTIONS_MENU_ID_DM:
            launchActivity(DmActivity.createIntent());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void draw() {
        getUserAdapter().refresh();
    }

    private void goTop() {
        getUserList().setSelection(1);
    }

    protected void adapterRefresh() {
        getUserAdapter().refresh();
    }

    // for HasFavorite interface

    public void doFavorite(String action, String id) {
        if (!TextHelper.isEmpty(id)) {
            if (mFavTask != null
                    && mFavTask.getStatus() == GenericTask.Status.RUNNING) {
                return;
            } else {
                mFavTask = new TweetCommonTask.FavoriteTask(this);
                mFavTask.setFeedback(mFeedback);
                mFavTask.setListener(mFavTaskListener);

                TaskParams params = new TaskParams();
                params.put("action", action);
                params.put("id", id);
                mFavTask.execute(params);
            }
        }
    }

    public void onFavSuccess() {
        // updateProgress(getString(R.string.refreshing));
        adapterRefresh();
    }

    public void onFavFailure() {
        // updateProgress(getString(R.string.refreshing));
    }

    protected void specialItemClicked(int position) {

    }

    /*
     * TODO：单击列表项
     */
    protected void registerOnClickListener(ListView listView) {
        
        listView.setOnItemClickListener(new OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                 Toast.makeText(getBaseContext(),
                 "选择第"+position+"个列表",Toast.LENGTH_SHORT).show();
                User user = getContextItemUser(position);
                if (user == null) {
                    Log.w(TAG, "selected item not available");
                    specialItemClicked(position);
                } else {
                    launchActivity(ProfileActivity.createIntent(user.id));
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFavTask != null
                && mFavTask.getStatus() == GenericTask.Status.RUNNING) {
            outState.putBoolean(SIS_RUNNING_KEY, true);
        }
    }

}
