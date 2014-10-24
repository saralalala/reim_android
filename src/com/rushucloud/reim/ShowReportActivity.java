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
	private TextView titleTextView;
	private ListView itemListView;
	private ItemListViewAdapter adapter;
	
	private Report report;
	private List<Item> itemList = null;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_approve_detail);
		dataInitialise();
		viewInitialise();
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
	
	private void dataInitialise()
	{
		report = (Report)getIntent().getSerializableExtra("report");
		itemList = DBManager.getDBManager().getReportItems(report.getLocalID());
	}
	
	private void viewInitialise()
	{
		ReimApplication.setProgressDialog(this);
		
		titleTextView = (TextView)findViewById(R.id.titleTextView);
		titleTextView.setText(report.getTitle());

		adapter = new ItemListViewAdapter(ShowReportActivity.this, itemList);
		itemListView = (ListView)findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent intent = new Intent(ShowReportActivity.this, ShowItemActivity.class);
				intent.putExtra("itemLocalID", itemList.get(position).getLocalID());
				startActivity(intent);	
			}
		});
	}

    private void goBackToMainActivity()
    {
    	Bundle bundle = new Bundle();
    	bundle.putInt("tabIndex", 1);
    	bundle.putInt("reportTabIndex", 1);
    	Intent intent = new Intent(ShowReportActivity.this, MainActivity.class);
    	intent.putExtras(bundle);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    	finish();
    }
}