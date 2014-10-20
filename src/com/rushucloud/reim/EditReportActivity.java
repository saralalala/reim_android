package com.rushucloud.reim;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.SyncDataCallback;
import netUtils.SyncUtils;
import netUtils.Request.DownloadImageRequest;
import netUtils.Response.DownloadImageResponse;
import classes.AppPreference;
import classes.Item;
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
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class EditReportActivity extends Activity
{
	private AppPreference appPreference;
	private DBManager dbManager;
	
	private EditText titleEditText;
	private ListView itemListView;
	private ItemListViewAdapter adapter;
	private MemberListViewAdapater memberAdapter;
	
	private Report report;
	private List<Item> itemList = null;
	private ArrayList<Integer> chosenItemIDList = null;
	private ArrayList<Integer> remainingItemIDList = null;
	
	private List<User> userList;
	private User currentUser;
	private int lastIndex = -1;
	private boolean[] checkList;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_edit);
		dataInitialise();
		viewInitialise();
		buttonInitialise();
	}
	
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("EditReportActivity");		
		MobclickAgent.onResume(this);
		refreshListView();
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
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onCreateOptionsMenu(Menu menu)
	{
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
		{
			return false;
		}
		else if (report.getStatus() == Report.STATUS_SUBMITTED && 
				report.getUser().getServerID() != appPreference.getCurrentUserID())
		{
			getMenuInflater().inflate(R.menu.approve_reject, menu);
			return true;	
		}
		else
		{
			getMenuInflater().inflate(R.menu.submit, menu);
			return true;
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_submit_item)
		{
			showManagerDialog();
			return true;
		}
		if (id == R.id.action_approve_item)
		{
			report.setStatus(Report.STATUS_APPROVED);
			saveReport("报告已批准");
			return true;
		}
		if (id == R.id.action_reject_item)
		{
			report.setStatus(Report.STATUS_REJECT);
			saveReport("报告已拒绝");
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
	
	private void dataInitialise()
	{
		appPreference = AppPreference.getAppPreference();
		dbManager = DBManager.getDBManager();
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null)
		{
			// new report from ReportFragment
			report = new Report();
			report.setStatus(Report.STATUS_DRAFT);
			report.setUser(dbManager.getUser(appPreference.getCurrentUserID()));
			chosenItemIDList = new ArrayList<Integer>();
			remainingItemIDList = Utils.itemListToIDArray(dbManager.getUnarchivedUserItems(appPreference.getCurrentUserID()));
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
				chosenItemIDList = Utils.itemListToIDArray(itemList);
				remainingItemIDList = Utils.itemListToIDArray(dbManager.getUnarchivedUserItems(appPreference.getCurrentUserID()));
			}
			else
			{
				// edit report from UnarchivedActivity
				remainingItemIDList = bundle.getIntegerArrayList("remainingItemIDList");	
				itemList = dbManager.getItems(chosenItemIDList);
			}
		}

    	currentUser = dbManager.getUser(appPreference.getCurrentUserID());
    	userList = dbManager.getGroupUsers(appPreference.getCurrentGroupID());
    	checkList = new boolean[userList.size()];
	}
	
	private void viewInitialise()
	{
		titleEditText = (EditText)findViewById(R.id.titleEditText);
		titleEditText.setText(report.getTitle());
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
		{
			titleEditText.setFocusable(false);
		}
		if (!report.getTitle().equals(""))
		{
			hideSoftKeyboard();
		}
		
		adapter = new ItemListViewAdapter(EditReportActivity.this, itemList);
		itemListView = (ListView)findViewById(R.id.itemListView);
		itemListView.setAdapter(adapter);
		itemListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
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
		if (report.getStatus() == Report.STATUS_DRAFT || report.getStatus() == Report.STATUS_REJECT)
		{
			registerForContextMenu(itemListView);			
		}
	}
	
	private void buttonInitialise()
	{
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
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
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
		if (report.getStatus() != Report.STATUS_DRAFT && report.getStatus() != Report.STATUS_REJECT)
		{
			saveButton.setEnabled(false);
		}
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
	}
	
	private void refreshListView()
	{
		adapter.set(itemList);
		adapter.notifyDataSetChanged();
	}	
	
    private void hideSoftKeyboard()
    {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
    }
    
    private void showManagerDialog()
    {
    	lastIndex = -1;
    	for (int i = 0; i < checkList.length; i++)
		{
			if (currentUser.getDefaultManagerID() == userList.get(i).getServerID())
			{
				checkList[i] = true;
				lastIndex = i;
			}
			else
			{
				checkList[i] = false;
			}
		}	
    	
    	memberAdapter = new MemberListViewAdapater(this, userList, checkList);
    	View view = View.inflate(this, R.layout.profile_manager, null);
    	ListView managerListView = (ListView) view.findViewById(R.id.managerListView);
    	managerListView.setAdapter(memberAdapter);
    	managerListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (lastIndex != -1)
				{
					checkList[lastIndex] = false;					
				}
				checkList[position] = true;
				lastIndex = position;
				memberAdapter.setCheck(checkList);
				memberAdapter.notifyDataSetChanged();
			}
		});

    	hideSoftKeyboard();
    	AlertDialog mDialog = new AlertDialog.Builder(this)
    							.setTitle("请选择汇报对象")
    							.setView(view)
    							.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which)
									{
										if (lastIndex == -1)
										{
											Toast.makeText(EditReportActivity.this, "未选择汇报对象", Toast.LENGTH_SHORT).show();
										}
										else
										{
											report.setManagerID(userList.get(lastIndex).getServerID());
											submitReport();
										}
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
			Toast.makeText(EditReportActivity.this, prompt, Toast.LENGTH_SHORT).show();
			if (Utils.canSyncToServer(EditReportActivity.this))
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
			boolean canBeSubmitted = report.canBeSubmitted();
			if (!canBeSubmitted)
			{
				report.setStatus(Report.STATUS_DRAFT);
				AlertDialog mDialog = new AlertDialog.Builder(EditReportActivity.this)
											.setTitle("无法提交报告")
											.setMessage("此报告为空报告或者部分条目信息未上传到服务器")
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
													finish();
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
												finish();
											}
										})
										.create();
				mDialog.show();	
			}
			dbManager.updateReportByLocalID(report);
			if (Utils.canSyncToServer(EditReportActivity.this))
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
							List<User> managerList = dbManager.getGroupUsers(appPreference.getCurrentGroupID());
							memberAdapter.setMember(managerList);
							memberAdapter.notifyDataSetChanged();
						}
					});	
				}
			}
		});
    }
}