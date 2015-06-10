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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.model.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.utils.WeChatUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.user.BindWeChatRequest;
import netUtils.response.user.BindWeChatResponse;

public class BindWeChatActivity extends Activity
{
    private ClearEditText passwordEditText;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_bind_wechat);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("BindWeChatActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("BindWeChatActivity");
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

        TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
        confirmTextView.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                String password = passwordEditText.getText().toString();
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(BindWeChatActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (password.isEmpty())
                {
                    ViewUtils.showToast(BindWeChatActivity.this, R.string.error_password_empty);
                }
                else
                {
                    sendBindWeChatRequest();
                }
            }
        });

        passwordEditText = (ClearEditText) findViewById(R.id.passwordEditText);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
        ViewUtils.requestFocus(this, passwordEditText);

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
            }
        });
    }

    private void sendBindWeChatRequest()
    {
        WeChatUtils.sendAuthRequest(this, new WeChatUtils.WeChatOAuthCallBack()
        {
            public void execute(String accessToken, String openID, final String unionID)
            {
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        ReimProgressDialog.setContext(BindWeChatActivity.this);
                        ReimProgressDialog.show();
                    }
                });

                BindWeChatRequest request = new BindWeChatRequest(passwordEditText.getText().toString(), openID, unionID, accessToken);
                request.sendRequest(new HttpConnectionCallback()
                {
                    public void execute(Object httpResponse)
                    {
                        final BindWeChatResponse response = new BindWeChatResponse(httpResponse);
                        if (response.getStatus())
                        {
                            AppPreference appPreference = AppPreference.getAppPreference();
                            User currentUser = AppPreference.getAppPreference().getCurrentUser();
                            currentUser.setWeChat(response.getWechatNickname());
                            DBManager.getDBManager().updateUser(currentUser);
                            if (appPreference.getPassword().isEmpty())
                            {
                                appPreference.setUsername(unionID);
                                appPreference.saveAppPreference();
                            }

                            runOnUiThread(new Runnable()
                            {
                                public void run()
                                {
                                    ReimProgressDialog.dismiss();
                                    ViewUtils.showToast(BindWeChatActivity.this, R.string.succeed_in_modifying_user_info);
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
                                    ViewUtils.showToast(BindWeChatActivity.this, R.string.failed_to_modify_user_info, response.getErrorMessage());
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }
}