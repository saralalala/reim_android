package com.rushucloud.reim.me;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import netUtils.HttpConnectionCallback;
import netUtils.Request.User.ChangePasswordRequest;
import netUtils.Response.User.ChangePasswordResponse;
import classes.AppPreference;
import classes.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ChangePasswordActivity extends Activity
{
	private AppPreference appPreference;
	private EditText oldPasswordEditText;
	private EditText newPasswordEditText;
	private EditText confirmPasswordEditText;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.me_change_password);
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
		
		oldPasswordEditText = (EditText)findViewById(R.id.oldPasswordEditText);
		newPasswordEditText = (EditText)findViewById(R.id.newPasswordEditText);
		confirmPasswordEditText = (EditText)findViewById(R.id.confirmPasswordEditText);
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
		
		TextView saveTextView = (TextView)findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(ChangePasswordActivity.this, "UMENG_MINE_CHANGE_USERINFO");
				if (Utils.isNetworkConnected())
				{
					changePassword();
				}
				else
				{
					Utils.showToast(ChangePasswordActivity.this, "网络未连接，无法修改密码");
				}
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
			Builder builder = new Builder(ChangePasswordActivity.this);
			builder.setTitle(R.string.error);
			builder.setMessage("旧密码不能为空！请重新输入");
			builder.setPositiveButton(R.string.confirm,	new DialogInterface.OnClickListener()
										{
											public void onClick(DialogInterface dialog, int which)
											{
												oldPasswordEditText.requestFocus();
											}
										});
			builder.setNegativeButton(R.string.cancel, null);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
		else if (newPassword.equals(""))
		{
			Builder builder = new Builder(ChangePasswordActivity.this);
			builder.setTitle(R.string.error);
			builder.setMessage("新密码不能为空！请重新输入");
			builder.setPositiveButton(R.string.confirm,	new DialogInterface.OnClickListener()
										{
											public void onClick(DialogInterface dialog, int which)
											{
												newPasswordEditText.requestFocus();
											}
										});
			builder.setNegativeButton(R.string.cancel, null);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
		else if (confirmPassword.equals(""))
		{
			Builder builder = new Builder(ChangePasswordActivity.this);
			builder.setTitle(R.string.error);
			builder.setMessage("确认密码不能为空！请重新输入");
			builder.setPositiveButton(R.string.confirm,	new DialogInterface.OnClickListener()
										{
											public void onClick(DialogInterface dialog, int which)
											{
												confirmPasswordEditText.requestFocus();
											}
										});
			builder.setNegativeButton(R.string.cancel, null);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
		else if (!confirmPassword.equals(newPassword))
		{
			Builder builder = new Builder(ChangePasswordActivity.this);
			builder.setTitle(R.string.error);
			builder.setMessage("新密码两次输入不一致！请重新输入");
			builder.setPositiveButton(R.string.confirm,	new DialogInterface.OnClickListener()
										{
											public void onClick(DialogInterface dialog, int which)
											{
												confirmPasswordEditText.requestFocus();
											}
										});
			builder.setNegativeButton(R.string.cancel, null);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
		else if (!oldPassword.equals(appPreference.getPassword()))
		{
			Builder builder = new Builder(ChangePasswordActivity.this);
			builder.setTitle(R.string.error);
			builder.setMessage("旧密码输入错误！请重新输入");
			builder.setPositiveButton(R.string.confirm,	new DialogInterface.OnClickListener()
										{
											public void onClick(DialogInterface dialog, int which)
											{
												oldPasswordEditText.requestFocus();
											}
										});
			builder.setNegativeButton(R.string.cancel, null);
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
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
								builder.setMessage("密码修改成功！");
								builder.setPositiveButton(R.string.confirm,	new DialogInterface.OnClickListener()
															{
																public void onClick(DialogInterface dialog, int which)
																{
																	finish();
																}
															});
								builder.setNegativeButton(R.string.cancel, null);
								AlertDialog alertDialog = builder.create();
								alertDialog.show();
							}
						});					
					}
				}
			});
		}		
	}
}
