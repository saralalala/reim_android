package com.rushucloud.reim.start;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.common.HttpConnectionCallback;
import netUtils.request.user.ForgotPasswordRequest;
import netUtils.response.user.ForgotPasswordResponse;

public class PhoneFindActivity extends Activity
{
    // Widgets
    private ClearEditText phoneEditText;
    private EditText codeEditText;
    private Button acquireCodeButton;

    // Local Data
    private int waitingTime;
    private Thread thread;

    // View
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_find_by_phone);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("PhoneFindActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("PhoneFindActivity");
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
        backImageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                goBack();
            }
        });

        phoneEditText = (ClearEditText) findViewById(R.id.phoneEditText);
        phoneEditText.setText(getIntent().getStringExtra("username"));
        ViewUtils.requestFocus(this, phoneEditText);

        codeEditText = (EditText) findViewById(R.id.codeEditText);
        codeEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        codeEditText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    verifyCode();
                }
                return false;
            }
        });

        acquireCodeButton = (Button) findViewById(R.id.acquireCodeButton);
        acquireCodeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                MobclickAgent.onEvent(PhoneFindActivity.this, "UMENG_REGIST_FORGETPASSWORD_TEL-CAPTCHA");

                String phoneNumber = phoneEditText.getText().toString();
                if (!PhoneUtils.isNetworkConnected())
                {
                    ViewUtils.showToast(PhoneFindActivity.this, R.string.error_request_network_unavailable);
                }
                else if (phoneNumber.isEmpty())
                {
                    ViewUtils.showToast(PhoneFindActivity.this, R.string.error_phone_empty);
                    ViewUtils.requestFocus(PhoneFindActivity.this, phoneEditText);
                }
                else if (!Utils.isPhone(phoneNumber))
                {
                    ViewUtils.showToast(PhoneFindActivity.this, R.string.error_phone_wrong_format);
                    ViewUtils.requestFocus(PhoneFindActivity.this, phoneEditText);
                }
                else
                {
                    hideSoftKeyboard();
                    sendTextMessage(Utils.removePhonePrefix(phoneNumber));
                }
            }
        });

        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                verifyCode();
            }
        });

        RelativeLayout baseLayout = (RelativeLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new View.OnClickListener()
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
        imm.hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(codeEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        waitingTime = -1;
        ViewUtils.goBackWithIntent(PhoneFindActivity.this, SignInActivity.class);
    }

    // Network
    private void verifyCode()
    {
        MobclickAgent.onEvent(PhoneFindActivity.this, "UMENG_REGIST_FORGETPASSWORD_TEL-SUBMIT");

        if (codeEditText.getText().toString().isEmpty())
        {
            ViewUtils.showToast(PhoneFindActivity.this, R.string.error_code_empty);
        }
        else
        {
            sendVerifyRequest();
        }
    }

    private void sendTextMessage(String phoneNumber)
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
        ForgotPasswordRequest request = new ForgotPasswordRequest(1, phoneNumber);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ForgotPasswordResponse response = new ForgotPasswordResponse(httpResponse);
                if (response.getStatus())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(PhoneFindActivity.this, R.string.succeed_in_sending_message);
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
                            ViewUtils.showToast(PhoneFindActivity.this, R.string.failed_to_send_message, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendVerifyRequest()
    {
        ReimProgressDialog.show();
        ForgotPasswordRequest request = new ForgotPasswordRequest(phoneEditText.getText().toString(), codeEditText.getText().toString());
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final ForgotPasswordResponse response = new ForgotPasswordResponse(httpResponse);
                if (response.getStatus())
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            Intent intent = new Intent(PhoneFindActivity.this, ResetPasswordActivity.class);
                            intent.putExtra("code", codeEditText.getText().toString());
                            ViewUtils.goForwardAndFinish(PhoneFindActivity.this, intent);
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
                            ViewUtils.showToast(PhoneFindActivity.this, R.string.failed_to_verify_code, response.getErrorMessage());
                        }
                    });
                }
            }
        });
    }
}