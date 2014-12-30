package com.rushucloud.reim.report;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Report.GetReportRequest;
import netUtils.Request.Report.ModifyReportRequest;
import netUtils.Response.Report.GetReportResponse;
import netUtils.Response.Report.ModifyReportResponse;
import classes.Comment;
import classes.Item;
import classes.ReimApplication;
import classes.Report;
import classes.User;
import classes.Adapter.ReportDetailListViewAdapter;
import classes.Utils.AppPreference;
import classes.Utils.Utils;

import com.rushucloud.reim.R;
import com.rushucloud.reim.item.ShowItemActivity;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog.Builder;
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
		setContentView(R.layout.activity_report_approve);
		initData();
		initView();
	}
	
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ApproveReportActivity");		
		MobclickAgent.onResume(this);
		ReimApplication.setProgressDialog(this);
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
				Bundle bundle = new Bundle();
				bundle.putString("source", "ApproveReportActivity");
				bundle.putInt("reportServerID", report.getServerID());
				Intent intent = new Intent(ApproveReportActivity.this, CommentActivity.class);
				intent.putExtras(bundle);
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
					sendModifyReportRequest(Report.STATUS_APPROVED, "");
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
					showCommentDialog();
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
	}

	private void refreshView()
	{		
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

					dbManager.deleteOthersReport(reportServerID, ownerID);
					dbManager.insertOthersReport(report);
					
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
					    	else if (!report.getManagerList().contains(appPreference.getCurrentUser()))
							{
					    		Bundle bundle = new Bundle();
								bundle.putSerializable("report", report);
								bundle.putBoolean("myReport", false);
								Intent intent = new Intent(ApproveReportActivity.this, ShowReportActivity.class);
								intent.putExtras(bundle);
								startActivity(intent);
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
    
    private void showCommentDialog()
    {
		View view = View.inflate(this, R.layout.dialog_report_comment, null);
		
		TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
		titleTextView.setText(R.string.reject_reason);
		
		final EditText commentEditText = (EditText)view.findViewById(R.id.commentEditText);
		commentEditText.setOnFocusChangeListener(Utils.getEditTextFocusChangeListener());
		commentEditText.requestFocus();
		
    	Builder builder = new Builder(this);
    	builder.setView(view);
    	builder.setPositiveButton(R.string.reject, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										sendModifyReportRequest(Report.STATUS_REJECTED, commentEditText.getText().toString());
									}
								});
    	builder.setNegativeButton(R.string.cancel, null);
    	builder.create().show();
    }
	
    private void sendModifyReportRequest(final int status, final String commentContent)
    {
    	ReimApplication.showProgressDialog();
    	report.setStatus(status);
		
    	ModifyReportRequest request = new ModifyReportRequest(report, commentContent);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyReportResponse response = new ModifyReportResponse(httpResponse);
				if (response.getStatus())
				{
					int currentTime = Utils.getCurrentTime();					
					report.setLocalUpdatedDate(currentTime);
					report.setServerUpdatedDate(currentTime);
					dbManager.updateOthersReport(report);
					
					if (!commentContent.equals(""))
					{
						User user = appPreference.getCurrentUser();
						
						Comment comment = new Comment();
						comment.setContent(commentContent);
						comment.setCreatedDate(currentTime);
						comment.setLocalUpdatedDate(currentTime);
						comment.setServerUpdatedDate(currentTime);
						comment.setReportID(report.getServerID());
						comment.setReviewer(user);
						
						dbManager.insertOthersComment(comment);						
					}
					
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
							Utils.showToast(ApproveReportActivity.this, "操作失败, " + response.getErrorMessage());
						}
					});					
				}
			}
		});
    }
}