package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Report.GetReportRequest;
import netUtils.Request.Report.ModifyReportRequest;
import netUtils.Response.Report.GetReportResponse;
import netUtils.Response.Report.ModifyReportResponse;
import classes.AppPreference;
import classes.Comment;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.User;
import classes.Utils;
import classes.Adapter.ReportDetailListViewAdapter;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ApproveReportActivity extends Activity
{
	private AppPreference appPreference;
	private DBManager dbManager;
	
	private ReportDetailListViewAdapter adapter;
	
	private int reportServerID;
	private Report report;
	private List<Item> itemList = new ArrayList<Item>();
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_approve);
		initData();
		initView();
	}
	
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ApproveReportActivity");		
		MobclickAgent.onResume(this);
		refreshView();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ApproveReportActivity");
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
	
	private void initData()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			report = (Report)getIntent().getExtras().getSerializable("report");
			if (report == null)
			{
				reportServerID = getIntent().getIntExtra("reportServerID", -1);				
			}
			else
			{
				reportServerID = report.getServerID();				
			}
		}
		else
		{
			reportServerID = -1;
		}
		
		if (report == null)
		{
			report = new Report();
		}
		itemList = dbManager.getOthersReportItems(reportServerID);
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
				Intent intent = new Intent(ApproveReportActivity.this, CommentActivity.class);
				intent.putExtra("reportServerID", report.getServerID());	
				startActivity(intent);
			}
		});

		Button approveButton = (Button)findViewById(R.id.approveButton);
		approveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(ApproveReportActivity.this, "UMENG_PASS_REPORT_DETAIL");
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(ApproveReportActivity.this, "网络未连接，无法审批");
				}
				else
				{
					saveReport(Report.STATUS_APPROVED);
				}
			}
		});

		Button rejectButton = (Button)findViewById(R.id.rejectButton);
		rejectButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(ApproveReportActivity.this, "UMENG_REJECT_REPORT_DETAIL");
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(ApproveReportActivity.this, "网络未连接，无法审批");
				}
				else
				{
					saveReport(Report.STATUS_REJECTED);
				}
			}
		});

		adapter = new ReportDetailListViewAdapter(ApproveReportActivity.this, report, itemList);
		ListView detailListView = (ListView)findViewById(R.id.detailListView);
		detailListView.setAdapter(adapter);
		detailListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if (position > 0)
				{
					Intent intent = new Intent(ApproveReportActivity.this, ShowItemActivity.class);
					intent.putExtra("othersItemServerID", itemList.get(position - 1).getServerID());
					startActivity(intent);	
				}
			}
		});
	
//		if (!Utils.isNetworkConnected())
//		{
//			Utils.showToast(ApproveReportActivity.this, "网络未连接，无法添加评论");
//		}
//		else
//		{
//			showAddCommentDialog();
//		}
	}

	private void refreshView()
	{
		ReimApplication.setProgressDialog(this);
		
		if (Utils.isNetworkConnected())
		{
			if (reportServerID == -1 && report == null)
			{
				Utils.showToast(this, "数据获取失败");
			}
			else if (reportServerID != -1 && report == null)
			{
				sendGetReportRequest(reportServerID);
			}
			else if (report != null && itemList.size() == 0)
			{
				sendGetReportRequest(reportServerID);
			}
			else
			{
				itemList = dbManager.getOthersReportItems(reportServerID);
				adapter.setItemList(itemList);
				adapter.notifyDataSetChanged();		
			}			
		}
		else
		{
			Utils.showToast(this, "网络未连接，无法获取数据");
		}
	}
	
    private void sendGetReportRequest(final int reportServerID)
    {
    	ReimApplication.showProgressDialog();
    	GetReportRequest request = new GetReportRequest(reportServerID);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final GetReportResponse response = new GetReportResponse(httpResponse);
				if (response.getStatus())
				{ 
					int ownerID = appPreference.getCurrentUserID();
					if (report.getServerID() == -1)
					{
						report = response.getReport();
					}
					else
					{
						report.setManagerList(response.getReport().getManagerList());
						report.setCCList(response.getReport().getCCList());
						report.setCommentList(response.getReport().getCommentList());						
					}
					
					dbManager.deleteOthersReport(reportServerID, ownerID);
					dbManager.insertOthersReport(report);
					
					dbManager.deleteOthersReportItems(reportServerID);
					for (Item item : response.getItemList())
					{
						dbManager.insertOthersItem(item);
					}
					itemList = dbManager.getOthersReportItems(reportServerID);
					
					dbManager.deleteOthersReportComments(report.getServerID());
					for (Comment comment : report.getCommentList())
					{
						comment.setReportID(report.getServerID());
						dbManager.insertOthersComment(comment);
					}
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
					    	ReimApplication.dismissProgressDialog();
					    	if (report.getStatus() != Report.STATUS_SUBMITTED)
							{
					    		Utils.showToast(ApproveReportActivity.this, "报告已被审批");
					    		finish();
							}
					    	else
					    	{
					    		adapter.setReport(report);
								adapter.setItemList(itemList);
								adapter.notifyDataSetChanged();						    		
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
					    	ReimApplication.dismissProgressDialog();
				    		Utils.showToast(ApproveReportActivity.this, "数据获取失败");
				    		finish();
						}
					});
				}
			}
		});
    }
    
    private void showAddCommentDialog()
    {
		View view = View.inflate(this, R.layout.report_comment_dialog, null);
		final EditText commentEditText = (EditText)view.findViewById(R.id.commentEditText);
		commentEditText.requestFocus();
		
    	AlertDialog mDialog = new AlertDialog.Builder(this)
								.setTitle("添加评论")
								.setView(view)
								.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										String comment = commentEditText.getText().toString();
										if (comment.equals(""))
										{
											Utils.showToast(ApproveReportActivity.this, "评论不能为空");
										}
										else
										{
											sendCommentRequest(comment);
										}
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
		mDialog.show();
    }
	
    private void sendCommentRequest(final String commentContent)
    {
    	ReimApplication.showProgressDialog();
		
    	ModifyReportRequest request = new ModifyReportRequest(report, commentContent);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyReportResponse response = new ModifyReportResponse(httpResponse);
				if (response.getStatus())
				{
					User user = appPreference.getCurrentUser();
					int currentTime = Utils.getCurrentTime();
					
					Comment comment = new Comment();
					comment.setContent(commentContent);
					comment.setCreatedDate(currentTime);
					comment.setLocalUpdatedDate(currentTime);
					comment.setServerUpdatedDate(currentTime);
					comment.setReportID(report.getServerID());
					comment.setReviewer(user);
					
					dbManager.insertOthersComment(comment);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(ApproveReportActivity.this, "评论发表成功");
						}
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Utils.showToast(ApproveReportActivity.this, "评论发表失败, " + response.getErrorMessage());
						}
					});					
				}
			}
		});
    }
    
    private void saveReport(final int status)
    {
    	ReimApplication.showProgressDialog();
    	report.setStatus(status);
    	
    	ModifyReportRequest request = new ModifyReportRequest(report);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyReportResponse response = new ModifyReportResponse(httpResponse);
				if (response.getStatus())
				{
					report.setLocalUpdatedDate(Utils.getCurrentTime());
					report.setServerUpdatedDate(report.getLocalUpdatedDate());

					int managerID = appPreference.getCurrentUserID();
					dbManager.deleteOthersReport(reportServerID, managerID);
					dbManager.deleteOthersReportItems(reportServerID);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
					    	ReimApplication.dismissProgressDialog();
							String message = status == 2 ? "报告已通过" : "报告已退回";
							Utils.showToast(ApproveReportActivity.this, message);
							finish();
						}
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
					    	ReimApplication.dismissProgressDialog();
							Utils.showToast(ApproveReportActivity.this, "操作失败，" + 
											response.getErrorMessage());
						}
					});
				}
			}
		});
    }
}
