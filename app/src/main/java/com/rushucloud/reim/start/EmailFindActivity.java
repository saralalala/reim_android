package com.rushucloud.reim.start;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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

public class EmailFindActivity extends Activity
{
    private ClearEditText emailEditText;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_find_by_email);
        initView();
    }

    protected void onResume()
    {
        super.onResume();
        MobclickAgent.onPageStart("EmailFindActivity");
        MobclickAgent.onResume(this);
        ReimProgressDialog.setContext(this);
    }

    protected void onPause()
    {
        super.onPause();
        MobclickAgent.onPageEnd("EmailFindActivity");
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

        emailEditText = (ClearEditText) findViewById(R.id.emailEditText);
        emailEditText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    resetEmail();
                }
                return false;
            }
        });
        emailEditText.setText(getIntent().getStringExtra("username"));
        ViewUtils.requestFocus(this, emailEditText);

        Button confirmButton = (Button) findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                resetEmail();
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

    private void resetEmail()
    {
        if (PhoneUtils.isNetworkConnected())
        {
            String emailAddress = emailEditText.getText().toString();
            if (Utils.isEmail(emailAddress))
            {
                hideSoftKeyboard();
                sendResetEmailRequest();
            }
            else
            {
                ViewUtils.showToast(EmailFindActivity.this, R.string.error_email_wrong_format);
                ViewUtils.requestFocus(this, emailEditText);
            }
        }
        else
        {
            ViewUtils.showToast(EmailFindActivity.this, R.string.error_request_network_unavailable);
        }
    }

    private void sendResetEmailRequest()
    {
        ReimProgressDialog.show();
        ForgotPasswordRequest request = new ForgotPasswordRequest(0, emailEditText.getText().toString());
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
                            ViewUtils.showToast(EmailFindActivity.this, R.string.succeed_in_sending_email);
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
                            ViewUtils.showToast(EmailFindActivity.this, R.string.failed_to_send_email, response.getErrorMessage());
                        }
                    });
                }
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
        ViewUtils.goBackWithIntent(EmailFindActivity.this, SignInActivity.class);
    }
}
