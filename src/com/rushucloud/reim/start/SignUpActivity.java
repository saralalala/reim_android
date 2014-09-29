package com.rushucloud.reim.start;

import netUtils.Request.BaseRequest.HttpConnectionCallback;
import netUtils.Request.User.RegisterRequest;
import netUtils.Response.User.RegisterResponse;
import classes.AppPreference;
import classes.ReimApplication;
import classes.User;
import classes.Utils;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class SignUpActivity extends Activity
{
	private EditText usernameEditText;
	private EditText passwordEditText;
	private EditText confirmPasswordEditText;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_sign_up);
		viewIntialise();
		buttonInitialise();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			startActivity(new Intent(SignUpActivity.this, WelcomeActivity.class));
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void viewIntialise()
	{
		usernameEditText = (EditText)findViewById(R.id.usernameEditText);
		passwordEditText = (EditText)findViewById(R.id.passwordEditText);
		confirmPasswordEditText = (EditText)findViewById(R.id.confirmPasswordEditText);
		
    	RelativeLayout baseLayout=(RelativeLayout)findViewById(R.id.baseLayout);
    	baseLayout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hideSoftKeyboard();
			}
		});    
	}
	
	private void buttonInitialise()
	{
		Button confirmButton = (Button)findViewById(R.id.confirmButton);
		confirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				final String username = usernameEditText.getText().toString();
				final String password = passwordEditText.getText().toString();
				final String confirmPassword = confirmPasswordEditText.getText().toString();
				if (!Utils.isEmailOrPhone(username))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
													.setTitle("错误")
													.setMessage("手机或邮箱格式不正确")
													.setPositiveButton("确定", new OnClickListener()
													{
														public void onClick(
																DialogInterface dialog,
																int which)
														{
															dialog.dismiss();
															usernameEditText.requestFocus();
														}
													})
													.create();
					alertDialog.show();
				}
				else if (password.equals(""))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
												.setTitle("错误")
												.setMessage("密码不能为空")
												.setPositiveButton("确定", new OnClickListener()
												{
													public void onClick(
															DialogInterface dialog,
															int which)
													{
														dialog.dismiss();
														passwordEditText.requestFocus();
													}
												})
												.create();
					alertDialog.show();
					
				}
				else if (!password.equals(confirmPassword))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
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
					User user = new User();
					if (Utils.isEmail(username))
					{
						user.setEmail(username);						
					}
					else
					{
						user.setPhone(username);
					}
					user.setPassword(password);
					register(user);
				}
			}
		});
		
		Button cancelbuButton= (Button)findViewById(R.id.cancelButton);
		cancelbuButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(SignUpActivity.this, WelcomeActivity.class));
				finish();
			}
		});
	}
	
	private void register(final User user)
	{
		RegisterRequest request = new RegisterRequest(user);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final RegisterResponse response = new RegisterResponse(httpResponse);
				if (response.getStatus())
				{
					AppPreference appPreference = AppPreference.getAppPreference();
					if (!user.getEmail().equals(""))
					{
						appPreference.setUsername(user.getEmail());						
					}
					else
					{
						appPreference.setUsername(user.getPhone());								
					}
					appPreference.setPassword(user.getPassword());
					appPreference.setServerToken(response.getServerToken());
					ReimApplication application = (ReimApplication)getApplication();
					application.saveAppPreference();
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
													.setTitle("提示")
													.setMessage("注册成功！")
													.setPositiveButton("确定", new OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															dialog.dismiss();
															startActivity(new Intent(SignUpActivity.this, MainActivity.class));
															finish();
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
							AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
														.setTitle("错误")
														.setMessage("注册失败！"+response.getErrorMessage())
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
		imm.hideSoftInputFromWindow(usernameEditText.getWindowToken(), 0);					
		imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);    	
		imm.hideSoftInputFromWindow(confirmPasswordEditText.getWindowToken(), 0);    	
    }
}
