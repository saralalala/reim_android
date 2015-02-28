package com.rushucloud.reim.start;

import netUtils.HttpConnectionCallback;
import netUtils.Response.User.ResetPasswordResponse;
import netUtils.Request.User.ResetPasswordRequest;

import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ResetPasswordActivity extends Activity
{
	private EditText newPasswordEditText;
	private EditText confirmPasswordEditText;
	
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
		ReimProgressDialog.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ResetPasswordActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			startActivity(new Intent(ResetPasswordActivity.this, EmailFindActivity.class));
			finish();
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
				startActivity(new Intent(ResetPasswordActivity.this, PhoneFindActivity.class));
				finish();
			}
		});
		
		newPasswordEditText = (EditText)findViewById(R.id.newPasswordEditText);
		newPasswordEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
    	
		confirmPasswordEditText = (EditText)findViewById(R.id.confirmPasswordEditText);
		confirmPasswordEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		Button completeButton = (Button)findViewById(R.id.completeButton);
		completeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				final String newPassword = newPasswordEditText.getText().toString();
				final String confirmPassword = confirmPasswordEditText.getText().toString();
				
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(ResetPasswordActivity.this, R.string.error_request_network_unavailable);
				}
				else if (newPassword.isEmpty())
				{
					ViewUtils.showToast(ResetPasswordActivity.this, R.string.error_new_password_empty);
					newPasswordEditText.requestFocus();
				}
				else if (confirmPassword.isEmpty())
				{
					ViewUtils.showToast(ResetPasswordActivity.this, R.string.error_confirm_password_empty);
					confirmPasswordEditText.requestFocus();
				}
				else if (!newPassword.equals(confirmPassword))
				{
					ViewUtils.showToast(ResetPasswordActivity.this, R.string.error_wrong_confirm_password);
					confirmPasswordEditText.requestFocus();
				}
				else
				{
					sendResetPasswordRequest(newPassword);
				}
			}
		});
		completeButton = ViewUtils.resizeLongButton(completeButton);
		
    	RelativeLayout baseLayout=(RelativeLayout)findViewById(R.id.baseLayout);
    	baseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
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
							Builder builder = new Builder(ResetPasswordActivity.this);
							builder.setTitle(R.string.tip);
							builder.setMessage(R.string.prompt_password_changed);
							builder.setNegativeButton(R.string.confirm, new OnClickListener()
														{
															public void onClick(DialogInterface dialog, int which)
															{
																startActivity(new Intent(ResetPasswordActivity.this, SignInActivity.class));
																finish();
															}
														});
							builder.create().show();
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
}