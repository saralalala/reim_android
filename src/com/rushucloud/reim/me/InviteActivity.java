package com.rushucloud.reim.me;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class InviteActivity extends Activity
{
	private ListView inviteListView;
	private TextView inviteTextView;
	private SimpleAdapter adapter;
	private List<Invite> inviteList = new ArrayList<Invite>();
	private List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
	
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

		inviteTextView = (TextView)findViewById(R.id.inviteTextView);

		mapList = Invite.getMessageList(null);
		String[] columns = new String[]{"message", "time"};
		int[] views = new int[]{android.R.id.text1, android.R.id.text2};
		adapter = new SimpleAdapter(this, mapList, android.R.layout.simple_list_item_2, columns, views);
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
				final GetInvitesResponse response = new GetInvitesResponse(httpResponse);
				if (response.getStatus())
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimApplication.pDialog.dismiss();
							inviteList = response.getInviteList();
							if (inviteList.size() == 0)
							{
								inviteListView.setVisibility(View.GONE);
							}
							else
							{
								mapList = Invite.getMessageList(inviteList);
								String[] columns = new String[]{"message", "time"};
								int[] views = new int[]{android.R.id.text1, android.R.id.text2};
								adapter = new SimpleAdapter(InviteActivity.this, mapList, android.R.layout.simple_list_item_2, columns, views);
								inviteListView.setAdapter(adapter);
								inviteTextView.setVisibility(View.GONE);
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