package com.rushucloud.reim.me;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Group.ModifyGroupRequest;
import netUtils.Request.User.ModifyUserRequest;
import netUtils.Response.Group.ModifyGroupResponse;
import netUtils.Response.User.ModifyUserResponse;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.AppPreference;
import classes.Group;
import classes.ReimApplication;
import classes.User;
import classes.Utils;
import database.DBManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProfileActivity extends Activity
{	
	private EditText emailEditText;
	private EditText phoneEditText;
	private EditText nicknameEditText;
	private EditText companyEditText;
	private TextView companyTextView;
	
	private RelativeLayout showCompanyLayout;
	private RelativeLayout editCompanyLayout;
	private RelativeLayout categoryLayout;
	private RelativeLayout tagLayout;

	private AppPreference appPreference;
	private DBManager dbManager;
	private User currentUser;
	private Group currentGroup;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.me_profile);
		initData();
		initView();
		loadInfoView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ProfileActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ProfileActivity");
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
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
	}	
	
	private void initView()
	{		
		getActionBar().hide();
		ReimApplication.setProgressDialog(this);
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
		
		TextView saveTextView = (TextView)findViewById(R.id.saveTextView);
		saveTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (Utils.isNetworkConnected())
				{
					sendModifyUserInfoRequest();				
				}
				else
				{
					Utils.showToast(ProfileActivity.this, "网络未连接，无法保存用户信息");
				}
			}
		});
		
		emailEditText = (EditText)findViewById(R.id.emailEditText);
		phoneEditText = (EditText)findViewById(R.id.phoneEditText);
		nicknameEditText = (EditText)findViewById(R.id.nicknameEditText);
		companyEditText = (EditText)findViewById(R.id.companyEditText);
		companyTextView = (TextView)findViewById(R.id.companyTextView);
		
        showCompanyLayout = (RelativeLayout) findViewById(R.id.showCompanyLayout);
        editCompanyLayout = (RelativeLayout) findViewById(R.id.editCompanyLayout);

        Button companyButton = (Button) findViewById(R.id.companyButton);
		companyButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				
				String originalName = currentGroup.getName();
				String newName = companyEditText.getText().toString();
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(ProfileActivity.this, "网络未连接，无法修改");			
				}
				else if (newName.equals(originalName))
				{
					Utils.showToast(ProfileActivity.this, "名称与原有相同，无需修改");
				}
				else if (newName.equals(""))
				{
					Utils.showToast(ProfileActivity.this, "新名称不可为空");
				}
				else
				{
					ReimApplication.showProgressDialog();
					sendModifyGroupRequest(newName);
				}
			}
		});
        
        RelativeLayout passwordLayout = (RelativeLayout) findViewById(R.id.passwordLayout);
        passwordLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
			}
		});
        
        categoryLayout = (RelativeLayout) findViewById(R.id.categoryLayout);
        categoryLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(ProfileActivity.this, "UMENG_MINE_CATEGORT_SETTING");
				startActivity(new Intent(ProfileActivity.this, CategoryActivity.class));
			}
		});
        
        tagLayout = (RelativeLayout) findViewById(R.id.tagLayout);
        tagLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(ProfileActivity.this, "UMENG_MINE_TAG_SETTING");
				startActivity(new Intent(ProfileActivity.this, TagActivity.class));
			}
		});
		
	}

	private void loadInfoView()
	{
		currentUser = appPreference.getCurrentUser();
		currentGroup = dbManager.getGroup(appPreference.getCurrentGroupID());
		
		emailEditText.setText(currentUser.getEmail());
		phoneEditText.setText(currentUser.getPhone());
		nicknameEditText.setText(currentUser.getNickname());
		
		String companyName = currentGroup != null ? currentGroup.getName() : getString(R.string.not_available);	
		
        if (!currentUser.isAdmin() || currentUser.getGroupID() <= 0)
		{
			editCompanyLayout.setVisibility(View.GONE);       
			showCompanyLayout.setVisibility(View.VISIBLE);	
			
			companyTextView.setText(companyName);

			categoryLayout.setVisibility(View.GONE);
			tagLayout.setVisibility(View.GONE);
		}
        else
        {
			showCompanyLayout.setVisibility(View.GONE);
			editCompanyLayout.setVisibility(View.VISIBLE);
			
			companyEditText.setText(companyName);

			categoryLayout.setVisibility(View.VISIBLE);
			tagLayout.setVisibility(View.VISIBLE);
        }
	}
	
	private void sendModifyUserInfoRequest()
	{
		hideSoftKeyboard();
		
		String email = emailEditText.getText().toString();
		String phone = phoneEditText.getText().toString();;
		String nickname = nicknameEditText.getText().toString();;
		
		if (email.equals("") && phone.equals(""))
		{
			Utils.showToast(this, "邮箱和手机号不能同时为空");
		}
		else
		{
			ReimApplication.showProgressDialog();
			currentUser.setEmail(email);
			currentUser.setPhone(phone);
			currentUser.setNickname(nickname);
			
			ModifyUserRequest request = new ModifyUserRequest(currentUser);
			request.sendRequest(new HttpConnectionCallback()
			{
				public void execute(Object httpResponse)
				{
					ModifyUserResponse response = new ModifyUserResponse(httpResponse);
					if (response.getStatus())
					{
						dbManager.updateUser(currentUser);
						
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								ReimApplication.dismissProgressDialog();
								Utils.showToast(ProfileActivity.this, "用户信息修改成功");
								loadInfoView();
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
								Utils.showToast(ProfileActivity.this, "用户信息修改失败");
								loadInfoView();
							}
						});						
					}
				}
			});
		}
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
					dbManager.updateGroup(currentGroup);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(ProfileActivity.this, "修改成功");
						}
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.showProgressDialog();
							Utils.showToast(ProfileActivity.this, "修改失败");
						}
					});
				}
			}
		});
	}

	private void hideSoftKeyboard()
	{
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(phoneEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(nicknameEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(companyEditText.getWindowToken(), 0);
	}
}