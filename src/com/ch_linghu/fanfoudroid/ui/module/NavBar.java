package com.ch_linghu.fanfoudroid.ui.module;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.SearchActivity;
import com.ch_linghu.fanfoudroid.TwitterActivity;
import com.ch_linghu.fanfoudroid.WriteActivity;
import com.ch_linghu.fanfoudroid.ui.base.Refreshable;

public class NavBar implements Widget {
    private static final String TAG = "NavBar";

    public static final int HEADER_STYLE_HOME = 1;
    public static final int HEADER_STYLE_WRITE = 2;
    public static final int HEADER_STYLE_BACK = 3;
    public static final int HEADER_STYLE_SEARCH = 4;

    private ImageView mRefreshButton;
    private ImageButton mSearchButton;
    private ImageButton mWriteButton;
    private TextView mTitleButton;
    private Button mBackButton;
    private ImageButton mHomeButton;
    private MenuDialog mDialog;
    private EditText mSearchEdit;

    /** @deprecated 已废弃 */
    protected AnimationDrawable mRefreshAnimation;

    private ProgressBar mProgressBar = null; // 进度条(横)
    private ProgressBar mLoadingProgress = null; // 旋转图标

    public NavBar(int style, Context context) {
        initHeader(style, (Activity) context);
    }

    private void initHeader(int style, final Activity activity) {
        switch (style) {
        case HEADER_STYLE_HOME:
            addTitleButtonTo(activity);
            addWriteButtonTo(activity);
            addSearchButtonTo(activity);
            addRefreshButtonTo(activity);
            break;
        case HEADER_STYLE_BACK:
            addBackButtonTo(activity);
            addWriteButtonTo(activity);
            addSearchButtonTo(activity);
            addRefreshButtonTo(activity);
            break;
        case HEADER_STYLE_WRITE:
            addBackButtonTo(activity);
            break;
        case HEADER_STYLE_SEARCH:
            addBackButtonTo(activity);
            addSearchBoxTo(activity);
            addSearchButtonTo(activity);
            break;
        }
    }

    /**
     * 搜索硬按键行为
     * @deprecated 这个不晓得还有没有用, 已经是已经被新的搜索替代的吧 ?
     */
    public boolean onSearchRequested() {
        /*
        Intent intent = new Intent();
        intent.setClass(this, SearchActivity.class);
        startActivity(intent);
        */
        return true;
    }

    /**
     * 添加[LOGO/标题]按钮
     * @param acticity
     */
    private void addTitleButtonTo(final Activity acticity) {
        mTitleButton = (TextView) acticity.findViewById(R.id.title);
        mTitleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int top = mTitleButton.getTop();
                int height = mTitleButton.getHeight();
                int x = top + height;

                if (null == mDialog) {
                    Log.d(TAG, "Create menu dialog.");
                    mDialog = new MenuDialog(acticity);
                    mDialog.bindEvent(acticity);
                    mDialog.setPosition(-1, x);
                }

                // toggle dialog
                if (mDialog.isShowing()) {
                    mDialog.dismiss(); // 没机会触发
                } else {
                    mDialog.show();
                }
            }
        });
    }

    /**
     * 设置标题
     * @param title
     */
    public void setHeaderTitle(String title) {
        if (null != mTitleButton) {
            mTitleButton.setText(title);
            TextPaint tp = mTitleButton.getPaint();
            tp.setFakeBoldText(true); // 中文粗体
        }
    }
    
    /**
     * 设置标题
     * @param resource R.string.xxx
     */
    public void setHeaderTitle(int resource) {
        if (null != mTitleButton) {
            mTitleButton.setBackgroundResource(resource);
        }
    }

    /**
     * 添加[刷新]按钮
     * @param activity
     */
    private void addRefreshButtonTo(final Activity activity) {
        mRefreshButton = (ImageView) activity.findViewById(R.id.top_refresh);

        // FIXME: DELETE ME 暂时取消旋转效果, 测试ProgressBar
        // refreshButton.setBackgroundResource(R.drawable.top_refresh);
        // mRefreshAnimation = (AnimationDrawable)
        // refreshButton.getBackground();

        mProgressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
        mLoadingProgress = (ProgressBar) activity
                .findViewById(R.id.top_refresh_progressBar);

        mRefreshButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (activity instanceof Refreshable) {
                    ((Refreshable) activity).doRetrieve();
                } else {
                    Log.e(TAG, "The current view "
                            + activity.getClass().getName()
                            + " cann't be retrieved");
                }
            }

        });
    }

    /**
     * Start/Stop Top Refresh Button's Animation
     * 
     * @param animate
     *            start or stop
     * @deprecated use feedback
     */
    public void setRefreshAnimation(boolean animate) {
        if (mRefreshAnimation != null) {
            if (animate) {
                mRefreshAnimation.start();
            } else {
                mRefreshAnimation.setVisible(true, true); // restart
                mRefreshAnimation.start(); // goTo frame 0
                mRefreshAnimation.stop();
            }
        } else {
            Log.w(TAG, "mRefreshAnimation is null");
        }
    }

    /**
     * 添加[搜索]按钮
     * @param activity
     */
    private void addSearchButtonTo(final Activity activity) {
        mSearchButton = (ImageButton) activity.findViewById(R.id.search);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startSearch(activity);
            }
        });
    }

    // 这个方法会在SearchActivity里重写
    protected boolean startSearch(final Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, SearchActivity.class);
        activity.startActivity(intent);
        return true;
    }

    /**
     * 添加[搜索框]
     * @param activity
     */
    private void addSearchBoxTo(final Activity activity) {
        mSearchEdit = (EditText) activity.findViewById(R.id.search_edit);
    }

    /**
     * 添加[撰写]按钮
     * @param activity
     */
    private void addWriteButtonTo(final Activity activity) {
        mWriteButton = (ImageButton) activity.findViewById(R.id.writeMessage);

        mWriteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // forward to write activity
                Intent intent = new Intent();
                intent.setClass(v.getContext(), WriteActivity.class);
                v.getContext().startActivity(intent);
            }
        });
    }

    /**
     * 添加[回首页]按钮
     * @param activity
     */
    @SuppressWarnings("unused")
    private void addHomeButton(final Activity activity) {
        mHomeButton = (ImageButton) activity.findViewById(R.id.home);

        mHomeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 动画
                Animation anim = AnimationUtils.loadAnimation(v.getContext(),
                        R.anim.scale_lite);
                v.startAnimation(anim);

                // forward to TwitterActivity
                Intent intent = new Intent();
                intent.setClass(v.getContext(), TwitterActivity.class);
                v.getContext().startActivity(intent);

            }
        });
    }

    /**
     * 添加[返回]按钮
     * @param activity
     */
    private void addBackButtonTo(final Activity activity) {
        mBackButton = (Button) activity.findViewById(R.id.top_back);
        // 中文粗体
        // TextPaint tp = backButton.getPaint();
        // tp.setFakeBoldText(true);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Go back to previous activity
                activity.finish();
            }
        });
    }

    public void destroy() {
        // dismiss dialog before destroy
        // to avoid android.view.WindowLeaked Exception
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mRefreshButton = null;
        mSearchButton = null;
        mWriteButton = null;
        mTitleButton = null;
        mBackButton = null;
        mHomeButton = null;
        mSearchButton = null;
        mSearchEdit = null;
        mProgressBar = null;
        mLoadingProgress = null;
    }

    public ImageView getRefreshButton() {
        return mRefreshButton;
    }

    public ImageButton getSearchButton() {
        return mSearchButton;
    }

    public ImageButton getWriteButton() {
        return mWriteButton;
    }

    public TextView getTitleButton() {
        return mTitleButton;
    }

    public Button getBackButton() {
        return mBackButton;
    }

    public ImageButton getHomeButton() {
        return mHomeButton;
    }

    public MenuDialog getDialog() {
        return mDialog;
    }

    public EditText getSearchEdit() {
        return mSearchEdit;
    }

    /** @deprecated 已废弃 */
    public AnimationDrawable getRefreshAnimation() {
        return mRefreshAnimation;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public ProgressBar getLoadingProgress() {
        return mLoadingProgress;
    }

    @Override
    public Context getContext() {
        if (null != mDialog) {
            return mDialog.getContext();
        }
        if (null != mTitleButton) {
            return mTitleButton.getContext();
        }
        return null;
    }

}
