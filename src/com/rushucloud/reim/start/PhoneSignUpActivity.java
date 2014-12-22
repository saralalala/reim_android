package com.rushucloud.reim.start;

import netUtils.Request.User.SignInRequest;
import netUtils.HttpConnectionCallback;
import netUtils.Request.User.RegisterRequest;
import netUtils.Request.User.VerifyCodeRequest;
import netUtils.Response.User.SignInResponse;
import netUtils.Response.User.RegisterResponse;
import netUtils.Response.User.VerifyCodeResponse;
import classes.User;
import classes.Utils.AppPreference;
import classes.Utils.ReimApplication;
import classes.Utils.Utils;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;

import android.app.Activity;
import android.app.AlertDialog.Builder;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PhoneSignUpActivity extends Activity
{	
	private EditText phoneEditText;
	private EditText passwordEditText;
	private EditText confirmPasswordEditText;
	private EditText codeEditText;
	private Button acquireCodeButton;
	
	private String code = "";
	private int waitingTime;
	private Thread thread;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_sign_up_by_phone);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("PhoneSignUpActivity");		
		MobclickAgent.onResume(this);
		ReimApplication.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("PhoneSignUpActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			waitingTime = -1;
			startActivity(new Intent(PhoneSignUpActivity.this, WelcomeActivity.class));
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initView()
	{
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				waitingTime = -1;
				startActivity(new Intent(PhoneSignUpActivity.this, WelcomeActivity.class));
				finish();
			}
		});
		
		phoneEditText = (EditText)findViewById(R.id.phoneEditText);
		phoneEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		
		passwordEditText = (EditText)findViewById(R.id.passwordEditText);
		passwordEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		
		confirmPasswordEditText = (EditText)findViewById(R.id.confirmPasswordEditText);
		confirmPasswordEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		
		codeEditText = (EditText)findViewById(R.id.codeEditText);	
		codeEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());

		acquireCodeButton = (Button)findViewById(R.id.acquireCodeButton);
		acquireCodeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				
				String phoneNumber = phoneEditText.getText().toString();
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(PhoneSignUpActivity.this, "网络未连接，无法发送请求");
				}
				else if (phoneNumber.equals(""))
				{
					Utils.showToast(PhoneSignUpActivity.this, "手机号不能为空");
					phoneEditText.requestFocus();
				}
				else if (!Utils.isPhone(phoneNumber))
				{
					Utils.showToast(PhoneSignUpActivity.this, "手机号格式不正确");
					phoneEditText.requestFocus();
				}
				else 
				{
					getVerifyCode(phoneNumber);
				}
			}
		});
		acquireCodeButton = Utils.resizeShortButton(acquireCodeButton, 32);
		
		Button signUpButton = (Button)findViewById(R.id.signUpButton);
		signUpButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(PhoneSignUpActivity.this, "UMENG_REGIST_TEL");
				
				hideSoftKeyboard();
				
				String phoneNumber = phoneEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				String confirmPassword = confirmPasswordEditText.getText().toString();
				String inputCode = codeEditText.getText().toString();
				
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(PhoneSignUpActivity.this, "网络未连接，无法发送请求");
				}
				else if (Utils.isPhone(phoneNumber))
				{
					Utils.showToast(PhoneSignUpActivity.this, "手机号格式不正确");
					phoneEditText.requestFocus();		
				}
				else if (phoneNumber.equals(""))
				{
					Utils.showToast(PhoneSignUpActivity.this, "手机号不能为空");
					phoneEditText.requestFocus();		
				}
				else if (password.equals(""))
				{
					Utils.showToast(PhoneSignUpActivity.this, "密码不能为空");
					passwordEditText.requestFocus();	
				}
				else if (confirmPassword.equals(""))
				{
					Utils.showToast(PhoneSignUpActivity.this, "确认密码不能为空");
					confirmPasswordEditText.requestFocus();
				}
				else if (!password.equals(confirmPassword))
				{
					Utils.showToast(PhoneSignUpActivity.this, "两次输入的密码不一致");
					confirmPasswordEditText.requestFocus();
				}
				else if (inputCode.equals(""))
				{
					Utils.showToast(PhoneSignUpActivity.this, "验证码不能为空");
					codeEditText.requestFocus();	
				}
				else if (!inputCode.equals(code))
				{
					Utils.showToast(PhoneSignUpActivity.this, "验证码错误");
					codeEditText.requestFocus();		
				}
				else
				{
					User user = new User();
					user.setPhone(phoneNumber);
					user.setPassword(password);
					
					sendRegisterRequest(user, inputCode);
				}
			}
		});
		signUpButton = Utils.resizeLongButton(signUpButton);
		
    	RelativeLayout baseLayout=(RelativeLayout)findViewById(R.id.baseLayout);
    	baseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
	}
	
	private void sendRegisterRequest(final User user, String verifyCode)
	{
		ReimApplication.showProgressDialog();
		RegisterRequest request = new RegisterRequest(user, verifyCode);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final RegisterResponse response = new RegisterResponse(httpResponse);
				if (response.getStatus())
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							Utils.showToast(PhoneSignUpActivity.this, "注册成功!正在获取数据");
						}
					});
					
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setUsername(user.getEmail());	
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
							ReimApplication.dismissProgressDialog();
							Utils.showToast(PhoneSignUpActivity.this, "注册失败！"+response.getErrorMessage());
						}
					});		
				}
			}
		});		
	}
        
    private void getVerifyCode(String phoneNumber)
    {
		waitingTime = 60;
		acquireCodeButton.setEnabled(false);
		acquireCodeButton.setText(waitingTime + "s后重新获取");
		thread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					while (waitingTime > 0)
					{
						java.lang.Thread.sleep(1000);
						waitingTime -= 1;
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								acquireCodeButton.setText(waitingTime + "s后重新获取");
							}
						});	
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							acquireCodeButton.setText(R.string.acquire_code);
							acquireCodeButton.setEnabled(true);	
						}
					});	
				}
			}
		});
		thread.start();
		
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
							Utils.showToast(PhoneSignUpActivity.this, "验证短信已发送");
						}
					});
				}
				else 
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							Utils.showToast(PhoneSignUpActivity.this, "注册失败！"+response.getErrorMessage());
						}
					});
				}
			}
		});
    }
    
    private void getCommonInfo()
    {
		SignInRequest request = new SignInRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final SignInResponse response = new SignInResponse(httpResponse);				
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
							ReimApplication.dismissProgressDialog();
							Builder builder = new Builder(PhoneSignUpActivity.this);
							builder.setTitle(R.string.tip);
							builder.setMessage(R.string.prompt_sign_up_succeed);
							builder.setNegativeButton(R.string.confirm, new OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															waitingTime = -1;
															startActivity(new Intent(PhoneSignUpActivity.this, MainActivity.class));
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
							ReimApplication.dismissProgressDialog();
							Utils.showToast(PhoneSignUpActivity.this, "获取信息失败！"+response.getErrorMessage());
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
		imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(confirmPasswordEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(codeEditText.getWindowToken(), 0);
    }
}