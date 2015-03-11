package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rushucloud.reim.R;
import com.rushucloud.reim.start.SignInActivity;
import com.umeng.analytics.MobclickAgent;

import classes.ReimApplication;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.Request.User.SignOutRequest;
import netUtils.Response.User.SignOutResponse;

public class SettingsActivity extends Activity
{	
	private RelativeLayout categoryLayout;
	private RelativeLayout tagLayout;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_settings);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("SettingsActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
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
				MobclickAgent.onEvent(SettingsActivity.this, "UMENG_MINE_SETTING_ABOUT");
				startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
			}
		});
        
        RelativeLayout feedbackLayout = (RelativeLayout) findViewById(R.id.feedbackLayout);
        feedbackLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(SettingsActivity.this, "UMENG_MINE_SETTING_OPINION");
				startActivity(new Intent(SettingsActivity.this, FeedbackActivity.class));
			}
		});
		
        categoryLayout = (RelativeLayout) findViewById(R.id.categoryLayout);
        categoryLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(SettingsActivity.this, "UMENG_MINE_CATEGORT_SETTING");
				startActivity(new Intent(SettingsActivity.this, CategoryActivity.class));
			}
		});
        
        tagLayout = (RelativeLayout) findViewById(R.id.tagLayout);
        tagLayout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(SettingsActivity.this, "UMENG_MINE_TAG_SETTING");
				startActivity(new Intent(SettingsActivity.this, TagActivity.class));
			}
		});
		
        User currentUser = AppPreference.getAppPreference().getCurrentUser();
        if (!currentUser.isAdmin() || currentUser.getGroupID() <= 0)
		{
        	categoryLayout.setVisibility(View.GONE);
			tagLayout.setVisibility(View.GONE);
		}
        else
        {
			categoryLayout.setVisibility(View.VISIBLE);
			tagLayout.setVisibility(View.VISIBLE);
        }
        
        Button signOutButton = (Button) findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (PhoneUtils.isNetworkConnected())
				{
					sendSignOutRequest();							
				}
				else
				{
					ViewUtils.showToast(SettingsActivity.this, R.string.error_sign_out_network_unavailable);							
				}
			}
		});
	}
	
	private void sendSignOutRequest()
	{
		ReimProgressDialog.show();
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
							ReimProgressDialog.dismiss();
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
							ReimProgressDialog.dismiss();
							ViewUtils.showToast(SettingsActivity.this, R.string.failed_to_sign_out);
						}
					});
				}
			}
		});
	}
}