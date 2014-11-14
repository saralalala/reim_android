package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Report.CreateReportRequest;
import netUtils.Request.Report.GetReportRequest;
import netUtils.Request.Report.ModifyReportRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Report.CreateReportResponse;
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
import classes.Adapter.MemberListViewAdapater;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class EditReportActivity extends Activity
{
	private AppPreference appPreference;
	private DBManager dbManager;
	
	private EditText titleEditText;
	private TextView managerTextView;
	private TextView ccTextView;
	private ListView itemListView;
	private ItemListViewAdapter adapter;
	private MemberListViewAdapater memberAdapter;

	private Report report;
	private List<Item> itemList = null;
	private ArrayList<Integer> chosenItemIDList = null;
	private ArrayList<Integer> remainingItemIDList = null;
	
	private List<User> userList;
	private User currentUser;
	private boolean[] managerCheckList;
	private boolean[] ccCheckList;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_edit);
		initData();
		initView();
		initButton();
	}
	
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EditReportActivity");		
		MobclickAgent.onResume(this);
		refreshView();
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
			goBackToMainActivity();
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.report_edit, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_submit_item)
		{
			MobclickAgent.onEvent(EditReportActivity.this, "UMENG_POST_REPORT_DETAIL");
			if (!Utils.isNetworkConnected())
			{
				Toast.makeText(this, "网络未连接，无法提交", Toast.LENGTH_SHORT).show();
			}
			else if (report.getManagerList() == null || report.getManagerList().size() == 0)
			{
				Toast.makeText(EditReportActivity.this, "未选择汇报对象", Toast.LENGTH_SHORT).show();
			}
			else
			{
				submitReport();
			}
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
	
	private void initData()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null)
		{
			// new report from ReportFragment
			report = new Report();
			report.setStatus(Report.STATUS_DRAFT);
			report.setUser(appPreference.getCurrentUser());
			chosenItemIDList = new ArrayList<Integer>();
			remainingItemIDList = Item.getItemsIDArray(dbManager.getUnarchivedUserItems(appPreference.getCurrentUserID()));
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
				chosenItemIDList = Item.getItemsIDArray(itemList);
				remainingItemIDList = Item.getItemsIDArray(dbManager.getUnarchivedUserItems(appPreference.getCurrentUserID()));
			}
			else
			{
				// edit report from UnarchivedActivity
				remainingItemIDList = bundle.getIntegerArrayList("remainingItemIDList");	
				itemList = dbManager.getItems(chosenItemIDList);
			}
		}

    	currentUser = appPreference.getCurrentUser();
    	
    	int currentGroupID = appPreference.getCurrentGroupID();
		userList = User.removeCurrentUserFromList(dbManager.getGroupUsers(currentGroupID));
	}
	
	private void initView()
	{
		ReimApplication.setProgressDialog(this);
		
		titleEditText = (EditText)findViewById(R.id.titleEditText);
		titleEditText.setText(report.getTitle());
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECTED)
		{
			titleEditText.setFocusable(false);
		}
		if (!report.getTitle().equals(""))
		{
			titleEditText.clearFocus();
		}
		
		managerTextView = (TextView)findViewById(R.id.managerTextView);
		managerTextView.setText(report.getManagersName());
		
		ccTextView = (TextView)findViewById(R.id.ccTextView);
		ccTextView.setText(report.getCCsName());
		
		adapter = new ItemListViewAdapter(EditReportActivity.this, itemList);
		itemListView = (ListView)findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECTED)
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
		if (report.getStatus() == Report.STATUS_DRAFT || report.getStatus() == Report.STATUS_REJECTED)
		{
			registerForContextMenu(itemListView);			
		}
	}
	
	private void initButton()
	{
		Button addCommentButton = (Button)findViewById(R.id.addCommentButton);
		addCommentButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!Utils.isNetworkConnected())
				{
					Toast.makeText(EditReportActivity.this, "网络未连接，无法添加评论", Toast.LENGTH_SHORT).show();
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
				Intent intent = new Intent(EditReportActivity.this, CommentActivity.class);
				intent.putExtra("reportLocalID", report.getLocalID());
				startActivity(intent);
			}
		});
		
		Button managerButton = (Button)findViewById(R.id.managerButton);
		managerButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showManagerDialog();
			}
		});
		
		Button ccButton = (Button)findViewById(R.id.ccButton);
		ccButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showCCDialog();
			}
		});
		
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
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECTED)
		{
			addButton.setVisibility(View.GONE);
		}
		
		Button saveButton = (Button)findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				hideSoftKeyboard();
				saveReport("报告保存成功");
			}
		});
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECTED)
		{
			saveButton.setEnabled(false);
		}
		
		Button cancelButton = (Button)findViewById(R.id.rejectButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				goBackToMainActivity();
			}
		});
	}
	
	private void refreshView()
	{
		adapter.set(itemList);
		adapter.notifyDataSetChanged();
		
		if (report.getServerID() != -1 && Utils.isNetworkConnected())
		{
			sendGetReportRequest(report.getServerID());
		}
	}	
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
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
											Toast.makeText(EditReportActivity.this, "评论不能为空", Toast.LENGTH_SHORT).show();
										}
										else
										{
											if (report.getServerID() == -1)
											{
												sendCreateReportRequest(comment);
											}
											else
											{
												sendModifyReportRequest(comment);
											}
										}
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
		mDialog.show();
    }
    
    private void showManagerDialog()
    {
    	hideSoftKeyboard();
		if (report.getManagerList() == null)
		{
			List<User> tempList = new ArrayList<User>();
			User defaultManager = dbManager.getUser(currentUser.getDefaultManagerID());
			if (defaultManager != null)
			{
				tempList.add(defaultManager);				
			}
			managerCheckList = User.getUsersCheck(userList, tempList);
		}
		else
		{
			managerCheckList = User.getUsersCheck(userList, report.getManagerList());
		}
		
    	memberAdapter = new MemberListViewAdapater(this, userList, managerCheckList);
    	View view = View.inflate(this, R.layout.profile_user, null);
    	ListView userListView = (ListView) view.findViewById(R.id.userListView);
    	userListView.setAdapter(memberAdapter);
    	userListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				managerCheckList[position] = !managerCheckList[position];
				memberAdapter.setCheck(managerCheckList);
				memberAdapter.notifyDataSetChanged();
			}
		});

    	AlertDialog mDialog = new AlertDialog.Builder(this)
    							.setTitle("请选择汇报对象")
    							.setView(view)
    							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										List<User> managerList = new ArrayList<User>();
										for (int i = 0; i < managerCheckList.length; i++)
										{
											if (managerCheckList[i])
											{
												managerList.add(userList.get(i));
											}
										}

										report.setManagerList(managerList);
										managerTextView.setText(report.getManagersName());
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
    	mDialog.show();
    	
    	for (User user : userList)
		{
			if (user.getAvatarPath().equals("") && user.getImageID() != -1)
			{
				sendDownloadAvatarRequest(user);
			}	
		}
    }
    
    private void showCCDialog()
    {
    	hideSoftKeyboard();
    	ccCheckList = User.getUsersCheck(userList, report.getCCList());
		
    	memberAdapter = new MemberListViewAdapater(this, userList, ccCheckList);
    	View view = View.inflate(this, R.layout.profile_user, null);
    	ListView userListView = (ListView) view.findViewById(R.id.userListView);
    	userListView.setAdapter(memberAdapter);
    	userListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				ccCheckList[position] = !ccCheckList[position];
				memberAdapter.setCheck(ccCheckList);
				memberAdapter.notifyDataSetChanged();
			}
		});

    	AlertDialog mDialog = new AlertDialog.Builder(this)
    							.setTitle("请选择抄送对象")
    							.setView(view)
    							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										List<User> ccList = new ArrayList<User>();
										for (int i = 0; i < ccCheckList.length; i++)
										{
											if (ccCheckList[i])
											{
												ccList.add(userList.get(i));
											}
										}
										
										report.setCCList(ccList);
										ccTextView.setText(report.getCCsName());
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.create();
    	mDialog.show();
		
		if (Utils.isNetworkConnected())
		{
	    	for (User user : userList)
			{
				if (user.getAvatarPath().equals("") && user.getImageID() != -1)
				{
					sendDownloadAvatarRequest(user);
				}	
			}		
		}
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
			AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
									.setTitle("提示")
									.setMessage(prompt)
									.setNegativeButton(R.string.confirm, 
											new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int which)
										{
											goBackToMainActivity();
										}
									})
									.create();
			mDialog.show();
			if (Utils.canSyncToServer())
			{
				SyncUtils.syncAllToServer(new SyncDataCallback()
				{
					public void execute()
					{
						report = dbManager.getReportByLocalID(report.getLocalID());
 					}
				});
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
			if (!report.hasItems())
			{
				if (report.getStatus() != Report.STATUS_REJECTED)
				{
					report.setStatus(Report.STATUS_DRAFT);					
				}
				AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
											.setTitle("无法提交报告")
											.setMessage("此报告为空报告")
											.setNegativeButton(R.string.confirm, null)
											.create();
				mDialog.show();
			}
			else if (appPreference.getCurrentGroupID() == -1)
			{
				report.setStatus(Report.STATUS_FINISHED);
				AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
											.setTitle("提示")
											.setMessage("报告提交成功")
											.setNegativeButton(R.string.confirm, 
													new DialogInterface.OnClickListener()
											{
												public void onClick(DialogInterface dialog, int which)
												{
													goBackToMainActivity();
												}
											})
											.create();
				mDialog.show();
			}
			else
			{
				report.setStatus(Report.STATUS_SUBMITTED);		
				AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
										.setTitle("提示")
										.setMessage("报告提交成功")
										.setNegativeButton(R.string.confirm, 
												new DialogInterface.OnClickListener()
										{
											public void onClick(DialogInterface dialog, int which)
											{
												goBackToMainActivity();
											}
										})
										.create();
				mDialog.show();	
			}
			dbManager.updateReportByLocalID(report);
			if (Utils.canSyncToServer())
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

    private void sendDownloadAvatarRequest(final User user)
    {
    	final DBManager dbManager = DBManager.getDBManager();
    	DownloadImageRequest request = new DownloadImageRequest(user.getImageID());
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					String avatarPath = Utils.saveBitmapToFile(response.getBitmap(), HttpConstant.IMAGE_TYPE_AVATAR);
					user.setAvatarPath(avatarPath);
					user.setLocalUpdatedDate(Utils.getCurrentTime());
					user.setServerUpdatedDate(user.getLocalUpdatedDate());
					dbManager.updateUser(user);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							List<User> memberList = dbManager.getGroupUsers(appPreference.getCurrentGroupID());
							memberAdapter.setMember(User.removeCurrentUserFromList(memberList));
							memberAdapter.notifyDataSetChanged();
						}
					});	
				}
			}
		});
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
					if (report.getLocalUpdatedDate() <= response.getReport().getServerUpdatedDate())
					{
						report.setManagerList(response.getReport().getManagerList());
						report.setCCList(response.getReport().getCCList());
						report.setCommentList(response.getReport().getCommentList());
						dbManager.updateReportByLocalID(report);
						
						dbManager.deleteReportComments(report.getLocalID());
						for (Comment comment : report.getCommentList())
						{
							comment.setReportID(report.getLocalID());
							dbManager.insertComment(comment);
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
							Toast.makeText(EditReportActivity.this, "获取详细信息失败", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		});
    }
    
    private void sendCreateReportRequest(final String commentContent)
    {
    	ReimApplication.showProgressDialog();

		report.setTitle(titleEditText.getText().toString());
		report.setCreatedDate(Utils.getCurrentTime());
		report.setLocalUpdatedDate(report.getCreatedDate());
		dbManager.insertReport(report);
		report.setLocalID(dbManager.getLastInsertReportID());
		dbManager.updateReportItems(chosenItemIDList, report.getLocalID());    	
    	
    	CreateReportRequest request = new CreateReportRequest(report, commentContent);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CreateReportResponse response = new CreateReportResponse(httpResponse);
				if (response.getStatus())
				{
					int currentTime = Utils.getCurrentTime();
					
					report.setServerID(response.getReportID());
					report.setServerUpdatedDate(currentTime);
					report.setLocalUpdatedDate(currentTime);
					dbManager.updateReportByLocalID(report);
					
					Comment comment = new Comment();
					comment.setContent(commentContent);
					comment.setCreatedDate(currentTime);
					comment.setLocalUpdatedDate(currentTime);
					comment.setServerUpdatedDate(currentTime);
					comment.setReportID(report.getLocalID());
					comment.setReviewer(currentUser);
					dbManager.insertComment(comment);					
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{							
							ReimApplication.dismissProgressDialog();
							Toast.makeText(EditReportActivity.this, "评论发表成功", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(EditReportActivity.this, "评论发表失败, " + response.getErrorMessage(), Toast.LENGTH_SHORT).show();
						}
					});					
				}
			}
		});
    }
    
    private void sendModifyReportRequest(final String commentContent)
    {
    	ReimApplication.showProgressDialog();
    	
		report.setTitle(titleEditText.getText().toString());
		dbManager.updateReportByLocalID(report);
		dbManager.updateReportItems(chosenItemIDList, report.getLocalID());
		
    	ModifyReportRequest request = new ModifyReportRequest(report, commentContent);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final ModifyReportResponse response = new ModifyReportResponse(httpResponse);
				if (response.getStatus())
				{
					int currentTime = Utils.getCurrentTime();
					
					report.setServerUpdatedDate(currentTime);
					report.setLocalUpdatedDate(currentTime);
					dbManager.updateReportByLocalID(report);
					
					Comment comment = new Comment();
					comment.setContent(commentContent);
					comment.setCreatedDate(currentTime);
					comment.setLocalUpdatedDate(currentTime);
					comment.setServerUpdatedDate(currentTime);
					comment.setReportID(report.getLocalID());
					comment.setReviewer(currentUser);
					dbManager.insertComment(comment);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							Toast.makeText(EditReportActivity.this, "评论发表成功", Toast.LENGTH_SHORT).show();
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
							Toast.makeText(EditReportActivity.this, "评论发表失败, " + response.getErrorMessage(), Toast.LENGTH_SHORT).show();
						}
					});					
				}
			}
		});
    }

    private void goBackToMainActivity()
    {
    	ReimApplication.setTabIndex(1);
    	ReimApplication.setReportTabIndex(0);
    	Intent intent = new Intent(EditReportActivity.this, MainActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    	finish();
    }
}