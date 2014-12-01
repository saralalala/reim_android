package com.rushucloud.reim.me;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.ReimApplication;
import classes.Adapter.SettingsListViewAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class SettingsActivity extends Activity
{	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.me_settings);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("SettingsActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("SettingsActivity");
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
		ReimApplication.setProgressDialog(this);

		getActionBar().hide();

        View divider = getLayoutInflater().inflate(R.layout.list_divider, null);
        
		SettingsListViewAdapter settingsAdapter = new SettingsListViewAdapter(this);
		ListView settingsListView = (ListView)findViewById(R.id.settingsListView);
		settingsListView.addHeaderView(divider);
		settingsListView.setAdapter(settingsAdapter);
		settingsListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				switch (position - 1)
				{
					case 0:
						startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
						break;
					case 1:
						MobclickAgent.onEvent(SettingsActivity.this, "UMENG_MINE_SETTING_FEEDBACK");
						startActivity(new Intent(SettingsActivity.this, FeedbackActivity.class));
						break;
					default:
						break;
				}
			}
		});
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
	}
}
