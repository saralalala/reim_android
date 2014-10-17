package com.rushucloud.reim.me;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.Adapter.ProfileListViewAdapater;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class InviteActivity extends Activity
{
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		viewInitialise();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ProfileActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ProfileActivity");
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

	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.save, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_save_item)
		{
			// TODO SAVE DETAILS
			Toast.makeText(InviteActivity.this, "保存", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void viewInitialise()
	{
		try
		{
			ProfileListViewAdapater adapter = new ProfileListViewAdapater(InviteActivity.this);
			ListView profileListView = (ListView)findViewById(R.id.profileListView);
			profileListView.setAdapter(adapter);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
