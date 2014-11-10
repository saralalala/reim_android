package com.rushucloud.reim;

import java.util.List;

import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.Adapter.ItemListViewAdapter;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ShowReportActivity extends Activity
{
	private Report report;
	private List<Item> itemList = null;
	private boolean myReport;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_show);
		MobclickAgent.onEvent(ShowReportActivity.this, "UMENG_VIEW_REPORT");
		initData();
		initView();
	}
	
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ShowReportActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ShowReportActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			goBackToMainActivity();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initData()
	{
		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			report = (Report)bundle.getSerializable("report");
			myReport = bundle.getBoolean("myReport");
			if (myReport)
			{
				itemList = DBManager.getDBManager().getReportItems(report.getLocalID());				
			}		
			else
			{
				itemList = DBManager.getDBManager().getOthersReportItems(report.getServerID());
			}
		}
	}
	
	private void initView()
	{
		ReimApplication.setProgressDialog(this);
		
		TextView titleTextView = (TextView)findViewById(R.id.titleTextView);
		titleTextView.setText(report.getTitle());
		
		TextView managerTextView = (TextView)findViewById(R.id.managerTextView);
		managerTextView.setText(report.getManagersName());		
		
		TextView ccTextView = (TextView)findViewById(R.id.ccTextView);
		ccTextView.setText(report.getCCsName());

		ItemListViewAdapter adapter = new ItemListViewAdapter(ShowReportActivity.this, itemList);
		ListView itemListView = (ListView)findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent intent = new Intent(ShowReportActivity.this, ShowItemActivity.class);
				if (myReport)
				{
					intent.putExtra("itemLocalID", itemList.get(position).getLocalID());					
				}
				else
				{
					intent.putExtra("othersItemServerID", itemList.get(position).getServerID());					
				}
				startActivity(intent);	
			}
		});
	}

    private void goBackToMainActivity()
    {
    	ReimApplication.setTabIndex(1);
    	if (myReport)
    	{
        	ReimApplication.setReportTabIndex(0);    		
    	}
    	else
    	{
        	ReimApplication.setReportTabIndex(1);
    	}
    	Intent intent = new Intent(ShowReportActivity.this, MainActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    	finish();
    }
}
