package com.rushucloud.reim.me;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import netUtils.HttpConnectionCallback;
import netUtils.Request.User.GetInvitesRequest;
import netUtils.Response.User.GetInvitesResponse;
import classes.Invite;
import classes.ReimApplication;
import classes.Utils;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MessageActivity extends Activity
{
	private ListView messageListView;
	private TextView messageTextView;
	private SimpleAdapter adapter;
	private List<Invite> messageList = new ArrayList<Invite>();
	private List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_messages);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("MessageActivity");		
		MobclickAgent.onResume(this);
		if (Utils.isNetworkConnected())
		{
			sendGetInvitesRequest();			
		}
		else
		{
			Utils.showToast(MessageActivity.this, "网络未连接，获取数据失败");
			messageListView.setVisibility(View.GONE);
			messageTextView.setVisibility(View.VISIBLE);
		}
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("MessageActivity");
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
	
	private void initView()
	{	
		getActionBar().hide();
		
		ReimApplication.setProgressDialog(this);

		messageTextView = (TextView)findViewById(R.id.messageTextView);

		mapList = Invite.getMessageList(null);
		String[] columns = new String[]{ "message", "time" };
		int[] views = new int[]{android.R.id.text1, android.R.id.text2};
		adapter = new SimpleAdapter(this, mapList, android.R.layout.simple_list_item_2, columns, views);
		messageListView = (ListView)findViewById(R.id.messageListView);
		messageListView.setAdapter(adapter);
		messageListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Bundle bundle = new Bundle();
				bundle.putSerializable("invite", messageList.get(position));
				Intent intent = new Intent(MessageActivity.this, InviteReplyActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
	}
	
    private void sendGetInvitesRequest()
    {
    	ReimApplication.showProgressDialog();
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
							ReimApplication.dismissProgressDialog();
							messageList = response.getInviteList();
							if (messageList.size() == 0)
							{
								messageListView.setVisibility(View.GONE);
								messageTextView.setVisibility(View.VISIBLE);
							}
							else
							{
								mapList = Invite.getMessageList(messageList);
								String[] columns = new String[]{"message", "time"};
								int[] views = new int[]{android.R.id.text1, android.R.id.text2};
								adapter = new SimpleAdapter(MessageActivity.this, mapList, android.R.layout.simple_list_item_2, columns, views);
								messageListView.setAdapter(adapter);
								messageTextView.setVisibility(View.GONE);
								messageListView.setVisibility(View.VISIBLE);
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
							Builder builder = new Builder(MessageActivity.this);
							builder.setTitle(R.string.tip);
							builder.setMessage("获取邀请列表失败");
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
			}
		});
    }
}
