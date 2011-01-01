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

package com.ch_linghu.android.fanfoudroid;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.photostream.UserTask;

public class LoginActivity extends Activity {
  private static final String TAG = "LoginActivity";

  // Views.
  private EditText mUsernameEdit;
  private EditText mPasswordEdit;

  // Displays progress of tasks.
  private TextView mProgressText;

  private Button mSigninButton;

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

  // Sources.
  private TwitterApi mApi;

  // Preferences.
  private SharedPreferences mPreferences;

  // Tasks.
  private UserTask<Void, String, Boolean> mLoginTask;

  private static final String SIS_RUNNING_KEY = "running";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    mApi = new TwitterApi();

    setContentView(R.layout.login);

    mUsernameEdit = (EditText) findViewById(R.id.username_edit);
    mPasswordEdit = (EditText) findViewById(R.id.password_edit);
//    mUsernameEdit.setOnKeyListener(enterKeyHandler);
    mPasswordEdit.setOnKeyListener(enterKeyHandler);

    mProgressText = (TextView) findViewById(R.id.progress_text);
    mProgressText.setFreezesText(true);

    mSigninButton = (Button) findViewById(R.id.signin_button);

    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(SIS_RUNNING_KEY)) {
        if (savedInstanceState.getBoolean(SIS_RUNNING_KEY)) {
          Log.i(TAG, "Was previously logging in. Restart action.");
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
    if (mLoginTask != null && mLoginTask.getStatus() == UserTask.Status.RUNNING) {
      mLoginTask.cancel(true);
    }

    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (mLoginTask != null && mLoginTask.getStatus() == UserTask.Status.RUNNING) {
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
    mLoginTask = new LoginTask().execute();
  }

  private void onLoginBegin() {
    disableLogin();
  }

  private void onLoginSuccess() {
    updateProgress("");

    String username = mUsernameEdit.getText().toString();
    String password = mPasswordEdit.getText().toString();
    mUsernameEdit.setText("");
    mPasswordEdit.setText("");

    Log.i(TAG, "Storing credentials.");
    SharedPreferences.Editor editor = mPreferences.edit();
    editor.putString(Preferences.USERNAME_KEY, username);
    editor.putString(Preferences.PASSWORD_KEY, password);
    editor.commit();

    TwitterApplication.mApi.getHttp().setCredentials(username, password);

    Intent intent = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
    String action = intent.getAction();

    if (intent.getAction() == null
        || !Intent.ACTION_SEND.equals(action)) {
      // We only want to reuse the intent if it was photo send.
      // Or else default to the main activity.
      intent = new Intent(this, TwitterActivity.class);
    }

    startActivity(intent);
    finish();
  }

  private void onLoginFailure() {
    enableLogin();
  }

  private class LoginTask extends UserTask<Void, String, Boolean> {
    @Override
    public void onPreExecute() {
      onLoginBegin();
    }

    @Override
    public Boolean doInBackground(Void... params) {
      String username = mUsernameEdit.getText().toString();
      String password = mPasswordEdit.getText().toString();

      publishProgress("Logging in...");

      if (!TwitterApi.isValidCredentials(username, password)) {
        publishProgress("Invalid username or password");
        return false;
      }

      try {
        mApi.login(username, password);
      } catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
        publishProgress("Network or connection error");
        return false;
      } catch (FanfouException e) {
        Log.e(TAG, e.getMessage(), e);
        publishProgress("Network or connection error");
        return false;
      }

      return true;
    }

    @Override
    public void onProgressUpdate(String... progress) {
      updateProgress(progress[0]);
    }

    @Override
    public void onPostExecute(Boolean result) {
      if (isCancelled()) {
        return;
      }

      if (result) {
        onLoginSuccess();
      } else {
        onLoginFailure();
      }
    }
  }

}