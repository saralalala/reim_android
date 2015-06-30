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

public class ChangePasswordActivity extends Activity
{
    // Widgets
    private ClearEditText oldPasswordEditText;
    private ClearEditText newPasswordEditText;
    private ClearEditText confirmPasswordEditText;

    // Local Data
    private AppPreference appPreference;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_change_password);
        appPreference = AppPreference.getAppPreference();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("ChangePasswordActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("ChangePasswordActivity");
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

        oldPasswordEditText = (ClearEditText) findViewById(R.id.oldPasswordEditText);
        oldPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
        ViewUtils.requestFocus(this, oldPasswordEditText);

        newPasswordEditText = (ClearEditText) findViewById(R.id.newPasswordEditText);
        newPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());

        confirmPasswordEditText = (ClearEditText) findViewById(R.id.confirmPasswordEditText);
        confirmPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());

        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
                MobclickAgent.onEvent(ChangePasswordActivity.this, "UMENG_MINE_CHANGE_USERINFO");

                final String oldPassword = oldPasswordEditText.getText().toString();
                final String newPassword = newPasswordEditText.getText().toString();
                final String confirmPassword = confirmPasswordEditText.getText().toString();

                if (appPreference.isProxyMode())
                {
                    ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_change_avatar_no_permission);
                }
                else if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_change_password_network_unavailable);
                }
                else if (oldPassword.isEmpty())
                {
                    ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_old_password_empty);
                    ViewUtils.requestFocus(ChangePasswordActivity.this, oldPasswordEditText);
                }
                else if (newPassword.isEmpty())
                {
                    ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_new_password_empty);
                    ViewUtils.requestFocus(ChangePasswordActivity.this, newPasswordEditText);
                }
                else if (confirmPassword.isEmpty())
                {
                    ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_confirm_password_empty);
                    ViewUtils.requestFocus(ChangePasswordActivity.this, confirmPasswordEditText);
                }
                else if (!confirmPassword.equals(newPassword))
                {
                    ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_wrong_confirm_password);
                    ViewUtils.requestFocus(ChangePasswordActivity.this, confirmPasswordEditText);
                }
                else
                {
                    sendChangePasswordRequest(oldPassword, newPassword);
                }
            }
        });

        LinearLayout layout = (LinearLayout) findViewById(R.id.baseLayout);
        layout.setOnClickListener(new View.OnClickListener()
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
        imm.hideSoftInputFromWindow(oldPasswordEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(newPasswordEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(confirmPasswordEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }

    // Network
    private void sendChangePasswordRequest(String oldPassword, final String newPassword)
    {
        ReimProgressDialog.show();
        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ChangePasswordResponse response = new ChangePasswordResponse(httpResponse);
                if (response.getStatus())
                {
                    appPreference.setPassword(newPassword);
                    appPreference.saveAppPreference();

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ChangePasswordActivity.this, R.string.succeed_in_changing_password);
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
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ChangePasswordActivity.this, R.string.failed_to_change_password, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}
