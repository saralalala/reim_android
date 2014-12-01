package com.rushucloud.reim.me;

import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.Group.GetGroupRequest;
import netUtils.Request.User.DefaultManagerRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.Group.GetGroupResponse;
import netUtils.Response.User.DefaultManagerResponse;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.AppPreference;
import classes.ReimApplication;
import classes.User;
import classes.Utils;
import classes.Adapter.MemberListViewAdapter;
import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ManagerActivity extends Activity
{
	private ListView managerListView;
	private MemberListViewAdapter adapter;

	private AppPreference appPreference;
	private DBManager dbManager;
	
	private List<User> userList;
	private User currentUser;
	private int lastIndex = -1;
	private boolean[] checkList;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.me_default_manager);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ManagerActivity");		
		MobclickAgent.onResume(this);
		if (Utils.isNetworkConnected())
		{
			sendGetGroupRequest();
		}
		else
		{
			initData();
			refreshListView();
		}
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("ManagerActivity");
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
		
		currentUser = appPreference.getCurrentUser();
		
    	int currentGroupID = appPreference.getCurrentGroupID();
		userList = User.removeCurrentUserFromList(dbManager.getGroupUsers(currentGroupID));
		
		checkList = new boolean[userList.size()];
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
	}
	
	private void initView()
	{		
		getActionBar().hide();
		ReimApplication.setProgressDialog(this);
		
		TextView cancelTextView = (TextView)findViewById(R.id.cancelTextView);
		cancelTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
		
		TextView confirmTextView = (TextView)findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MobclickAgent.onEvent(ManagerActivity.this, "UMENG_MINE_CHANGE_USERINFO");
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(ManagerActivity.this, "网络未连接，无法保存");
				}
				else if (lastIndex == -1)
				{
					sendDefaultManagerRequest(-1);			
				}
				else if (userList.get(lastIndex).getServerID() == currentUser.getServerID())
				{
					Utils.showToast(ManagerActivity.this, "不能选择自己作为上级");				
				}
				else if (userList.get(lastIndex).getServerID() == currentUser.getDefaultManagerID())
				{
					Utils.showToast(ManagerActivity.this, "与原有默认上级相同，无需保存");				
				}
				else
				{
					sendDefaultManagerRequest(userList.get(lastIndex).getServerID());
				}
			}
		});
		
		managerListView = (ListView)findViewById(R.id.userListView);
		managerListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if (lastIndex == position)
				{
					checkList[position] = !checkList[position];
					lastIndex = checkList[position] ? position : -1;
				}
				else
				{
					if (lastIndex != -1)
					{
						checkList[lastIndex] = false;					
					}
					checkList[position] = true;
					lastIndex = position;
				}
				adapter.setCheck(checkList);
				adapter.notifyDataSetChanged();
			}
		});	
	}
	
	private void refreshListView()
	{
		adapter = new MemberListViewAdapter(this, userList, checkList);
		managerListView.setAdapter(adapter);	
		
		if (Utils.isNetworkConnected())
		{
			for (User user : userList)
			{
				if (user.hasUndownloadedAvatar())
				{
					sendDownloadAvatarRequest(user);
				}
			}
		}	
	}

	private void sendGetGroupRequest()
	{
		ReimApplication.showProgressDialog();
		GetGroupRequest request = new GetGroupRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				GetGroupResponse response = new GetGroupResponse(httpResponse);
				if (response.getStatus())
				{
					appPreference = AppPreference.getAppPreference();
					dbManager = DBManager.getDBManager();
					
					List<User> memberList = response.getMemberList();
					User currentUser = appPreference.getCurrentUser();
					
					for (User user : memberList)
					{
						if (user.getServerID() == currentUser.getServerID())							
						{
							if (user.getServerUpdatedDate() > currentUser.getServerUpdatedDate())
							{
								if (user.getImageID() == currentUser.getImageID())
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
					
					dbManager.updateGroupUsers(memberList, appPreference.getCurrentGroupID());

					dbManager.syncGroup(response.getGroup());
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							initData();
							refreshListView();
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
							Utils.showToast(ManagerActivity.this, "刷新数据失败");
						}
					});
				}
			}
		});
	}
	
	private void sendDefaultManagerRequest(final int newManagerID)
	{
		ReimApplication.showProgressDialog();
		DefaultManagerRequest request = new DefaultManagerRequest(newManagerID);
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DefaultManagerResponse response = new DefaultManagerResponse(httpResponse);
				if (response.getStatus())
				{
					currentUser.setDefaultManagerID(newManagerID);
					currentUser.setLocalUpdatedDate(Utils.getCurrentTime());
					currentUser.setServerUpdatedDate(currentUser.getLocalUpdatedDate());
					dbManager.updateUser(currentUser);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.dismissProgressDialog();
							AlertDialog mDialog = new AlertDialog.Builder(ManagerActivity.this)
																.setTitle("提示")
																.setMessage("默认上级修改成功")
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
							Utils.showToast(ManagerActivity.this, "默认上级修改失败");							
						}
					});
				}
			}
		});
	}

    private void sendDownloadAvatarRequest(final User user)
    {
    	final DBManager dbManager = DBManager.getDBManager();
    	DownloadImageRequest request = new DownloadImageRequest(user.getImageID(), DownloadImageRequest.IMAGE_QUALITY_VERY_HIGH);
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
							userList = dbManager.getGroupUsers(appPreference.getCurrentGroupID());
							adapter.setMember(userList);
							adapter.notifyDataSetChanged();
						}
					});	
				}
			}
		});
    }
}
