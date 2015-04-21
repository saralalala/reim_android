package com.rushucloud.reim.start;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rushucloud.reim.R;
import com.rushucloud.reim.guide.GuideStartActivity;
import com.umeng.analytics.MobclickAgent;

import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ClearEditText;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.user.RegisterRequest;
import netUtils.request.user.SignInRequest;
import netUtils.response.user.RegisterResponse;
import netUtils.response.user.SignInResponse;

public class EmailSignUpActivity extends Activity
{
	private ClearEditText emailEditText;
	private ClearEditText passwordEditText;
	private ClearEditText confirmPasswordEditText;
	
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
		ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("EmailSignUpActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
            goBack();
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
                goBack();
			}
		});

		emailEditText = (ClearEditText) findViewById(R.id.emailEditText);

		passwordEditText = (ClearEditText) findViewById(R.id.passwordEditText);
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

		confirmPasswordEditText = (ClearEditText) findViewById(R.id.confirmPasswordEditText);
        confirmPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
        confirmPasswordEditText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    signUp();
                }
                return false;
            }
        });

		Button signUpButton = (Button) findViewById(R.id.signUpButton);
		signUpButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
                signUp();
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

    private void signUp()
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
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setUsername(user.getEmail());	
					appPreference.setPassword(user.getPassword());
					appPreference.setServerToken(response.getServerToken());
					appPreference.setCurrentUserID(response.getUserID());
					appPreference.setCurrentGroupID(-1);
					appPreference.saveAppPreference();
					
					sendSignInRequest();
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
    
    private void sendSignInRequest()
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

                    // update tags
                    dbManager.updateGroupTags(response.getTagList(), -1);
					
					// refresh UI
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
                            ViewUtils.showToast(EmailSignUpActivity.this, R.string.succeed_in_sign_up_with_email);
                            ViewUtils.goForwardAndFinish(EmailSignUpActivity.this, GuideStartActivity.class);
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

    private void goBack()
    {
        ViewUtils.goBackWithIntent(EmailSignUpActivity.this, WelcomeActivity.class);
    }
}