package com.rushucloud.reim.me;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.Group;
import classes.utils.AppPreference;

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

		Group group = AppPreference.getAppPreference().getCurrentGroup();
		TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
		String title = group == null ? getString(R.string.invoice_invalid) : group.getName();
		titleTextView.setText(title);
	}
}
