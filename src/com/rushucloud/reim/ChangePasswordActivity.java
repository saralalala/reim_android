package com.rushucloud.reim;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordActivity extends Activity
{
	private EditText oldPasswordEditText;
	private EditText newPasswordEditText;
	private EditText confirmPasswordEditText;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_change_password);
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
			Toast.makeText(ChangePasswordActivity.this, "保存", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void viewInitialise()
	{
		try
		{
			oldPasswordEditText = (EditText)findViewById(R.id.oldPasswordEditText);
			newPasswordEditText = (EditText)findViewById(R.id.newPasswordEditText);
			confirmPasswordEditText = (EditText)findViewById(R.id.confirmPasswordEditText);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
