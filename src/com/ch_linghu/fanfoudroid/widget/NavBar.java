package com.ch_linghu.fanfoudroid.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.SearchActivity;
import com.ch_linghu.fanfoudroid.TwitterActivity;
import com.ch_linghu.fanfoudroid.WriteActivity;
import com.ch_linghu.fanfoudroid.ui.base.Refreshable;
import com.ch_linghu.fanfoudroid.ui.module.MenuDialog;

public class NavBar implements Widget {
    private static final String TAG = "NavBar";

    public static final int HEADER_STYLE_HOME = 1;
    public static final int HEADER_STYLE_WRITE = 2;
    public static final int HEADER_STYLE_BACK = 3;
    public static final int HEADER_STYLE_SEARCH = 4;

    protected ImageView refreshButton;
    protected ImageButton searchButton;
    protected ImageButton writeButton;
    protected TextView titleButton;
    protected Button backButton;
    protected ImageButton homeButton;
    protected MenuDialog dialog;
    protected EditText searchEdit;

    // FIXME: 刷新动画二选一, DELETE ME
    protected AnimationDrawable mRefreshAnimation;
    protected ProgressBar mProgress = null;
    protected ProgressBar mLoadingProgress = null;

    public NavBar(int style, Context context) {
        initHeader(style, (Activity) context);
    }

    protected void initHeader(int style, final Activity activity) {
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

    /*
     * //搜索硬按键行为, 这个不晓得还有没有用, 已经是已经被新的搜索替代的吧 ?
     * 
     * @Override public boolean onSearchRequested() { Intent intent = new
     * Intent(); intent.setClass(this, SearchActivity.class);
     * startActivity(intent); return true; }
     */

    // LOGO按钮
    protected void addTitleButtonTo(final Activity acticity) {
        titleButton = (TextView) acticity.findViewById(R.id.title);
        titleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int top = titleButton.getTop();
                int height = titleButton.getHeight();
                int x = top + height;

                if (null == dialog) {
                    Log.d(TAG, "Create menu dialog.");
                    dialog = new MenuDialog(acticity);
                    dialog.bindEvent(acticity);
                    dialog.setPosition(-1, x);
                }

                // toggle dialog
                if (dialog.isShowing()) {
                    dialog.dismiss(); // 没机会触发
                } else {
                    dialog.show();
                }
            }
        });
    }

    public void setHeaderTitle(String title) {
        if (null != titleButton) {
            titleButton.setBackgroundDrawable(new BitmapDrawable());
            titleButton.setText(title);
            LayoutParams lp = new LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(3, 12, 0, 0);
            titleButton.setLayoutParams(lp);
            // 中文粗体
            TextPaint tp = titleButton.getPaint();
            tp.setFakeBoldText(true);
        }
    }

    protected void setHeaderTitle(int resource) {
        if (null != titleButton) {
            titleButton.setBackgroundResource(resource);
        }
    }

    // 刷新
    protected void addRefreshButtonTo(final Activity activity) {
        refreshButton = (ImageView) activity.findViewById(R.id.top_refresh);

        // FIXME: 暂时取消旋转效果, 测试ProgressBar
        // refreshButton.setBackgroundResource(R.drawable.top_refresh);
        // mRefreshAnimation = (AnimationDrawable)
        // refreshButton.getBackground();

        // FIXME: DELETE ME
        mProgress = (ProgressBar) activity.findViewById(R.id.progress_bar);
        mLoadingProgress = (ProgressBar) activity
                .findViewById(R.id.top_refresh_progressBar);

        refreshButton.setOnClickListener(new View.OnClickListener() {

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

    // 搜索
    protected void addSearchButtonTo(final Activity activity) {
        searchButton = (ImageButton) activity.findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
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

    // 搜索框
    protected void addSearchBoxTo(final Activity activity) {
        searchEdit = (EditText) activity.findViewById(R.id.search_edit);
    }

    // 撰写
    protected void addWriteButtonTo(final Activity activity) {
        writeButton = (ImageButton) activity.findViewById(R.id.writeMessage);

        writeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 动画
                Animation anim = AnimationUtils.loadAnimation(v.getContext(),
                        R.anim.scale_lite);
                v.startAnimation(anim);

                // forward to write activity
                Intent intent = new Intent();
                intent.setClass(v.getContext(), WriteActivity.class);
                v.getContext().startActivity(intent);
            }
        });
    }

    // 回首页
    protected void addHomeButton(final Activity activity) {
        homeButton = (ImageButton) activity.findViewById(R.id.home);

        homeButton.setOnClickListener(new View.OnClickListener() {
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

    // 返回
    protected void addBackButtonTo(final Activity activity) {
        backButton = (Button) activity.findViewById(R.id.top_back);
        // 中文粗体
        // TextPaint tp = backButton.getPaint();
        // tp.setFakeBoldText(true);

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Go back to previous activity
                activity.finish();
            }
        });
    }

    /**
     * @deprecated 已废弃
     * @param resource
     * @param activity
     */
    private void addHeaderView(int resource, final Activity activity) {
        // find content root view
        ViewGroup root = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup content = (ViewGroup) root.getChildAt(0);
        View header = View.inflate(activity, resource, null);
        // LayoutParams params = new
        // LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);

        content.addView(header, 0);
    }

    protected void onDestroy() {
        // dismiss dialog before destroy
        // to avoid android.view.WindowLeaked Exception
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public ImageView getRefreshButton() {
        return refreshButton;
    }

    public ImageButton getSearchButton() {
        return searchButton;
    }

    public ImageButton getWriteButton() {
        return writeButton;
    }

    public TextView getTitleButton() {
        return titleButton;
    }

    public Button getBackButton() {
        return backButton;
    }

    public ImageButton getHomeButton() {
        return homeButton;
    }

    public MenuDialog getDialog() {
        return dialog;
    }

    public EditText getSearchEdit() {
        return searchEdit;
    }

    public AnimationDrawable getmRefreshAnimation() {
        return mRefreshAnimation;
    }

    public ProgressBar getProgress() {
        return mProgress;
    }

    public ProgressBar getLoadingProgress() {
        return mLoadingProgress;
    }


    @Override
    public Context getContext() {
        if (null != dialog) {
            return dialog.getContext();
        }
        if (null != titleButton) {
            return titleButton.getContext();
        }
        return null;
    }

}
