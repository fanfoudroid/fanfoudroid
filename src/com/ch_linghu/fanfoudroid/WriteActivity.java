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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ch_linghu.fanfoudroid.app.ImageManager;
import com.ch_linghu.fanfoudroid.app.Preferences;
import com.ch_linghu.fanfoudroid.http.HttpClient;
import com.ch_linghu.fanfoudroid.http.HttpException;
import com.ch_linghu.fanfoudroid.task.GenericTask;
import com.ch_linghu.fanfoudroid.task.TaskAdapter;
import com.ch_linghu.fanfoudroid.task.TaskListener;
import com.ch_linghu.fanfoudroid.task.TaskParams;
import com.ch_linghu.fanfoudroid.task.TaskResult;
import com.ch_linghu.fanfoudroid.ui.base.BaseActivity;
import com.ch_linghu.fanfoudroid.ui.module.NavBar;
import com.ch_linghu.fanfoudroid.ui.module.TweetEdit;
import com.ch_linghu.fanfoudroid.util.FileHelper;
import com.ch_linghu.fanfoudroid.util.TextHelper;

public class WriteActivity extends BaseActivity {

    // FIXME: for debug, delete me
    private long startTime = -1;
    private long endTime = -1;

    public static final String NEW_TWEET_ACTION = "com.ch_linghu.fanfoudroid.NEW";
    public static final String REPLY_TWEET_ACTION = "com.ch_linghu.fanfoudroid.REPLY";
    public static final String REPOST_TWEET_ACTION = "com.ch_linghu.fanfoudroid.REPOST";
    public static final String EXTRA_REPLY_TO_NAME = "reply_to_name";
    public static final String EXTRA_REPLY_ID = "reply_id";
    public static final String EXTRA_REPOST_ID = "repost_status_id";

    private static final String TAG = "WriteActivity";
    private static final String SIS_RUNNING_KEY = "running";
    private static final String PREFS_NAME = "com.ch_linghu.fanfoudroid";

    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_PHOTO_LIBRARY = 3;

    // View
    private TweetEdit mTweetEdit;
    private EditText mTweetEditText;
    private TextView mProgressText;
    private ImageButton mLocationButton;
    private ImageButton chooseImagesButton;
    private ImageButton mCameraButton;
    private ProgressDialog dialog;
    
    private NavBar mNavbar;

    // Picture
    private boolean withPic=false ;
    private File mFile;
    private ImageView mPreview;
    private ImageView imageDelete;
    private static final int MAX_BITMAP_SIZE = 400;

    private File mImageFile;
    private Uri mImageUri;

    // Task
    private GenericTask mSendTask;

    private TaskListener mSendTaskListener = new TaskAdapter() {
        @Override
        public void onPreExecute(GenericTask task) {
            onSendBegin();
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            endTime = System.currentTimeMillis();
            Log.d("LDS", "Sended a status in " + (endTime - startTime));

            if (result == TaskResult.AUTH_ERROR) {
                logout();
            } else if (result == TaskResult.OK) {
                onSendSuccess();
            } else if (result == TaskResult.IO_ERROR) {
                onSendFailure();
            }
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return "SendTask";
        }
    };

    private String _reply_id;
    private String _repost_id;
    private String _reply_to_name;

    // sub menu
    protected void openImageCaptureMenu() {
        try {
            // TODO: API < 1.6, images size too small
            mImageFile = new File(FileHelper.getBasePath(), "upload.jpg");
            mImageUri = Uri.fromFile(mImageFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    protected void openPhotoLibraryMenu() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PHOTO_LIBRARY);
    }

    /**
     * @deprecated 已废弃, 分解成两个按钮
     */
    protected void createInsertPhotoDialog() {
        final CharSequence[] items = {
                getString(R.string.write_label_take_a_picture),
                getString(R.string.write_label_choose_a_picture) };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.write_label_insert_picture));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                case 0:
                    openImageCaptureMenu();
                    break;
                case 1:
                    openPhotoLibraryMenu();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaColumns.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void getPic(Intent intent, Uri uri) {

        // layout for picture mode
        changeStyleWithPic();

        withPic = true;
        mFile = null;

        mImageUri = uri;
        if (uri.getScheme().equals("content")) {
            mFile = new File(getRealPathFromURI(mImageUri));
        } else {
            mFile = new File(mImageUri.getPath());
        }

        // TODO:想将图片放在EditText左边
        mPreview.setImageBitmap(createThumbnailBitmap(mImageUri,
                MAX_BITMAP_SIZE));

        if (mFile == null) {
            updateProgress("Could not locate picture file. Sorry!");
            disableEntry();
        }

    }

    private File bitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(FileHelper.getBasePath(), "upload.jpg");
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG,
                    ImageManager.DEFAULT_COMPRESS_QUALITY, out)) {
                out.flush();
                out.close();
            }
            return file;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Sorry, the file can not be created. " + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e(TAG,
                    "IOException occurred when save upload file. "
                            + e.getMessage());
            return null;
        }
    }

    private void changeStyleWithPic() {
        // 修改布局 ，以前 图片居中，现在在左边
        // mPreview.setLayoutParams(
        // new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
        // LayoutParams.FILL_PARENT)
        // );
        mPreview.setVisibility(View.VISIBLE);
        imageDelete.setVisibility(View.VISIBLE);
        mTweetEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 2f));
    }

    /**
     * 制作微缩图
     * 
     * @param uri
     * @param size
     * @return
     */
    private Bitmap createThumbnailBitmap(Uri uri, int size) {
        InputStream input = null;

        try {
            input = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            input.close();

            // Compute the scale.
            int scale = 1;
            while ((options.outWidth / scale > size)
                    || (options.outHeight / scale > size)) {
                scale *= 2;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;

            input = getContentResolver().openInputStream(uri);

            return BitmapFactory.decodeStream(input, null, options);
        } catch (IOException e) {
            Log.w(TAG, e);

            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }

    @Override
    protected boolean _onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate.");
        if (super._onCreate(savedInstanceState)) {
            
            // init View
            setContentView(R.layout.write);
            mNavbar = new NavBar(NavBar.HEADER_STYLE_WRITE, this);

            // Intent & Action & Extras
            Intent intent = getIntent();
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            String text = null;
            Uri uri = null;
            if (extras != null) {
                text = extras.getString(Intent.EXTRA_TEXT);
                uri = (Uri) (extras.get(Intent.EXTRA_STREAM));
            }

            _reply_id = null;
            _repost_id = null;
            _reply_to_name = null;

            // View
            mProgressText = (TextView) findViewById(R.id.progress_text);
            mTweetEditText = (EditText) findViewById(R.id.tweet_edit);
            
            // TODO: @某人-- 类似饭否自动补全
            ImageButton mAddUserButton = (ImageButton) findViewById(R.id.add_user);
            mAddUserButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    
                    int start = mTweetEditText.getSelectionStart();
                    int end = mTweetEditText.getSelectionEnd();
                    mTweetEditText.getText().replace(Math.min(start, end),
                            Math.max(start, end), "@");
                }
            });

            // 插入图片
            chooseImagesButton = (ImageButton) findViewById(R.id.choose_images_button);
            chooseImagesButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d(TAG, "chooseImagesButton onClick");
                    openPhotoLibraryMenu();
                }
            });

            // 打开相机
            mCameraButton = (ImageButton) findViewById(R.id.camera_button);
            mCameraButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d(TAG, "mCameraButton onClick");
                    openImageCaptureMenu();
                }
            });

            // With picture
            imageDelete = (ImageView) findViewById(R.id.image_delete);
            imageDelete.setOnClickListener(deleteListener);
            mPreview = (ImageView) findViewById(R.id.preview);
           
            if (Intent.ACTION_SEND.equals(intent.getAction()) && uri != null) {
                getPic(intent, uri);
            }

            // Update status
            mTweetEdit = new TweetEdit(mTweetEditText,
                    (TextView) findViewById(R.id.chars_text));
            mTweetEdit.setOnKeyListener(tweetEnterHandler);
            mTweetEdit.addTextChangedListener(new MyTextWatcher(
                    WriteActivity.this));


            if (NEW_TWEET_ACTION.equals(action)){
                if (!TextHelper.isEmpty(text)){
                    //始终将光标置于最末尾，以方便回复消息时保持@用户在最前面
                	EditText inputField = mTweetEdit.getEditText();
    	            inputField.setTextKeepState(text);
    	
    	            Editable etext = inputField.getText();
    	            int position = etext.length();
    	            Selection.setSelection(etext, position);
                }
            }else if (REPLY_TWEET_ACTION.equals(action)) {
                _reply_id = intent.getStringExtra(EXTRA_REPLY_ID);
                _reply_to_name = intent.getStringExtra(EXTRA_REPLY_TO_NAME);

                if (!TextHelper.isEmpty(text)){
                	String reply_to_name = "@"+_reply_to_name + " ";
                	String other_replies = "";

                	for (String mention : TextHelper.getMentions(text)){
                		//获取名字时不包括自己
                		if (!mention.equals(TwitterApplication.getMyselfName())){
                			other_replies += "@"+mention+" ";
                		}
                    }

                	EditText inputField = mTweetEdit.getEditText();
    	            inputField.setTextKeepState(reply_to_name + other_replies);
    	
                    //将除了reply_to_name的其他名字默认选中
    	            Editable etext = inputField.getText();
    	            int start = reply_to_name.length();
    	            int stop = etext.length();
    	            Selection.setSelection(etext, start, stop);
                }
                
            }else if (REPOST_TWEET_ACTION.equals(action)) {
                if (!TextHelper.isEmpty(text)){
                    // 如果是转发消息，则根据用户习惯，将光标放置在转发消息的头部或尾部
                    SharedPreferences prefereces = getPreferences();
	                boolean isAppendToTheBeginning = prefereces.getBoolean(
	                        Preferences.RT_INSERT_APPEND, true);
	
	                EditText inputField = mTweetEdit.getEditText();
		            inputField.setTextKeepState(text);
	
	                Editable etext = inputField.getText();
	                int position = (isAppendToTheBeginning) ? 0 : etext.length();
	                Selection.setSelection(etext, position);
                }
            }
            

            mLocationButton = (ImageButton) findViewById(R.id.location_button);
            mLocationButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(WriteActivity.this, "LBS地理定位功能开发中, 敬请期待",
                            Toast.LENGTH_SHORT).show();
                }
            });

            Button mTopSendButton = (Button) findViewById(R.id.top_send_btn);
            mTopSendButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    doSend();
                }
            });

            return true;
        } else {
            return false;
        }

    }

    private View.OnClickListener deleteListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = getIntent();
            intent.setAction(null);
            withPic = false;
            mPreview.setVisibility(View.INVISIBLE);
            imageDelete.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause.");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart.");
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

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy.");

        if (mSendTask != null
                && mSendTask.getStatus() == GenericTask.Status.RUNNING) {
            // Doesn't really cancel execution (we let it continue running).
            // See the SendTask code for more details.
            mSendTask.cancel(true);
        }

        // Don't need to cancel FollowersTask (assuming it ends properly).

        if (dialog != null) {
            dialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSendTask != null
                && mSendTask.getStatus() == GenericTask.Status.RUNNING) {
            outState.putBoolean(SIS_RUNNING_KEY, true);
        }
    }

    public static Intent createNewTweetIntent(String text) {
        Intent intent = new Intent(NEW_TWEET_ACTION);
        intent.putExtra(Intent.EXTRA_TEXT, text);

        return intent;
    }

    public static Intent createNewReplyIntent(String tweetText, String screenName, String replyId) {
        Intent intent = new Intent(WriteActivity.REPLY_TWEET_ACTION);
        intent.putExtra(Intent.EXTRA_TEXT, TextHelper.getSimpleTweetText(tweetText));
        intent.putExtra(WriteActivity.EXTRA_REPLY_TO_NAME, screenName);
        intent.putExtra(WriteActivity.EXTRA_REPLY_ID, replyId);

        return intent;
    }

    public static Intent createNewRepostIntent(Context content,
            String tweetText, String screenName, String repostId) {
        SharedPreferences mPreferences = PreferenceManager
                .getDefaultSharedPreferences(content);

        String prefix = mPreferences.getString(Preferences.RT_PREFIX_KEY,
                content.getString(R.string.pref_rt_prefix_default));
        String retweet = " " + prefix + " @" + screenName + " "
                + TextHelper.getSimpleTweetText(tweetText);
        Intent intent = new Intent(WriteActivity.REPOST_TWEET_ACTION);
        intent.putExtra(Intent.EXTRA_TEXT, retweet);
        intent.putExtra(WriteActivity.EXTRA_REPOST_ID, repostId);

        return intent;
    }

    public static Intent createImageIntent(Activity activity, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            WriteActivity writeActivity = (WriteActivity) activity;
            intent.putExtra(Intent.EXTRA_TEXT,
                    writeActivity.mTweetEdit.getText());
            intent.putExtra(WriteActivity.EXTRA_REPLY_TO_NAME,
                    writeActivity._reply_to_name);
            intent.putExtra(WriteActivity.EXTRA_REPLY_ID,
                    writeActivity._reply_id);
            intent.putExtra(WriteActivity.EXTRA_REPOST_ID,
                    writeActivity._repost_id);
        } catch (ClassCastException e) {
            // do nothing
        }
        return intent;

    }

    private class MyTextWatcher implements TextWatcher {

        private WriteActivity _activity;

        public MyTextWatcher(WriteActivity activity) {
            _activity = activity;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                _activity._reply_id = null;
                _activity._reply_to_name = null;
                _activity._repost_id = null;
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            // TODO Auto-generated method stub

        }

    }

    private View.OnKeyListener tweetEnterHandler = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER
                    || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    WriteActivity t = (WriteActivity) (v.getContext());
                    doSend();
                }
                return true;
            }
            return false;
        }
    };

    private void doSend() {
        Log.d(TAG, "dosend  "+withPic);
        startTime = System.currentTimeMillis();
        Log.d(TAG, String.format("doSend, reply_id=%s", _reply_id));

        if (mSendTask != null
                && mSendTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
            String status = mTweetEdit.getText().toString();

            if (!TextHelper.isEmpty(status) || withPic) {
                int mode = SendTask.TYPE_NORMAL;

                if (withPic) {
                    mode = SendTask.TYPE_PHOTO;
                } else if (null != _reply_id) {
                    mode = SendTask.TYPE_REPLY;
                } else if (null != _repost_id) {
                    mode = SendTask.TYPE_REPOST;
                }

                mSendTask = new SendTask();
                mSendTask.setListener(mSendTaskListener);

                TaskParams params = new TaskParams();
                params.put("mode", mode);
                mSendTask.execute(params);
            } else {
                updateProgress(getString(R.string.page_text_is_null));
            }
        }
    }

    private class SendTask extends GenericTask {

        public static final int TYPE_NORMAL = 0;
        public static final int TYPE_REPLY = 1;
        public static final int TYPE_REPOST = 2;
        public static final int TYPE_PHOTO = 3;

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
            TaskParams param = params[0];
            try {
                String status = mTweetEdit.getText().toString();

                int mode = param.getInt("mode");

                Log.d(TAG, "Send Status. Mode : " + mode);

                // Send status in different way
                switch (mode) {

                case TYPE_REPLY:
                    // 增加容错性，即使reply_id为空依然允许发送
                    if (null == WriteActivity.this._reply_id) {
                        Log.e(TAG,
                                "Cann't send status in REPLY mode, reply_id is null");
                    }
                    getApi().updateStatus(status, WriteActivity.this._reply_id);
                    break;

                case TYPE_REPOST:
                    // 增加容错性，即使repost_id为空依然允许发送
                    if (null == WriteActivity.this._repost_id) {
                        Log.e(TAG,
                                "Cann't send status in REPOST mode, repost_id is null");
                    }
                    getApi().repost(status, WriteActivity.this._repost_id);
                    break;

                case TYPE_PHOTO:
                    if (null != mFile) {
                        // Compress image
                        try {
                            mFile = getImageManager().compressImage(mFile, 100); 
                                    //ImageManager.DEFAULT_COMPRESS_QUALITY);
                        } catch (IOException ioe) {
                            Log.e(TAG, "Cann't compress images.");
                        }
                        getApi().updateStatus(status, mFile);
                    } else {
                        Log.e(TAG,
                                "Cann't send status in PICTURE mode, photo is null");
                    }
                    break;

                case TYPE_NORMAL:
                default:
                    getApi().updateStatus(status); // just send a status
                    break;
                }
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);

                if (e.getStatusCode() == HttpClient.NOT_AUTHORIZED) {
                    return TaskResult.AUTH_ERROR;
                }
                return TaskResult.IO_ERROR;
            }

            return TaskResult.OK;
        }

        private ImageManager getImageManager() {
            return TwitterApplication.mImageLoader.getImageManager();
        }
    }

    private void onSendBegin() {
        disableEntry();
        dialog = ProgressDialog.show(WriteActivity.this, "",
                getString(R.string.page_status_updating), true);
        if (dialog != null) {
            dialog.setCancelable(false);
        }
        updateProgress(getString(R.string.page_status_updating));
    }

    private void onSendSuccess() {
        if (dialog != null) {
            dialog.setMessage(getString(R.string.page_status_update_success));
            dialog.dismiss();
        }
        _reply_id = null;
        _repost_id = null;
        updateProgress(getString(R.string.page_status_update_success));
        enableEntry();

        // FIXME: 不理解这段代码的含义，暂时注释掉
        // try {
        // Thread.currentThread();
        // Thread.sleep(500);
        // updateProgress("");
        // } catch (InterruptedException e) {
        // Log.d(TAG, e.getMessage());
        // }
        updateProgress("");

        // 发送成功就自动关闭界面
        finish();

        // 关闭软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mTweetEdit.getEditText().getWindowToken(),
                0);

    }

    private void onSendFailure() {
        dialog.setMessage(getString(R.string.page_status_unable_to_update));
        dialog.dismiss();
        updateProgress(getString(R.string.page_status_unable_to_update));
        enableEntry();
    }

    private void enableEntry() {
        mTweetEdit.setEnabled(true);
        mLocationButton.setEnabled(true);
        chooseImagesButton.setEnabled(true);
    }

    private void disableEntry() {
        mTweetEdit.setEnabled(false);
        mLocationButton.setEnabled(false);
        chooseImagesButton.setEnabled(false);
    }

    // UI helpers.

    private void updateProgress(String progress) {
        mProgressText.setText(progress);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = WriteActivity.createImageIntent(this, mImageUri);
            intent.setClass(this, WriteActivity.class);

            startActivity(intent);

            // 打开发送图片界面后将自身关闭
            finish();
        } else if (requestCode == REQUEST_PHOTO_LIBRARY
                && resultCode == RESULT_OK) {
            mImageUri = data.getData();

            Intent intent = WriteActivity.createImageIntent(this, mImageUri);
            intent.setClass(this, WriteActivity.class);

            startActivity(intent);

            // 打开发送图片界面后将自身关闭
            finish();
        }
    }

}