package com.rushucloud.reim.me;

import classes.AppPreference;
import classes.Group;
import classes.ReimApplication;
import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class InvoiceTitleActivity extends Activity
{	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_invoice_title);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("InvoiceTitleActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("InvoiceTitleActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			goBackToMainActivity();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initView()
	{
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_right);
		
		TextView promptTextView = (TextView)findViewById(R.id.promptTextView);
		promptTextView.setText(R.string.getInvoicePrompt);
		promptTextView.setAnimation(animation);
		
		animation = AnimationUtils.loadAnimation(this, R.anim.rotate_right_center);
		Group group = DBManager.getDBManager().getGroup(AppPreference.getAppPreference().getCurrentGroupID());
		TextView titleTextView = (TextView)findViewById(R.id.titleTextView);
		titleTextView.setText(group.getName());
		titleTextView.setAnimation(animation);
	}

    private void goBackToMainActivity()
    {
    	ReimApplication.setTabIndex(3);
    	Intent intent = new Intent(InvoiceTitleActivity.this, MainActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    	finish();
    }
}
