package com.rushucloud.reim.me;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import classes.Message;
import classes.adapter.MessageListViewAdapter;
import classes.utils.PhoneUtils;
import classes.utils.Utils;
import classes.utils.ViewUtils;
import classes.widget.ReimProgressDialog;
import classes.widget.XListView;
import netUtils.HttpConnectionCallback;
import netUtils.request.user.GetMessagesRequest;
import netUtils.response.user.GetMessagesResponse;

public class MessageListActivity extends Activity
{
	private TextView messageTextView;
	private XListView messageListView;
	private MessageListViewAdapter adapter;
	
	private List<Message> messageList = new ArrayList<Message>();
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_messages);
		initView();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("MessageListActivity");
		MobclickAgent.onResume(this);
		ReimProgressDialog.setContext(this);
		if (PhoneUtils.isNetworkConnected())
		{
            ReimProgressDialog.show();
			sendGetInvitesRequest();
		}
		else
		{
			ViewUtils.showToast(MessageListActivity.this, R.string.error_get_data_network_unavailable);
		}
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("MessageListActivity");
		MobclickAgent.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
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

        adapter = new MessageListViewAdapter(MessageListActivity.this, messageList);
		messageListView = (XListView) findViewById(R.id.messageListView);
        messageListView.setAdapter(adapter);
        messageListView.setXListViewListener(new XListView.IXListViewListener()
        {
            public void onRefresh()
            {
                if (PhoneUtils.isNetworkConnected())
                {
                    sendGetInvitesRequest();
                }
                else
                {
                    ViewUtils.showToast(MessageListActivity.this, R.string.error_get_data_network_unavailable);
                    messageListView.stopRefresh();
                }
            }

            public void onLoadMore()
            {

            }
        });
        messageListView.setPullRefreshEnable(true);
        messageListView.setPullLoadEnable(false);
        messageListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
		messageListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
                if (position > 0)
                {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("message", messageList.get(position - 1));
                    Intent intent = new Intent(MessageListActivity.this, MessageActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
			}
		});
	}
	
    private void sendGetInvitesRequest()
    {
    	GetMessagesRequest request = new GetMessagesRequest();
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final GetMessagesResponse response = new GetMessagesResponse(httpResponse);
				if (response.getStatus())
				{
                    messageList.clear();
                    messageList.addAll(response.getMessageList());
                    Message.sortByUpdateDate(messageList);

					runOnUiThread(new Runnable()
					{
						public void run()
						{
							ReimProgressDialog.dismiss();
                            adapter.setMessages(messageList);
                            adapter.notifyDataSetChanged();

                            int visibility = messageList.isEmpty()? View.VISIBLE : View.GONE;
                            messageTextView.setVisibility(visibility);

                            messageListView.stopRefresh();
                            messageListView.setRefreshTime(Utils.secondToStringUpToMinute(Utils.getCurrentTime()));
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
                            ViewUtils.showToast(MessageListActivity.this, R.string.failed_to_get_invite_list);
                            messageListView.stopRefresh();
						}
					});
				}
			}
		});
    }
}