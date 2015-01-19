package com.rushucloud.reim.me;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import netUtils.HttpConnectionCallback;
import netUtils.Response.User.ChangePasswordResponse;
import netUtils.Request.User.ChangePasswordRequest;
import classes.utils.AppPreference;
import classes.utils.Utils;

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
		
		oldPasswordEditText = (EditText)findViewById(R.id.oldPasswordEditText);
		oldPasswordEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		
		newPasswordEditText = (EditText)findViewById(R.id.newPasswordEditText);
		newPasswordEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		
		confirmPasswordEditText = (EditText)findViewById(R.id.confirmPasswordEditText);
		confirmPasswordEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		
		Button submitButton = (Button)findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				MobclickAgent.onEvent(ChangePasswordActivity.this, "UMENG_MINE_CHANGE_USERINFO");
				if (Utils.isNetworkConnected())
				{
					changePassword();
				}
				else
				{
					Utils.showToast(ChangePasswordActivity.this, R.string.error_change_password_network_unavailable);
				}
			}
		});
		submitButton = Utils.resizeLongButton(submitButton);
		
		LinearLayout layout = (LinearLayout)findViewById(R.id.baseLayout);
		layout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
	}

	private void changePassword()
	{
		final String oldPassword = oldPasswordEditText.getText().toString();
		final String newPassword = newPasswordEditText.getText().toString();
		final String confirmPassword = confirmPasswordEditText.getText().toString();
		if (oldPassword.equals(""))
		{
			Utils.showToast(ChangePasswordActivity.this, R.string.error_old_password_empty);
			oldPasswordEditText.requestFocus();
		}
		else if (newPassword.equals(""))
		{
			Utils.showToast(ChangePasswordActivity.this, R.string.error_new_password_empty);
			newPasswordEditText.requestFocus();
		}
		else if (confirmPassword.equals(""))
		{
			Utils.showToast(ChangePasswordActivity.this, R.string.error_confirm_password_empty);
			confirmPasswordEditText.requestFocus();
		}
		else if (oldPassword.equals(newPassword))
		{
			Utils.showToast(ChangePasswordActivity.this, R.string.error_same_password);
			newPasswordEditText.requestFocus();
		}
		else if (!confirmPassword.equals(newPassword))
		{
			Utils.showToast(ChangePasswordActivity.this, R.string.error_wrong_confirm_password);
			confirmPasswordEditText.requestFocus();
		}
		else if (!oldPassword.equals(appPreference.getPassword()))
		{
			Utils.showToast(ChangePasswordActivity.this, R.string.error_wrong_old_password);
			oldPasswordEditText.requestFocus();
		}
		else
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
								builder.setMessage(R.string.prompt_password_changed);
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
	}

    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(oldPasswordEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(newPasswordEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(confirmPasswordEditText.getWindowToken(), 0);
    }
}
