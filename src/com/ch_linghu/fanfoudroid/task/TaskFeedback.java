package com.ch_linghu.fanfoudroid.task;

import com.ch_linghu.fanfoudroid.ui.base.WithHeaderActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

public abstract class TaskFeedback {
	private static TaskFeedback _instance = null;
	
	public static final int DIALOG_MODE = 0x01;
	public static final int REFRESH_MODE = 0x02;
	public static TaskFeedback getInstance(int type, Context context){
		switch(type){
		case DIALOG_MODE:
			_instance = DialogFeedback.getInstance();
			break;
		case REFRESH_MODE:
			_instance = RefreshAnimationFeedback.getInstance();
			break;
		}
		_instance.setContext(context);
		return _instance;
	}
	
	protected Context _context;
	protected void setContext(Context context){
		_context = context;
	}
	
	abstract public void start(String prompt);
	abstract public void cancel();
	abstract public void success(String prompt);	
	abstract public void failed(String prompt);
}

class DialogFeedback extends TaskFeedback{
	private static DialogFeedback _instance = null;
	public static DialogFeedback getInstance(){
		if (_instance == null){
			_instance = new DialogFeedback();
		}
		return _instance;
	}
	
	private ProgressDialog _dialog = null;
	
	@Override
	public void cancel() {
		if(_dialog != null){
			_dialog.dismiss();
		}		
	}

	@Override
	public void failed(String prompt) {
		if(_dialog != null){
			_dialog.dismiss();
		}
		
		Toast toast = Toast.makeText(_context, prompt, Toast.LENGTH_LONG);
		toast.show();
	}

	@Override
	public void start(String prompt) {
		_dialog = ProgressDialog.show(_context, "", prompt, true);		
		_dialog.setCancelable(true);
	}

	@Override
	public void success(String prompt) {
		if(_dialog != null){
			_dialog.dismiss();
		}
	}
}

class RefreshAnimationFeedback extends TaskFeedback{
	private static RefreshAnimationFeedback _instance = null;
	public static RefreshAnimationFeedback getInstance(){
		if (_instance == null){
			_instance = new RefreshAnimationFeedback();
		}
		return _instance;
	}

	
	private WithHeaderActivity _activity;
	
	@Override
	protected void setContext(Context context){
		super.setContext(context);
		_activity = (WithHeaderActivity)context;
	}
	
	@Override
	public void cancel() {
		_activity.setRefreshAnimation(false);
	}

	@Override
	public void failed(String prompt) {
		_activity.setRefreshAnimation(false);
		
		Toast toast = Toast.makeText(_context, prompt, Toast.LENGTH_LONG);
		toast.show();
	}

	@Override
	public void start(String prompt) {
		_activity.setRefreshAnimation(true);
	}

	@Override
	public void success(String prompt) {
		_activity.setRefreshAnimation(false);
	}
	
}
