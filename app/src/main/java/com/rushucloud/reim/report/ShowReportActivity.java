package com.rushucloud.reim.report;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.rushucloud.reim.item.ShowItemActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import classes.Comment;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.adapter.ReportDetailListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.Request.Report.GetReportRequest;
import netUtils.Response.Report.GetReportResponse;

public class ShowReportActivity extends Activity
{
	private DBManager dbManager;

	private ImageView tipImageView;
	private ListView detailListView;
	private ReportDetailListViewAdapter adapter;
	
	private Report report;
	private List<Item> itemList = null;
	private boolean fromPush;
	private boolean myReport;
	private int lastCommentCount;	
	
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
		ReimProgressDialog.setContext(this);
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
			fromPush = bundle.getBoolean("fromPush", false);
			myReport = bundle.getBoolean("myReport", false);
			if (myReport)
			{
                if (fromPush)
                {
                    report = dbManager.getReportByServerID(report.getServerID());
                }
				itemList = dbManager.getReportItems(report.getLocalID());	
			}		
			else
			{
				itemList = dbManager.getOthersReportItems(report.getServerID());
			}

			lastCommentCount = report.getCommentList() != null ? report.getCommentList().size() : 0;
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
				goBackToMainActivity();
			}
		});

		ImageView commentImageView = (ImageView) findViewById(R.id.commentImageView);
		commentImageView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (myReport)
				{
					MobclickAgent.onEvent(ShowReportActivity.this, "UMENG_REPORT_MINE_COMMENT");					
				}
				else
				{
					MobclickAgent.onEvent(ShowReportActivity.this, "UMENG_REPORT_OTHER_COMMENT");					
				}
				
				tipImageView.setVisibility(View.GONE);
				
				Bundle bundle = new Bundle();
				bundle.putSerializable("report", report);
				bundle.putBoolean("myReport", myReport);
				Intent intent = new Intent(ShowReportActivity.this, CommentActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		tipImageView = (ImageView) findViewById(R.id.tipImageView);
		
		adapter = new ReportDetailListViewAdapter(ShowReportActivity.this, report, itemList);
		detailListView = (ListView) findViewById(R.id.detailListView);
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
		if (PhoneUtils.isNetworkConnected())
		{
			sendGetReportRequest(report.getServerID());
		}
		else
		{
			ViewUtils.showToast(this, R.string.error_get_data_network_unavailable);
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
					int localID = report.getLocalID();
					report = new Report(response.getReport());
					report.setLocalID(localID);
					
					if (myReport)
					{
						dbManager.updateReportByServerID(report);
						
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
							if (report.canBeApproved())
					    	{
								Bundle bundle = new Bundle();
								bundle.putSerializable("report", report);								
					        	Intent intent = new Intent(ShowReportActivity.this, ApproveReportActivity.class);			    		
					        	intent.putExtras(bundle);
					        	startActivity(intent);
					        	finish();
							}
					    	else
					    	{
						    	adapter.setReport(report);
						    	adapter.setItemList(itemList);
						    	adapter.notifyDataSetChanged();
								
								if (report.getCommentList().size() != lastCommentCount)
								{
									tipImageView.setVisibility(View.VISIBLE);
									lastCommentCount = report.getCommentList().size();
								}					    		
					    	}
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
				    		ViewUtils.showToast(ShowReportActivity.this, R.string.failed_to_get_data, response.getErrorMessage());
				    		if (response.getCode() == NetworkConstant.ERROR_REPORT_DELETED || response.getCode() == NetworkConstant.ERROR_REPORT_NOT_EXISTS)
							{
								dbManager.deleteOthersReport(reportServerID, AppPreference.getAppPreference().getCurrentUserID());
							}
				    		goBackToMainActivity();
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
    	if (fromPush)
		{
        	ReimApplication.setTabIndex(1);
        	Intent intent = new Intent(ShowReportActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        	startActivity(intent);
        	finish();
		}
    	else
    	{
        	finish();
		}
    }
}