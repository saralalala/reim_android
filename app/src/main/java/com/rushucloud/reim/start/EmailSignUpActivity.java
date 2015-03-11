package com.rushucloud.reim.start;

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

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.Request.User.RegisterRequest;
import netUtils.Request.User.SignInRequest;
import netUtils.Response.User.RegisterResponse;
import netUtils.Response.User.SignInResponse;

public class EmailSignUpActivity extends Activity
{
	private EditText emailEditText;
	private EditText passwordEditText;
	private EditText confirmPasswordEditText;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_sign_up_by_email);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EmailSignUpActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("EmailSignUpActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			startActivity(new Intent(EmailSignUpActivity.this, WelcomeActivity.class));
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
				startActivity(new Intent(EmailSignUpActivity.this, WelcomeActivity.class));
				finish();
			}
		});

		emailEditText = (EditText) findViewById(R.id.emailEditText);
    	emailEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
    	
		passwordEditText = (EditText) findViewById(R.id.passwordEditText);
		passwordEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		
		confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
		confirmPasswordEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);

		Button signUpButton = (Button) findViewById(R.id.signUpButton);
		signUpButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(EmailSignUpActivity.this, "UMENG_REGIST_MAIL-SUBMIT");
				
				hideSoftKeyboard();
				
				String email = emailEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				String confirmPassword = confirmPasswordEditText.getText().toString();
				
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(EmailSignUpActivity.this, R.string.error_request_network_unavailable);
				}
				else if (email.isEmpty())
				{
					ViewUtils.showToast(EmailSignUpActivity.this, R.string.error_email_empty);
					emailEditText.requestFocus();
				}
				else if (password.isEmpty())
				{
					ViewUtils.showToast(EmailSignUpActivity.this, R.string.error_password_empty);
					passwordEditText.requestFocus();
				}
				else if (confirmPassword.isEmpty())
				{
					ViewUtils.showToast(EmailSignUpActivity.this, R.string.error_confirm_password_empty);
					confirmPasswordEditText.requestFocus();
				}
				else if (!password.equals(confirmPassword))
				{
					ViewUtils.showToast(EmailSignUpActivity.this, R.string.error_wrong_confirm_password);
					confirmPasswordEditText.requestFocus();
				}
				else
				{
					User user = new User();
					user.setEmail(email);
					user.setPassword(password);
					
					sendRegisterRequest(user);
				}
			}
		});
		
    	RelativeLayout baseLayout=(RelativeLayout) findViewById(R.id.baseLayout);
    	baseLayout.setOnClickListener(new View.OnClickListener()
    	{
			public void onClick(View v)
			{
				hideSoftKeyboard();				
			}
		}); 
	}
	
	private void sendRegisterRequest(final User user)
	{
		ReimProgressDialog.show();
		RegisterRequest request = new RegisterRequest(user, "");
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
							ViewUtils.showToast(EmailSignUpActivity.this, R.string.succeed_in_signing_up);
						}
					});
					
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setUsername(user.getEmail());	
					appPreference.setPassword(user.getPassword());
					appPreference.setServerToken(response.getServerToken());
					appPreference.setCurrentUserID(response.getUserID());
					appPreference.setCurrentGroupID(-1);
					appPreference.saveAppPreference();
					
					sendGetCommonInfoRequest();
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EmailSignUpActivity.this, R.string.failed_to_sign_up, response.getErrorMessage());
						}
					});		
				}
			}
		});		
	}
    
    private void sendGetCommonInfoRequest()
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
							Builder builder = new Builder(EmailSignUpActivity.this);
							builder.setTitle(R.string.tip);
							builder.setMessage(R.string.prompt_sign_up_succeed_with_email);
							builder.setNegativeButton(R.string.confirm, new OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															startActivity(new Intent(EmailSignUpActivity.this, MainActivity.class));
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
							ViewUtils.showToast(EmailSignUpActivity.this, R.string.failed_to_get_data);
							Intent intent = new Intent(EmailSignUpActivity.this, SignInActivity.class);
							intent.putExtra("username", AppPreference.getAppPreference().getUsername());
							intent.putExtra("password", AppPreference.getAppPreference().getPassword());
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
		imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);  
		imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);    	
    }
}