package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.user.ModifyUserRequest;
import netUtils.response.user.ModifyUserResponse;

public class BindEmailActivity extends Activity
{
    // Widgets
    private ClearEditText emailEditText;

    // Local Data
    private User currentUser;
    private String originalEmail;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_bind_email);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("BindEmailActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("BindEmailActivity");
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

        TextView saveTextView = (TextView) findViewById(R.id.saveTextView);
        saveTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                String newEmail = emailEditText.getText().toString();
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(BindEmailActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (newEmail.equals(originalEmail))
                {
                    goBack();
                }
                else if (newEmail.isEmpty() && currentUser.getPhone().isEmpty())
                {
                    ViewUtils.showToast(BindEmailActivity.this, R.string.error_new_email_empty);
                }
                else if (!newEmail.isEmpty() && !Utils.isEmail(newEmail))
                {
                    ViewUtils.showToast(BindEmailActivity.this, R.string.error_email_wrong_format);
                }
                else
                {
                    currentUser.setEmail(newEmail);
                    currentUser.setIsActive(false);
                    sendModifyUserInfoRequest();
                }
            }
        });

        emailEditText = (ClearEditText) findViewById(R.id.emailEditText);
        ViewUtils.requestFocus(this, emailEditText);

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
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
        imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }

    // Data
    private void initData()
    {
        currentUser = AppPreference.getAppPreference().getCurrentUser();
        originalEmail = currentUser.getEmail();
    }

    // Network
    private void sendModifyUserInfoRequest()
    {
        ReimProgressDialog.show();
        ModifyUserRequest request = new ModifyUserRequest(currentUser);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ModifyUserResponse response = new ModifyUserResponse(httpResponse);
                if (response.getStatus())
                {
                    DBManager.getDBManager().updateUser(currentUser);
                    AppPreference appPreference = AppPreference.getAppPreference();
                    if (appPreference.getUsername().equals(originalEmail))
                    {
                        appPreference.setUsername(currentUser.getEmail());
                        appPreference.saveAppPreference();
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(BindEmailActivity.this, R.string.succeed_in_modifying_user_info);
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
                            ViewUtils.showToast(BindEmailActivity.this, R.string.failed_to_modify_user_info, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}