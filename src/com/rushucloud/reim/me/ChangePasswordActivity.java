package com.rushucloud.reim.me;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import netUtils.Request.BaseRequest.HttpConnectionCallback;
import netUtils.Request.User.ChangePasswordRequest;
import netUtils.Response.User.ChangePasswordResponse;
import classes.AppPreference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class ChangePasswordActivity extends Activity
{
	private AppPreference appPreference;
	private EditText oldPasswordEditText;
	private EditText newPasswordEditText;
	private EditText confirmPasswordEditText;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_change_password);
		appPreference = AppPreference.getAppPreference();
		viewInitialise();
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

	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.single_item, menu);
		MenuItem item = menu.getItem(0);
		item.setTitle(getResources().getString(R.string.save));
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == R.id.action_item)
		{
			final String oldPassword = oldPasswordEditText.getText().toString();
			final String newPassword = newPasswordEditText.getText().toString();
			final String confirmPassword = confirmPasswordEditText.getText().toString();
			if (oldPassword.equals(""))
			{
				AlertDialog alertDialog = new AlertDialog.Builder(ChangePasswordActivity.this)
											.setTitle("错误").setMessage("旧密码不能为空！请重新输入")
											.setPositiveButton("确定", new OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													dialog.dismiss();
													oldPasswordEditText.requestFocus();
												}
											}).create();
				alertDialog.show();
			}
			else if (newPassword.equals(""))
			{
				AlertDialog alertDialog = new AlertDialog.Builder(ChangePasswordActivity.this)
											.setTitle("错误").setMessage("新密码不能为空！请重新输入")
											.setPositiveButton("确定", new OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													dialog.dismiss();
													newPasswordEditText.requestFocus();
												}
											}).create();
				alertDialog.show();
			}
			else if (confirmPassword.equals(""))
			{
				AlertDialog alertDialog = new AlertDialog.Builder(ChangePasswordActivity.this)
											.setTitle("错误").setMessage("确认密码不能为空！请重新输入")
											.setPositiveButton("确定", new OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													dialog.dismiss();
													confirmPasswordEditText.requestFocus();
												}
											}).create();
				alertDialog.show();
			}
			else if (!confirmPassword.equals(newPassword))
			{
				AlertDialog alertDialog = new AlertDialog.Builder(ChangePasswordActivity.this)
											.setTitle("错误").setMessage("新密码两次输入不一致！请重新输入")
											.setPositiveButton("确定", new OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													dialog.dismiss();
													confirmPasswordEditText.requestFocus();
												}
											}).create();
				alertDialog.show();
			}
			else if (!oldPassword.equals(appPreference.getPassword()))
			{
				AlertDialog alertDialog = new AlertDialog.Builder(ChangePasswordActivity.this)
											.setTitle("错误").setMessage("旧密码输入错误！请重新输入")
											.setPositiveButton("确定", new OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													dialog.dismiss();
													oldPasswordEditText.requestFocus();
												}
											}).create();
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
									AlertDialog alertDialog = new AlertDialog.Builder(ChangePasswordActivity.this)
																.setTitle("提示")
																.setMessage("密码修改成功！")
																.setPositiveButton("确定", new OnClickListener()
																{
																	public void onClick(DialogInterface dialog, int which)
																	{
																		dialog.dismiss();
																		finish();
																	}
																}).create();
									alertDialog.show();									
								}
							});					
						}
					}
				});
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void viewInitialise()
	{
		try
		{
			oldPasswordEditText = (EditText) findViewById(R.id.oldPasswordEditText);
			newPasswordEditText = (EditText) findViewById(R.id.newPasswordEditText);
			confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
