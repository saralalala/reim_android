package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import classes.AppPreference;
import classes.Item;
import classes.Report;
import classes.Adapter.ItemListViewAdapter;

import com.rushucloud.reim.R;

import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class EditReportActivity extends Activity
{
	private static DBManager dbManager;
	
	private EditText titleEditText;
	private ListView itemListView;
	private ItemListViewAdapter adapter;
	
	private Report report;
	private List<Item> itemList = null;
	private int[] itemLocalIDList = null;
	private boolean newReport;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reim_edit_report);
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
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("选项");
		menu.add(0, 0, 0, "删除");
	}
	
	public boolean onContextItemSelected(MenuItem item)
	{
		return super.onContextItemSelected(item);
	}
	
	private void dataInitialise()
	{
		AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null)
		{
			// new report from ReportFragment
			report = new Report();
			newReport = true;
		}
		else
		{
			report = (Report)bundle.getSerializable("report");
			itemLocalIDList = (int[])bundle.getIntArray("itemLocalIDList");
			if (itemLocalIDList == null)
			{
				// modify a report from ReportFragment
				itemList = dbManager.getReportItems(report.getLocalID());
				newReport = false;
			}
			else
			{
				// get chosen items from UnarchivedItemsActivity
				newReport = bundle.getBoolean("newReport");
				itemLocalIDList = bundle.getIntArray("itemLocalIDList");
				itemList = new ArrayList<Item>();
				for (int i = 0; i < itemLocalIDList.length; i++)
				{
					Item item = dbManager.getItemByLocalID(itemLocalIDList[i]);
					itemList.add(item);
				}
			}
		}
	}
	
	private void viewInitialise()
	{
		titleEditText = (EditText)findViewById(R.id.titleEditText);
		
		adapter = new ItemListViewAdapter(EditReportActivity.this, itemList);
		itemListView = (ListView)findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent intent = new Intent(EditReportActivity.this, EditItemActivity.class);
				intent.putExtra("itemLocalID", itemList.get(position).getLocalID());
				startActivity(intent);
			}
		});
		registerForContextMenu(itemListView);
	}
	
	private void buttonInitialise()
	{
		Button addButton = (Button)findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				Bundle bundle = new Bundle();
				bundle.putSerializable("report", report);
				bundle.putBoolean("newReport", newReport);
				bundle.putIntArray("itemLocalIDList", itemLocalIDList);
				Intent intent = new Intent(EditReportActivity.this, UnarchivedItemsActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		});
		
		Button saveButton = (Button)findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					dbManager.syncReport(report);
					if (report.getLocalID() == -1)
					{
						report.setLocalID(dbManager.getLastInsertRowID());						
					}
					if (dbManager.updateReportItems(itemLocalIDList, report.getLocalID()))
					{
						AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
															.setTitle("保存成功")
															.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener()
															{
																public void onClick(DialogInterface dialog, int which)
																{
																	finish();
																}
															})
															.create();
						mDialog.show();
					}
					else
					{
						AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
															.setTitle("保存失败")
															.setNegativeButton(R.string.confirm, null)
															.create();
						mDialog.show();
					}
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
				finish();
			}
		});
	}
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
    }
}