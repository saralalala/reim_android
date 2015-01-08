package com.rushucloud.reim.start;

import netUtils.HttpConnectionCallback;
import netUtils.Request.User.SignInRequest;
import netUtils.Response.User.SignInResponse;
import classes.User;
import classes.Utils.AppPreference;
import classes.Utils.DBManager;
import classes.Utils.Utils;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity
{
	private AppPreference appPreference;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_splash);
		
		appPreference = AppPreference.getAppPreference();
		getActionBar().hide();
		start();
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
	
	private void start()
	{
		if (appPreference.getUsername().equals(""))
		{		
			Thread splashThread = new Thread()
			{
				public void run()
				{
					try
					{
						int waitingTime = 0;
						int splashTime = 2000;
						while (waitingTime < splashTime)
						{
							sleep(100);
							waitingTime += 100;						
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
						finish();
					}
				}
			};
			splashThread.start();
		}
		else
		{
			if (Utils.isNetworkConnected())
			{
				sendSignInRequest();
			}
			else 
			{
				Thread splashThread = new Thread()
				{
					public void run()
					{
						try
						{
							int waitingTime = 0;
							int splashTime = 2000;
							while (waitingTime < splashTime)
							{
								sleep(100);
								waitingTime += 100;						
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						finally
						{
							startActivity(new Intent(SplashActivity.this, MainActivity.class));
							finish();
						}
					}
				};
				splashThread.start();
			}
		}		
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
						User currentUser = response.getCurrentUser();
						User localUser = dbManager.getUser(response.getCurrentUser().getServerID());
						if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
						{
							currentUser.setAvatarPath(localUser.getAvatarPath());
						}
						
						dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

						dbManager.syncUser(currentUser);
						
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
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							startActivity(new Intent(SplashActivity.this, MainActivity.class));
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
							Utils.showToast(SplashActivity.this, "登录失败，请稍候重试");
							Bundle bundle = new Bundle();
							bundle.putString("username", appPreference.getUsername());
							bundle.putString("password", appPreference.getPassword());
							Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
							intent.putExtras(bundle);
							startActivity(intent);
							finish();
						}
					});
				}
			}
		});		
	}
}