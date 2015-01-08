package com.rushucloud.reim.start;

import classes.Utils.DBManager;
import classes.Utils.Utils;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;


import android.app.Activity;
import android.content.Intent;
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
		setContentView(R.layout.activity_start_welcome);
		
		exitTime=0;
		initView();
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
	
	private void initView()
	{
		getActionBar().hide();
		
		Button signInButton = (Button)findViewById(R.id.signInButton);
		signInButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
				finish();
			}			
		});
		signInButton = Utils.resizeLongButton(signInButton);

		final int buttonHeight = signInButton.getLayoutParams().height;
		
		final Button phoneSignUpButton = (Button)findViewById(R.id.phoneSignUpButton);
		phoneSignUpButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(WelcomeActivity.this, PhoneSignUpActivity.class));
				finish();
			}			
		});
		phoneSignUpButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				ViewGroup.LayoutParams params = phoneSignUpButton.getLayoutParams();
				params.height = buttonHeight;
				phoneSignUpButton.setLayoutParams(params);
			}
		});
		
		final Button emailSignUpButton = (Button)findViewById(R.id.emailSignUpButton);
		emailSignUpButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(WelcomeActivity.this, EmailSignUpActivity.class));
				finish();
			}			
		});
		emailSignUpButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			public void onGlobalLayout()
			{
				ViewGroup.LayoutParams params = emailSignUpButton.getLayoutParams();
				params.height = buttonHeight;
				emailSignUpButton.setLayoutParams(params);
			}
		});
	}
}