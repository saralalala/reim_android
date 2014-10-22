package com.rushucloud.reim.me;

import org.json.JSONException;
import org.json.JSONObject;

import netUtils.HttpConnectionCallback;
import netUtils.Request.User.InviteReplyRequest;
import netUtils.Response.User.InviteReplyResponse;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

public class InvitedActivity extends Activity
{	
	private int inviteCode;
	private String message;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_invited);
		dataInitialise();
		viewInitialise();
		buttonInitialise();
	}

	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("InvitedActivity");		
		MobclickAgent.onResume(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("InvitedActivity");
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
	
	private void dataInitialise()
	{
		Intent intent = getIntent();
		try
		{
			JSONObject jObject = new JSONObject(intent.getStringExtra("data"));
			message = jObject.getString("msg");
			inviteCode = jObject.getInt("code");
		}
		catch (JSONException e)
		{
			message = "数据读取出错了！";
			inviteCode = -1;
		}
	}
	
	private void viewInitialise()
	{
		TextView textView = (TextView)findViewById(R.id.inviteTextView);
		textView.setText(message);
	}
	
	private void buttonInitialise()
	{	
		Button confirmButton = (Button)findViewById(R.id.confirmButton);
		confirmButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				sendInviteReplyRequest(1, inviteCode);
			}
		});
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (inviteCode == -1)
				{
					finish();					
				}
				else
				{
					sendInviteReplyRequest(0, inviteCode);
				}
			}
		});
		
		if (inviteCode == -1)
		{
			confirmButton.setVisibility(View.GONE);
			cancelButton.setText(R.string.cancel);
		}
	}
	
    private void sendInviteReplyRequest(int agree, int inviteCode)
    {
    	InviteReplyRequest request = new InviteReplyRequest(agree, inviteCode);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				InviteReplyResponse response = new InviteReplyResponse(httpResponse);
				if (response.getStatus())
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							AlertDialog mDialog = new AlertDialog.Builder(InvitedActivity.this)
														.setTitle("提示")
														.setMessage("邀请回复已发送成功！")
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
							AlertDialog mDialog = new AlertDialog.Builder(InvitedActivity.this)
														.setTitle("提示")
														.setMessage("邀请回复发送失败")
														.setNegativeButton(R.string.confirm, null)
														.create();
							mDialog.show();
						}						
					});
				}
			}
		});
    }
}