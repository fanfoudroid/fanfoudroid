package com.ch_linghu.fanfoudroid;

import java.text.MessageFormat;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ch_linghu.fanfoudroid.app.LazyImageLoader.ImageLoaderCallback;
import com.ch_linghu.fanfoudroid.db.TwitterDatabase;
import com.ch_linghu.fanfoudroid.db.UserInfoTable;
import com.ch_linghu.fanfoudroid.fanfou.User;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.BaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.Feedback;
import com.ch_linghu.fanfoudroid.ui.module.FeedbackFactory;
import com.ch_linghu.fanfoudroid.ui.module.FeedbackFactory.FeedbackType;
import com.ch_linghu.fanfoudroid.ui.module.NavBar;

/**
 * 
 * @author Dino 2011-02-26
 */
// public class ProfileActivity extends WithHeaderActivity {
public class ProfileActivity extends BaseActivity {
    private static final String TAG = "ProfileActivity";
    private static final String LAUNCH_ACTION = "com.ch_linghu.fanfoudroid.PROFILE";
    private static final String STATUS_COUNT = "status_count";
    private static final String EXTRA_USER = "user";
    private static final String FANFOUROOT = "http://fanfou.com/";
    private static final String USER_ID = "userid";
    private static final String USER_NAME = "userName";

    private GenericTask profileInfoTask;// 获取用户信息
    private GenericTask setFollowingTask;
    private GenericTask cancelFollowingTask;

    private String userId;
    private String userName;
    private String myself;
    private User profileInfo;// 用户信息

    private ImageView profileImageView;// 头像
    private TextView profileName;// 名称
    private TextView profileScreenName;// 昵称
    private TextView userLocation;// 地址
    private TextView userUrl;// url
    private TextView userInfo;// 自述
    private TextView friendsCount;// 好友
    private TextView followersCount;// 收听
    private TextView statusCount;// 消息
    private TextView favouritesCount;// 收藏
    private TextView isFollowingText;// 是否关注
    private Button followingBtn;// 收听/取消关注按钮
    private Button sendMentionBtn;// 发送留言按钮
    private Button sendDmBtn;// 发送私信按钮
    private ProgressDialog dialog; // 请稍候

    private RelativeLayout friendsLayout;
    private LinearLayout followersLayout;
    private LinearLayout statusesLayout;
    private LinearLayout favouritesLayout;

    private NavBar mNavBar;
    private Feedback mFeedback;

    private TwitterDatabase db;

    public static Intent createIntent(String userId) {
        Intent intent = new Intent(LAUNCH_ACTION);
        intent.putExtra(USER_ID, userId);
        return intent;
    }

    private ImageLoaderCallback callback = new ImageLoaderCallback() {

        @Override
        public void refresh(String url, Bitmap bitmap) {
            profileImageView.setImageBitmap(bitmap);
        }

    };

    @Override
    protected boolean _onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate start");

        if (super._onCreate(savedInstanceState)) {
            setContentView(R.layout.profile);

            Intent intent = getIntent();
            Bundle extras = intent.getExtras();

            myself = TwitterApplication.getMyselfId();
            if (extras != null) {
                this.userId = extras.getString(USER_ID);
                this.userName = extras.getString(USER_NAME);
            } else {
                this.userId = myself;
                this.userName = TwitterApplication.getMyselfName();
            }
            Uri data = intent.getData();
            if (data != null) {
                userId = data.getLastPathSegment();
            }

            // 初始化控件
            initControls();

            Log.d(TAG, "the userid is " + userId);
            db = this.getDb();
            draw();

            return true;
        } else {
            return false;
        }
    }

    private void initControls() {
        mNavBar = new NavBar(NavBar.HEADER_STYLE_HOME, this);
        mNavBar.setHeaderTitle("");

        mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);

        sendMentionBtn = (Button) findViewById(R.id.sendmetion_btn);
        sendDmBtn = (Button) findViewById(R.id.senddm_btn);

        profileImageView = (ImageView) findViewById(R.id.profileimage);
        profileName = (TextView) findViewById(R.id.profilename);
        profileScreenName = (TextView) findViewById(R.id.profilescreenname);
        userLocation = (TextView) findViewById(R.id.user_location);
        userUrl = (TextView) findViewById(R.id.user_url);
        userInfo = (TextView) findViewById(R.id.tweet_user_info);
        friendsCount = (TextView) findViewById(R.id.friends_count);
        followersCount = (TextView) findViewById(R.id.followers_count);

        TextView friendsCountTitle = (TextView) findViewById(R.id.friends_count_title);
        TextView followersCountTitle = (TextView) findViewById(R.id.followers_count_title);
        String who;
        if (userId.equals(myself)) {
            who = "我";
        } else {
            who = "ta";
        }
        friendsCountTitle.setText(MessageFormat.format(
                getString(R.string.profile_friends_count_title), who));
        followersCountTitle.setText(MessageFormat.format(
                getString(R.string.profile_followers_count_title), who));

        statusCount = (TextView) findViewById(R.id.statuses_count);
        favouritesCount = (TextView) findViewById(R.id.favourites_count);

        friendsLayout = (RelativeLayout) findViewById(R.id.friendsLayout);
        followersLayout = (LinearLayout) findViewById(R.id.followersLayout);
        statusesLayout = (LinearLayout) findViewById(R.id.statusesLayout);
        favouritesLayout = (LinearLayout) findViewById(R.id.favouritesLayout);

        isFollowingText = (TextView) findViewById(R.id.isfollowing_text);
        followingBtn = (Button) findViewById(R.id.following_btn);

        // 为按钮面板添加事件
        friendsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在没有得到profileInfo时，不允许点击事件生效
                if (profileInfo == null) {
                    return;
                }
                String showName;
                if (!TextUtils.isEmpty(profileInfo.getScreenName())) {
                    showName = profileInfo.getScreenName();
                } else {
                    showName = profileInfo.getName();
                }
                Intent intent = FollowingActivity
                        .createIntent(userId, showName);
                intent.setClass(ProfileActivity.this, FollowingActivity.class);
                startActivity(intent);

            }
        });

        followersLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 在没有得到profileInfo时，不允许点击事件生效
                if (profileInfo == null) {
                    return;
                }
                String showName;
                if (!TextUtils.isEmpty(profileInfo.getScreenName())) {
                    showName = profileInfo.getScreenName();
                } else {
                    showName = profileInfo.getName();
                }
                Intent intent = FollowersActivity
                        .createIntent(userId, showName);
                intent.setClass(ProfileActivity.this, FollowersActivity.class);
                startActivity(intent);

            }
        });

        statusesLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 在没有得到profileInfo时，不允许点击事件生效
                if (profileInfo == null) {
                    return;
                }
                String showName;
                if (!TextUtils.isEmpty(profileInfo.getScreenName())) {
                    showName = profileInfo.getScreenName();
                } else {
                    showName = profileInfo.getName();
                }
                Intent intent = UserTimelineActivity.createIntent(
                        profileInfo.getId(), showName);
                launchActivity(intent);
            }
        });

        favouritesLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 在没有得到profileInfo时，不允许点击事件生效
                if (profileInfo == null) {
                    return;
                }
                Intent intent = FavoritesActivity.createIntent(userId,
                        profileInfo.getName());
                intent.setClass(ProfileActivity.this, FavoritesActivity.class);
                startActivity(intent);
            }
        });

        // 刷新
        View.OnClickListener refreshListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doGetProfileInfo();
            }
        };

        mNavBar.getRefreshButton().setOnClickListener(refreshListener);
    }

    private void draw() {
        Log.d(TAG, "draw");

        bindProfileInfo();
        // doGetProfileInfo();
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.d(TAG, "onResume.");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart.");

    }

    @Override
    protected void onStop() {

        super.onStop();
        Log.d(TAG, "onStop.");
    }

    /**
     * 从数据库获取,如果数据库不存在则创建
     */
    private void bindProfileInfo() {
        dialog = ProgressDialog.show(ProfileActivity.this, "请稍候", "正在加载信息...");

        if (null != db && db.existsUser(userId)) {

            Cursor cursor = db.getUserInfoById(userId);
            profileInfo = User.parseUser(cursor);
            cursor.close();
            if (profileInfo == null) {
                Log.w(TAG, "cannot get userinfo from userinfotable the id is"
                        + userId);
            }
            bindControl();
            if (dialog != null) {
                dialog.dismiss();
            }

        } else {
            doGetProfileInfo();
        }

    }

    private void doGetProfileInfo() {
        mFeedback.start("");

        if (profileInfoTask != null
                && profileInfoTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            profileInfoTask = new GetProfileTask();
            profileInfoTask.setListener(profileInfoTaskListener);
            TaskParams params = new TaskParams();
            profileInfoTask.execute(params);
        }
    }

    private void bindControl() {
        if (profileInfo.getId().equals(myself)) {
            sendMentionBtn.setVisibility(View.GONE);
            sendDmBtn.setVisibility(View.GONE);
        } else {
            // 发送留言
            sendMentionBtn.setVisibility(View.VISIBLE);
            sendMentionBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = WriteActivity.createNewTweetIntent(String
                            .format("@%s ", profileInfo.getScreenName()));
                    startActivity(intent);
                }
            });

            // 发送私信
            sendDmBtn.setVisibility(View.VISIBLE);
            sendDmBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = WriteDmActivity.createIntent(profileInfo
                            .getId());
                    startActivity(intent);
                }
            });
        }

        if (userId.equals(myself)) {
            mNavBar.setHeaderTitle("我"
                    + getString(R.string.cmenu_user_profile_prefix));
        } else {
            mNavBar.setHeaderTitle(profileInfo.getScreenName()
                    + getString(R.string.cmenu_user_profile_prefix));
        }
        profileImageView
                .setImageBitmap(TwitterApplication.mImageLoader
                        .get(profileInfo.getProfileImageURL().toString(),
                                callback));

        profileName.setText(profileInfo.getId());

        profileScreenName.setText(profileInfo.getScreenName());

        if (profileInfo.getId().equals(myself)) {
            isFollowingText.setText(R.string.profile_isyou);
            followingBtn.setVisibility(View.GONE);
        } else if (profileInfo.isFollowing()) {
            isFollowingText.setText(R.string.profile_isfollowing);
            followingBtn.setVisibility(View.VISIBLE);
            followingBtn.setText(R.string.user_label_unfollow);
            followingBtn.setOnClickListener(cancelFollowingListener);
            followingBtn.setCompoundDrawablesWithIntrinsicBounds(getResources()
                    .getDrawable(R.drawable.ic_unfollow), null, null, null);
        } else {
            isFollowingText.setText(R.string.profile_notfollowing);
            followingBtn.setVisibility(View.VISIBLE);
            followingBtn.setText(R.string.user_label_follow);
            followingBtn.setOnClickListener(setfollowingListener);
            followingBtn.setCompoundDrawablesWithIntrinsicBounds(getResources()
                    .getDrawable(R.drawable.ic_follow), null, null, null);
        }

        String location = profileInfo.getLocation();
        if (location == null || location.length() == 0) {
            location = getResources().getString(R.string.profile_location_null);
        }
        userLocation.setText(location);

        if (profileInfo.getURL() != null) {
            userUrl.setText(profileInfo.getURL().toString());
        } else {
            userUrl.setText(FANFOUROOT + profileInfo.getId());
        }

        String description = profileInfo.getDescription();
        if (description == null || description.length() == 0) {
            description = getResources().getString(
                    R.string.profile_description_null);
        }
        userInfo.setText(description);
        friendsCount.setText(String.valueOf(profileInfo.getFriendsCount()));
        followersCount.setText(String.valueOf(profileInfo.getFollowersCount()));
        statusCount.setText(String.valueOf(profileInfo.getStatusesCount()));
        favouritesCount
                .setText(String.valueOf(profileInfo.getFavouritesCount()));

    }

    private TaskListener profileInfoTaskListener = new TaskAdapter() {
        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {

            // 加载成功
            if (result == TaskResult.OK) {
                mFeedback.success("");

                // 绑定控件
                bindControl();
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        }

        @Override
        public String getName() {
            return "GetProfileInfo";
        }
    };

    /**
     * 更新数据库中的用户
     * 
     * @return
     */
    private boolean updateUser() {
        ContentValues v = new ContentValues();
        v.put(BaseColumns._ID, profileInfo.getName());
        v.put(UserInfoTable.FIELD_USER_NAME, profileInfo.getName());
        v.put(UserInfoTable.FIELD_USER_SCREEN_NAME, profileInfo.getScreenName());
        v.put(UserInfoTable.FIELD_PROFILE_IMAGE_URL, profileInfo
                .getProfileImageURL().toString());
        v.put(UserInfoTable.FIELD_LOCALTION, profileInfo.getLocation());
        v.put(UserInfoTable.FIELD_DESCRIPTION, profileInfo.getDescription());
        v.put(UserInfoTable.FIELD_PROTECTED, profileInfo.isProtected());
        v.put(UserInfoTable.FIELD_FOLLOWERS_COUNT,
                profileInfo.getFollowersCount());
        v.put(UserInfoTable.FIELD_LAST_STATUS, profileInfo.getStatusSource());
        v.put(UserInfoTable.FIELD_FRIENDS_COUNT, profileInfo.getFriendsCount());
        v.put(UserInfoTable.FIELD_FAVORITES_COUNT,
                profileInfo.getFavouritesCount());
        v.put(UserInfoTable.FIELD_STATUSES_COUNT,
                profileInfo.getStatusesCount());
        v.put(UserInfoTable.FIELD_FOLLOWING, profileInfo.isFollowing());
        if (profileInfo.getURL() != null) {
            v.put(UserInfoTable.FIELD_URL, profileInfo.getURL().toString());
        }

        return db.updateUser(profileInfo.getId(), v);
    }

    /**
     * 获取用户信息task
     * 
     * @author Dino
     * 
     */
    private class GetProfileTask extends GenericTask {

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            Log.v(TAG, "get profile task");

            try {
                profileInfo = getApi().showUser(userId);
                mFeedback.update(80);

                if (profileInfo != null) {
                    if (null != db && !db.existsUser(userId)) {

                        com.ch_linghu.fanfoudroid.data.User userinfodb = profileInfo
                                .parseUser();
                        db.createUserInfo(userinfodb);

                    } else {
                        // 更新用户
                        updateUser();
                    }
                }
            } catch (HttpException e) {

                Log.e(TAG, e.getMessage());
                return TaskResult.FAILED;
            }
            mFeedback.update(99);
            return TaskResult.OK;
        }
    }

    /**
     * 设置关注监听
     */
    private OnClickListener setfollowingListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Builder diaBuilder = new AlertDialog.Builder(ProfileActivity.this)
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

    };

    /*
     * 取消关注监听
     */
    private OnClickListener cancelFollowingListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Builder diaBuilder = new AlertDialog.Builder(ProfileActivity.this)
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
    };

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
                followingBtn.setText("取消关注");
                isFollowingText.setText(getResources().getString(
                        R.string.profile_isfollowing));
                followingBtn.setOnClickListener(cancelFollowingListener);
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
                followingBtn.setText("添加关注");
                isFollowingText.setText(getResources().getString(
                        R.string.profile_notfollowing));
                followingBtn.setOnClickListener(setfollowingListener);
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

}
