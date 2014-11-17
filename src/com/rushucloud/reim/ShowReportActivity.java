package com.rushucloud.reim;

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
import classes.Adapter.ItemListViewAdapter;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ShowReportActivity extends Activity
{
	private DBManager dbManager;

	private TextView managerTextView;
	private TextView ccTextView;
	
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
		ReimApplication.setProgressDialog(this);

		Button addCommentButton = (Button)findViewById(R.id.addCommentButton);
		addCommentButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!Utils.isNetworkConnected())
				{
					Toast.makeText(ShowReportActivity.this, "网络未连接，无法添加", Toast.LENGTH_SHORT).show();
				}
				else
				{
					showAddCommentDialog();
				}
			}
		});

		Button checkCommentButton = (Button)findViewById(R.id.checkCommentButton);
		checkCommentButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(ShowReportActivity.this, CommentActivity.class);
				if (myReport)
				{
					intent.putExtra("reportLocalID", report.getLocalID());					
				}
				else
				{
					intent.putExtra("reportServerID", report.getServerID());						
				}
				startActivity(intent);
			}
		});
		
		TextView titleTextView = (TextView)findViewById(R.id.titleTextView);
		titleTextView.setText(report.getTitle());
		
		managerTextView = (TextView)findViewById(R.id.managerTextView);
		managerTextView.setText(report.getManagersName());		
		
		ccTextView = (TextView)findViewById(R.id.ccTextView);
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

	private void refreshView()
	{
		if (Utils.isNetworkConnected())
		{
			sendGetReportRequest(report.getServerID());		
		}
		else
		{
			Toast.makeText(this, "网络未连接，无法获取详细信息", Toast.LENGTH_SHORT).show();
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
					}
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
					    	ReimApplication.dismissProgressDialog();
							managerTextView.setText(report.getManagersName());
							ccTextView.setText(report.getCCsName());
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
							Toast.makeText(ShowReportActivity.this, "获取详细信息失败", Toast.LENGTH_SHORT).show();
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
											Toast.makeText(ShowReportActivity.this, "评论不能为空", Toast.LENGTH_SHORT).show();
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
					User user = dbManager.getUser(AppPreference.getAppPreference().getCurrentUserID());
					int currentTime = Utils.getCurrentTime();
					
					Comment comment = new Comment();
					comment.setContent(commentContent);
					comment.setCreatedDate(currentTime);
					comment.setLocalUpdatedDate(currentTime);
					comment.setServerUpdatedDate(currentTime);
					comment.setReviewer(user);
					
					if (myReport)
					{
						comment.setReportID(report.getLocalID());
						dbManager.insertComment(comment);						
					}
					else
					{
						comment.setReportID(report.getServerID());
						dbManager.insertOthersComment(comment);
					}
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Toast.makeText(ShowReportActivity.this, "评论发表成功", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(ShowReportActivity.this, "评论发表失败, " + response.getErrorMessage(), Toast.LENGTH_SHORT).show();
						}
					});					
				}
			}
		});
    }
    
    private void goBackToMainActivity()
    {
    	int reportTabIndex = myReport ? 0 : 1;
    	ReimApplication.setTabIndex(1);
    	ReimApplication.setReportTabIndex(reportTabIndex);
    	finish();
    }
}