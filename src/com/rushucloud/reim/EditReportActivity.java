package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import netUtils.SyncUtils;
import classes.AppPreference;
import classes.Item;
import classes.Report;
import classes.Utils;
import classes.Adapter.ItemListViewAdapter;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class EditReportActivity extends Activity
{
	private AppPreference appPreference;
	private DBManager dbManager;
	
	private EditText titleEditText;
	private ListView itemListView;
	private ItemListViewAdapter adapter;
	
	private Report report;
	private List<Item> itemList = null;
	private ArrayList<Integer> chosenItemIDList = null;
	private ArrayList<Integer> remainingItemIDList = null;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_edit);
		dataInitialise();
		viewInitialise();
		buttonInitialise();
	}
	
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EditReportActivity");		
		MobclickAgent.onResume(this);
		refreshListView();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("EditReportActivity");
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
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
		{
			return false;
		}
		else if (report.getStatus() == Report.STATUS_SUBMITTED && 
				report.getUser().getServerID() != appPreference.getCurrentUserID())
		{
			getMenuInflater().inflate(R.menu.approve_reject, menu);
			return true;	
		}
		else
		{
			getMenuInflater().inflate(R.menu.submit, menu);
			return true;
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_submit_item)
		{
			submitReport();
			return true;
		}
		if (id == R.id.action_approve_item)
		{
			report.setStatus(Report.STATUS_APPROVED);
			saveReport("报告已批准");
			return true;
		}
		if (id == R.id.action_reject_item)
		{
			report.setStatus(Report.STATUS_REJECT);
			saveReport("报告已拒绝");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("选项");
		menu.add(0, 0, 0, "删除");
	}
	
	public boolean onContextItemSelected(MenuItem item)
	{
    	AdapterContextMenuInfo menuInfo=(AdapterContextMenuInfo)item.getMenuInfo();
    	final int index = (int)itemListView.getAdapter().getItemId(menuInfo.position);
    	switch (item.getItemId()) 
    	{
			case 0:
				int id = chosenItemIDList.remove(index);
				remainingItemIDList.add(id);
				itemList.remove(index);
				adapter.set(itemList);
				adapter.notifyDataSetChanged();
				break;
			default:
				break;
		}    		
		
		return super.onContextItemSelected(item);
	}
	
	private void dataInitialise()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null)
		{
			// new report from ReportFragment
			report = new Report();
			report.setStatus(Report.STATUS_DRAFT);
			report.setUser(dbManager.getUser(appPreference.getCurrentUserID()));
			chosenItemIDList = new ArrayList<Integer>();
			remainingItemIDList = Utils.itemListToIDArray(dbManager.getUnarchivedUserItems(appPreference.getCurrentUserID()));
			itemList = new ArrayList<Item>();
		}
		else
		{
			report = (Report)bundle.getSerializable("report");
			chosenItemIDList = bundle.getIntegerArrayList("chosenItemIDList");
			if (chosenItemIDList == null)
			{
				// edit report from ReportFragment
				itemList = dbManager.getReportItems(report.getLocalID());
				chosenItemIDList = Utils.itemListToIDArray(itemList);
				remainingItemIDList = Utils.itemListToIDArray(dbManager.getUnarchivedUserItems(appPreference.getCurrentUserID()));
			}
			else
			{
				// edit report from UnarchivedActivity
				remainingItemIDList = bundle.getIntegerArrayList("remainingItemIDList");	
				itemList = dbManager.getItems(chosenItemIDList);
			}
		}
	}
	
	private void viewInitialise()
	{
		titleEditText = (EditText)findViewById(R.id.titleEditText);
		titleEditText.setText(report.getTitle());
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
		{
			titleEditText.setFocusable(false);
		}
		
		adapter = new ItemListViewAdapter(EditReportActivity.this, itemList);
		itemListView = (ListView)findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
				{
					Intent intent = new Intent(EditReportActivity.this, ShowItemActivity.class);
					intent.putExtra("itemLocalID", itemList.get(position).getLocalID());
					startActivity(intent);	
				}
				else
				{
					Intent intent = new Intent(EditReportActivity.this, EditItemActivity.class);
					intent.putExtra("itemLocalID", itemList.get(position).getLocalID());
					startActivity(intent);					
				}
			}
		});
		if (report.getStatus() == Report.STATUS_DRAFT || report.getStatus() == Report.STATUS_REJECT)
		{
			registerForContextMenu(itemListView);			
		}
	}
	
	private void buttonInitialise()
	{
		Button addButton = (Button)findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				if (remainingItemIDList.size() == 0)
				{
					AlertDialog alertDialog = new AlertDialog.Builder(EditReportActivity.this)
												.setTitle("提示")
												.setMessage("所有条目均已被添加到各个报告中")
												.setNegativeButton(R.string.confirm, null)
												.create();
					alertDialog.show();
				}
				else
				{
					report.setTitle(titleEditText.getText().toString());
					
					Bundle bundle = new Bundle();
					bundle.putSerializable("report", report);
					bundle.putIntegerArrayList("chosenItemIDList", chosenItemIDList);
					bundle.putIntegerArrayList("remainingItemIDList", remainingItemIDList);
					Intent intent = new Intent(EditReportActivity.this, UnarchivedItemsActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();					
				}
			}
		});
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
		{
			addButton.setVisibility(View.GONE);
		}
		
		Button saveButton = (Button)findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				saveReport("报告保存成功");
			}
		});
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
		{
			saveButton.setEnabled(false);
		}
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
	}
	
	private void refreshListView()
	{
		adapter.set(itemList);
		adapter.notifyDataSetChanged();
	}	
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
    }
    
    private void saveReport(String prompt)
    {
    	hideSoftKeyboard();
    	report.setLocalUpdatedDate(Utils.getCurrentTime());
		report.setTitle(titleEditText.getText().toString());
		if (report.getLocalID() == -1)
		{
			report.setCreatedDate(Utils.getCurrentTime());
			dbManager.insertReport(report);
			report.setLocalID(dbManager.getLastInsertReportID());								
		}
		else
		{
			dbManager.updateReportByLocalID(report);
		}
		if (dbManager.updateReportItems(chosenItemIDList, report.getLocalID()))
		{
			Toast.makeText(EditReportActivity.this, prompt, Toast.LENGTH_SHORT).show();
			if (Utils.canSyncToServer(EditReportActivity.this))
			{
				SyncUtils.syncAllToServer(null);
			}
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

    private void submitReport()
    {
    	hideSoftKeyboard();
    	report.setLocalUpdatedDate(Utils.getCurrentTime());
		report.setTitle(titleEditText.getText().toString());
		if (report.getLocalID() == -1)
		{
			report.setCreatedDate(Utils.getCurrentTime());
			dbManager.insertReport(report);
			report.setLocalID(dbManager.getLastInsertReportID());								
		}
		else
		{
			dbManager.updateReportByLocalID(report);
		}
		if (dbManager.updateReportItems(chosenItemIDList, report.getLocalID()))
		{
			boolean canBeSubmitted = report.canBeSubmitted();
			if (!canBeSubmitted)
			{
				report.setStatus(Report.STATUS_DRAFT);
				Toast.makeText(EditReportActivity.this, "部分条目信息未上传到服务器，无法提交报告", Toast.LENGTH_SHORT).show();
			}
			else if (appPreference.getCurrentGroupID() == -1)
			{
				report.setStatus(Report.STATUS_FINISHED);
				Toast.makeText(EditReportActivity.this, "报告提交成功", Toast.LENGTH_SHORT).show();
			}
			else
			{
				report.setStatus(Report.STATUS_SUBMITTED);		
				Toast.makeText(EditReportActivity.this, "报告提交成功", Toast.LENGTH_SHORT).show();		
			}
			dbManager.updateReportByLocalID(report);
			if (Utils.canSyncToServer(EditReportActivity.this))
			{
				SyncUtils.syncAllToServer(null);
			}
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
}