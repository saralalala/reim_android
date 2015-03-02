package com.rushucloud.reim.me;

import classes.Group;
import classes.utils.AppPreference;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

public class InvoiceTitleActivity extends Activity
{	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_invoice_title);
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
	    	finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initView()
	{
		getActionBar().hide();
		
//		Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_right);
//		
//		TextView promptTextView = (TextView) findViewById(R.id.promptTextView);
//		promptTextView.setText(R.string.getInvoicePrompt);
//		promptTextView.setAnimation(animation);
//		
//		animation = AnimationUtils.loadAnimation(this, R.anim.rotate_right_center);
		Group group = AppPreference.getAppPreference().getCurrentGroup();
		TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
		String title = group == null ? getString(R.string.invoice_invalid) : group.getName();
		titleTextView.setText(title);
//		titleTextView.setAnimation(animation);
	}
}
