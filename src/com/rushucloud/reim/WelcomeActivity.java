package com.rushucloud.reim;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class WelcomeActivity extends Activity
{
	private long exitTime;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		exitTime=0;
		buttonInitialise();
	}
	
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) 
        {
        	if(System.currentTimeMillis()-exitTime>2000)
        	{
        		Toast.makeText(WelcomeActivity.this, "再按一次返回健退出程序", Toast.LENGTH_LONG).show();
        		exitTime=System.currentTimeMillis();
        	}
        	else 
        	{
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
            return true;
        } 
        else 
        {
            return super.onKeyDown(keyCode, event);
        }
    }
	
	private void buttonInitialise()
	{
		Button signInButton = (Button)findViewById(R.id.signInButton);
		signInButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
				finish();
			}			
		});

		Button signUpButton = (Button)findViewById(R.id.signUpButton);
		signUpButton.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class));
				finish();
			}			
		});
	}
}