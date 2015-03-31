package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import classes.Group;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.request.group.CreateGroupRequest;
import netUtils.request.group.ModifyGroupRequest;
import netUtils.response.group.CreateGroupResponse;
import netUtils.response.group.ModifyGroupResponse;

public class CompanyActivity extends Activity
{
	private EditText companyEditText;

    private AppPreference appPreference;
    private DBManager dbManager;
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
		ReimProgressDialog.setContext(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("CompanyActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initData()
	{
        appPreference = AppPreference.getAppPreference();
        dbManager = DBManager.getDBManager();
		currentGroup = appPreference.getCurrentGroup();
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
				
				String originalName = "";
                if (currentGroup != null)
                {
                    originalName = currentGroup.getName();
                }
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
                    if (currentGroup == null)
                    {
                        sendCreateGroupRequest(newName);
                    }
                    else
                    {
                        sendModifyGroupRequest(newName);
                    }
				}
			}
		});
		
		companyEditText = (EditText) findViewById(R.id.companyEditText);
		companyEditText.setOnFocusChangeListener(ViewUtils.onFocusChangeListener);
        if (currentGroup != null)
        {
            companyEditText.setText(currentGroup.getName());
        }

        LinearLayout baseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        baseLayout.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
			}
		});        
	}

    private void sendCreateGroupRequest(final String newName)
    {
        CreateGroupRequest request = new CreateGroupRequest(newName);
        request.sendRequest(new HttpConnectionCallback()
        {
            public void execute(Object httpResponse)
            {
                final CreateGroupResponse response = new CreateGroupResponse(httpResponse);
                if (response.getStatus())
                {
                    Group group = new Group();
                    group.setName(newName);
                    group.setServerID(response.getGroupID());
                    group.setLocalUpdatedDate(response.getDate());
                    group.setServerUpdatedDate(response.getDate());

                    User currentUser = appPreference.getCurrentUser();
                    currentUser.setGroupID(group.getServerID());

                    dbManager.insertGroup(group);
                    dbManager.updateUser(currentUser);
                    appPreference.setCurrentGroupID(group.getServerID());
                    appPreference.saveAppPreference();

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            ReimProgressDialog.dismiss();
                            ViewUtils.showToast(CompanyActivity.this, R.string.succeed_in_creating_company);
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
                            ViewUtils.showToast(CompanyActivity.this, R.string.failed_to_create_company, response.getErrorMessage());
                        }
                    });
                }
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
				final ModifyGroupResponse response = new ModifyGroupResponse(httpResponse);
				if (response.getStatus())
				{
					currentGroup.setName(newName);
					dbManager.updateGroup(currentGroup);
					
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
							ViewUtils.showToast(CompanyActivity.this, R.string.failed_to_modify, response.getErrorMessage());
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
