package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.user.ModifyUserRequest;
import netUtils.response.user.ModifyUserResponse;

public class EmailActivity extends Activity
{
	private EditText emailEditText;

	private User currentUser;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_email);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EmailActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("EmailActivity");
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
	
	private void initData()
	{
		currentUser = AppPreference.getAppPreference().getCurrentUser();
	}
	
	private void initView()
	{		
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				finish();
			}
		});
		
		TextView saveTextView = (TextView) findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				
				String originalEmail = currentUser.getEmail();
				String newEmail = emailEditText.getText().toString();
				if (PhoneUtils.isNetworkConnected())
				{	
					if (newEmail.equals(originalEmail))
					{
						finish();
					}
					else if (newEmail.isEmpty() && currentUser.getPhone().isEmpty())
					{
						ViewUtils.showToast(EmailActivity.this, R.string.error_new_email_empty);
					}
					else if (!newEmail.isEmpty() && !Utils.isEmail(newEmail))
					{
						ViewUtils.showToast(EmailActivity.this, R.string.error_email_wrong_format);
					}
					else
					{
						currentUser.setEmail(newEmail);
						sendModifyUserInfoRequest();					
					}
				}
				else
				{
					ViewUtils.showToast(EmailActivity.this, R.string.error_modify_network_unavailable);
				}
			}
		});
		
		emailEditText = (EditText) findViewById(R.id.emailEditText);
    	emailEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
    	emailEditText.setText(currentUser.getEmail());

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});        
	}
	
	private void sendModifyUserInfoRequest()
	{
		ReimProgressDialog.show();		
		ModifyUserRequest request = new ModifyUserRequest(currentUser);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyUserResponse response = new ModifyUserResponse(httpResponse);
				if (response.getStatus())
				{
					DBManager.getDBManager().updateUser(currentUser);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(EmailActivity.this, R.string.succeed_in_modifying_user_info);
							finish();
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
							ViewUtils.showToast(EmailActivity.this, R.string.failed_to_modify_user_info, response.getErrorMessage());
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
	}
}
