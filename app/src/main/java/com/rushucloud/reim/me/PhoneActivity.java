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

public class PhoneActivity extends Activity
{
	private EditText phoneEditText;

	private User currentUser;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_phone);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("PhoneActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("PhoneActivity");
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
				
				String originalPhone = currentUser.getPhone();
				String newPhone = phoneEditText.getText().toString();
				if (PhoneUtils.isNetworkConnected())
				{
					if (newPhone.equals(originalPhone))
					{
						finish();
					}
					else if (newPhone.isEmpty() && currentUser.getEmail().isEmpty())
					{
						ViewUtils.showToast(PhoneActivity.this, R.string.error_new_phone_empty);
					}
					else if (!newPhone.isEmpty() && !Utils.isPhone(newPhone))
					{
						ViewUtils.showToast(PhoneActivity.this, R.string.error_phone_wrong_format);
					}
					else
					{
						currentUser.setPhone(newPhone);
						sendModifyUserInfoRequest();						
					}
				}
				else
				{
					ViewUtils.showToast(PhoneActivity.this, R.string.error_modify_network_unavailable);						
				}
			}
		});
		
		phoneEditText = (EditText) findViewById(R.id.phoneEditText);
		phoneEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		phoneEditText.setText(currentUser.getPhone());

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
							ViewUtils.showToast(PhoneActivity.this, R.string.succeed_in_modifying_user_info);
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
							ViewUtils.showToast(PhoneActivity.this, R.string.failed_to_modify_user_info, response.getErrorMessage());
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
	}
}
