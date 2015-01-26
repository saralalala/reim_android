package com.rushucloud.reim.start;

import netUtils.HttpConnectionCallback;
import netUtils.Response.User.SignInResponse;
import netUtils.Response.User.RegisterResponse;
import netUtils.Response.User.VerifyCodeResponse;
import netUtils.Request.User.RegisterRequest;
import netUtils.Request.User.SignInRequest;
import netUtils.Request.User.VerifyCodeRequest;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;


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
		ReimProgressDialog.setProgressDialog(this);
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
		phoneEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		passwordEditText = (EditText)findViewById(R.id.passwordEditText);
		passwordEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		confirmPasswordEditText = (EditText)findViewById(R.id.confirmPasswordEditText);
		confirmPasswordEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		codeEditText = (EditText)findViewById(R.id.codeEditText);	
		codeEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);

		acquireCodeButton = (Button)findViewById(R.id.acquireCodeButton);
		acquireCodeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				
				String phoneNumber = phoneEditText.getText().toString();
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_request_network_unavailable);
				}
				else if (phoneNumber.equals(""))
				{
					ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_phone_empty);
					phoneEditText.requestFocus();
				}
				else if (!Utils.isPhone(phoneNumber))
				{
					ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_phone_wrong_format);
					phoneEditText.requestFocus();
				}
				else 
				{
					getVerifyCode(phoneNumber);
				}
			}
		});
		acquireCodeButton = ViewUtils.resizeShortButton(acquireCodeButton, 32, true);
		
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
				
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_request_network_unavailable);
				}
				else if (!Utils.isPhone(phoneNumber))
				{
					ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_phone_wrong_format);
					phoneEditText.requestFocus();		
				}
				else if (phoneNumber.equals(""))
				{
					ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_phone_empty);
					phoneEditText.requestFocus();		
				}
				else if (password.equals(""))
				{
					ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_password_empty);
					passwordEditText.requestFocus();	
				}
				else if (confirmPassword.equals(""))
				{
					ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_confirm_password_empty);
					confirmPasswordEditText.requestFocus();
				}
				else if (!password.equals(confirmPassword))
				{
					ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_wrong_confirm_password);
					confirmPasswordEditText.requestFocus();
				}
				else if (inputCode.equals(""))
				{
					ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_code_empty);
					codeEditText.requestFocus();	
				}
				else if (!inputCode.equals(code))
				{
					ViewUtils.showToast(PhoneSignUpActivity.this, R.string.error_wrong_code);
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
		signUpButton = ViewUtils.resizeLongButton(signUpButton);
		
    	RelativeLayout baseLayout=(RelativeLayout)findViewById(R.id.baseLayout);
    	baseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});
	}
	  
    private void getVerifyCode(String phoneNumber)
    {
		waitingTime = 60;
		acquireCodeButton.setEnabled(false);
		acquireCodeButton.setText(waitingTime + "秒");
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
								acquireCodeButton.setText(waitingTime + "秒");
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
		
		ReimProgressDialog.show();
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(PhoneSignUpActivity.this, R.string.prompt_message_sent);
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
							thread.interrupt();
							ViewUtils.showToast(PhoneSignUpActivity.this, R.string.failed_to_get_code, response.getErrorMessage());
						}
					});
				}
			}
		});
    }
    
	private void sendRegisterRequest(final User user, String verifyCode)
	{
		ReimProgressDialog.show();
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
							ViewUtils.showToast(PhoneSignUpActivity.this, R.string.succeed_in_signing_up);
						}
					});
					
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setUsername(user.getPhone());	
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(PhoneSignUpActivity.this, R.string.failed_to_sign_up, response.getErrorMessage());
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
							ReimProgressDialog.dismiss();
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(PhoneSignUpActivity.this, R.string.failed_to_get_data);
							Bundle bundle = new Bundle();
							bundle.putString("username", AppPreference.getAppPreference().getUsername());
							bundle.putString("password", AppPreference.getAppPreference().getPassword());
							Intent intent = new Intent(PhoneSignUpActivity.this, SignInActivity.class);
							intent.putExtras(bundle);
							startActivity(intent);
							finish();
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