package com.rushucloud.reim.me;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

import classes.utils.AppPreference;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import netUtils.HttpConnectionCallback;
import netUtils.request.user.ChangePasswordRequest;
import netUtils.response.user.ChangePasswordResponse;

public class ChangePasswordActivity extends Activity
{
	private AppPreference appPreference;
	private EditText oldPasswordEditText;
	private EditText newPasswordEditText;
	private EditText confirmPasswordEditText;

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
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ChangePasswordActivity");
		MobclickAgent.onPause(this);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initView()
	{
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
		
		oldPasswordEditText = (EditText) findViewById(R.id.oldPasswordEditText);
		oldPasswordEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		newPasswordEditText = (EditText) findViewById(R.id.newPasswordEditText);
		newPasswordEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
		confirmPasswordEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
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
				
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_change_password_network_unavailable);
				}
				else if (oldPassword.isEmpty())
				{
					ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_old_password_empty);
					oldPasswordEditText.requestFocus();
				}
				else if (newPassword.isEmpty())
				{
					ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_new_password_empty);
					newPasswordEditText.requestFocus();
				}
				else if (confirmPassword.isEmpty())
				{
					ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_confirm_password_empty);
					confirmPasswordEditText.requestFocus();
				}
				else if (oldPassword.equals(newPassword))
				{
					ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_same_password);
					newPasswordEditText.requestFocus();
				}
				else if (!confirmPassword.equals(newPassword))
				{
					ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_wrong_confirm_password);
					confirmPasswordEditText.requestFocus();
				}
				else if (!oldPassword.equals(appPreference.getPassword()))
				{
					ViewUtils.showToast(ChangePasswordActivity.this, R.string.error_wrong_old_password);
					oldPasswordEditText.requestFocus();
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

	private void sendChangePasswordRequest(String oldPassword, final String newPassword)
	{
		ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				ChangePasswordResponse response = new ChangePasswordResponse(httpResponse);
				if (response.getStatus())
				{
					appPreference.setPassword(newPassword);
					appPreference.saveAppPreference();

					runOnUiThread(new Runnable()
					{
						public void run()
						{
							Builder builder = new Builder(ChangePasswordActivity.this);
							builder.setTitle(R.string.tip);
							builder.setMessage(R.string.succeed_in_changing_password);
							builder.setPositiveButton(R.string.confirm,	new DialogInterface.OnClickListener()
														{
															public void onClick(DialogInterface dialog, int which)
															{
																finish();
															}
														});
							builder.setNegativeButton(R.string.cancel, null);
							builder.create().show();
						}
					});					
				}
			}
		});
	}
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(oldPasswordEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(newPasswordEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(confirmPasswordEditText.getWindowToken(), 0);
    }
}
