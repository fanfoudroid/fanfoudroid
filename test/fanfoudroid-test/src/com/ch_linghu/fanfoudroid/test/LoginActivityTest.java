package com.ch_linghu.fanfoudroid.test;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.KeyEvent;
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

        assertNotNull(mUsernameEdit);
        assertNotNull(mPasswordEdit);
        assertNotNull(mSigninButton);
    }

    /**
     * TODO: 因为如果多次使用错误密码和帐号进行登录，会被封IP，所以这里需要真实帐号进行测试，
     * 为避免隐私问题，真实帐号的密码需要存于独立的文本中，并且不要将该文件加入到代码库。
     */
    public void testLogin() {

        final String username = "username";
        final String password = "password";

        // 任何对于UI元素进行操作的行为都需要在UI Thread中进行，因此需要加上 @UiThreadTest 或者使用
        // mActivity.runOnUiThread()
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mUsernameEdit.setText(username);
                mPasswordEdit.setText(password);
                mSigninButton.requestFocus(); // click LOGIN button
            }
        });

        mInstrumentation.waitForIdleSync();

        // or press ENTER
        this.sendKeys(KeyEvent.KEYCODE_ENTER);
    }

}
