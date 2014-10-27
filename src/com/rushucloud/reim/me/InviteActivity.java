package com.rushucloud.reim.me;

import java.util.ArrayList;
import java.util.List;

import netUtils.HttpConnectionCallback;
import netUtils.Request.User.GetInvitesRequest;
import netUtils.Response.User.GetInvitesResponse;
import classes.Invite;
import classes.ReimApplication;
import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class InviteActivity extends Activity
{
	private ListView inviteListView;
	private ArrayAdapter<String> adapter;
	private List<Invite> inviteList = new ArrayList<Invite>();
	private List<String> messageList = new ArrayList<String>();
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_invite_list);
		viewInitialise();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("InviteActivity");		
		MobclickAgent.onResume(this);
		sendGetInvitesRequest();
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("InviteActivity");
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
	
	private void viewInitialise()
	{	
		ReimApplication.setProgressDialog(this);
		
		messageList = Invite.getMessageList(inviteList);
		String[] list =  messageList.toArray(new String[messageList.size()]);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, list);
		inviteListView = (ListView)findViewById(R.id.inviteListView);
		inviteListView.setAdapter(adapter);
		inviteListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Bundle bundle = new Bundle();
				bundle.putSerializable("invite", inviteList.get(position));
				Intent intent = new Intent(InviteActivity.this, InviteReplyActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
    private void sendGetInvitesRequest()
    {
    	ReimApplication.pDialog.show();
    	GetInvitesRequest request = new GetInvitesRequest();
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				GetInvitesResponse response = new GetInvitesResponse(httpResponse);
				if (response.getStatus())
				{
					// TODO init list
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.pDialog.dismiss();
							String[] list =  messageList.toArray(new String[messageList.size()]);
							adapter = new ArrayAdapter<String>(InviteActivity.this, android.R.layout.simple_list_item_2, list);
							inviteListView.setAdapter(adapter);
						}						
					});
				}
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
					    	ReimApplication.pDialog.dismiss();
							AlertDialog mDialog = new AlertDialog.Builder(InviteActivity.this)
														.setTitle("提示")
														.setMessage("获取邀请列表失败")
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
					});
				}
			}
		});
    }

    private void goBackToMainActivity()
    {
    	Bundle bundle = new Bundle();
    	bundle.putInt("tabIndex", 3);
    	Intent intent = new Intent(InviteActivity.this, MainActivity.class);
    	intent.putExtras(bundle);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    	finish();
    }
}