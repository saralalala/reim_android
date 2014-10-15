package com.rushucloud.reim.me;

import com.rushucloud.reim.R;
import classes.Adapter.ProfileListViewAdapater;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class ProfileActivity extends Activity
{
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		viewInitialise();
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
		getMenuInflater().inflate(R.menu.single_item, menu);
		MenuItem item = menu.getItem(0);
		item.setTitle(getResources().getString(R.string.save));
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_item)
		{
			// TODO SAVE DETAILS
			Toast.makeText(ProfileActivity.this, "保存", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void viewInitialise()
	{
		try
		{
			ProfileListViewAdapater adapter = new ProfileListViewAdapater(ProfileActivity.this);
			ListView profileListView = (ListView)findViewById(R.id.profileListView);
			profileListView.setAdapter(adapter);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
