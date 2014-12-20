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
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ManagerActivity extends Activity
{
	private ListView managerListView;
	private MemberListViewAdapter adapter;

	private AppPreference appPreference;
	private DBManager dbManager;

	private int currentGroupID;
	private User currentUser;
	private List<User> userList;
	private boolean[] checkList;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_default_manager);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ManagerActivity");		
		MobclickAgent.onResume(this);
		ReimApplication.setProgressDialog(this);
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
    	currentGroupID = appPreference.getCurrentGroupID();
    	
		userList = User.removeCurrentUserFromList(dbManager.getGroupUsers(currentGroupID));
		checkList = User.getUsersCheck(userList, currentUser.constructListWithManager());
	}
	
	private void initView()
	{
		getActionBar().hide();
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
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
				
				User manager = null;
				for (int i = 0; i < checkList.length; i++)
				{
					if (checkList[i])
					{
						manager = userList.get(i);
						break;
					}
				}
				
				if (!Utils.isNetworkConnected())
				{
					Utils.showToast(ManagerActivity.this, "网络未连接，无法保存");
				}
				else if (manager == null)
				{
					sendDefaultManagerRequest(-1);			
				}
				else if (manager.getServerID() == currentUser.getServerID())
				{
					Utils.showToast(ManagerActivity.this, "不能选择自己作为上级");				
				}
				else if (manager.getServerID() == currentUser.getDefaultManagerID())
				{
					Utils.showToast(ManagerActivity.this, "与原有默认上级相同，无需保存");				
				}
				else
				{
					sendDefaultManagerRequest(manager.getServerID());
				}
			}
		});
		
		managerListView = (ListView)findViewById(R.id.userListView);
		managerListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				for (int i = 0; i < checkList.length; i++)
				{
					checkList[i] = false;
				}
				checkList[position] = true;
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
					
					currentGroupID = appPreference.getCurrentGroupID();
					currentUser = appPreference.getCurrentUser();
					
					dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);
					dbManager.syncUser(currentUser);

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
							Builder builder = new Builder(ManagerActivity.this);
							builder.setTitle(R.string.tip);
							builder.setMessage(R.string.prompt_default_manager_changed);
							builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener()
																{
																	public void onClick(DialogInterface dialog, int which)
																	{
																		finish();
																	}
																});
							builder.create().show();
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
    	DownloadImageRequest request = new DownloadImageRequest(user.getAvatarID(), DownloadImageRequest.IMAGE_QUALITY_VERY_HIGH);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				DownloadImageResponse response = new DownloadImageResponse(httpResponse);
				if (response.getBitmap() != null)
				{
					System.out.println("download succeed:"+user.getNickname()+"-"+user.getAvatarID());
					String avatarPath = Utils.saveBitmapToFile(response.getBitmap(), HttpConstant.IMAGE_TYPE_AVATAR);
					user.setAvatarPath(avatarPath);
					user.setLocalUpdatedDate(Utils.getCurrentTime());
					user.setServerUpdatedDate(user.getLocalUpdatedDate());
					dbManager.updateUser(user);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							userList = User.removeCurrentUserFromList(dbManager.getGroupUsers(currentGroupID));
							adapter.setMember(userList);
							adapter.notifyDataSetChanged();
						}
					});	
				}
				else
				{
					System.out.println("download failed:"+user.getNickname()+"-"+user.getAvatarID());					
				}
			}
		});
    }
}