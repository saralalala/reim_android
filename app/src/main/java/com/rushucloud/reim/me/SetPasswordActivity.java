package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.AppPreference;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.user.ChangePasswordRequest;
import netUtils.response.user.ChangePasswordResponse;

public class SetPasswordActivity extends Activity
{
    // Widgets
    private ClearEditText passwordEditText;
    private ClearEditText confirmPasswordEditText;

    // Local Data
    private AppPreference appPreference;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_set_password);
        appPreference = AppPreference.getAppPreference();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("SetPasswordActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("SetPasswordActivity");
        MobclickAgent.onPause(this);
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            goBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView()
    {
        ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        passwordEditText = (ClearEditText) findViewById(R.id.passwordEditText);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
        ViewUtils.requestFocus(this, passwordEditText);

        confirmPasswordEditText = (ClearEditText) findViewById(R.id.confirmPasswordEditText);
        confirmPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());

        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
                MobclickAgent.onEvent(SetPasswordActivity.this, "UMENG_MINE_CHANGE_USERINFO");

                final String password = passwordEditText.getText().toString();
                final String confirmPassword = confirmPasswordEditText.getText().toString();

                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(SetPasswordActivity.this, R.string.error_change_password_network_unavailable);
                }
                else if (password.isEmpty())
                {
                    ViewUtils.showToast(SetPasswordActivity.this, R.string.error_password_empty);
                    ViewUtils.requestFocus(SetPasswordActivity.this, passwordEditText);
                }
                else if (confirmPassword.isEmpty())
                {
                    ViewUtils.showToast(SetPasswordActivity.this, R.string.error_confirm_password_empty);
                    ViewUtils.requestFocus(SetPasswordActivity.this, confirmPasswordEditText);
                }
                else if (!confirmPassword.equals(password))
                {
                    ViewUtils.showToast(SetPasswordActivity.this, R.string.error_wrong_set_password_confirm);
                    ViewUtils.requestFocus(SetPasswordActivity.this, confirmPasswordEditText);
                }
                else
                {
                    sendChangePasswordRequest(password);
                }
            }
        });

        LinearLayout layout = (LinearLayout) findViewById(R.id.baseLayout);
        layout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
            }
        });
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(confirmPasswordEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }

    // Network
    private void sendChangePasswordRequest(final String password)
    {
        ReimProgressDialog.show();
        ChangePasswordRequest request = new ChangePasswordRequest("", password);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ChangePasswordResponse response = new ChangePasswordResponse(httpResponse);
                if (response.getStatus())
                {
                    appPreference.setServerToken(response.getServerToken());
                    appPreference.setPassword(password);
                    appPreference.setHasPassword(true);
                    appPreference.saveAppPreference();

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.show();
                            ViewUtils.showToast(SetPasswordActivity.this, R.string.succeed_in_setting_password);
                            goBack();
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.show();
                            ViewUtils.showToast(SetPasswordActivity.this, R.string.failed_to_change_password, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}
