package com.rushucloud.reim.report;

import java.util.ArrayList;
import java.util.List;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.Item;
import classes.Report;
import classes.Adapter.ReportItemListViewAdapter;
import classes.Utils.AppPreference;
import classes.Utils.Utils;
import classes.Widget.ReimProgressDialog;
import database.DBManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class UnarchivedItemsActivity extends Activity implements OnClickListener
{
	private AppPreference appPreference;
	private static DBManager dbManager;

	private TextView consumedTextView;
	private TextView proveAheadTextView;
	private TextView itemCountTextView;
	private TextView warningTextView;
	private ListView itemListView;
	private ReportItemListViewAdapter adapter;
	
	private Report report;
	private List<Item> consumedItemList;
	private List<Item> proveAheadItemList;
	private boolean[] consumedCheck;
	private boolean[] proveAheadCheck;
	private ArrayList<Integer> chosenItemIDList = null;
	private boolean isProveAhead;
	private int tabIndex = 0;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_unarchived_items);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("UnarchivedItemsActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
		refreshView();
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
		if (chosenItemIDList == null)
		{
			chosenItemIDList = new ArrayList<Integer>();
		}

		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		
		consumedItemList = dbManager.getUnarchivedConsumedItems(appPreference.getCurrentUserID());
		proveAheadItemList = dbManager.getUnarchivedProveAheadItems(appPreference.getCurrentUserID());

		isProveAhead = report.isProveAhead();
		tabIndex = Utils.booleanToInt(isProveAhead);
		
		if (report.getLocalID() != -1)
		{
			List<Item> items = dbManager.getReportItems(report.getLocalID());
			if (items.size() > 0)
			{
				Item item = items.get(0);
				if (item.isProveAhead() && (item.getStatus() == Item.STATUS_DRAFT || item.getStatus() == Item.STATUS_REJECTED))
				{
					proveAheadItemList.addAll(items);
					Item.sortByUpdateDate(proveAheadItemList);
				}
				else
				{
					consumedItemList.addAll(items);
					Item.sortByUpdateDate(consumedItemList);
				}
			}			
		}
		
		List<Item> chosenItems = dbManager.getItems(chosenItemIDList);
		if (isProveAhead)
		{
			proveAheadCheck = Item.getItemsCheck(proveAheadItemList, chosenItems);
			consumedCheck = Item.getItemsCheck(consumedItemList, null);
		}
		else
		{
			proveAheadCheck = Item.getItemsCheck(proveAheadItemList, null);
			consumedCheck = Item.getItemsCheck(consumedItemList, chosenItems);			
		}
	}
	
	private void initView()
	{
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Bundle bundle = new Bundle();
				bundle.putSerializable("report", report);
				bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
				Intent intent = new Intent(UnarchivedItemsActivity.this, EditReportActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		});

		consumedTextView = (TextView)findViewById(R.id.consumedTextView);
		consumedTextView.setOnClickListener(this);
		proveAheadTextView = (TextView)findViewById(R.id.proveAheadTextView);
		proveAheadTextView.setOnClickListener(this);
		
		int itemCount = isProveAhead ? checkCount(proveAheadCheck) : checkCount(consumedCheck);
		itemCountTextView = (TextView)findViewById(R.id.itemCountTextView);
		itemCountTextView.setText(Integer.toString(itemCount));
		
		TextView confirmTextView = (TextView)findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					report.setIsProveAhead(isProveAhead);
					constructList();
					Bundle bundle = new Bundle();
					bundle.putSerializable("report", report);
					bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
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

		warningTextView = (TextView)findViewById(R.id.warningTextView);
		
		if (isProveAhead)
		{
			adapter = new ReportItemListViewAdapter(UnarchivedItemsActivity.this, consumedItemList, consumedCheck);			
		}
		else
		{
			adapter = new ReportItemListViewAdapter(UnarchivedItemsActivity.this, proveAheadItemList, proveAheadCheck);	
		}
		itemListView = (ListView) findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (tabIndex == 0)
				{
					consumedCheck[position] = !consumedCheck[position];
					isProveAhead = false;
					itemCountTextView.setText(Integer.toString(checkCount(consumedCheck)));
					adapter.setCheck(consumedCheck);
				}
				else
				{
					proveAheadCheck[position] = !proveAheadCheck[position];
					isProveAhead = true;
					itemCountTextView.setText(Integer.toString(checkCount(proveAheadCheck)));
					adapter.setCheck(proveAheadCheck);
				}
				resetCheck();
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	private void refreshView()
	{
		ReimProgressDialog.show();
		
		if (tabIndex == 0)
		{
			consumedTextView.setTextColor(getResources().getColor(R.color.major_light));
			proveAheadTextView.setTextColor(getResources().getColor(R.color.hint_white));
			
			if (consumedItemList.size() == 0)
			{
				itemListView.setVisibility(View.INVISIBLE);
				warningTextView.setVisibility(View.VISIBLE);
			}
			else
			{
				itemListView.setVisibility(View.VISIBLE);
				warningTextView.setVisibility(View.INVISIBLE);
				adapter.set(consumedItemList, consumedCheck);
				adapter.notifyDataSetChanged();		
			}
		}
		else
		{
			consumedTextView.setTextColor(getResources().getColor(R.color.hint_white));
			proveAheadTextView.setTextColor(getResources().getColor(R.color.major_light));
			
			if (proveAheadItemList.size() == 0)
			{
				itemListView.setVisibility(View.INVISIBLE);
				warningTextView.setVisibility(View.VISIBLE);
			}
			else
			{
				itemListView.setVisibility(View.VISIBLE);
				warningTextView.setVisibility(View.INVISIBLE);
				adapter.set(proveAheadItemList, proveAheadCheck);
				adapter.notifyDataSetChanged();		
			}
		}

		ReimProgressDialog.dismiss();
	}
	
	private void constructList()
	{
		chosenItemIDList.clear();
		if (isProveAhead)
		{
			if (proveAheadCheck != null)
			{
				for (int i = 0; i < proveAheadCheck.length; i++)
				{
					if (proveAheadCheck[i])
					{
						chosenItemIDList.add(proveAheadItemList.get(i).getLocalID());
					}
				}
			}
		}
		else
		{
			if (consumedCheck != null)
			{
				for (int i = 0; i < consumedCheck.length; i++)
				{
					if (consumedCheck[i])
					{
						chosenItemIDList.add(consumedItemList.get(i).getLocalID());
					}
				}
			}
		}
	}

	private void resetCheck()
	{
		if (tabIndex == 0)
		{
			if (proveAheadCheck != null)
			{
				for (int i = 0; i < proveAheadCheck.length; i++)
				{
					proveAheadCheck[i] = false;
				}
			}
		}
		else
		{
			if (consumedCheck != null)
			{
				for (int i = 0; i < consumedCheck.length; i++)
				{
					consumedCheck[i] = false;
				}		
			}	
		}
	}
	
	private int checkCount(boolean[] checkList)
	{
		int count = 0;
		if (checkList != null)
		{
			for (boolean b : checkList)
			{
				if (b)
				{
					count++;
				}
			}
		}
		
		return count;
	}
	
	public void onClick(View v)
	{
		if (v.equals(consumedTextView))
		{
			tabIndex = 0;		
		}
		else
		{
			tabIndex = 1;
		}
		refreshView();
	}
}
