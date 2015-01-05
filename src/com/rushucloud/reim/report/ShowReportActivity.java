package com.rushucloud.reim.report;

import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Report.GetReportRequest;
import netUtils.Response.Report.GetReportResponse;
import classes.Comment;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.Adapter.ReportDetailListViewAdapter;
import classes.Utils.AppPreference;
import classes.Utils.Utils;
import classes.Widget.ReimProgressDialog;

import com.rushucloud.reim.R;
import com.rushucloud.reim.item.ShowItemActivity;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ShowReportActivity extends Activity
{
	private DBManager dbManager;

	private ListView detailListView;
	private ReportDetailListViewAdapter adapter;
	
	private Report report;
	private List<Item> itemList = null;
	private boolean myReport;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_show);
		MobclickAgent.onEvent(ShowReportActivity.this, "UMENG_VIEW_REPORT");
		initData();
		initView();
	}
	
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ShowReportActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
		refreshView();
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
		dbManager = DBManager.getDBManager();
		
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
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
		
		TextView commentTextView = (TextView)findViewById(R.id.commentTextView);
		commentTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Bundle bundle = new Bundle();
				bundle.putString("source", "ShowReportActivity");
				if (myReport)
				{
					bundle.putInt("reportLocalID", report.getLocalID());				
				}
				else
				{
					bundle.putInt("reportServerID", report.getServerID());
				}
				Intent intent = new Intent(ShowReportActivity.this, CommentActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		adapter = new ReportDetailListViewAdapter(ShowReportActivity.this, report, itemList);
		detailListView = (ListView)findViewById(R.id.detailListView);
		detailListView.setAdapter(adapter);
		detailListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if (position > 0)
				{
					Intent intent = new Intent(ShowReportActivity.this, ShowItemActivity.class);
					if (myReport)
					{
						intent.putExtra("itemLocalID", itemList.get(position - 1).getLocalID());					
					}
					else
					{
						intent.putExtra("othersItemServerID", itemList.get(position - 1).getServerID());					
					}
					startActivity(intent);	
				}
			}
		});
	}

	private void refreshView()
	{		
		if (Utils.isNetworkConnected())
		{
			sendGetReportRequest(report.getServerID());		
		}
		else
		{
			Utils.showToast(this, "网络未连接，无法获取详细信息");
		}
	}
	
    private void sendGetReportRequest(final int reportServerID)
    {
		ReimProgressDialog.show();
    	GetReportRequest request = new GetReportRequest(reportServerID);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final GetReportResponse response = new GetReportResponse(httpResponse);
				if (response.getStatus())
				{ 
					int ownerID = AppPreference.getAppPreference().getCurrentUserID();
					
					report.setManagerList(response.getReport().getManagerList());
					report.setCCList(response.getReport().getCCList());
					report.setCommentList(response.getReport().getCommentList());
					
					if (myReport)
					{						
						dbManager.updateReportByLocalID(report);
						
						dbManager.deleteReportComments(report.getLocalID());
						for (Comment comment : report.getCommentList())
						{
							comment.setReportID(report.getLocalID());
							dbManager.insertComment(comment);
						}
					}
					else
					{
						dbManager.deleteOthersReport(reportServerID, ownerID);
						dbManager.insertOthersReport(report);
												
						for (Item item : response.getItemList())
						{
							dbManager.insertOthersItem(item);
						}
						itemList = dbManager.getOthersReportItems(reportServerID);
						
						for (Comment comment : report.getCommentList())
						{
							comment.setReportID(report.getServerID());
							dbManager.insertOthersComment(comment);
						}
					}
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
					    	adapter.setReport(report);
					    	adapter.setItemList(itemList);
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
							ReimProgressDialog.dismiss();
							Utils.showToast(ShowReportActivity.this, "获取详细信息失败");
						}
					});
				}
			}
		});
    }
	
    private void goBackToMainActivity()
    {
    	int reportTabIndex = myReport ? 0 : 1;
    	ReimApplication.setReportTabIndex(reportTabIndex);
    	finish();
    }
}