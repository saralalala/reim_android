package com.rushucloud.reim.start;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.user.ResetPasswordRequest;
import netUtils.response.user.ResetPasswordResponse;

public class ResetPasswordActivity extends Activity
{
	private ClearEditText newPasswordEditText;
	private ClearEditText confirmPasswordEditText;
	
	private int cid;
	private String code;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_reset_password);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ResetPasswordActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ResetPasswordActivity");
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
		Bundle bundle = this.getIntent().getExtras();
		cid = bundle.getInt("cid");
		code = bundle.getString("code");
	}
	
	private void initView()
	{
		getActionBar().hide();

		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                goBack();
			}
		});
		
		newPasswordEditText = (ClearEditText) findViewById(R.id.newPasswordEditText);
        newPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
    	
		confirmPasswordEditText = (ClearEditText) findViewById(R.id.confirmPasswordEditText);
        confirmPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
        confirmPasswordEditText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    resetPassword();
                }
                return false;
            }
        });
		
		Button completeButton = (Button) findViewById(R.id.completeButton);
		completeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                resetPassword();
			}
		});
		
    	RelativeLayout baseLayout=(RelativeLayout) findViewById(R.id.baseLayout);
    	baseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
	}

    private void resetPassword()
    {
        MobclickAgent.onEvent(ResetPasswordActivity.this, "UMENG_REGIST_FORGETPASSWORD-NEWPASSWORD");

        final String newPassword = newPasswordEditText.getText().toString();
        final String confirmPassword = confirmPasswordEditText.getText().toString();

        if (!PhoneUtils.isNetworkConnected())
        {
            ViewUtils.showToast(ResetPasswordActivity.this, R.string.error_request_network_unavailable);
        }
        else if (newPassword.isEmpty())
        {
            ViewUtils.showToast(ResetPasswordActivity.this, R.string.error_new_password_empty);
            ViewUtils.requestFocus(ResetPasswordActivity.this, newPasswordEditText);
        }
        else if (confirmPassword.isEmpty())
        {
            ViewUtils.showToast(ResetPasswordActivity.this, R.string.error_confirm_password_empty);
            ViewUtils.requestFocus(ResetPasswordActivity.this, confirmPasswordEditText);
        }
        else if (!newPassword.equals(confirmPassword))
        {
            ViewUtils.showToast(ResetPasswordActivity.this, R.string.error_wrong_confirm_password);
            ViewUtils.requestFocus(ResetPasswordActivity.this, confirmPasswordEditText);
        }
        else
        {
            sendResetPasswordRequest(newPassword);
        }
    }

	private void sendResetPasswordRequest(String password)
	{
		ReimProgressDialog.show();
		ResetPasswordRequest request = new ResetPasswordRequest(password, cid, code);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ResetPasswordResponse response = new ResetPasswordResponse(httpResponse);
				if (response.getStatus())
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
                            ViewUtils.showToast(ResetPasswordActivity.this, R.string.succeed_in_changing_password);
                            ViewUtils.goBackWithIntent(ResetPasswordActivity.this, SignInActivity.class);
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
							ViewUtils.showToast(ResetPasswordActivity.this, R.string.failed_to_change_password, response.getErrorMessage());
						}
					});				
				}
			}
		});
	}
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(newPasswordEditText.getWindowToken(), 0);					
		imm.hideSoftInputFromWindow(confirmPasswordEditText.getWindowToken(), 0);
    }

    private void goBack()
    {
        ViewUtils.goBackWithIntent(ResetPasswordActivity.this, PhoneFindActivity.class);
    }
}