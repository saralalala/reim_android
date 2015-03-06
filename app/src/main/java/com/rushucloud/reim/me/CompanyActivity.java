package com.rushucloud.reim.me;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Group.ModifyGroupRequest;
import netUtils.Response.Group.ModifyGroupResponse;
import classes.Group;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

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

public class CompanyActivity extends Activity
{
	private EditText companyEditText;

	private Group currentGroup;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_company);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("CompanyActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("CompanyActivity");
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
		currentGroup = AppPreference.getAppPreference().getCurrentGroup();
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
				
				String originalName = currentGroup.getName();
				String newName = companyEditText.getText().toString();
				if (!PhoneUtils.isNetworkConnected())
				{
					ViewUtils.showToast(CompanyActivity.this, R.string.error_modify_network_unavailable);			
				}
				else if (newName.equals(originalName))
				{
					finish();
				}
				else if (newName.isEmpty())
				{
					ViewUtils.showToast(CompanyActivity.this, R.string.error_new_company_name_empty);
				}
				else
				{
					ReimProgressDialog.show();
					sendModifyGroupRequest(newName);
				}
			}
		});
		
		companyEditText = (EditText) findViewById(R.id.nicknameEditText);
		companyEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
		companyEditText.setText(currentGroup.getName());

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});        
	}
	
	private void sendModifyGroupRequest(final String newName)
	{
		ModifyGroupRequest request = new ModifyGroupRequest(newName);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				ModifyGroupResponse response = new ModifyGroupResponse(httpResponse);
				if (response.getStatus())
				{
					currentGroup.setName(newName);
					DBManager.getDBManager().updateGroup(currentGroup);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(CompanyActivity.this, R.string.succeed_in_modifying);
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
							ViewUtils.showToast(CompanyActivity.this, R.string.failed_to_modify);
						}
					});
				}
			}
		});
	}

	private void hideSoftKeyboard()
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(companyEditText.getWindowToken(), 0);
	}
}
