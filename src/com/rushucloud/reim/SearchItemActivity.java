package com.rushucloud.reim;

import java.util.List;

import com.umeng.analytics.MobclickAgent;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Item.SearchItemsRequest;
import netUtils.Response.Item.SearchItemsResponse;

import classes.AppPreference;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.Utils;
import classes.Adapter.ItemListViewAdapter;
import database.DBManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;
import android.support.v4.view.MenuItemCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SearchItemActivity extends Activity
{
	private SearchView searchView;
	private ItemListViewAdapter adapter;
	
	private List<Item> itemList = null;
	private boolean fromReim;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_search);
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
			goBack();
		}
		return super.onKeyDown(keyCode, event);
	}

	@SuppressLint("NewApi") 
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.searchview, menu);
		MenuItem menuItem = menu.getItem(0);
		menuItem.expandActionView();
		searchView = (SearchView)MenuItemCompat.getActionView(menuItem);
		searchView.setQueryHint(getString(R.string.inputKeyword));
		searchView.setOnQueryTextListener(new OnQueryTextListener()
		{
			public boolean onQueryTextSubmit(String query)
			{
				MobclickAgent.onEvent(SearchItemActivity.this, "UMENG_SEARCH");
				if (Utils.isNetworkConnected())
				{
					sendSearchItemsRequest(query);
				}
				else
				{
					Toast.makeText(SearchItemActivity.this, "网络未连接，无法联网查找", Toast.LENGTH_SHORT).show();
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
			goBack();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initData()
	{
		DBManager dbManager = DBManager.getDBManager();
		itemList = dbManager.getUserItems(AppPreference.getAppPreference().getCurrentUserID());
		fromReim = getIntent().getBooleanExtra("fromReim", false);
	}
	
	private void initView()
	{
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		adapter = new ItemListViewAdapter(this, itemList);
		ListView resultListView = (ListView)findViewById(R.id.resultListView);
		resultListView.setAdapter(adapter);
		resultListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Item item = itemList.get(position);
				if (item.getBelongReport() == null
						|| item.getBelongReport().getStatus() == Report.STATUS_DRAFT
						|| item.getBelongReport().getStatus() == Report.STATUS_REJECTED)
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
							AlertDialog alertDialog = new AlertDialog.Builder(SearchItemActivity.this)
														.setTitle("错误")
														.setMessage("网络搜索失败")
														.setPositiveButton("确定", null)
														.create();
							alertDialog.show();
						}
					});
				}
			}
		});
	}
	
	private void goBack()
	{
		if (fromReim)
		{
	    	ReimApplication.setTabIndex(0);
	    	Intent intent = new Intent(SearchItemActivity.this, MainActivity.class);
	    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(intent);
	    	finish();
		}
		else
		{
			finish();
		}
	}
}
