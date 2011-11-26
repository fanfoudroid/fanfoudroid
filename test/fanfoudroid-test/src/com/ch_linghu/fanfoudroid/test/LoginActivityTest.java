package com.ch_linghu.fanfoudroid.test;

import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ch_linghu.fanfoudroid.LoginActivity;
import com.ch_linghu.fanfoudroid.R;

/**
 * 对于Login Activity的测试
 * 
 * @author lds
 */
public class LoginActivityTest extends
        ActivityInstrumentationTestCase2<LoginActivity> {

    private LoginActivity mActivity; // the activity under test
    private Button mSigninButton;
    private EditText mUsernameEdit;
    private EditText mPasswordEdit;
    private Instrumentation mInstrumentation;

    public LoginActivityTest() {
        super("com.ch_linghu.fanfoudroid", LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);

        mActivity = this.getActivity();
        mInstrumentation = getInstrumentation();
        mUsernameEdit = (EditText) mActivity.findViewById(R.id.username_edit);
        mPasswordEdit = (EditText) mActivity.findViewById(R.id.password_edit);
        mSigninButton = (Button) mActivity.findViewById(R.id.signin_button);

        // Good to go
        assertNotNull(mUsernameEdit);
        assertNotNull(mPasswordEdit);
        assertNotNull(mSigninButton);
    }

    /**
     * TODO: 因为如果多次使用错误密码和帐号进行登录，会被封IP，所以这里需要真实帐号进行测试，
     * 为避免隐私问题，真实帐号的密码需要存于独立的文本中，并且不要将该文件PUSH到代码库。
     */
    //@UiThreadTest
    public void testLogin() {
        final String username = "username";
        final String password = "password";

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mUsernameEdit.setText(username);
                mPasswordEdit.setText(password);
            }
        });
        
        // click sign in button
        TouchUtils.clickView(this, (View) mSigninButton);

        mInstrumentation.waitForIdleSync();
        
        
    }

}
