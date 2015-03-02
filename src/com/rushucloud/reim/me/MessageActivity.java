package com.rushucloud.reim.me;

import java.util.ArrayList;
import java.util.List;
import netUtils.HttpConnectionCallback;
import netUtils.Response.User.GetInvitesResponse;
import netUtils.Request.User.GetInvitesRequest;
import classes.Invite;
import classes.adapter.MessageListViewAdapter;
import classes.utils.PhoneUtils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;

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
import android.widget.TextView;

public class MessageActivity extends Activity
{
	private TextView messageTextView;
	private ListView messageListView;
	private MessageListViewAdapter adapter;
	
	private List<Invite> messageList = new ArrayList<Invite>();
	
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
		ReimProgressDialog.setProgressDialog(this);
		if (PhoneUtils.isNetworkConnected())
		{
			sendGetInvitesRequest();			
		}
		else
		{
			ViewUtils.showToast(MessageActivity.this, R.string.error_get_data_network_unavailable);
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
		
		ImageView backImageView = (ImageView) findViewById(R.id.backImageView);
		backImageView.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});	

		messageTextView = (TextView) findViewById(R.id.messageTextView);

		messageListView = (ListView) findViewById(R.id.messageListView);
		messageListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Bundle bundle = new Bundle();
				bundle.putSerializable("invite", messageList.get(position));
				Intent intent = new Intent(MessageActivity.this, MessageDetailActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
    private void sendGetInvitesRequest()
    {
		ReimProgressDialog.show();
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
							ReimProgressDialog.dismiss();
							messageList = response.getInviteList();
							
							if (messageList.isEmpty())
							{
								messageListView.setVisibility(View.GONE);
								messageTextView.setVisibility(View.VISIBLE);
							}
							else
							{
								Invite.sortByUpdateDate(messageList);
								adapter = new MessageListViewAdapter(MessageActivity.this, messageList);
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
							ReimProgressDialog.dismiss();
							Builder builder = new Builder(MessageActivity.this);
							builder.setTitle(R.string.tip);
							builder.setMessage(R.string.prompt_invite_list_failed);
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