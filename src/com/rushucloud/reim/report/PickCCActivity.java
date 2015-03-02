package com.rushucloud.reim.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.NetworkConstant;
import netUtils.Request.DownloadImageRequest;
import netUtils.Response.DownloadImageResponse;

import classes.User;
import classes.adapter.MemberListViewAdapter;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.PhoneUtils;
import classes.utils.Utils;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class PickCCActivity extends Activity
{
	private MemberListViewAdapter adapter;

	private DBManager dbManager;
	private List<User> userList;
	private boolean[] check;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_cc);
		initData();
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("PickCCActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("PickCCActivity");
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
	
	@SuppressWarnings("unchecked")
	private void initData()
	{
		dbManager = DBManager.getDBManager();
		
		int currentGroupID = AppPreference.getAppPreference().getCurrentGroupID();
		int currentUserID = AppPreference.getAppPreference().getCurrentUserID();
		int senderID = getIntent().getIntExtra("sender", -1);
		userList = User.removeUserFromList(dbManager.getGroupUsers(currentGroupID), currentUserID);
		if (senderID != -1)
		{
			userList = User.removeUserFromList(userList, senderID);
		}

		List<User> ccList = (List<User>) getIntent().getSerializableExtra("ccs");
		check = User.getUsersCheck(userList, ccList);
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
		
		TextView confirmTextView = (TextView) findViewById(R.id.confirmTextView);
		confirmTextView.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{				
				List<User> ccList = new ArrayList<User>();
				if (check != null)
				{
					for (int i = 0; i < check.length; i++)
					{
						if (check[i])
						{
							ccList.add(userList.get(i));
						}
					}
				}
				
				Intent intent = new Intent();
				intent.putExtra("ccs", (Serializable) ccList);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		adapter = new MemberListViewAdapter(this, userList, check);
		ListView ccListView = (ListView) findViewById(R.id.ccListView);
		ccListView.setAdapter(adapter);
    	ccListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				check[position] = !check[position];
				adapter.setCheck(check);
				adapter.notifyDataSetChanged();
			}
		});

		for (User user : userList)
		{
			if (user.hasUndownloadedAvatar())
			{
				sendDownloadAvatarRequest(user);
			}
		}
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
					String avatarPath = PhoneUtils.saveBitmapToFile(response.getBitmap(), NetworkConstant.IMAGE_TYPE_AVATAR);
					user.setAvatarPath(avatarPath);
					user.setLocalUpdatedDate(Utils.getCurrentTime());
					user.setServerUpdatedDate(user.getLocalUpdatedDate());
					dbManager.updateUser(user);
					
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							int currentGroupID = AppPreference.getAppPreference().getCurrentGroupID();
							userList = dbManager.getGroupUsers(currentGroupID);
							adapter.setMember(userList);
							adapter.notifyDataSetChanged();
						}
					});	
				}
			}
		});
    }
}
