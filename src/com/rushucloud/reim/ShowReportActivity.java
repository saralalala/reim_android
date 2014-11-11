package com.rushucloud.reim;

import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.Report.ModifyReportRequest;
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
											sendModifyReportRequest(comment);
										}
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
		mDialog.show();
    }
	
    private void sendModifyReportRequest(final String commentContent)
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
