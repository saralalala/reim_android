package com.rushucloud.reim.start;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.utils.DBManager;
import classes.utils.ViewUtils;

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
		MobclickAgent.onPageStart("WelcomeActivity");
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("WelcomeActivity");
		MobclickAgent.onPause(this);
	}

	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {
        	if(System.currentTimeMillis() - exitTime > 2000)
        	{
        		ViewUtils.showToast(WelcomeActivity.this, R.string.prompt_press_back_to_exit);
        		exitTime = System.currentTimeMillis();
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
		
		Button signInButton = (Button) findViewById(R.id.signInButton);
		signInButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
                ViewUtils.goForwardAndFinish(WelcomeActivity.this, SignInActivity.class);
			}
		});
		
		Button phoneSignUpButton = (Button) findViewById(R.id.phoneSignUpButton);
		phoneSignUpButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForwardAndFinish(WelcomeActivity.this, PhoneSignUpActivity.class);
            }
        });
		
		Button emailSignUpButton = (Button) findViewById(R.id.emailSignUpButton);
		emailSignUpButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ViewUtils.goForwardAndFinish(WelcomeActivity.this, EmailSignUpActivity.class);
            }
        });
	}
}