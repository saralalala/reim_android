package com.rushucloud.reim;

import java.util.List;

import android.graphics.Color;
import classes.AppPreference;
import classes.Item;
import classes.Report;
import classes.Adapter.ChooseItemListViewAdapter;
import classes.Adapter.ItemListViewAdapter;
import database.DBManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class UnarchivedItemsActivity extends Activity
{
	private static DBManager dbManager;
	
	private ListView itemListView;
	private ChooseItemListViewAdapter adapter;
	
	private List<Item> itemList;
	private int[] itemLocalIDList;
	private int[] originalitemLocalIDList;
	private Report report;
	private boolean[] checkList;
	private boolean newReport;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_unarchived_items);
		dataInitialise();
		viewInitialise();
		buttonInitialise();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void dataInitialise()
	{
		AppPreference appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		itemList = dbManager.getUnarchivedUserItems(appPreference.getCurrentUserID());
		checkList = new boolean[itemList.size()];
		for (int i = 0; i < itemList.size(); i++)
		{
			checkList[i] = false;
		}
		
		Bundle bundle = this.getIntent().getExtras();
		report = (Report)bundle.getSerializable("report");
		newReport = bundle.getBoolean("newReport");
		itemLocalIDList = bundle.getIntArray("itemLocalIDList");
	}
	
	private void viewInitialise()
	{
		adapter = new ChooseItemListViewAdapter(UnarchivedItemsActivity.this, itemList, checkList);
		itemListView = (ListView)findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				checkList[position] = checkList[position] ? false : true;
				refreshItemList();
			}
		});
	}
	
	private void buttonInitialise()
	{
		Button confirmButton = (Button)findViewById(R.id.confirmButton);
		confirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					Bundle bundle = new Bundle();
					bundle.putSerializable("report", report);
					bundle.putBoolean("newReport", newReport);
					bundle.putIntArray("itemLocalIDList", itemLocalIDList);
					Intent intent = new Intent(UnarchivedItemsActivity.this, EditReportActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Bundle bundle = new Bundle();
				bundle.putSerializable("report", report);
				Intent intent = new Intent(UnarchivedItemsActivity.this, EditReportActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
	public void refreshItemList()
	{
		adapter.setCheck(checkList);
		adapter.notifyDataSetChanged();
	}
}
