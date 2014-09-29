package com.rushucloud.reim.start;

import com.rushucloud.reim.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity
{
	private int splashTime = 1000;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_splash);
		
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
					// TODO: handle exception
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
}
