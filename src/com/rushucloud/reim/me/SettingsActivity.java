package com.rushucloud.reim.me;

import netUtils.HttpConnectionCallback;
import netUtils.Request.User.SignOutRequest;
import netUtils.Response.User.SignOutResponse;

import com.rushucloud.reim.R;
import com.rushucloud.reim.start.SignInActivity;
import com.umeng.analytics.MobclickAgent;

import classes.AppPreference;
import classes.ReimApplication;
import classes.Utils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SettingsActivity extends Activity
{	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.me_settings);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("SettingsActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("SettingsActivity");
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
        
        RelativeLayout aboutLayout = (RelativeLayout) findViewById(R.id.aboutLayout);
        aboutLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
			}
		});
        
        RelativeLayout feedbackLayout = (RelativeLayout) findViewById(R.id.feedbackLayout);
        feedbackLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(SettingsActivity.this, "UMENG_MINE_SETTING_FEEDBACK");
				startActivity(new Intent(SettingsActivity.this, FeedbackActivity.class));
			}
		});
        
        Button signOutButton = (Button) findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (Utils.isNetworkConnected())
				{
					sendSignOutRequest();							
				}
				else
				{
					Utils.showToast(SettingsActivity.this, "网络未连接，无法登出");							
				}
			}
		});  
	}
	
	private void sendSignOutRequest()
	{
		ReimApplication.showProgressDialog();
		SignOutRequest request = new SignOutRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				SignOutResponse response = new SignOutResponse(httpResponse);
				if (response.getStatus())
				{
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setCurrentUserID(-1);
					appPreference.setCurrentGroupID(-1);
					appPreference.setUsername("");
					appPreference.setPassword("");
					appPreference.setServerToken("");
					appPreference.setLastSyncTime(0);
					appPreference.saveAppPreference();
					
					ReimApplication.setTabIndex(0);
					ReimApplication.setReportTabIndex(0);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{						
							ReimApplication.dismissProgressDialog();	
							Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(intent);
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
							ReimApplication.dismissProgressDialog();
							Utils.showToast(SettingsActivity.this, "登出失败");
						}
					});
				}
			}
		});
	}
}