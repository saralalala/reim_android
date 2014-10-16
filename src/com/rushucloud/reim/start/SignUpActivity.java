package com.rushucloud.reim.start;

import netUtils.Request.CommonRequest;
import netUtils.Request.BaseRequest.HttpConnectionCallback;
import netUtils.Request.User.RegisterRequest;
import netUtils.Request.User.VerifyCodeRequest;
import netUtils.Response.CommonResponse;
import netUtils.Response.User.RegisterResponse;
import netUtils.Response.User.VerifyCodeResponse;
import classes.AppPreference;
import classes.User;
import classes.Utils;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;

public class SignUpActivity extends Activity
{
	private TabHost tabHost;
	
	private String code = "";
	
	private EditText phoneEditText;
	private EditText phonePasswordEditText;
	private EditText codeEditText;
	private EditText emailEditText;
	private EditText emailPasswordEditText;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_sign_up);
		tabViewInitialise();
		viewIntialise();
		buttonInitialise();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("SignUpActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("SignUpActivity");
		MobclickAgent.onPause(this);
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
	
	private void tabViewInitialise()
	{
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
		tabHost.setup();
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		layoutInflater.inflate(R.layout.start_register_by_phone, tabHost.getTabContentView());
		layoutInflater.inflate(R.layout.start_register_by_email, tabHost.getTabContentView());
		
		tabHost.addTab(tabHost.newTabSpec("registerByPhone")
				.setIndicator(getResources().getString(R.string.signUpByPhone))
				.setContent(R.id.phoneBaseLayout));
		
		tabHost.addTab(tabHost.newTabSpec("registerByEmail")
				.setIndicator(getResources().getString(R.string.signUpByEmail))
				.setContent(R.id.emailBaseLayout));
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		tabHost.getTabWidget().getChildTabViewAt(0).setMinimumWidth(screenWidth / 2);
		tabHost.getTabWidget().getChildTabViewAt(1).setMinimumWidth(screenWidth / 2);
	}
	
	private void viewIntialise()
	{
		phoneEditText = (EditText)findViewById(R.id.phoneEditText);
		phonePasswordEditText = (EditText)findViewById(R.id.phonePasswordEditText);
		codeEditText = (EditText)findViewById(R.id.codeEditText);
		emailEditText = (EditText)findViewById(R.id.emailEditText);
		emailPasswordEditText = (EditText)findViewById(R.id.emailPasswordEditText);
		
    	RelativeLayout phoneBaseLayout=(RelativeLayout)findViewById(R.id.phoneBaseLayout);
    	phoneBaseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});    
		
    	RelativeLayout emailBaseLayout=(RelativeLayout)findViewById(R.id.emailBaseLayout);
    	emailBaseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();				
			}
		}); 
	}
	
	private void buttonInitialise()
	{
		Button acquireCodeButton = (Button)findViewById(R.id.acquireCodeButton);
		acquireCodeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				String phoneNumber = phoneEditText.getText().toString();
				if (phoneNumber.equals(""))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
												.setTitle("错误")
												.setMessage("手机号不能为空")
												.setPositiveButton("确定", new OnClickListener()
												{
													public void onClick(
															DialogInterface dialog,
															int which)
													{
														dialog.dismiss();
														phoneEditText.requestFocus();
													}
												})
												.create();
					alertDialog.show();					
				}
				else if (!Utils.isPhone(phoneNumber))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
												.setTitle("错误")
												.setMessage("手机号格式不正确")
												.setPositiveButton("确定", new OnClickListener()
												{
													public void onClick(
															DialogInterface dialog,
															int which)
													{
														dialog.dismiss();
														phoneEditText.requestFocus();
													}
												})
												.create();
					alertDialog.show();
				}
				else 
				{
					hideSoftKeyboard();
					getVerifyCode(phoneNumber);
				}
			}
		});
		
		Button phoneConfirmButton = (Button)findViewById(R.id.phoneConfirmButton);
		phoneConfirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				String phoneNumber = phoneEditText.getText().toString();
				String password = phonePasswordEditText.getText().toString();
				String inputCode = codeEditText.getText().toString();
				
				if (phoneNumber.equals(""))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
												.setTitle("错误")
												.setMessage("手机号不能为空")
												.setPositiveButton("确定", new OnClickListener()
												{
													public void onClick(
															DialogInterface dialog,
															int which)
													{
														dialog.dismiss();
														phoneEditText.requestFocus();
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
														phonePasswordEditText.requestFocus();
													}
												})
												.create();
					alertDialog.show();					
				}
				else if (inputCode.equals(""))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
												.setTitle("错误")
												.setMessage("验证码不能为空")
												.setPositiveButton("确定", new OnClickListener()
												{
													public void onClick(
															DialogInterface dialog,
															int which)
													{
														dialog.dismiss();
														codeEditText.requestFocus();
													}
												})
												.create();
					alertDialog.show();					
				}
				else if (!inputCode.equals(code))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
												.setTitle("错误")
												.setMessage("验证码错误")
												.setPositiveButton("确定", new OnClickListener()
												{
													public void onClick(
															DialogInterface dialog,
															int which)
													{
														dialog.dismiss();
														codeEditText.requestFocus();
													}
												})
												.create();
					alertDialog.show();					
				}
				else
				{
					User user = new User();
					user.setPhone(phoneNumber);
					user.setPassword(password);
					
					hideSoftKeyboard();
					register(user, inputCode);
				}
			}
		});

		Button phoneCancelButton= (Button)findViewById(R.id.phoneCancelButton);
		phoneCancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(SignUpActivity.this, WelcomeActivity.class));
				finish();
			}
		});
	
		Button emailConfirmButton = (Button)findViewById(R.id.emailConfirmButton);
		emailConfirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				String email = emailEditText.getText().toString();
				String password = emailPasswordEditText.getText().toString();
				code = "";
				
				if (email.equals(""))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
												.setTitle("错误")
												.setMessage("邮箱不能为空")
												.setPositiveButton("确定", new OnClickListener()
												{
													public void onClick(
															DialogInterface dialog,
															int which)
													{
														dialog.dismiss();
														emailEditText.requestFocus();
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
														phonePasswordEditText.requestFocus();
													}
												})
												.create();
					alertDialog.show();					
				}
				else
				{
					User user = new User();
					user.setEmail(email);
					user.setPassword(password);
					
					hideSoftKeyboard();
					register(user, "");
				}
			}
		});		
		
		Button emailCancelButton= (Button)findViewById(R.id.emailCancelButton);
		emailCancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(SignUpActivity.this, WelcomeActivity.class));
				finish();
			}
		});
	}
	
	private void register(final User user, String verifyCode)
	{
		RegisterRequest request = new RegisterRequest(user, verifyCode);
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
					appPreference.setCurrentUserID(response.getUserID());
					appPreference.setCurrentGroupID(-1);
					appPreference.saveAppPreference();
					
					getCommonInfo();
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
        
    private void getVerifyCode(String phoneNumber)
    {
		VerifyCodeRequest request = new VerifyCodeRequest(phoneNumber);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final VerifyCodeResponse response = new VerifyCodeResponse(httpResponse);
				if (response.getStatus())
				{
					code = response.getVerifyCode();
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							Toast.makeText(SignUpActivity.this, "验证短信已发送", Toast.LENGTH_SHORT).show();
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
    
    private void getCommonInfo()
    {
		CommonRequest request = new CommonRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CommonResponse response = new CommonResponse(httpResponse);				
				if (response.getStatus())
				{
					DBManager dbManager = DBManager.getDBManager();
					
					// update current user
					dbManager.syncUser(response.getCurrentUser());
					
					// update categories
					dbManager.updateGroupCategories(response.getCategoryList(), -1);
					
					// refresh UI
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							String message = "注册成功！";
							if (code.equals(""))
							{
								message += "激活邮件已发送，请到邮箱中查看！";
							}
							AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this)
													.setTitle("提示")
													.setMessage(message)
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
														.setMessage("获取信息失败！" + response.getErrorMessage())
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
		imm.hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);					
		imm.hideSoftInputFromWindow(phonePasswordEditText.getWindowToken(), 0);    	
		imm.hideSoftInputFromWindow(codeEditText.getWindowToken(), 0);  
		imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);  
		imm.hideSoftInputFromWindow(emailPasswordEditText.getWindowToken(), 0);    	
    }
}
