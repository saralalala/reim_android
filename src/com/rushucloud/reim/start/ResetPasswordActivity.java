package com.rushucloud.reim.start;

import netUtils.HttpConnectionCallback;
import netUtils.Request.User.ResetPasswordRequest;
import netUtils.Response.User.ResetPasswordResponse;

import classes.ReimApplication;
import classes.Utils;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ResetPasswordActivity extends Activity
{
	private EditText newPasswordEditText;
	private EditText confirmPasswordEditText;
	
	private int cid;
	private String code;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_reset_password);
		initData();
		initView();
		initButton();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ResetPasswordActivity");		
		MobclickAgent.onResume(this);
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
			startActivity(new Intent(ResetPasswordActivity.this, ForgotPasswordActivity.class));
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
		ReimApplication.setProgressDialog(this);
		
		newPasswordEditText = (EditText)findViewById(R.id.newPasswordEditText);
		confirmPasswordEditText = (EditText)findViewById(R.id.confirmPasswordEditText);
		
    	RelativeLayout baseLayout=(RelativeLayout)findViewById(R.id.baseLayout);
    	baseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
	}
	
	private void initButton()
	{
		Button confirmButton = (Button)findViewById(R.id.agreeButton);
		confirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				final String newPassword = newPasswordEditText.getText().toString();
				final String confirmPassword = confirmPasswordEditText.getText().toString();
				if (!Utils.isNetworkConnected())
				{
					Toast.makeText(ResetPasswordActivity.this, "网络未连接，无法发送请求", Toast.LENGTH_SHORT).show();
				}
				else if (newPassword.equals(""))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(ResetPasswordActivity.this)
												.setTitle("错误")
												.setMessage("新密码不能为空")
												.setPositiveButton("确定", new OnClickListener()
												{
													public void onClick(
															DialogInterface dialog,
															int which)
													{
														dialog.dismiss();
														newPasswordEditText.requestFocus();
													}
												})
												.create();
					alertDialog.show();
				}
				else if (confirmPassword.equals(""))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(ResetPasswordActivity.this)
												.setTitle("错误")
												.setMessage("确认密码不能为空")
												.setPositiveButton("确定", new OnClickListener()
												{
													public void onClick(
															DialogInterface dialog,
															int which)
													{
														dialog.dismiss();
														confirmPasswordEditText.requestFocus();
													}
												})
												.create();
					alertDialog.show();					
				}
				else if (!newPassword.equals(confirmPassword))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(ResetPasswordActivity.this)
													.setTitle("错误")
													.setMessage("两次输入的密码不一致")
													.setPositiveButton("确定", new OnClickListener()
													{
														public void onClick(
																DialogInterface dialog,
																int which)
														{
															dialog.dismiss();
															confirmPasswordEditText.requestFocus();
														}
													})
													.create();
					alertDialog.show();
				}
				else
				{
					sendResetPasswordRequest(newPassword);
				}
			}
		});
		
		Button cancelbuButton= (Button)findViewById(R.id.rejectButton);
		cancelbuButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(ResetPasswordActivity.this, ForgotPasswordActivity.class));
				finish();
			}
		});
	}
	
	private void sendResetPasswordRequest(String password)
	{
		ReimApplication.showProgressDialog();
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
							ReimApplication.dismissProgressDialog();
							AlertDialog alertDialog = new AlertDialog.Builder(ResetPasswordActivity.this)
														.setTitle("成功")
														.setMessage("修改密码成功")
														.setPositiveButton("确定", new OnClickListener()
														{
															public void onClick(
																	DialogInterface dialog,
																	int which)
															{
																dialog.dismiss();
																startActivity(new Intent(ResetPasswordActivity.this,
																						 SignInActivity.class));
															}
														})
														.create();
							alertDialog.show();
						}
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							AlertDialog alertDialog = new AlertDialog.Builder(ResetPasswordActivity.this)
															.setTitle("错误")
															.setMessage("修改密码失败！"+response.getErrorMessage())
															.setPositiveButton("确定", null)
															.create();
							alertDialog.show();	
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
