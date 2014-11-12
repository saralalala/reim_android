package com.rushucloud.reim.me;

import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.HttpConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Request.User.DefaultManagerRequest;
import netUtils.Response.DownloadImageResponse;
import netUtils.Response.User.DefaultManagerResponse;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import classes.AppPreference;
import classes.ReimApplication;
import classes.User;
import classes.Utils;
import classes.Adapter.MemberListViewAdapater;
import database.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class ManagerActivity extends Activity
{
	private ListView managerListView;
	private MemberListViewAdapater adapter;

	private AppPreference appPreference;
	private DBManager dbManager;
	
	private List<User> userList;
	private User currentUser;
	private int lastIndex = -1;
	private boolean[] checkList;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_user);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("ManagerActivity");		
		MobclickAgent.onResume(this);
		ReimApplication.setProgressDialog(this);
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

	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.save, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_save_item)
		{
			MobclickAgent.onEvent(ManagerActivity.this, "UMENG_MINE_CHANGE_USERINFO");
			if (!Utils.isNetworkConnected())
			{
				Toast.makeText(this, "网络未连接，无法保存", Toast.LENGTH_SHORT).show();
			}
			else if (lastIndex == -1)
			{
				sendDefaultManagerRequest(-1);			
			}
			else if (userList.get(lastIndex).getServerID() == currentUser.getServerID())
			{
				Toast.makeText(this, "不能选择自己作为上级", Toast.LENGTH_SHORT).show();				
			}
			else if (userList.get(lastIndex).getServerID() == currentUser.getDefaultManagerID())
			{
				Toast.makeText(this, "与原有默认上级相同，无需保存", Toast.LENGTH_SHORT).show();				
			}
			else
			{
				sendDefaultManagerRequest(userList.get(lastIndex).getServerID());
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		adapter = new MemberListViewAdapater(this, userList, checkList);
		managerListView = (ListView)findViewById(R.id.userListView);
		managerListView.setAdapter(adapter);
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
		
		for (User user : userList)
		{
			if (user.getAvatarPath().equals("") && user.getImageID() != -1)
			{
				sendDownloadAvatarRequest(user);
			}
		}
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
							Toast.makeText(ManagerActivity.this, "默认上级修改失败", Toast.LENGTH_SHORT).show();							
						}
					});
				}
			}
		});
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
