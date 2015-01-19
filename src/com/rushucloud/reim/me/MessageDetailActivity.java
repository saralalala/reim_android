package com.rushucloud.reim.me;

import netUtils.HttpConnectionCallback;
import netUtils.Response.CommonResponse;
import netUtils.Response.User.InviteReplyResponse;
import netUtils.Request.CommonRequest;
import netUtils.Request.User.InviteReplyRequest;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import classes.Invite;
import classes.ReimApplication;
import classes.User;
import classes.utils.AppPreference;
import classes.utils.DBManager;
import classes.utils.Utils;
import classes.widget.ReimProgressDialog;

import com.rushucloud.reim.MainActivity;
import com.rushucloud.reim.R;
import com.umeng.analytics.MobclickAgent;


public class MessageDetailActivity extends Activity
{	
	private Invite invite;
	
	private boolean fromPush;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_invite);
		initData();
		initView();
	}
  
	protected void onResume()
	{
		super.onResume();
		MobclickAgent.onPageStart("MessageDetailActivity");		
		MobclickAgent.onResume(this);
		ReimProgressDialog.setProgressDialog(this);
	}

	protected void onPause()
	{
		super.onPause();
		MobclickAgent.onPageEnd("MessageDetailActivity");
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
			fromPush = bundle.getBoolean("fromPush", false);
		}
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
		
		TextView inviteTextView = (TextView)findViewById(R.id.inviteTextView);
		inviteTextView.setText(invite.getMessage());

		TextView dateTextView = (TextView)findViewById(R.id.dateTextView);
		dateTextView.setText(Utils.secondToStringUpToDay(invite.getUpdateTime()));
		
		Button agreeButton = (Button)findViewById(R.id.agreeButton);
		agreeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (Utils.isNetworkConnected())
				{
					sendInviteReplyRequest(Invite.TYPE_ACCEPTED, invite.getInviteCode());					
				}
				else
				{
					Utils.showToast(MessageDetailActivity.this, R.string.error_send_reply_network_unavailable);
				}
			}
		});
		agreeButton = Utils.resizeLongButton(agreeButton);
		
		Button rejectButton = (Button)findViewById(R.id.rejectButton);
		rejectButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (Utils.isNetworkConnected())
				{
					sendInviteReplyRequest(Invite.TYPE_REJECTED, invite.getInviteCode());				
				}
				else
				{
					Utils.showToast(MessageDetailActivity.this, R.string.error_send_reply_network_unavailable);
				}
			}
		});
		rejectButton = Utils.resizeLongButton(rejectButton);
		
		if (invite.getTypeCode() != Invite.TYPE_NEW)
		{
			agreeButton.setVisibility(View.GONE);
			rejectButton.setVisibility(View.GONE);
		}
	}
	
    private void sendInviteReplyRequest(final int agree, String inviteCode)
    {
		ReimProgressDialog.show();
    	InviteReplyRequest request = new InviteReplyRequest(agree, inviteCode);
    	request.sendRequest(new HttpConnectionCallback()
		{
			public void execute(Object httpResponse)
			{
				InviteReplyResponse response = new InviteReplyResponse(httpResponse);
				if (response.getStatus())
				{
					if (agree == Invite.TYPE_ACCEPTED)
					{
						sendCommonRequest();						
					}
					else
					{
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								ReimProgressDialog.dismiss();
								Builder builder = new Builder(MessageDetailActivity.this);
								builder.setTitle(R.string.tip);
								builder.setMessage(R.string.prompt_invite_reply_sent);
								builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener()
															{
																public void onClick(DialogInterface dialog, int which)
																{
																	goBackToMainActivity();
																}
															});
								builder.create().show();
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
							ReimProgressDialog.dismiss();
					    	Utils.showToast(MessageDetailActivity.this, R.string.failed_to_send_reply);
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
						User currentUser = response.getCurrentUser();
						User localUser = dbManager.getUser(response.getCurrentUser().getServerID());
						if (localUser != null && currentUser.getAvatarID() == localUser.getAvatarID())
						{
							currentUser.setAvatarPath(localUser.getAvatarPath());
						}
						
						dbManager.updateGroupUsers(response.getMemberList(), currentGroupID);

						dbManager.syncUser(currentUser);
						
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
						ReimProgressDialog.dismiss();
						Builder builder = new Builder(MessageDetailActivity.this);
						builder.setTitle(R.string.tip);
						builder.setMessage(R.string.prompt_invite_reply_sent);
						builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener()
													{
														public void onClick(DialogInterface dialog, int which)
														{
															goBackToMainActivity();
														}
													});
						builder.create().show();
					}
				});
			}
		});
    }

    private void goBackToMainActivity()
    {
    	if (fromPush)
		{
        	ReimApplication.setTabIndex(3);
        	Intent intent = new Intent(MessageDetailActivity.this, MainActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    		Intent intent2 = new Intent(MessageDetailActivity.this, MessageActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivities(new Intent[] {intent, intent2});
        	finish();
		}
    	else
    	{
			finish();
		}
    }
}