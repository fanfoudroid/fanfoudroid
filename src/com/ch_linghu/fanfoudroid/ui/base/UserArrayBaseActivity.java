package com.ch_linghu.fanfoudroid.ui.base;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ch_linghu.fanfoudroid.R;
import com.ch_linghu.fanfoudroid.data.Tweet;
import com.ch_linghu.fanfoudroid.data.User;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskManager;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.module.TweetAdapter;
import com.ch_linghu.fanfoudroid.ui.module.UserArrayAdapter;
import com.ch_linghu.fanfoudroid.weibo.Paging;

public abstract class UserArrayBaseActivity extends UserListBaseActivity {
	static final String TAG = "UserArrayBaseActivity";
	// Views.
	protected ListView mUserList;
	protected UserArrayAdapter mUserListAdapter;

	protected TextView loadMoreBtn;
	protected ProgressBar loadMoreGIF;
	protected TextView loadMoreBtnTop;
	protected ProgressBar loadMoreGIFTop;

	protected static int lastPosition = 0;

	// Tasks.
	protected TaskManager taskManager = new TaskManager();
	private GenericTask mRetrieveTask;
	private GenericTask mFollowersRetrieveTask;
	private GenericTask mGetMoreTask;// 每次100用户

	public abstract Paging getCurrentPage();// 加载
	public abstract Paging getNextPage();// 加载
	//protected abstract String[] getIds();
	protected abstract List<com.ch_linghu.fanfoudroid.weibo.User> getUsers(String userId,Paging page) throws HttpException; 

	

	private ArrayList<com.ch_linghu.fanfoudroid.data.User> allUserList;
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate.");
		if (super._onCreate(savedInstanceState)) {
		
			doRetrieve();//加载第一页

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void doRetrieve() {
		Log.i(TAG, "Attempting retrieve.");

		// 旋转刷新按钮
		animRotate(refreshButton);

		if (mRetrieveTask != null && mRetrieveTask.getStatus() == GenericTask.Status.RUNNING){
			return;
		}else{
			mRetrieveTask = new RetrieveTask();
			mRetrieveTask.setListener(mRetrieveTaskListener);
			mRetrieveTask.execute();
			
			// Add Task to manager
			taskManager.addTask(mRetrieveTask);
		}

	}
	
	private TaskListener mRetrieveTaskListener = new TaskAdapter(){

		@Override
		public String getName() {
			return "RetrieveTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				draw();
			
			} 

			// 刷新按钮停止旋转
			getRefreshButton().clearAnimation();

			updateProgress("");
		}
		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveBegin();
		}

		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
			Log.d(TAG, "onProgressUpdate");
			draw();
		}
	};

	public void updateProgress(String progress) {
		mProgressText.setText(progress);
	}
	
	public void onRetrieveBegin() {
		updateProgress(getString(R.string.page_status_refreshing));
	}
	
	/**
	 * TODO：从API获取当前Followers
	 * @author Dino
	 *
	 */
	private class RetrieveTask extends GenericTask{

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			
			Log.i(TAG, "load RetrieveTask");
		
			List<com.ch_linghu.fanfoudroid.weibo.User> usersList=null;
			try {
				usersList=getUsers(getUserId(),getCurrentPage());
				
			} catch (HttpException e) {
				e.printStackTrace();
				return TaskResult.IO_ERROR;
			}
			
			for (com.ch_linghu.fanfoudroid.weibo.User user : usersList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				
				allUserList.add(User.create(user));

				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}
			
			return TaskResult.OK;
		}
		
	}
	

	@Override
	protected int getLayoutId() {
		return R.layout.follower;
	}

	@Override
	protected void setupState() {
		setTitle(getActivityTitle());
		
		mUserList = (ListView) findViewById(R.id.follower_list);
		
	    setupListHeader(true);
	    
	    mUserListAdapter = new UserArrayAdapter(this);
		mUserList.setAdapter(mUserListAdapter);
		allUserList=new ArrayList<com.ch_linghu.fanfoudroid.data.User>();
		
	}

	@Override
	protected String getActivityTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean useBasicMenu() {
		return true;
	}

	@Override
	protected User getContextItemUser(int position) {
		// position = position - 1;
		// 加入footer跳过footer
		if (position < mUserListAdapter.getCount()) {
			User item = (User)mUserListAdapter.getItem(position);
			if (item == null) {
				return null;
			} else {
				return item;
			}
		} else {
			return null;
		}
	}

	/**
	 * TODO:不知道啥用
	 */
	@Override
	protected void updateTweet(Tweet tweet) {
		// TODO Auto-generated method stub

	}

	@Override
	protected ListView getUserList() {
		return mUserList;
	}

	@Override
	protected TweetAdapter getUserAdapter() {
		return mUserListAdapter;
	}

	/**
	 * 绑定listView底部 - 载入更多 NOTE: 必须在listView#setAdapter之前调用
	 */
	protected void setupListHeader(boolean addFooter) {

		// TODO: 完成listView底部的事件绑定
		View footer = View.inflate(this, R.layout.listview_footer, null);
		// TextView footerText=(TextView)
		// footer.findViewById(R.id.ask_for_more);

		mUserList.addFooterView(footer, null, true);
		footer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadMoreGIF.setVisibility(View.VISIBLE);
				doGetMore();
			}
		});

		// Find View
		loadMoreBtn = (TextView) findViewById(R.id.ask_for_more);
		loadMoreGIF = (ProgressBar) findViewById(R.id.rectangleProgressBar);
	}

	public void doGetMore() {
		Log.i(TAG, "Attempting getMore.");

		// 旋转刷新按钮
		animRotate(refreshButton);

		if (mGetMoreTask != null
				&& mGetMoreTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mGetMoreTask = new GetMoreTask();
			mGetMoreTask.setListener(getMoreListener);
			mGetMoreTask.execute();

			// Add Task to manager
			taskManager.addTask(mGetMoreTask);
		}
	}

	private TaskListener getMoreListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "getMore";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			super.onPostExecute(task, result);
			draw();
			getRefreshButton().clearAnimation();
			loadMoreGIF.setVisibility(View.GONE);
		}

	};

	/**
	 * TODO:需要重写,获取下一批用户,按页分100页一次
	 * 
	 * @author Dino
	 * 
	 */
	private class GetMoreTask extends GenericTask {
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {

			Log.i(TAG, "load RetrieveTask");

			List<com.ch_linghu.fanfoudroid.weibo.User> usersList = null;
			try {
				usersList = getUsers(getUserId(),
						getNextPage());

			} catch (HttpException e) {
				e.printStackTrace();
				return TaskResult.IO_ERROR;
			}
		
			for (com.ch_linghu.fanfoudroid.weibo.User user : usersList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				
				allUserList.add(User.create(user));
				
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
			}

			return TaskResult.OK;
		}
	}

	public void draw() {
		mUserListAdapter.refresh(allUserList);
		
	}

	public ImageButton getRefreshButton() {
		return refreshButton;
	}
	
}
