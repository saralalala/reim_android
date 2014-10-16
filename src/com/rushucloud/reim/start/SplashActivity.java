package com.rushucloud.reim.start;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity
{
	private int splashTime = 1000;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_splash);
		
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
					startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
					finish();
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
}
