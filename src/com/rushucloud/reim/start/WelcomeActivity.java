package com.rushucloud.reim.start;

import classes.Utils;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;

public class WelcomeActivity extends Activity
{
	private long exitTime;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_welcome);
		
		exitTime=0;
		initButton();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("WelcomActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("WelcomActivity");
		MobclickAgent.onPause(this);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {
        	if(System.currentTimeMillis()-exitTime>2000)
        	{
        		Utils.showToast(WelcomeActivity.this, "再按一次返回键退出程序");
        		exitTime=System.currentTimeMillis();
        	}
        	else 
        	{
				finish();
				DBManager dbManager = DBManager.getDBManager();
				dbManager.close();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
            return true;
        } 
        else 
        {
            return super.onKeyDown(keyCode, event);
        }
    }
	
	private void initButton()
	{
		final Button signUpButton = (Button)findViewById(R.id.signUpButton);
		signUpButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class));
				finish();
			}			
		});
		signUpButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_long_dark);
				double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
				ViewGroup.LayoutParams params = signUpButton.getLayoutParams();
				params.height = (int)(signUpButton.getWidth() * ratio);;
				signUpButton.setLayoutParams(params);
			}
		});
		
		final Button signInButton = (Button)findViewById(R.id.signInButton);
		signInButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
				finish();
			}			
		});
		signInButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_long_light);
				double ratio = ((double)bitmap.getHeight()) / bitmap.getWidth();
				ViewGroup.LayoutParams params = signInButton.getLayoutParams();
				params.height = (int)(signInButton.getWidth() * ratio);;
				signInButton.setLayoutParams(params);
			}
		});
	}
}
