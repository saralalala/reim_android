package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import classes.Item;
import classes.Report;
import classes.Adapter.ItemListViewAdapter;
import database.DBManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
	private ItemListViewAdapter adapter;
	
	private Report report;
	private List<Item> itemList;
	private ArrayList<Integer> chosenItemIDList = null;
	private ArrayList<Integer> remainingItemIDList = null;
	private boolean[] checkList;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_unarchived_items);
		initData();
		initView();
		initButton();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("UnarchivedItemsActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("UnarchivedItemsActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			Bundle bundle = new Bundle();
			bundle.putSerializable("report", report);
			bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
			bundle.putIntegerArrayList("remainingItemIDList", remainingItemIDList);
			Intent intent = new Intent(UnarchivedItemsActivity.this, EditReportActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initData()
	{		
		Bundle bundle = this.getIntent().getExtras();
		report = (Report)bundle.getSerializable("report");
		chosenItemIDList = bundle.getIntegerArrayList("chosenItemIDList");
		remainingItemIDList = bundle.getIntegerArrayList("remainingItemIDList");

		dbManager = DBManager.getDBManager();
		itemList = dbManager.getItems(remainingItemIDList);
		
		checkList = new boolean[remainingItemIDList.size()];
		for (int i = 0; i < checkList.length; i++)
		{
			checkList[i] = false;
		}		
	}
	
	private void initView()
	{
		adapter = new ItemListViewAdapter(UnarchivedItemsActivity.this, itemList);
		itemListView = (ListView)findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				checkList[position] = checkList[position] ? false : true;
				int color = checkList[position] ? Color.rgb(102, 204, 255) : Color.WHITE;
				view.setBackgroundColor(color);
			}
		});
	}
	
	private void initButton()
	{
		Button confirmButton = (Button)findViewById(R.id.confirmButton);
		confirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					constructList();
					Bundle bundle = new Bundle();
					bundle.putSerializable("report", report);
					bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
					bundle.putIntegerArrayList("remainingItemIDList", remainingItemIDList);
					Intent intent = new Intent(UnarchivedItemsActivity.this, EditReportActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();
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
				bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
				bundle.putIntegerArrayList("remainingItemIDList", remainingItemIDList);
				Intent intent = new Intent(UnarchivedItemsActivity.this, EditReportActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		});
	}
	
	private void constructList()
	{
		ArrayList<Integer> newList = new ArrayList<Integer>();
		for (int i = 0; i < checkList.length; i++)
		{
			if (checkList[i])
			{
				chosenItemIDList.add(remainingItemIDList.get(i));
			}
			else
			{
				newList.add(remainingItemIDList.get(i));
			}
		}
		remainingItemIDList.clear();
		remainingItemIDList.addAll(newList);
	}
}
