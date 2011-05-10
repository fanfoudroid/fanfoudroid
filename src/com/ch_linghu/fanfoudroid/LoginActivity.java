/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ch_linghu.fanfoudroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ch_linghu.fanfoudroid.helper.Preferences;
import com.ch_linghu.fanfoudroid.helper.Utils;
import com.ch_linghu.fanfoudroid.http.HttpAuthException;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.http.HttpRefusedException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.weibo.User;

//登录页面需要个性化的菜单绑定, 不直接继承 BaseActivity
public class LoginActivity extends Activity { 
    private static final String TAG = "LoginActivity";
    private static final String SIS_RUNNING_KEY = "running";

    private String mUsername;
    private String mPassword;

    // Views.
    private EditText mUsernameEdit;
    private EditText mPasswordEdit;
    private TextView mProgressText;
    private Button mSigninButton;
    private ProgressDialog dialog;

    // Preferences.
    private SharedPreferences mPreferences;

    // Tasks.
    private GenericTask mLoginTask;
    
    private User user;
    
    private TaskListener mLoginTaskListener = new TaskAdapter(){

        @Override
        public void onPreExecute(GenericTask task) {
            onLoginBegin();
        }

        @Override
        public void onProgressUpdate(GenericTask task, Object param) {
            updateProgress((String)param);
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            if (result == TaskResult.OK) {
                onLoginSuccess();
            } else {
                onLoginFailure(((LoginTask)task).getMsg());
            }
        }

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return "Login";
		}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // No Title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_PROGRESS);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.login);

        // TextView中嵌入HTML链接
        TextView registerLink = (TextView) findViewById(R.id.register_link);
        registerLink.setMovementMethod(LinkMovementMethod.getInstance());

        mUsernameEdit = (EditText) findViewById(R.id.username_edit);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        // mUsernameEdit.setOnKeyListener(enterKeyHandler);
        mPasswordEdit.setOnKeyListener(enterKeyHandler);

        mProgressText = (TextView) findViewById(R.id.progress_text);
        mProgressText.setFreezesText(true);
        mSigninButton = (Button) findViewById(R.id.signin_button);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SIS_RUNNING_KEY)) {
                if (savedInstanceState.getBoolean(SIS_RUNNING_KEY)) {
                    Log.d(TAG, "Was previously logging in. Restart action.");
                    doLogin();
                }
            }
        }

        mSigninButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doLogin();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestory");
        if (mLoginTask != null && mLoginTask.getStatus() == GenericTask.Status.RUNNING) {
            mLoginTask.cancel(true);
        }

        // dismiss dialog before destroy
        // to avoid android.view.WindowLeaked Exception
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mLoginTask != null
                && mLoginTask.getStatus() == GenericTask.Status.RUNNING) {
            // If the task was running, want to start it anew when the
            // Activity restarts.
            // This addresses the case where you user changes orientation
            // in the middle of execution.
            outState.putBoolean(SIS_RUNNING_KEY, true);
        }
    }

    // UI helpers.

    private void updateProgress(String progress) {
        mProgressText.setText(progress);
    }

    private void enableLogin() {
        mUsernameEdit.setEnabled(true);
        mPasswordEdit.setEnabled(true);
        mSigninButton.setEnabled(true);
    }

    private void disableLogin() {
        mUsernameEdit.setEnabled(false);
        mPasswordEdit.setEnabled(false);
        mSigninButton.setEnabled(false);
    }

    // Login task.

    private void doLogin() {
        mUsername = mUsernameEdit.getText().toString();
        mPassword = mPasswordEdit.getText().toString();
        
        if (mLoginTask != null && mLoginTask.getStatus() == GenericTask.Status.RUNNING){
        	return;
        }else{
	        if (!Utils.isEmpty(mUsername) & !Utils.isEmpty(mPassword) ) {
	            mLoginTask = new LoginTask();
	            mLoginTask.setListener(mLoginTaskListener);
	            
	            TaskParams params = new TaskParams();
	            params.put("username", mUsername);
	            params.put("password", mPassword);
	            mLoginTask.execute(params);
	        } else {
	            updateProgress(getString(R.string.login_status_null_username_or_password));
	        }
        }
    }

    private void onLoginBegin() {
        disableLogin();
        dialog = ProgressDialog.show(LoginActivity.this, "",
                getString(R.string.login_status_logging_in), true);
        dialog.setCancelable(true);
    }

    private void onLoginSuccess() {
        dialog.dismiss();
        updateProgress("");
        mUsernameEdit.setText("");
        mPasswordEdit.setText("");

        Log.d(TAG, "Storing credentials.");
        TwitterApplication.mApi.setCredentials(mUsername, mPassword);

        Intent intent = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
        String action = intent.getAction();

        if (intent.getAction() == null || !Intent.ACTION_SEND.equals(action)) {
            // We only want to reuse the intent if it was photo send.
            // Or else default to the main activity.
            intent = new Intent(this, TwitterActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private void onLoginFailure(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
        if (dialog != null){
        	dialog.dismiss();
        }
        enableLogin();
    }

    private class LoginTask extends GenericTask {
        
        private String msg = getString(R.string.login_status_failure);
        
        public String getMsg(){
        	return msg;
        }
        
        @Override
        protected TaskResult _doInBackground(TaskParams...params) {
        	TaskParams param = params[0];
            publishProgress(getString(R.string.login_status_logging_in) + "...");

            try {
            	String username = param.getString("username");
            	String password = param.getString("password");
               user= TwitterApplication.mApi.login(username, password);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);

                Throwable cause = e.getCause(); // Maybe null
                if (cause instanceof HttpAuthException) {
                    // Invalid userName/password
                    msg = ((HttpRefusedException) cause).getError().getMessage();
                } else {
                    msg = getString(R.string.login_status_network_or_connection_error);
                }
                publishProgress(msg);
                return TaskResult.FAILED;
            }

            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(Preferences.USERNAME_KEY, mUsername);
            editor.putString(Preferences.PASSWORD_KEY, mPassword);
            //add 存储当前用户的id
            editor.putString(Preferences.CURRENT_USER_ID, user.getId());
            editor.commit();
            
            return TaskResult.OK;
        }
    }

    private View.OnKeyListener enterKeyHandler = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER
                    || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    doLogin();
                }
                return true;
            }
            return false;
        }
    };

}