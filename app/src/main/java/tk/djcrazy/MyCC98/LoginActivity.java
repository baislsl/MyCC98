/**
 * Entrance of program
 * The login activity
 *
 */

package tk.djcrazy.MyCC98;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.inject.Inject;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import tk.djcrazy.MyCC98.dialog.AuthDialogFragment;
import tk.djcrazy.MyCC98.security.Md5;
import tk.djcrazy.MyCC98.util.Intents;
import tk.djcrazy.MyCC98.util.ToastUtils;
import tk.djcrazy.libCC98.CachedCC98Service;
import tk.djcrazy.libCC98.NewCC98Service;
import tk.djcrazy.libCC98.data.LoginType;
import tk.djcrazy.libCC98.util.RequestResultListener;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

public class LoginActivity extends BaseFragmentActivity implements
        OnClickListener {

    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String REMEMBERPWD = "REMEMBERPWD";
    private static final String AUTHINFO = "AUTHINFO";
    @InjectView(R.id.username)
    private EditText mUsernameEdit;
    @InjectView(R.id.password)
    private EditText mPasswordEdit;
    @InjectView(R.id.login)
    private Button mSignInButton;
    @InjectView(R.id.proxy_setting)
    private Button mProxyButton;
    @InjectExtra(value = Intents.EXTRA_NEED_LOGIN, optional = true)
    private boolean mNeedLogin = false;
    private String mUsername = "";
    private String mPassword = "";
    private String authUserName = "";
    private String authPassword = "";
    private String authHost = "";
    private LoginType mLoginType = LoginType.NORMAL;
    private Boolean authRememberPwd = false;
    @Inject
    private CachedCC98Service service;
    @Inject
    private NewCC98Service mNewCC98Service;

    private void forwardToNextActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void showLoginField() {
        final View layout = findViewById(R.id.login_field);
        layout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mSignInButton.setOnClickListener(this);
        mProxyButton.setOnClickListener(this);
        if (!mNeedLogin && service.getusersInfo().users.size() > 0) {
            forwardToNextActivity();
        } else {
            mProxyButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showLoginField();
                }
            }, 500);
        }
    }

    @Override
    protected void onStop() {
        mNewCC98Service.cancelRequest(this.getClass());
        super.onStop();
    }

    public void onOkClick(String userName, String password, String host, LoginType type) {
        authUserName = userName;
        authPassword = password;
        authHost = host;
        mLoginType = LoginType.USER_DEFINED;
    }

    public void onCancelClick() {
        mLoginType = LoginType.NORMAL;
        showLoginField();
    }

    private void initAuthInfo() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment newFragment = new AuthDialogFragment();
        newFragment.show(ft, "dialog");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                doLogin();
                break;
            case R.id.proxy_setting:
                initAuthInfo();
                break;
        }
    }

    private void doLogin() {
        mUsername = mUsernameEdit.getText().toString().trim();
        mPassword = mPasswordEdit.getText().toString().trim();
        if (mUsername.equals("")) {
            ToastUtils.alert(this, "用户名不能为空");
            return;
        } else if (mPassword.length() <= 0) {
            ToastUtils.alert(this, "密码不能为空");
            return;
        }
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("正在登录，请稍后......");
        dialog.show();
        mNewCC98Service.login(this.getClass(), mUsername, Md5.MyMD5(mPassword, Md5.T32), Md5.MyMD5(mPassword, Md5.T16), authUserName, authPassword,
                 authHost, mLoginType, new RequestResultListener<Boolean>() {
            @Override
            public void onRequestComplete(Boolean result) {
                dialog.dismiss();
                forwardToNextActivity();
            }

            @Override
            public void onRequestError(String msg) {
                dialog.dismiss();
                ToastUtils.alert(LoginActivity.this, msg);
            }
        });
    }
}
