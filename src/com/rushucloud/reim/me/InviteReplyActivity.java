package com.rushucloud.reim.me;

import netUtils.HttpConnectionCallback;
import netUtils.Request.CommonRequest;
import netUtils.Request.User.InviteReplyRequest;
import netUtils.Response.CommonResponse;
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
import android.widget.Toast;
import classes.AppPreference;
import classes.Invite;
import classes.ReimApplication;
import classes.User;
import classes.Utils;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;

import database.DBManager;

public class InviteReplyActivity extends Activity
{	
	private Invite invite;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_invited);
		initData();
		initView();
		initButton();
	}
  
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("InviteReplyActivity");		
		MobclickAgent.onResume(this);
		ReimApplication.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("InviteReplyActivity");
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
	
	private void initData()
	{
		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			invite = (Invite)bundle.getSerializable("invite");
		}
	}
	
	private void initView()
	{	
		TextView textView = (TextView)findViewById(R.id.inviteTextView);
		textView.setText(invite.getMessage());
	}
	
	private void initButton()
	{	
		Button agreeButton = (Button)findViewById(R.id.agreeButton);
		agreeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (Utils.isNetworkConnected())
				{
					sendInviteReplyRequest(2, invite.getInviteCode());					
				}
				else
				{
					Toast.makeText(InviteReplyActivity.this, "网络未连接，无法发送回复", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		Button rejectButton = (Button)findViewById(R.id.rejectButton);
		rejectButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (invite.getInviteCode().equals(""))
				{
					goBackToMainActivity();					
				}
				else
				{
					if (Utils.isNetworkConnected())
					{
						sendInviteReplyRequest(3, invite.getInviteCode());				
					}
					else
					{
						Toast.makeText(InviteReplyActivity.this, "网络未连接，无法发送回复", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		if (invite.getInviteCode().equals(""))
		{
			agreeButton.setVisibility(View.GONE);
			rejectButton.setText(R.string.cancel);
		}
	}
	
    private void sendInviteReplyRequest(final int agree, String inviteCode)
    {
    	ReimApplication.showProgressDialog();
    	InviteReplyRequest request = new InviteReplyRequest(agree, inviteCode);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				InviteReplyResponse response = new InviteReplyResponse(httpResponse);
				if (response.getStatus())
				{
					if (agree == 2)
					{
						sendCommonRequest();						
					}
					else
					{
						runOnUiThread(new Runnable()
						{
							public void run()
							{
						    	ReimApplication.dismissProgressDialog();
								AlertDialog mDialog = new AlertDialog.Builder(InviteReplyActivity.this)
															.setTitle("提示")
															.setMessage("邀请回复已发送成功！")
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
				else
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
					    	ReimApplication.dismissProgressDialog();
							AlertDialog mDialog = new AlertDialog.Builder(InviteReplyActivity.this)
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

    private void sendCommonRequest()
    {
    	CommonRequest request = new CommonRequest();
		request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				final CommonResponse response = new CommonResponse(httpResponse);				
				if (response.getStatus())
				{
					int currentUserID = response.getCurrentUser().getServerID();
					int currentGroupID = -1;

					DBManager dbManager = DBManager.getDBManager();
					AppPreference appPreference = AppPreference.getAppPreference();
					appPreference.setServerToken(response.getServerToken());
					appPreference.setCurrentUserID(currentUserID);
					appPreference.setSyncOnlyWithWifi(true);
					appPreference.setEnablePasswordProtection(true);
					
					if (response.getGroup() != null)
					{
						currentGroupID = response.getGroup().getServerID();
						
						// update AppPreference
						appPreference.setCurrentGroupID(currentGroupID);
						appPreference.saveAppPreference();
						
						// update members
						dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);
						
						User localUser = dbManager.getUser(response.getCurrentUser().getServerID());						
						if (localUser.getServerUpdatedDate() == response.getCurrentUser().getServerUpdatedDate())
						{
							if (localUser.getAvatarPath().equals(""))
							{
								dbManager.updateUser(response.getCurrentUser());								
							}
						}
						else
						{
							dbManager.syncUser(response.getCurrentUser())	;
						}
						
						// update categories
						dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);
						
						// update tags
						dbManager.updateGroupTags(response.getTagList(), currentGroupID);
						
						// update group info
						dbManager.syncGroup(response.getGroup());
					}
					else
					{						
						// update AppPreference
						appPreference.setCurrentGroupID(currentGroupID);
						appPreference.saveAppPreference();
						
						// update current user
						dbManager.syncUser(response.getCurrentUser());
						
						// update categories
						dbManager.updateGroupCategories(response.getCategoryList(), currentGroupID);						
					}
				}
				
				// refresh UI
				runOnUiThread(new Runnable()
				{
					public void run()
					{
				    	ReimApplication.dismissProgressDialog();
						AlertDialog mDialog = new AlertDialog.Builder(InviteReplyActivity.this)
													.setTitle("提示")
													.setMessage("邀请回复已发送成功！")
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
		});
    }

    private void goBackToMainActivity()
    {
    	ReimApplication.setTabIndex(3);
    	Intent intent = new Intent(InviteReplyActivity.this, MainActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    	finish();
    }
}
