package com.rushucloud.reim.start;

import netUtils.HttpConnectionCallback;
import netUtils.Request.CommonRequest;
import netUtils.Response.CommonResponse;
import classes.AppPreference;
import classes.User;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity
{
	private int splashTime = 2000;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_splash);
		
		if (!AppPreference.getAppPreference().getUsername().equals(""))
		{
			sendCommonRequest();			
		}
		
		Thread splashThread = new Thread()
		{
			public void run()
			{
				try
				{
					int waitingTime = 0;
					while (waitingTime < splashTime)
					{
						sleep(100);
						waitingTime += 100;						
					}
				}
				catch (Exception e)
				{
					System.out.println(e);
				}
				finally
				{
					if (!AppPreference.getAppPreference().getUsername().equals(""))
					{
						startActivity(new Intent(SplashActivity.this, MainActivity.class));
						finish();			
					}
					else
					{
						startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
						finish();						
					}
				}
			}
		};
		splashThread.start();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("SplashActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("SplashActivity");
		MobclickAgent.onPause(this);
	}
	
	private void sendCommonRequest()
	{
		CommonRequest request = new CommonRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CommonResponse response = new CommonResponse(httpResponse);				
				if (response.getStatus())
				{
					int currentUserID = response.getCurrentUser().getServerID();
					int currentGroupID = -1;

					DBManager dbManager = DBManager.getDBManager();
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setServerToken(response.getServerToken());
					appPreference.setCurrentUserID(currentUserID);
					appPreference.setSyncOnlyWithWifi(true);
					appPreference.setEnablePasswordProtection(true);
					
					if (response.getGroup() != null)
					{
						currentGroupID = response.getGroup().getServerID();
						
						// update AppPreference
						appPreference.setCurrentGroupID(currentGroupID);
						appPreference.saveAppPreference();
						
						// update members
						dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);
						
						User localUser = dbManager.getUser(response.getCurrentUser().getServerID());						
						if (localUser.getServerUpdatedDate() == response.getCurrentUser().getServerUpdatedDate())
						{
							if (localUser.getAvatarPath().equals(""))
							{
								dbManager.updateUser(response.getCurrentUser());								
							}
						}
						else
						{
							dbManager.syncUser(response.getCurrentUser())	;
						}
						
						// update categories
						dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);
						
						// update tags
						dbManager.updateGroupTags(response.getTagList(), currentGroupID);
						
						// update group info
						dbManager.syncGroup(response.getGroup());
					}
					else
					{						
						// update AppPreference
						appPreference.setCurrentGroupID(currentGroupID);
						appPreference.saveAppPreference();
						
						// update current user
						dbManager.syncUser(response.getCurrentUser());
						
						// update categories
						dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);						
					}
				}
			}
		});		
	}
}
