package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Group.GetGroupRequest;
import netUtils.Request.Report.GetReportRequest;
import netUtils.Request.Report.ModifyReportRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Group.GetGroupResponse;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ApproveReportActivity extends Activity
{
	private AppPreference appPreference;
	private DBManager dbManager;
	
	private TextView titleTextView;
	private TextView senderTextView;
	private ImageView senderImageView;
	private TextView managerTextView;
	private TextView ccTextView;
	private ListView itemListView;
	private ItemListViewAdapter adapter;
	
	private int reportServerID;
	private Report report;
	private int senderID;
	private User sender;
	private List<Item> itemList = new ArrayList<Item>();
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_show);
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

	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.approve_reject, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_approve_item)
		{
			MobclickAgent.onEvent(ApproveReportActivity.this, "UMENG_PASS_REPORT_DETAIL");
			if (!Utils.isNetworkConnected())
			{
				Utils.showToast(this, "网络未连接，无法审批");
			}
			else
			{
				saveReport(Report.STATUS_APPROVED);
			}
			return true;
		}
		if (id == R.id.action_reject_item)
		{
			MobclickAgent.onEvent(ApproveReportActivity.this, "UMENG_REJECT_REPORT_DETAIL");
			if (!Utils.isNetworkConnected())
			{
				Utils.showToast(this, "网络未连接，无法审批");
			}
			else
			{
				saveReport(Report.STATUS_REJECTED);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		itemList = dbManager.getOthersReportItems(reportServerID);
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
					Utils.showToast(ApproveReportActivity.this, "网络未连接，无法添加评论");
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
				Intent intent = new Intent(ApproveReportActivity.this, CommentActivity.class);
				intent.putExtra("reportServerID", report.getServerID());	
				startActivity(intent);
			}
		});

		LinearLayout senderLayout = (LinearLayout)findViewById(R.id.senderLayout);
		senderLayout.setVisibility(View.VISIBLE);
		
		titleTextView = (TextView)findViewById(R.id.titleTextView);
		senderTextView = (TextView)findViewById(R.id.senderTextView);
		senderImageView = (ImageView)findViewById(R.id.senderImageView);
		managerTextView = (TextView)findViewById(R.id.managerTextView);
		ccTextView = (TextView)findViewById(R.id.ccTextView);
		
		if (report != null)
		{
			titleTextView.setText(report.getTitle());
			managerTextView.setText(report.getManagersName());	
			ccTextView.setText(report.getCCsName());
			
			if (report.getUser() != null)
			{
				senderID = report.getUser().getServerID();
				sender = dbManager.getUser(senderID);
				if (sender != null)
				{
					senderTextView.setText(sender.getNickname());
					if (!sender.getAvatarPath().equals("")) 
					{
						Bitmap bitmap = BitmapFactory.decodeFile(sender.getAvatarPath());
						if (bitmap != null)
						{
							senderImageView.setImageBitmap(bitmap);
						}
					}
					if (sender.hasUndownloadedAvatar() && Utils.isNetworkConnected())
					{
						sendDownloadAvatarRequest(sender);
					}
				}
				else
				{
					sendGetGroupRequest();
				}
			}
		}		
		
		adapter = new ItemListViewAdapter(ApproveReportActivity.this, itemList);
		itemListView = (ListView)findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent intent = new Intent(ApproveReportActivity.this, ShowItemActivity.class);
				intent.putExtra("othersItemServerID", itemList.get(position).getServerID());
				startActivity(intent);	
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
				titleTextView.setText(report.getTitle());
				itemList = dbManager.getOthersReportItems(reportServerID);
				adapter.set(itemList);
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
					if (report == null)
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
								AlertDialog mDialog = new AlertDialog.Builder(ApproveReportActivity.this)
													.setTitle("提示")
													.setMessage("报告已被审批")
													.setNegativeButton(R.string.confirm, 
															new DialogInterface.OnClickListener()
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
								titleTextView.setText(report.getTitle());
								managerTextView.setText(report.getManagersName());		
								ccTextView.setText(report.getCCsName());
								
								adapter.set(itemList);
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
							AlertDialog mDialog = new AlertDialog.Builder(ApproveReportActivity.this)
												.setTitle("提示")
												.setMessage("数据获取失败")
												.setNegativeButton(R.string.confirm, 
														new DialogInterface.OnClickListener()
												{
													public void onClick(DialogInterface dialog, int which)
													{
														finish();
													}
												})
												.create();
							mDialog.show();
						}
					});
				}
			}
		});
    }

	private void sendGetGroupRequest()
	{
		GetGroupRequest request = new GetGroupRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				GetGroupResponse response = new GetGroupResponse(httpResponse);
				if (response.getStatus())
				{
					DBManager dbManager = DBManager.getDBManager();
					int currentGroupID = response.getGroup() == null ? -1 : response.getGroup().getServerID();
					
					// update members
					List<User> memberList = response.getMemberList();
					User currentUser = AppPreference.getAppPreference().getCurrentUser();
					
					for (User user : memberList)
					{
						if (user.getServerID() == currentUser.getServerID())							
						{
							if (user.getServerUpdatedDate() > currentUser.getServerUpdatedDate())
							{
								if (user.getAvatarID() == currentUser.getAvatarID())
								{
									user.setAvatarPath(currentUser.getAvatarPath());								
								}								
							}
							else
							{
								user = currentUser;
							}
						}
					}
					
					dbManager.updateGroupUsers(memberList, currentGroupID);

					// update group info
					dbManager.syncGroup(response.getGroup());
					
					sender = dbManager.getUser(senderID);
					if (sender != null)
					{
						senderTextView.setText(sender.getNickname());
						if (!sender.getAvatarPath().equals("")) 
						{
							Bitmap bitmap = BitmapFactory.decodeFile(sender.getAvatarPath());
							if (bitmap != null)
							{
								senderImageView.setImageBitmap(bitmap);
							}
						}
						if (sender.hasUndownloadedAvatar() && Utils.isNetworkConnected())
						{
							sendDownloadAvatarRequest(sender);
						}
					}
				}
			}
		});
	}

    private void sendDownloadAvatarRequest(final User user)
    {
    	final DBManager dbManager = DBManager.getDBManager();
    	DownloadImageRequest request = new DownloadImageRequest(user.getAvatarID(), DownloadImageRequest.IMAGE_QUALITY_VERY_HIGH);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					String avatarPath = Utils.saveBitmapToFile(response.getBitmap(), HttpConstant.IMAGE_TYPE_AVATAR);
					user.setAvatarPath(avatarPath);
					int currentTime = Utils.getCurrentTime();
					user.setLocalUpdatedDate(currentTime);
					user.setServerUpdatedDate(currentTime);
					dbManager.updateUser(user);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							if (!user.getAvatarPath().equals("")) 
							{
								Bitmap bitmap = BitmapFactory.decodeFile(user.getAvatarPath());
								if (bitmap != null)
								{
									senderImageView.setImageBitmap(bitmap);
								}
							}
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
							AlertDialog mDialog = new AlertDialog.Builder(ApproveReportActivity.this)
												.setTitle("提示")
												.setMessage(message)
												.setNegativeButton(R.string.confirm, 
														new DialogInterface.OnClickListener()
												{
													public void onClick(DialogInterface dialog, int which)
													{
														finish();
													}
												})
												.create();
							mDialog.show();
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
