package com.rushucloud.reim.item;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import classes.Item;
import classes.Report;
import classes.adapter.ItemListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import netUtils.HttpConnectionCallback;
import netUtils.Request.Item.SearchItemsRequest;
import netUtils.Response.Item.SearchItemsResponse;

public class SearchItemActivity extends Activity
{
	private SearchView searchView;
	private ItemListViewAdapter adapter;
	
	private List<Item> itemList = null;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reim_search);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("SearchItemActivity");		
		MobclickAgent.onResume(this);
		initData();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("SearchItemActivity");
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
		getMenuInflater().inflate(R.menu.searchview, menu);
		MenuItem menuItem = menu.getItem(0);
		menuItem.expandActionView();
		searchView = (SearchView)MenuItemCompat.getActionView(menuItem);
		searchView.setQueryHint(getString(R.string.input_keyword));
		searchView.setOnQueryTextListener(new OnQueryTextListener()
		{
			public boolean onQueryTextSubmit(String query)
			{
				MobclickAgent.onEvent(SearchItemActivity.this, "UMENG_SEARCH");
				if (PhoneUtils.isNetworkConnected())
				{
					sendSearchItemsRequest(query);
				}
				else
				{
					ViewUtils.showToast(SearchItemActivity.this, R.string.error_search_network_unavailable);
				}
				return true;
			}
			
			public boolean onQueryTextChange(String newText)
			{
				adapter.getFilter().filter(newText);
				return true;
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
	
	private void initData()
	{
		DBManager dbManager = DBManager.getDBManager();
		itemList = dbManager.getUserItems(AppPreference.getAppPreference().getCurrentUserID());
	}
	
	private void initView()
	{
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);		
		actionBar.setBackgroundDrawable(new ColorDrawable(ViewUtils.getColor(R.color.major_dark)));
		
		adapter = new ItemListViewAdapter(this, itemList);
		ListView resultListView = (ListView) findViewById(R.id.resultListView);
		resultListView.setAdapter(adapter);
		resultListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Item item = itemList.get(position);
				if (item.getBelongReport() == null || item.getBelongReport().isEditable())
				{
					Intent intent = new Intent(SearchItemActivity.this, EditItemActivity.class);
					intent.putExtra("itemLocalID", item.getLocalID());
					startActivity(intent);
				}
				else
				{
					Intent intent = new Intent(SearchItemActivity.this, ShowItemActivity.class);
					intent.putExtra("itemLocalID", item.getLocalID());
					startActivity(intent);
				}
			}
		});
	}
	
	private void sendSearchItemsRequest(String query)
	{
		SearchItemsRequest request = new SearchItemsRequest(query);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				SearchItemsResponse response = new SearchItemsResponse(httpResponse);
				if (response.getStatus())
				{
					itemList.clear();
					itemList.addAll(response.getItemList());
					
					DBManager dbManager = DBManager.getDBManager(); 
					for (Item item : itemList)
					{
						int reportServerID = item.getBelongReport().getServerID();
						Report report = dbManager.getReportByServerID(reportServerID);
						item.setBelongReport(report);
						dbManager.syncItem(item);
						Item localItem = dbManager.getItemByServerID(item.getServerID());
						item.setLocalID(localItem.getLocalID());
					}
					
					adapter.set(itemList);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							adapter.notifyDataSetChanged();
						}
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ViewUtils.showToast(SearchItemActivity.this, R.string.failed_to_search);
						}
					});
				}
			}
		});
	}
}
