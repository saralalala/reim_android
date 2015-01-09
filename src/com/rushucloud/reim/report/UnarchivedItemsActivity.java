package com.rushucloud.reim.report;

import java.util.ArrayList;
import java.util.List;

import com.rushucloud.reim.R;
import com.rushucloud.reim.item.EditItemActivity;
import com.umeng.analytics.MobclickAgent;

import classes.Item;
import classes.Report;
import classes.Adapter.ReportItemListViewAdapter;
import classes.Utils.AppPreference;
import classes.Utils.DBManager;
import classes.Utils.Utils;
import classes.Widget.ReimProgressDialog;
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
	private ArrayList<Integer> tempItemIDList = null;
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
		refreshData();
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
		tempItemIDList = new ArrayList<Integer>(chosenItemIDList);

		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();

		isProveAhead = report.isProveAhead();
		tabIndex = Utils.booleanToInt(isProveAhead);
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
		
		itemCountTextView = (TextView)findViewById(R.id.itemCountTextView);
		
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
					bundle.putIntegerArrayList("chosenItemIDList", tempItemIDList);
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
		
		itemListView = (ListView) findViewById(R.id.itemListView);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (position == 0)
				{
					constructList();
					Intent intent = new Intent(UnarchivedItemsActivity.this, EditItemActivity.class);
					intent.putExtra("fromReim", true);
					startActivity(intent);
				}
				else
				{
					if (tabIndex == 0)
					{
						consumedCheck[position - 1] = !consumedCheck[position - 1];
						isProveAhead = false;
						itemCountTextView.setText(Integer.toString(checkCount(consumedCheck)));
						adapter.setCheck(consumedCheck);
					}
					else
					{
						proveAheadCheck[position - 1] = !proveAheadCheck[position - 1];
						isProveAhead = true;
						itemCountTextView.setText(Integer.toString(checkCount(proveAheadCheck)));
						adapter.setCheck(proveAheadCheck);
					}
					resetCheck();
					adapter.notifyDataSetChanged();					
				}
			}
		});
	}

	private void refreshData()
	{
		consumedItemList = dbManager.getUnarchivedConsumedItems(appPreference.getCurrentUserID());
		proveAheadItemList = dbManager.getUnarchivedProveAheadItems(appPreference.getCurrentUserID());
		
		if (report.getLocalID() != -1)
		{
			List<Item> items = dbManager.getReportItems(report.getLocalID());
			if (!items.isEmpty())
			{
				Item item = items.get(0);
				if (item.isProveAhead() && !item.isPaApproved())
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
		
		List<Item> chosenItems = dbManager.getItems(tempItemIDList);
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
	
	private void refreshView()
	{
		ReimProgressDialog.show();

		int itemCount = isProveAhead ? checkCount(proveAheadCheck) : checkCount(consumedCheck);
		itemCountTextView.setText(Integer.toString(itemCount));
		
		if (adapter == null)
		{
			if (isProveAhead)
			{
				adapter = new ReportItemListViewAdapter(UnarchivedItemsActivity.this, consumedItemList, consumedCheck);			
			}
			else
			{
				adapter = new ReportItemListViewAdapter(UnarchivedItemsActivity.this, proveAheadItemList, proveAheadCheck);	
			}
			itemListView.setAdapter(adapter);			
		}
		
		if (tabIndex == 0)
		{
			consumedTextView.setTextColor(getResources().getColor(R.color.major_light));
			proveAheadTextView.setTextColor(getResources().getColor(R.color.hint_white));
			
			if (consumedItemList.isEmpty())
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
			
			if (proveAheadItemList.isEmpty())
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
		tempItemIDList.clear();
		if (isProveAhead)
		{
			if (proveAheadCheck != null)
			{
				for (int i = 0; i < proveAheadCheck.length; i++)
				{
					if (proveAheadCheck[i])
					{
						tempItemIDList.add(proveAheadItemList.get(i).getLocalID());
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
						tempItemIDList.add(consumedItemList.get(i).getLocalID());
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
