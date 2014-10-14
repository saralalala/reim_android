package com.rushucloud.reim;

import classes.Adapter.ItemListViewAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.support.v4.view.MenuItemCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class SearchItemActivity extends Activity
{
	private SearchView searchView;
	private ItemListViewAdapter adapter;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_search);
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
		getMenuInflater().inflate(R.menu.searchview, menu);
		MenuItem menuItem = menu.getItem(0);
		searchView = (SearchView)MenuItemCompat.getActionView(menuItem);
		searchView.setQueryHint(getString(R.string.inputKeyword));
		searchView.setOnQueryTextListener(new OnQueryTextListener()
		{
			public boolean onQueryTextSubmit(String query)
			{
				// TODO get from server
				return false;
			}
			
			public boolean onQueryTextChange(String newText)
			{
				// TODO filter local
				return false;
			}
		});
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == android.R.id.home)
		{
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void viewInitialise()
	{
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
}
