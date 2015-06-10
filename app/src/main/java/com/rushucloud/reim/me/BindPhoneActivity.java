package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import netUtils.request.user.VerifyCodeRequest;
import netUtils.response.user.ModifyUserResponse;
import netUtils.response.user.VerifyCodeResponse;

public class BindPhoneActivity extends Activity
{
    private ClearEditText phoneEditText;
    private EditText codeEditText;
    private Button acquireCodeButton;

    private User currentUser;
    private String originalPhone;

    private int waitingTime;
    private Thread thread;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_bind_phone);
        initData();
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("BindPhoneActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("BindPhoneActivity");
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

    private void initData()
    {
        currentUser = AppPreference.getAppPreference().getCurrentUser();
        originalPhone = currentUser.getPhone();
    }

    private void initView()
    {
        ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        phoneEditText = (ClearEditText) findViewById(R.id.phoneEditText);
        ViewUtils.requestFocus(this, phoneEditText);

        codeEditText = (EditText) findViewById(R.id.codeEditText);

        acquireCodeButton = (Button) findViewById(R.id.acquireCodeButton);
        acquireCodeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                String phoneNumber = phoneEditText.getText().toString();
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(BindPhoneActivity.this, R.string.error_request_network_unavailable);
                }
                else if (phoneNumber.isEmpty())
                {
                    ViewUtils.showToast(BindPhoneActivity.this, R.string.error_phone_empty);
                    ViewUtils.requestFocus(BindPhoneActivity.this, phoneEditText);
                }
                else if (!Utils.isPhone(phoneNumber))
                {
                    ViewUtils.showToast(BindPhoneActivity.this, R.string.error_phone_wrong_format);
                    ViewUtils.requestFocus(BindPhoneActivity.this, phoneEditText);
                }
                else
                {
                    getVerifyCode(Utils.removePhonePrefix(phoneNumber));
                }
            }
        });

        Button confirmButton = (Button) findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();

                String newPhone = phoneEditText.getText().toString();
                String inputCode = codeEditText.getText().toString();

                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(BindPhoneActivity.this, R.string.error_modify_network_unavailable);
                }
                else if (newPhone.isEmpty())
                {
                    ViewUtils.showToast(BindPhoneActivity.this, R.string.error_new_phone_empty);
                }
                else if (!Utils.isPhone(newPhone))
                {
                    ViewUtils.showToast(BindPhoneActivity.this, R.string.error_phone_wrong_format);
                }
                else if (inputCode.isEmpty())
                {
                    ViewUtils.showToast(BindPhoneActivity.this, R.string.error_code_empty);
                    ViewUtils.requestFocus(BindPhoneActivity.this, codeEditText);
                }
                else
                {
                    currentUser.setPhone(Utils.removePhonePrefix(newPhone));
                    sendModifyUserInfoRequest(inputCode);
                }
            }
        });

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                hideSoftKeyboard();
            }
        });
    }

    private void getVerifyCode(String phoneNumber)
    {
        final String second = ViewUtils.getString(R.string.second);
        waitingTime = 60;
        acquireCodeButton.setEnabled(false);
        acquireCodeButton.setText(waitingTime + second);
        thread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    while (waitingTime > 0)
                    {
                        java.lang.Thread.sleep(1000);
                        waitingTime -= 1;
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                acquireCodeButton.setText(waitingTime + second);
                            }
                        });
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            acquireCodeButton.setText(R.string.acquire_code);
                            acquireCodeButton.setEnabled(true);
                        }
                    });
                }
            }
        });
        thread.start();

        ReimProgressDialog.show();
        VerifyCodeRequest request = new VerifyCodeRequest(phoneNumber);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final VerifyCodeResponse response = new VerifyCodeResponse(httpResponse);
                if (response.getStatus())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(BindPhoneActivity.this, R.string.succeed_in_sending_message);
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
                            thread.interrupt();
                            ViewUtils.showToast(BindPhoneActivity.this, R.string.failed_to_get_code, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendModifyUserInfoRequest(String verifyCode)
    {
        ReimProgressDialog.show();
        ModifyUserRequest request = new ModifyUserRequest(currentUser, verifyCode);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ModifyUserResponse response = new ModifyUserResponse(httpResponse);
                if (response.getStatus())
                {
                    DBManager.getDBManager().updateUser(currentUser);
                    AppPreference appPreference = AppPreference.getAppPreference();
                    if (appPreference.getUsername().equals(originalPhone))
                    {
                        appPreference.setUsername(currentUser.getPhone());
                        appPreference.saveAppPreference();
                    }

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(BindPhoneActivity.this, R.string.succeed_in_modifying_user_info);
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
                            ViewUtils.showToast(BindPhoneActivity.this, R.string.failed_to_modify_user_info, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void hideSoftKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(codeEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        hideSoftKeyboard();
        ViewUtils.goBack(this);
    }
}
